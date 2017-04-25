package com.cleytongoncalves.centralufmt.data.jobs;

/**
 * To be subclassed by any exception intentionally used to <b>cancel/retry a job</b>
 * i.e. exceptions, failures, or illegal states
 */
public abstract class JobExitingException extends RuntimeException {
	
	public JobExitingException() {
	}
	
	public JobExitingException(String message) {
		super(message);
	}
	
	public JobExitingException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public JobExitingException(Throwable cause) {
		super(cause);
	}
}
