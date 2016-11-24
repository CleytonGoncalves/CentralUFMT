package com.cleytongoncalves.centralufmt.ui.schedule;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;


@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Value @AllArgsConstructor
public final class ScheduleData {
	private final int mMaxDailyClasses;
	private final int mAmountOfDays;
	@Getter(AccessLevel.NONE) private final List<DisciplineModelView> mSchedule;

	static ScheduleData emptySchedule() {
		return new ScheduleData(1, SchedulePresenter.MINIMUM_AMOUNT_OF_DAYS,
		                        new ArrayList<>(0));
	}

	DisciplineModelView getItem(int position) {
		return mSchedule.get(position);
	}

	int getScheduleSize() {
		return mSchedule.size();
	}

	boolean containsData() {
		return mSchedule.size() > mAmountOfDays;
	}

	@Value @AllArgsConstructor
	static class DisciplineModelView implements Comparable<DisciplineModelView> {
		private final int mColumn;
		private final String mTitle;
		private final String mSchedule;
		private final String mRoom;

		static DisciplineModelView emptyItem() {
			return new DisciplineModelView(- 1, null, null, null);
		}

		@Override
		public int compareTo(@NonNull DisciplineModelView o) {
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
