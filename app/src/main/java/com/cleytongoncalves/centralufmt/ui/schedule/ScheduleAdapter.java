package com.cleytongoncalves.centralufmt.ui.schedule;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.data.model.Schedule;
import com.cleytongoncalves.centralufmt.data.model.SubjectClass;
import com.cleytongoncalves.centralufmt.util.TextUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

final class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private Schedule mSchedule;
	private int mWeekday;
	
	ScheduleAdapter(int weekday) {
		mWeekday = weekday;
		setHasStableIds(true);
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new ClassViewHolder(inflater.inflate(R.layout.item_schedule, parent, false));
	}
	
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		SubjectClass subjectClass = getCurrentWeekdayClasses().get(position);
		ClassViewHolder viewHolder = (ClassViewHolder) holder;
		
		viewHolder.timeTextView.setText(subjectClass.getClassTime().toString());
		viewHolder.titleTextView.setText(
				TextUtil.capsWordsFirstLetter(subjectClass.getSubject().getTitle()));
		viewHolder.classroomTextView.setText(subjectClass.getClassroom());
	}
	
	@Override
	public int getItemCount() {
		return mSchedule != null ? getCurrentWeekdayClasses().size() : 0;
	}
	
	@Override
	public long getItemId(int position) {
		return getCurrentWeekdayClasses().get(position).hashCode();
	}
	
	void setSchedule(Schedule schedule) {
		mSchedule = schedule;
	}
	
	void setWeekday(int weekday) {
		mWeekday = weekday;
	}
	
	private List<SubjectClass> getCurrentWeekdayClasses() {
		return mSchedule.getWeekdayClasses(mWeekday);
	}
	
	static class ClassViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.schedule_card_time) TextView timeTextView;
		@BindView(R.id.schedule_card_title) TextView titleTextView;
		@BindView(R.id.schedule_card_classroom) TextView classroomTextView;
		
		public ClassViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}
}
