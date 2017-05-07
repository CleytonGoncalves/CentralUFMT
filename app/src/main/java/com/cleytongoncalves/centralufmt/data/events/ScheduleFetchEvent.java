package com.cleytongoncalves.centralufmt.data.events;

public final class ScheduleFetchEvent extends AbstractEvent<Boolean> {
	
	public ScheduleFetchEvent() {
		super(true);
	}
	
	public ScheduleFetchEvent(int failureReason) {
		super(failureReason);
	}
}
