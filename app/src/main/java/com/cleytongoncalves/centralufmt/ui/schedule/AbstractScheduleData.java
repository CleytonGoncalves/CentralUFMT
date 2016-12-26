package com.cleytongoncalves.centralufmt.ui.schedule;

import android.support.annotation.NonNull;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.Collections;
import java.util.List;

@Gson.TypeAdapters
@Value.Style(allParameters = true,
             typeAbstract = {"Abstract*"},
             typeImmutable = "*",
             defaults = @Value.Immutable(builder = false, copy = false))
@Value.Immutable
public abstract class AbstractScheduleData {
	public abstract int getMaxDailyClasses();

	public abstract int getAmountOfDays();

	public abstract List<DisciplineModelView> getSchedule();

	static ScheduleData emptySchedule() {
		List<DisciplineModelView> empty = Collections.emptyList();
		return ScheduleData.of(1, SchedulePresenter.MINIMUM_AMOUNT_OF_DAYS, empty);
	}

	DisciplineModelView getItem(int position) {
		return getSchedule().get(position);
	}

	int getScheduleSize() {
		return getSchedule().size();
	}

	boolean isEmpty() {
		return getSchedule().size() > getAmountOfDays();
	}

	@Value.Immutable
	static abstract class AbstractDisciplineModelView implements Comparable<DisciplineModelView> {
		public abstract int getColumn();

		public abstract String getTitle();

		public abstract String getScheduleHours();

		public abstract String getRoom();

		static DisciplineModelView emptyItem() {
			return DisciplineModelView.of(- 1, "", "", "");
		}

		@Override
		public int compareTo(@NonNull DisciplineModelView o) {
			if (getColumn() != o.getColumn()) {
				return getColumn() - o.getColumn();
			} else if (! getScheduleHours().equals(o.getScheduleHours())) {
				return getScheduleHours().compareTo(o.getScheduleHours());
			} else {
				return getTitle().compareTo(o.getTitle());
			}
		}

		boolean isFiller() {
			return getColumn() < 0;
		}
	}
}
