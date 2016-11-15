package com.cleytongoncalves.centralufmt.ui.schedule;

import java.util.ArrayList;
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

	static ScheduleData emptySchedule() {
		return new ScheduleData(1, SchedulePresenter.MINIMUM_AMOUNT_OF_DAYS,
		                        new ArrayList<ScheduleItemData>(0));
	}

	ScheduleItemData getItem(int position) {
		return mSchedule.get(position);
	}

	int getScheduleSize() {
		return mSchedule.size();
	}

	boolean containsData() {
		return mSchedule.size() > mAmountOfDays;
	}

	@Value @AllArgsConstructor
	static class ScheduleItemData implements Comparable<ScheduleItemData> {
		private final int mColumn;
		private final String mTitle;
		private final String mSchedule;
		private final String mRoom;

		static ScheduleItemData emptyItem() {
			return new ScheduleItemData(- 1, null, null, null);
		}

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

		boolean isFiller() {
			return mColumn < 0;
		}
	}
}
