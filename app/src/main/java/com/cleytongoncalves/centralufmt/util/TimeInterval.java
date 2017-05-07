package com.cleytongoncalves.centralufmt.util;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

public final class TimeInterval implements Comparable<TimeInterval> {
	public static final String STR_SEPARATOR = "->";
	
	private final Interval mInterval;
	
	public TimeInterval(String startStr, String endStr, DateTimeFormatter fmt) {
		DateTime start = fmt.parseDateTime(startStr);
		DateTime end = fmt.parseDateTime(endStr);
		
		mInterval = new Interval(start, end);
	}
	
	public TimeInterval(DateTime start, DateTime end) {
		mInterval = new Interval(start, end);
	}
	
	public TimeInterval(Long startMillis, Long endMillis) {
		mInterval = new Interval(startMillis, endMillis);
	}
	
	public boolean overlapsWith(TimeInterval timeInterval) {
		return mInterval.overlaps(timeInterval.getInterval());
	}
	
	public DateTime getStart() {
		return mInterval.getStart();
	}
	
	public DateTime getEnd() {
		return mInterval.getEnd();
	}
	
	public boolean isBeforeNow() {
		return mInterval.isBeforeNow();
	}
	
	public boolean isAfterNow() {
		return mInterval.isAfterNow();
	}
	
	public boolean isNow() {
		return ! (isBeforeNow() || isAfterNow());
	}
	
	public Interval getInterval() {
		return mInterval;
	}
	
	@Override
	public String toString() {
		long start = mInterval.getStartMillis();
		long end = mInterval.getEndMillis();
		
		return String.valueOf(start) + STR_SEPARATOR + String.valueOf(end);
	}
	
	@Override
	public int compareTo(@NonNull TimeInterval o) {
		Interval i1 = this.getInterval();
		Interval i2 = o.getInterval();
		if (i1.isAfter(i2)) {
			return 1;
		} else if (i1.isBefore(i2)) {
			return - 1;
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		
		TimeInterval that = (TimeInterval) o;
		
		return mInterval.equals(that.mInterval);
	}
	
	@Override
	public int hashCode() {
		return mInterval.hashCode();
	}
}
