package com.cleytongoncalves.centralufmt.ui.schedule;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;


@Value @AllArgsConstructor
public final class ScheduleData {
	private final int mMaxDailyClasses;
	private final int mAmountOfDays;
	@Getter(AccessLevel.NONE) private final List<ScheduleItemData> mSchedule;

	ScheduleItemData getItem(int position) {
		return mSchedule.get(position);
	}

	int getScheduleSize() {
		return mSchedule.size();
	}

	@Value @AllArgsConstructor
	static class ScheduleItemData implements Comparable<ScheduleItemData> {
		private final int mColumn;
		private final String mTitle;
		private final String mSchedule;
		private final String mRoom;

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

		static ScheduleItemData empty() {
			return new ScheduleItemData(- 1, null, null, null);
		}
	}
}
