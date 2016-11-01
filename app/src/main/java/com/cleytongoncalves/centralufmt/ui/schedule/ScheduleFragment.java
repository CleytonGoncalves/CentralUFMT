package com.cleytongoncalves.centralufmt.ui.schedule;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import javax.inject.Inject;

public final class ScheduleFragment extends Fragment {
	private static final String TAG = ScheduleFragment.class.getSimpleName();

	@Inject SchedulePresenter mSchedulePresenter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((BaseActivity) getActivity()).activityComponent().inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
		RecyclerView rcView = (RecyclerView) rootView.findViewById(R.id.grid_schedule);

		setUpRecyclerView(rcView);

		return rootView;
	}

	private void setUpRecyclerView(RecyclerView rcView) {
		int gridSpan = mSchedulePresenter.getAmountOfDays();
		rcView.setLayoutManager(new GridLayoutManager(getActivity(), gridSpan));
		rcView.setAdapter(new ScheduleAdapter(mSchedulePresenter));
	}

}
