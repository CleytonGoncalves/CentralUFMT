package com.cleytongoncalves.centralufmt.data.events;

public final class SigaLogInEvent extends AbstractEvent<Boolean> {
	public static final int ACCESS_DENIED = 3;
	
	public SigaLogInEvent() {
		super(true);
	}
	
	public SigaLogInEvent(int failureReason) {
		super(failureReason);
	}
}
