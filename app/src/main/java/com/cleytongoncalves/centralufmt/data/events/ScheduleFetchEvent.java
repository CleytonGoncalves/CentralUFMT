package com.cleytongoncalves.centralufmt.data.events;

import com.cleytongoncalves.centralufmt.data.model.Discipline;

import java.util.List;

public final class ScheduleFetchEvent extends BusEvent<List<Discipline>> {
	
	public ScheduleFetchEvent(List<Discipline> result) {
		super(result);
	}
	
	public ScheduleFetchEvent(int failureReason) {
		super(failureReason);
	}
}
