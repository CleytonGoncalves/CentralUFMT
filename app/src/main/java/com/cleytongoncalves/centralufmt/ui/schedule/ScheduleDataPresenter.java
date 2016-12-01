package com.cleytongoncalves.centralufmt.ui.schedule;

interface ScheduleDataPresenter {
	void attachAdapter(ScheduleAdapter adapter);

	void detachAdapter();

	DisciplineModelView getDataForPosition(int position);

	boolean isHeader(int position);

	int getItemId(int position);

	int getItemCount();
}
