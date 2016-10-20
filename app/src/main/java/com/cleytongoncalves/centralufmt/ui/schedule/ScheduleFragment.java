package com.cleytongoncalves.centralufmt.ui.schedule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleytongoncalves.centralufmt.R;

public final class ScheduleFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

		RecyclerView rcView = (RecyclerView) rootView.findViewById(R.id.grid_schedule);
		rcView.setHasFixedSize(true);
		rcView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
		rcView.setAdapter(new ClassAdapter(8));

		return rootView;
	}

}
