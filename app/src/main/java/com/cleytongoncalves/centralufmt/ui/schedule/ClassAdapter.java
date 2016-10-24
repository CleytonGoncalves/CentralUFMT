package com.cleytongoncalves.centralufmt.ui.schedule;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cleytongoncalves.centralufmt.R;

import butterknife.BindView;
import butterknife.ButterKnife;

final class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

	@Override
	public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
		                              .inflate(R.layout.class_item, parent, false);
		return new ClassViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(ClassViewHolder holder, int position) {
		holder.classTitleView.setText("");
		holder.classScheduleView.setText("");
		holder.classRoomView.setText("");
	}

	@Override
	public int getItemCount() {
		return 0;
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
