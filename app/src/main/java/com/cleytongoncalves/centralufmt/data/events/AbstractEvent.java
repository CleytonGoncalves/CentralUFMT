package com.cleytongoncalves.centralufmt.data.events;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

abstract class AbstractEvent<T> {
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({GENERAL_ERROR, USER_CANCELLED})
	@interface FailureReason {}
	public static final int GENERAL_ERROR = 1;
	public static final int USER_CANCELLED = 2;
	
	private T mResult;
	private int mFailureReason;
	
	AbstractEvent(T result) {
		mResult = result;
	}
	
	AbstractEvent(int failureReason) {
		mFailureReason = failureReason;
	}
	
	public boolean isSuccessful() {
		return mResult != null;
	}
	
	public T getResult() {
		return mResult;
	}
	
	public int getFailureReason() {
		return mFailureReason;
	}
}
