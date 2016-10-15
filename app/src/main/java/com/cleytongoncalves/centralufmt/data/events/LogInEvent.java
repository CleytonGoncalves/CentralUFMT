package com.cleytongoncalves.centralufmt.data.events;

public final class LogInEvent {
	public final static String ACCESS_DENIED = "ACCESS DENIED";
	public final static String USER_CANCELED = "USER CANCELED";
	public final static String GENERAL_ERROR = "NETWORK/IO ERROR";

	private Object mLogInObjectResult;
	private String mFailureReason;

	public LogInEvent(Object result) {
		mLogInObjectResult = result;
	}

	public LogInEvent(String reason) {
		mFailureReason = reason;
	}

	public Object getObjectResult() {
		return mLogInObjectResult;
	}

	public boolean isSuccessful() {
		return mLogInObjectResult != null;
	}

	public String getFailureReason() {
		return mFailureReason;
	}
}
