package com.cleytongoncalves.centralufmt.ui.schedule;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class ScheduleFragment extends Fragment implements ScheduleMvpView {
	@Inject SchedulePresenter mPresenter;

	@BindView(R.id.schedule_progress_bar) ProgressBar mProgressBar;
	@BindView(R.id.schedule_grid) RecyclerView mRecyclerView;
	private Unbinder mUnbinder;

	private View mRootView;
	private Snackbar mSnackbar;

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

		//Sets an recycler view containing header only
		mRecyclerView.setLayoutManager(
				new GridLayoutManager(getActivity(), SchedulePresenter.MINIMUM_AMOUNT_OF_DAYS));
		mRecyclerView.setAdapter(new ScheduleAdapter(mPresenter));

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
		CentralUfmt.getRefWatcher(getActivity()).watch(this);
		super.onDestroy();
	}

	/* MVP Methods */

	@Override
	public void setGridSpanCount(int amountOfDays) {
		((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(amountOfDays);
	}

	@Override
	public void showRecyclerView(boolean enabled) {
		mRecyclerView.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	@Override
	public void showProgressBar(boolean enabled) {
		mProgressBar.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
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
		                    .setAction(getString(R.string.snack_reload_schedule), v -> mPresenter.loadSchedule(true));
		mSnackbar.show();
	}

	@Override
	public void hideSnackIfShown() {
		if (mSnackbar != null && mSnackbar.isShownOrQueued()) {
			mSnackbar.dismiss();
		}
	}
}
