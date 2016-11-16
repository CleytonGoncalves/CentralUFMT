package com.cleytongoncalves.centralufmt.data.model;

import org.joda.time.Interval;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public final class Discipline {
	final private String mTitle;
	final private String mCode;
	final private String mGroup;
	final private String mRoom;
	final private String mCrd;
	final private String mCourseLoad;
	final private String mType;
	final private String mTerm;
	final private List<Interval> mClassTimes;
}
