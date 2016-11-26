package com.cleytongoncalves.centralufmt.data.model;

import org.immutables.value.Value;
import org.joda.time.Interval;

import java.util.List;

@Value.Immutable()
public abstract class Discipline {
	public abstract String getTitle();

	public abstract String getCode();

	public abstract String getGroup();

	public abstract String getRoom();

	public abstract String getCrd();

	public abstract String getCourseLoad();

	public abstract String getType();

	public abstract String getTerm();

	public abstract List<Interval> getClassTimes();
}
