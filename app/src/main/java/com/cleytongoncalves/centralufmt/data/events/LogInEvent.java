package com.cleytongoncalves.centralufmt.data.events;

public final class LogInEvent implements BusEvent<Object> {
	public static final String ACCESS_DENIED = "Access Denied";
	public static final String USER_CANCELED = "User Canceled";
	public static final String GENERAL_ERROR = "Network/IO Error";

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
