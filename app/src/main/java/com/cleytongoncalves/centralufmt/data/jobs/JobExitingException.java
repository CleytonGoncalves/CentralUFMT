package com.cleytongoncalves.centralufmt.data.jobs;

/**
 * To be subclassed by any exception intentionally used to <b>cancel/retry a job</b>
 * i.e. exceptions, failures, or illegal states
 */
public abstract class JobExitingException extends RuntimeException {
	
	JobExitingException() {
	}
	
	JobExitingException(String message) {
		super(message);
	}
	
	JobExitingException(String message, Throwable cause) {
		super(message, cause);
	}
	
	JobExitingException(Throwable cause) {
		super(cause);
	}
}
