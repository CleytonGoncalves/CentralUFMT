package com.cleytongoncalves.centralufmt.ui.schedule;

import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
import com.cleytongoncalves.centralufmt.data.local.DatabaseHelper;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

final class SchedulePresenter implements Presenter<ScheduleMvpView> {
	private final DataManager mDataManager;
	private final DatabaseHelper mDatabaseHelper;
	
	@Nullable private ScheduleMvpView mView;
	
	private boolean mFetchingData;
	
	@Inject
	SchedulePresenter(DataManager dataManager, DatabaseHelper databaseHelper) {
		mDataManager = dataManager;
		mDatabaseHelper = databaseHelper;
	}

	/* View Methods */
	
	@Override
	public void attachView(ScheduleMvpView mvpView) {
		mView = mvpView;
	}
	
	@Override
	public void detachView() {
		mView = null;
		//should be here?
		if (EventBus.getDefault().isRegistered(this)) { EventBus.getDefault().unregister(this); }
	}
	
	void loadSchedule(boolean forceUpdate) {
		if (isFetchingData()) { return; }
		
		showViewDataLoading(true);
		if (mView != null) { mView.hideSnackIfShown(); }
		
		if (forceUpdate || ! mDatabaseHelper.hasSchedule()) {
			EventBus.getDefault().register(this);
			mDataManager.fetchSchedule();
			mFetchingData = true;
		} else {
			displayDataFromDb();
		}
	}

	/* Data Methods */
	
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onScheduleFetched(ScheduleFetchEvent event) {
		EventBus.getDefault().unregister(this);
		
		if (event.isSuccessful()) {
			onFetchSuccess();
		} else {
			onFetchFailure();
		}
	}
	
	private void onFetchSuccess() {
		mFetchingData = false;
		
		if (mView != null) { mView.showDataUpdatedSnack(); }
		displayDataFromDb();
	}
	
	private void onFetchFailure() {
		if (mView == null) { return; }
		
		mView.showGeneralErrorSnack();
		displayDataFromDb(); //display non-updated data if available
	}
	
	private void displayDataFromDb() {
		if (mView == null) { return; }
		
		if (mDatabaseHelper.hasSchedule()) {
			mView.updateAdapterData(mDatabaseHelper.getSchedule());
		}
		
		showViewDataLoading(false);
	}
	
	/* Helper Methods */
	
	private boolean isFetchingData() {
		return mFetchingData;
	}
	
	private void showViewDataLoading(boolean isLoading) {
		if (mView == null) { return; }
		
		mView.showRecyclerView(! isLoading);
		mView.showProgressBar(isLoading);
	}
}
