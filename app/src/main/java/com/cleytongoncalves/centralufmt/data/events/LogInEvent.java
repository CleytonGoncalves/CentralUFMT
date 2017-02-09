package com.cleytongoncalves.centralufmt.data.events;

public final class LogInEvent implements BusEvent<Object> {
	public static final String ACCESS_DENIED = "Access Denied";

	private Object mLogInObjectResult;
	private String mFailureReason;

	public LogInEvent(Object result) {
		mLogInObjectResult = result;
	}

	public LogInEvent(String reason) {
		mFailureReason = reason;
	}

	public boolean isSuccessful() {
		return mLogInObjectResult != null;
	}

	public Object getResult() {
		return mLogInObjectResult;
	}

	public String getFailureReason() {
		return mFailureReason;
	}
}
