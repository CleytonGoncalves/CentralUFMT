package com.cleytongoncalves.centralufmt.data.jobs;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class NetworkJob extends Job {
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({UI_HIGH, BACKGROUND})
	@interface Priority {}
	static final int BACKGROUND = 1;
	static final int BACKGROUND_HIGH = 2;
	static final int UI_HIGH = 10;
	
	@Retention(RetentionPolicy.SOURCE)
	@StringDef({SIGA})
	@interface Group {}
	static final String SIGA = "siga";
	
	NetworkJob(Params params) {
		super(params);
	}
	
	public abstract void inject(ApplicationComponent appComponent);
	
	boolean shouldRetry(Throwable throwable) {
		if (throwable instanceof NetworkFailedException) {
			NetworkFailedException exception = (NetworkFailedException) throwable;
			return exception.shouldRetry();
		}
		return true;
	}
	
	/* Convenience Assertions */
	
	/**
	 * Convenience method that checks if the NetworkOperation has failed and throws a
	 * NetworkFailedException if it has.
	 * @param operation the NetworkOperation obtained
	 */
	void assertNetworkSuccess(NetworkOperation operation) {
		if (!operation.isSuccessful()) {
			throw new NetworkFailedException(operation.getErrorCode());
		}
	}
	
	/**
	 * Convenience method that checks if the job is cancelled and throws a JobCancelledException
	 * if it is.
	 */
	@Override
	public void assertNotCancelled() {
		if (isCancelled()) {
			throw new JobCancelledException();
		}
	}
	
	/* Job Exiting Exceptions */
	
	static final class AuthenticationErrorException extends JobExitingException {
		AuthenticationErrorException() {
			super("It seems the authentication has failed");
		}
	}
	
	static final class ParsingErrorException extends JobExitingException {
		ParsingErrorException(Throwable root) {
			super("There seems to be a parsing problem", root);
		}
	}
	
	private static final class NetworkFailedException extends JobExitingException {
		private final int mErrorCode;
		
		private NetworkFailedException(int errorCode) {
			mErrorCode = errorCode;
		}
		
		private boolean shouldRetry() {
			return mErrorCode < 400 || mErrorCode > 499; //HTTP status 400 range = client error
		}
	
	}
	
	private static final class JobCancelledException extends JobExitingException {
		public JobCancelledException() {
			super("The job has been cancelled");
		}
	}
}
