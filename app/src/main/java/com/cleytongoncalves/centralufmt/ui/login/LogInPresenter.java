package com.cleytongoncalves.centralufmt.ui.login;

import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.SigaLogInEvent;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import timber.log.Timber;

public final class LogInPresenter implements Presenter<LogInMvpView> {
	private final DataManager mDataManager;
	@Nullable private LogInMvpView mView;
	
	@Inject
	LogInPresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}
	
	/* View Methods */
	
	@Override
	public void attachView(LogInMvpView mvpView) {
		mView = mvpView;
	}
	
	@Override
	public void detachView() {
		mView = null;
		if (EventBus.getDefault().isRegistered(this)) { EventBus.getDefault().unregister(this); }
	}
	
	void anonymousLogIn() {
		if (mView == null) { return; }
		
		mView.setLogInButtonEnabled(false);
		mView.setAnonymousLogInEnabled(false);
		mView.showProgress(true);
		
		onLogInSuccess(); //Goes directly to the result
		Timber.d("Anonymous LogIn");
	}
	
	void logIn(String rga, char[] password) {
		if (mView != null) {
			mView.setLogInButtonEnabled(false);
			mView.setAnonymousLogInEnabled(false);
			mView.showProgress(true);
		}
		
		EventBus.getDefault().register(this);
		mDataManager.initialLogIn(rga, password);
	}
	
	boolean isLogInHappening() {
		return EventBus.getDefault().isRegistered(this);
	}
	
	void cancelLogIn() {
		mDataManager.cancelSigaLogIn();
	}
	
	/* Data Methods */
	
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSigaLogInEvent(SigaLogInEvent event) {
		EventBus.getDefault().unregister(this);
		
		if (event.isSuccessful()) {
			onLogInSuccess();
		} else {
			onLogInFailure(event.getFailureReason());
		}
	}
	
	private void onLogInSuccess() {
		if (mView == null) { return; }
		
		mView.showProgress(false);
		mView.onLogInSuccessful();
	}
	
	private void onLogInFailure(int reason) {
		if (mView == null) { return; }
		
		mView.showProgress(false);
		mView.setLogInButtonEnabled(true);
		mView.setAnonymousLogInEnabled(true);
		
		switch (reason) {
			case SigaLogInEvent.ACCESS_DENIED:
				mView.showAccessDenied();
				break;
			case SigaLogInEvent.USER_CANCELLED:
				mView.onUserCanceled();
				break;
			default:
				mView.showGeneralLogInError();
		}
	}
}
