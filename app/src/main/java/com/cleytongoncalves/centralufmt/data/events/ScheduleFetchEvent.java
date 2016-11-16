package com.cleytongoncalves.centralufmt.data.events;

import com.cleytongoncalves.centralufmt.data.model.Discipline;

import java.util.List;

public final class ScheduleFetchEvent {
	public static final String GENERAL_ERROR = "Network/IO Error";

	private List<Discipline> mDisciplineList;
	private String mFailureReason;

	public ScheduleFetchEvent(List<Discipline> disciplineList) {
		mDisciplineList = disciplineList;
	}

	public ScheduleFetchEvent(String failureReason) {
		mFailureReason = failureReason;
	}

	public List<Discipline> getDisciplineList() {
		return mDisciplineList;
	}

	public String getFailureReason() {
		return mFailureReason;
	}

	public boolean isSuccessful() {
		return mDisciplineList != null;
	}
}
