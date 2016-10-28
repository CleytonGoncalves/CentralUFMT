package com.cleytongoncalves.centralufmt.ui.schedule;

import lombok.AllArgsConstructor;
import lombok.Value;

/* Must be public to be written on shared preferences */
@Value @AllArgsConstructor
public final class ScheduleItemData implements Comparable<ScheduleItemData> {
	int mColumn;
	String mTitle;
	String mSchedule;
	String mRoom;

	@Override
	public int compareTo(ScheduleItemData o) {
		if (mColumn != o.getColumn()) {
			return mColumn - o.getColumn();
		} else if (! mSchedule.equals(o.getSchedule())) {
			return mSchedule.compareTo(o.getSchedule());
		} else {
			return mTitle.compareTo(o.getTitle());
		}
	}
}
