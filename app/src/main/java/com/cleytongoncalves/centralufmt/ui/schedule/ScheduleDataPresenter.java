package com.cleytongoncalves.centralufmt.ui.schedule;

import com.cleytongoncalves.centralufmt.ui.schedule.ScheduleData.ScheduleItemData;

interface ScheduleDataPresenter {
	void attachAdapter(ScheduleAdapter adapter);

	void detachAdapter();

	ScheduleItemData getDataForPosition(int position);

	boolean isHeader(int position);

	int getItemId(int position);

	int getItemCount();
}
