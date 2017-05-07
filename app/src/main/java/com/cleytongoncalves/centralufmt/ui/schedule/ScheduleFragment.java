package com.cleytongoncalves.centralufmt.ui.schedule;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.data.model.Schedule;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import org.joda.time.DateTimeConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class ScheduleFragment extends Fragment implements ScheduleMvpView {
	@Inject SchedulePresenter mPresenter;
	
	@BindView(R.id.schedule_progress_bar) ContentLoadingProgressBar mProgressBar;
	@BindView(R.id.schedule_recycler_view) RecyclerView mRecyclerView;
	private Unbinder mUnbinder;
	
	private ScheduleAdapter mAdapter;
	private View mRootView;
	private Snackbar mSnackbar;
	
	private int mTabWeekday = DateTimeConstants.MONDAY;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((BaseActivity) getActivity()).activityComponent().inject(this);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_schedule, container, false);
		
		mPresenter.attachView(this);
		mUnbinder = ButterKnife.bind(this, mRootView);
		
		mRecyclerView.setLayoutManager(
				new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		
		mAdapter = new ScheduleAdapter();
		mRecyclerView.setAdapter(mAdapter);
		
		mPresenter.loadSchedule(false);
		
		return mRootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_schedule, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.schedule_menu_refresh:
				mPresenter.loadSchedule(true);
				return true;
		}
		
		return false;
	}
	
	@Override
	public void onDestroyView() {
		hideSnackIfShown();
		mPresenter.detachView();
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		mUnbinder.unbind();
		super.onDestroy();
		CentralUfmt.getRefWatcher(getActivity()).watch(this);
	}

	/* MVP Methods */
	
	@Override
	public void updateAdapterData(Schedule schedule) {
		mAdapter.setWeekday(mTabWeekday);
		mAdapter.setSchedule(schedule);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void showRecyclerView(boolean enabled) {
		mRecyclerView.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}
	
	@Override
	public void showProgressBar(boolean enabled) {
		if (enabled) {
			mProgressBar.setVisibility(View.VISIBLE); //Work-around ProgressBar not displaying bug
			mProgressBar.show();
		} else {
			mProgressBar.hide();
		}
	}
	
	@Override
	public void showDataUpdatedSnack() {
		mSnackbar = Snackbar.make(mRootView, getString(R.string.snack_success_schedule),
		                          Snackbar.LENGTH_SHORT);
		mSnackbar.show();
	}
	
	@Override
	public void showEmptyScheduleSnack() {
		mSnackbar = Snackbar.make(mRootView, getString(R.string.snack_empty_schedule),
		                          Snackbar.LENGTH_INDEFINITE);
		
		mSnackbar.show();
	}
	
	@Override
	public void showGeneralErrorSnack() {
		mSnackbar = Snackbar.make(mRootView, getString(R.string.snack_error_schedule),
		                          Snackbar.LENGTH_INDEFINITE)
		                    .setAction(getString(R.string.snack_reload_schedule),
		                               v -> mPresenter.loadSchedule(true));
		mSnackbar.show();
	}
	
	@Override
	public void hideSnackIfShown() {
		if (mSnackbar != null && mSnackbar.isShownOrQueued()) {
			mSnackbar.dismiss();
		}
	}
}
