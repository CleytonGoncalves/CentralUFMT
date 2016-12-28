package com.cleytongoncalves.centralufmt.data.events;

import com.cleytongoncalves.centralufmt.data.model.EnrolledDiscipline;

import java.util.List;

public final class ScheduleFetchEvent implements BusEvent<List<EnrolledDiscipline>> {
	public static final String GENERAL_ERROR = "Network/IO Error";

	private List<EnrolledDiscipline> mDisciplineList;
	private String mFailureReason;

	public ScheduleFetchEvent(List<EnrolledDiscipline> disciplineList) {
		mDisciplineList = disciplineList;
	}

	public ScheduleFetchEvent(String failureReason) {
		mFailureReason = failureReason;
	}

	public boolean isSuccessful() {
		return mDisciplineList != null;
	}

	public List<EnrolledDiscipline> getResult() {
		return mDisciplineList;
	}

	public String getFailureReason() {
		return mFailureReason;
	}
}
