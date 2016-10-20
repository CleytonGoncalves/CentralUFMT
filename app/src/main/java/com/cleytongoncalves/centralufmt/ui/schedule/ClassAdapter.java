package com.cleytongoncalves.centralufmt.ui.schedule;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cleytongoncalves.centralufmt.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

final class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {
	private static final String[] filler = {"Fundamentos da Computação", "13:30-15:30", "Maria Rita", "CB03"};
	private List<String[]> mock;

	ClassAdapter(int amount) {
		mock = new ArrayList<>(amount);
		for (int i = 0; i < amount; i++) {
			mock.add(filler);
		}
	}

	@Override
	public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_item, parent, false);
		return new ClassViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(ClassViewHolder holder, int position) {
		String[] info = mock.get(position);
		holder.classNameView.setText(info[0]);
		//holder.classTimeView.setText(info[1]);
		holder.classTeacherView.setText(info[2]);
		holder.classRoomView.setText(info[3]);
	}

	@Override
	public int getItemCount() {
		return mock.size();
	}

	static class ClassViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.class_parent) LinearLayout mClassLayout;
		@BindView(R.id.class_name) TextView classNameView;
		//@BindView(R.id.class_time) TextView classTimeView;
		@BindView(R.id.class_teacher) TextView classTeacherView;
		@BindView(R.id.class_room) TextView classRoomView;

		ClassViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}
}
