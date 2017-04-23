package com.cleytongoncalves.centralufmt.data.events;

import com.cleytongoncalves.centralufmt.data.model.SubjectClass;

import java.util.List;

public final class ScheduleFetchEvent extends AbstractEvent<List<SubjectClass>> {
	
	public ScheduleFetchEvent(List<SubjectClass> result) {
		super(result);
	}
	
	public ScheduleFetchEvent(int failureReason) {
		super(failureReason);
	}
}
