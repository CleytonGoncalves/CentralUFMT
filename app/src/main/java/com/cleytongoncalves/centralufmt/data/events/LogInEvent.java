package com.cleytongoncalves.centralufmt.data.events;

public final class LogInEvent {
	public final static int ACCESS_DENIED = - 3;
	public final static int USER_CANCELED = - 4;
	public final static int GENERAL_ERROR = - 5;

	private Object mLogInObjectResult;
	private Integer mFailureReason;

	public LogInEvent(Object result) {
		mLogInObjectResult = result;
	}

	public LogInEvent(int reason) {
		this.mFailureReason = reason;
	}

	public Object getObjectResult() {
		return mLogInObjectResult;
	}

	public boolean isSuccessful() {
		return mLogInObjectResult != null;
	}

	public int getFailureReason() {
		return mFailureReason;
	}
}
