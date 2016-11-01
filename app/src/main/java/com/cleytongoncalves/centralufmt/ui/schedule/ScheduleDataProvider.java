package com.cleytongoncalves.centralufmt.ui.schedule;

interface ScheduleDataProvider {
	ScheduleItemData getDataForPosition(int position);

	boolean isHeader(int position);

	int getItemId(int position);

	int getItemCount();
}
