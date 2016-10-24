package com.cleytongoncalves.centralufmt.data.model;

import org.joda.time.Interval;

import java.util.SortedSet;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public final class Discipline {
	String mTitle;
	String mCode;
	String mGroup;
	String mRoom;
	String mCrd;
	String mCourseLoad;
	String mType;
	String mTerm;
	SortedSet<Interval> mClassTimes;
}
