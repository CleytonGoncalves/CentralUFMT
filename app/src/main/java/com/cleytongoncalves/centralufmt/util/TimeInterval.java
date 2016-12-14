package com.cleytongoncalves.centralufmt.util;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

public final class TimeInterval {
	private static final Instant CONSTANT = new Instant(0);
	private final LocalTime start;
	private final LocalTime end;
	
	public TimeInterval(LocalTime start, LocalTime end) {
		this.start = start;
		this.end = end;
	}
	
	public TimeInterval(String start, String end) {
		this.start = LocalTime.parse(start);
		this.end = LocalTime.parse(end);
	}
	
	public boolean overlapsWith(TimeInterval timeInterval) {
		return this.toInterval().overlaps(timeInterval.toInterval());
	}
	
	public LocalTime getStart() {
		return start;
	}
	
	public LocalTime getEnd() {
		return end;
	}
	
	public boolean isBeforeNow() {
		return LocalTime.now().isBefore(start);
	}
	
	public boolean isNow() {
		return ! isBeforeNow() && ! isAfterNow();
	}
	
	public boolean isAfterNow() {
		return LocalTime.now().isAfter(end);
	}
	
	public boolean isValidJodaInterval() {
		try {
			toInterval(); //throws Exception if not valid
			return true;
		} catch (IllegalArgumentException e) { return false; }
	}
	
	/**
	 * @return this represented as a proper Interval
	 * @throws IllegalArgumentException if invalid (end is before start)
	 */
	private Interval toInterval() throws IllegalArgumentException {
		return new Interval(start.toDateTime(CONSTANT), end.toDateTime(CONSTANT));
	}
	
	@Override
	public String toString() {
		return "TimeInterval{" +
				       "start=" + start +
				       ", end=" + end +
				       '}';
	}
}
