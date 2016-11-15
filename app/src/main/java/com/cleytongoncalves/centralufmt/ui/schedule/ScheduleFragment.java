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

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class ScheduleFragment extends Fragment implements ScheduleMvpView {
	private static final String TAG = ScheduleFragment.class.getSimpleName();

	@Inject SchedulePresenter mPresenter;

	@BindView(R.id.schedule_progress_bar) ProgressBar mProgressBar;
	@BindView(R.id.schedule_grid) RecyclerView mRecyclerView;
	private Unbinder mUnbinder;

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
		View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

		mPresenter.attachView(this);
		mUnbinder = ButterKnife.bind(this, rootView);

		//Sets an empty recycler view
		mRecyclerView.setLayoutManager(
				new GridLayoutManager(getActivity(), SchedulePresenter.MINIMUM_AMOUNT_OF_DAYS));
		mRecyclerView.setAdapter(new ScheduleAdapter(mPresenter));

		mPresenter.loadSchedule(false);

		return rootView;
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
		super.onDestroyView();
		mPresenter.detachView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUnbinder.unbind();
	}

	/* MVP Methods */

	@Override
	public void setGridSpanCount(int amountOfDays) {
		((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(amountOfDays);
	}

	@Override
	public void showRecyclerView() {
		mRecyclerView.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideRecyclerView() {
		mRecyclerView.setVisibility(View.GONE);
	}

	@Override
	public void showProgressBar() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideProgressBar() {
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void showGeneralErrorSnack() {
		View rootView = getView();
		if (rootView != null) {
			mSnackbar = Snackbar.make(rootView, getString(R.string.snack_error_schedule),
			                          Snackbar.LENGTH_INDEFINITE)
			                    .setAction(getString(R.string.snack_reload_schedule),
			                               new View.OnClickListener() {
				                               @Override
				                               public void onClick(View v) {
					                               mPresenter.loadSchedule(true);
				                               }
			                               });

			mSnackbar.show();
		}
	}

	@Override
	public void hideSnackIfShown() {
		if (mSnackbar != null) {
			mSnackbar.dismiss();
		}
	}
}
