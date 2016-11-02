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

final class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final static String TAG = ScheduleAdapter.class.getSimpleName();
	private static final int TYPE_HEADER = 0;
	private static final int TYPE_ITEM = 1;
	private static final int TYPE_EMPTY = 2;
	private static final String[] shortDayName = {"seg", "ter", "qua", "qui", "sex", "sáb",
	                                              "dom"};

	private final ScheduleDataPresenter mSchedulePresenter;

	@Inject
	ScheduleAdapter(SchedulePresenter schedulePresenter) {
		mSchedulePresenter = schedulePresenter;
		setHasStableIds(true);
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		mSchedulePresenter.attachAdapter(this);
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
		if (mSchedulePresenter.isHeader(position)) {
			((HeaderViewHolder) holder).header.setText(shortDayName[position]);
			return;
		} else if (isEmpty(position)) {
			((EmptyViewHolder) holder).view.setBackgroundColor(Color.parseColor("#f9f9f9"));
			return;
		}

		ScheduleItemData data = mSchedulePresenter.getDataForPosition(position);
		ClassViewHolder classViewHolder = (ClassViewHolder) holder;

		classViewHolder.classLayout.setBackgroundColor(Color.parseColor("#3370ff"));
		classViewHolder.classTitleView.setText(data.getTitle());
		classViewHolder.classScheduleView.setText(data.getSchedule());

		if (data.getRoom().isEmpty()) {
			classViewHolder.classRoomView.setVisibility(View.INVISIBLE);
		} else {
			classViewHolder.classRoomView.setVisibility(View.VISIBLE);
			classViewHolder.classRoomView.setText(data.getRoom());
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (mSchedulePresenter.isHeader(position)) {
			return TYPE_HEADER;
		} else if (isEmpty(position)) {
			return TYPE_EMPTY;
		}

		return TYPE_ITEM;
	}

	@Override
	public long getItemId(int position) {
		return mSchedulePresenter.getItemId(position);
	}

	@Override
	public int getItemCount() {
		return mSchedulePresenter.getItemCount();
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		super.onDetachedFromRecyclerView(recyclerView);

	}

	private boolean isEmpty(int position) {
		return mSchedulePresenter.getDataForPosition(position).getTitle().isEmpty();
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
