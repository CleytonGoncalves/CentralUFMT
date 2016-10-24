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
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import java.util.List;

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
		rcView.setHasFixedSize(true);
		rcView.setLayoutManager(new GridLayoutManager(getActivity(), 5));

		List<Discipline> classes = HtmlHelper.parseSchedule(HtmlHelper.getScheduleHtml());

		rcView.setAdapter(new ClassAdapter());

		return rootView;
	}

}
