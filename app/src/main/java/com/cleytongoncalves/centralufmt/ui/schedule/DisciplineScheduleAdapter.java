package com.cleytongoncalves.centralufmt.ui.schedule;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.schedule.DisciplineSchedulePresenter.DisciplineData;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

final class DisciplineScheduleAdapter extends RecyclerView.Adapter<DisciplineScheduleAdapter
		                                                                   .ClassViewHolder> {
	private final static String TAG = DisciplineScheduleAdapter.class.getSimpleName();

	private final DisciplineSchedulePresenter mPresenter;
	private List<DisciplineData> mDataList;

	DisciplineScheduleAdapter(DisciplineSchedulePresenter presenter) {
		mPresenter = presenter;
		mDataList = mPresenter.createCompleteSchedule();
		setHasStableIds(true);
	}

	@Override
	public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
		                              .inflate(R.layout.class_item, parent, false);
		return new ClassViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(ClassViewHolder holder, int position) {
		DisciplineData data = mDataList.get(position);
		if (! data.getTitle().isEmpty()) {
			holder.classTitleView.setText(data.getTitle());
			holder.classScheduleView.setText(data.getSchedule());
			holder.classRoomView.setText(data.getRoom());
			holder.classLayout.setBackgroundColor(Color.parseColor("#3370ff"));
		} else {
			holder.classLayout.setBackgroundColor(Color.parseColor("#fcfcfc"));
		}
	}

	@Override
	public long getItemId(int position) {
		DisciplineData data = mDataList.get(position);
		return data.getTitle().hashCode() * 5 + data.getDayOfWeek() * 3
				       + data.getSchedule().hashCode() + data.getRoom().hashCode();
	}

	@Override
	public int getItemCount() {
		return mDataList.size();
	}

	static class ClassViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.class_parent) LinearLayout classLayout;
		@BindView(R.id.class_title) TextView classTitleView;
		@BindView(R.id.class_schedule) TextView classScheduleView;
		@BindView(R.id.class_room) TextView classRoomView;

		ClassViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}
}
