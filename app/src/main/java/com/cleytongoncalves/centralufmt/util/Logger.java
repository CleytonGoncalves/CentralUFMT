package com.cleytongoncalves.centralufmt.util;


import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.log.CustomLogger;
import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Severity;
import com.cleytongoncalves.centralufmt.data.jobs.JobExitingException;

import timber.log.Timber;
import timber.log.Timber.DebugTree;
import timber.log.Timber.Tree;

public final class Logger {
	private Logger() {
	}
	
	public static Tree getDebugTree() {
		return new DebugTree() {
			@Override
			protected void log(int priority, String tag, String message, Throwable t) {
				super.log(priority, "Timber_" + tag, message, t);
			}
		};
	}
	
	public static Tree getBugsnagTree() {
		return new Timber.Tree() {
			@Override
			protected void log(int priority, @Nullable String tag, @Nullable String message,
			                   @Nullable Throwable t) {
				if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
					return;
				}
				
				Severity severity = (priority == Log.WARN) ? Severity.WARNING : Severity.ERROR;
				if (t == null) {
					Bugsnag.notify("Timber_" + tag, message, new StackTraceElement[0],
					               severity,
					               null);
				} else {
					Bugsnag.notify(t, severity);
				}
			}
		};
	}
	
	public static CustomLogger getJobQueueLogger() {
		return new CustomLogger() {
			@Override
			public boolean isDebugEnabled() {
				return false; //Too much text pollution
			}
			
			@Override
			public void d(String text, Object... args) {
				//Timber.d(text, args); //Too much text pollution
			}
			
			@Override
			public void e(Throwable t, String text, Object... args) {
				if (t instanceof JobExitingException) {
					//prevents from printing an error on expected Job exceptions
					Timber.i(t, text, args);
				} else {
					Timber.e(t, text, args);
				}
			}
			
			@Override
			public void e(String text, Object... args) {
				Timber.e(text, args);
			}
			
			@Override
			public void v(String text, Object... args) {
			}
		};
	}
}
