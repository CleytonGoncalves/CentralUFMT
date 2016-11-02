package com.cleytongoncalves.centralufmt.ui.schedule;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class ScheduleFragment extends Fragment implements ScheduleMvpView {
	private static final String TAG = ScheduleFragment.class.getSimpleName();

	@Inject SchedulePresenter mSchedulePresenter;

	@BindView(R.id.swipe_schedule) SwipeRefreshLayout mSwipeRefreshLayout;
	private Unbinder mUnbinder;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((BaseActivity) getActivity()).activityComponent().inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

		mSchedulePresenter.attachView(this);
		mUnbinder = ButterKnife.bind(this, rootView);

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mSchedulePresenter.refreshSchedule();
			}
		});

		RecyclerView rcView = (RecyclerView) rootView.findViewById(R.id.grid_schedule);
		setUpRecyclerView(rcView);

		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mSchedulePresenter.detachView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUnbinder.unbind();
	}

	private void setUpRecyclerView(RecyclerView rcView) {
		int gridSpan = mSchedulePresenter.getAmountOfDays();
		rcView.setLayoutManager(new GridLayoutManager(getActivity(), gridSpan));
		rcView.setAdapter(new ScheduleAdapter(mSchedulePresenter));
	}

	@Override
	public void OnItemsLoadStarted() {
		if (! mSwipeRefreshLayout.isRefreshing()) {
			mSwipeRefreshLayout.setRefreshing(true);
		}
	}

	@Override
	public void OnItemsLoadComplete() {
		if (mSwipeRefreshLayout.isRefreshing()) {
			mSwipeRefreshLayout.setRefreshing(false);
		}
	}
}
