package com.cleytongoncalves.centralufmt.data.model;


import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

public final class ClassTime implements Comparable<ClassTime> {
	Interval mInterval;
	
	public ClassTime(Interval interval) {
		mInterval = interval;
	}
	
	public ClassTime(String startStr, String endStr, DateTimeFormatter fmt) {
		DateTime start = fmt.parseDateTime(startStr);
		DateTime end = fmt.parseDateTime(endStr);
		
		mInterval = new Interval(start, end);
	}
	
	public int getWeekday() {
		return mInterval.getStart().getDayOfWeek();
	}
	
	public String getStartHourString() {
		return mInterval.getStart().toString("HH:mm");
	}
	
	public String getEndHourString() {
		return mInterval.getEnd().toString("HH:mm");
	}
	
	public Interval getInterval() {
		return mInterval;
	}
	
	@Override
	public String toString() {
		return getStartHourString() + " - " + getEndHourString();
	}
	
	@Override
	public int compareTo(@NonNull ClassTime o) {
		if (mInterval.isBefore(o.getInterval())) { return - 1; } else if (mInterval.isAfter(
				o.getInterval())) { return 1; } else { return 0; }
	}
}
