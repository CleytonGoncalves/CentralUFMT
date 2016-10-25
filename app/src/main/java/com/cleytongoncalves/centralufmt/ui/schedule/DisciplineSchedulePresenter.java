package com.cleytongoncalves.centralufmt.ui.schedule;

import com.cleytongoncalves.centralufmt.data.model.Discipline;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.AllArgsConstructor;
import lombok.Value;

final class DisciplineSchedulePresenter {
	private final static String TAG = DisciplineSchedulePresenter.class.getSimpleName();

	private Map<Integer, SortedSet<DisciplineData>> mSchedule;
	private final int mDaysOfWeekAmount;
	private int mMaxDailyClassAmount;

	DisciplineSchedulePresenter(List<Discipline> disciplineList) {
		mSchedule = createWeekSchedule(disciplineList);
		if (hasWeekDaysOnly()) {
			mDaysOfWeekAmount = 5;
		} else if (hasSunday()) {
			mDaysOfWeekAmount = 7;
		} else {
			mDaysOfWeekAmount = 6;
		}

		mMaxDailyClassAmount = 0;
	}

	int getDaysOfWeekAmount() {
		return mDaysOfWeekAmount;
	}

	boolean hasWeekDaysOnly() {
		return mSchedule.get(DateTimeConstants.SATURDAY) == null && ! hasSunday();
	}

	boolean hasSunday() {
		return mSchedule.get(DateTimeConstants.SUNDAY) != null;
	}

	private Map<Integer, SortedSet<DisciplineData>> createWeekSchedule(List<Discipline>
			                                                                   disciplineList) {
		Map<Integer, SortedSet<DisciplineData>> hourlySchedule = new TreeMap<>();

		for (Discipline disc : disciplineList) {
			for (Interval inter : disc.getClassTimes()) {
				DateTime start = inter.getStart();
				DateTime end = inter.getEnd();

				int dayOfWeek = start.getDayOfWeek() - 1;
				String classTime = start.hourOfDay().getAsText() + ":" +
						                   start.minuteOfHour().getAsText() + " - " +
						                   end.hourOfDay().getAsText() + ":" +
						                   end.minuteOfHour().getAsText();

				DisciplineData discData = new DisciplineData(dayOfWeek, disc.getTitle(),
						                                            classTime, disc.getRoom());

				SortedSet<DisciplineData> viewSet = hourlySchedule.get(start.getHourOfDay());
				if (viewSet == null) {
					viewSet = new TreeSet<>();
					hourlySchedule.put(start.getHourOfDay(), viewSet);
					mMaxDailyClassAmount++;
				}
				viewSet.add(discData);
			}
		}

		return hourlySchedule;
	}

	List<DisciplineData> createCompleteSchedule() {
		List<DisciplineData> list = new ArrayList<>(mDaysOfWeekAmount * mMaxDailyClassAmount);

		int position = 0;
		for (Integer hourRow : mSchedule.keySet()) {
			SortedSet<DisciplineData> hourlyData = mSchedule.get(hourRow);

			for (DisciplineData data : hourlyData) {
				int currDay = position % mDaysOfWeekAmount;
				int nextDataDay = data.getDayOfWeek();
				if (nextDataDay != currDay) {
					for (; currDay < nextDataDay; currDay++) {
						DisciplineData empty = new DisciplineData(currDay, "", "", "");
						list.add(empty);
						position++;
					}
				}

				list.add(data);
				position++;
			}
		}

		return list;
	}

	@Value @AllArgsConstructor
	static class DisciplineData implements Comparable<DisciplineData> {
		int mDayOfWeek;
		String mTitle;
		String mSchedule;
		String mRoom;

		@Override
		public int compareTo(DisciplineData o) {
			if (mDayOfWeek != o.getDayOfWeek()) {
				return mDayOfWeek - o.getDayOfWeek();
			} else if (! mSchedule.equals(o.getSchedule())) {
				return mSchedule.compareTo(o.getSchedule());
			} else {
				return mTitle.compareTo(o.getTitle());
			}
		}
	}
}
