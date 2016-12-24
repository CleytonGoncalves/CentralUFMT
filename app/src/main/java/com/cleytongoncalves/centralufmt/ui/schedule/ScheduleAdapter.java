package com.cleytongoncalves.centralufmt.ui.schedule;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cleytongoncalves.centralufmt.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cleytongoncalves.centralufmt.ui.schedule.SchedulePresenter.MINIMUM_AMOUNT_OF_DAYS;

final class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int TYPE_HEADER = 0;
	private static final int TYPE_ITEM = 1;
	private static final int TYPE_EMPTY = 2;
	private static final String[] SHORT_DAY_NAME = {"seg", "ter", "qua", "qui", "sex", "sáb",
	                                                "dom"};
	private ScheduleData mScheduleData;

	@Inject
	ScheduleAdapter() {
		mScheduleData = ScheduleData.emptySchedule();
		setHasStableIds(true);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());

		if (viewType == TYPE_HEADER) {
			return new HeaderViewHolder(inflater.inflate(R.layout.schedule_header, parent, false));
		} else if (viewType == TYPE_ITEM) {
			return new ClassViewHolder(inflater.inflate(R.layout.schedule_item, parent, false));
		}

		return new EmptyViewHolder(inflater.inflate(R.layout.schedule_item_empty, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (isHeader(position)) {
			((HeaderViewHolder) holder).header.setText(SHORT_DAY_NAME[position]);
			return;
		} else if (isFiller(position)) {
			((EmptyViewHolder) holder).view.setBackgroundColor(Color.parseColor("#f9f9f9"));
			return;
		}

		DisciplineModelView data = getDataForPosition(position);
		ClassViewHolder classViewHolder = (ClassViewHolder) holder;

		classViewHolder.classLayout.setBackgroundColor(Color.parseColor("#3370ff"));
		classViewHolder.classTitleView.setText(data.getTitle());
		classViewHolder.classScheduleView.setText(data.getScheduleHours());

		if (data.getRoom().isEmpty()) {
			classViewHolder.classRoomView.setVisibility(View.INVISIBLE);
		} else {
			classViewHolder.classRoomView.setVisibility(View.VISIBLE);
			classViewHolder.classRoomView.setText(data.getRoom());
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (isHeader(position)) {
			return TYPE_HEADER;
		} else if (isFiller(position)) {
			return TYPE_EMPTY;
		}

		return TYPE_ITEM;
	}

	@Override
	public long getItemId(int position) {
		if (isHeader(position)) {
			return position;
		}

		int realPos = correctItemPosition(position);
		return mScheduleData.getItem(realPos).hashCode();
	}

	@Override
	public int getItemCount() {
		if (mScheduleData == null) {
			return MINIMUM_AMOUNT_OF_DAYS;
		}

		return mScheduleData.getScheduleSize() + mScheduleData.getAmountOfDays();
	}

	/* Private Helper Methods */

	private boolean isHeader(int position) {
		return mScheduleData == null || position < mScheduleData.getAmountOfDays();
	}

	private boolean isFiller(int position) {
		return getDataForPosition(position).isFiller();
	}

	private DisciplineModelView getDataForPosition(int pos) {
		//Not called on header positions
		return mScheduleData.getItem(correctItemPosition(pos));
	}

	private int correctItemPosition(int position) {
		return position - mScheduleData.getAmountOfDays(); //Removes the header from the count;
	}

	void setScheduleData(ScheduleData data) {
		mScheduleData = data;
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

	static class HeaderViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.header_schedule_item) TextView header;

		HeaderViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}

	static class EmptyViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.class_empty) View view;

		EmptyViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}
}
