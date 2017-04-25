package com.cleytongoncalves.centralufmt.util;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

public final class TimeInterval {
	private static final String STR_SEPARATOR = "->";
	
	private final Interval mInterval;
	
	public TimeInterval(String startStr, String endStr, DateTimeFormatter fmt) {
		DateTime start = fmt.parseDateTime(startStr);
		DateTime end = fmt.parseDateTime(endStr);
		
		mInterval = new Interval(start, end);
	}
	
	public TimeInterval(DateTime start, DateTime end) {
		mInterval = new Interval(start, end);
	}
	
	public TimeInterval(String fromToString) {
		String[] split = fromToString.split(STR_SEPARATOR);
		long startMillis = Long.parseLong(split[0]);
		long endMillis = Long.parseLong(split[1]);
		
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
	
	private Interval getInterval() {
		return mInterval;
	}
	
	@Override
	public String toString() {
		long start = mInterval.getStartMillis();
		long end = mInterval.getEndMillis();
		
		return String.valueOf(start) + STR_SEPARATOR + String.valueOf(end);
	}
}
