package com.cleytongoncalves.centralufmt;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Severity;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.component.DaggerApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.module.ApplicationModule;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;

import timber.log.Timber;

public class CentralUfmt extends Application {
	ApplicationComponent mApplicationComponent;
	private RefWatcher mRefWatcher;

	public static CentralUfmt get(Context context) {
		return (CentralUfmt) context.getApplicationContext();
	}

	public static RefWatcher getRefWatcher(Context context) {
		return ((CentralUfmt) context.getApplicationContext()).mRefWatcher;
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onCreate() {
		super.onCreate();
		if (LeakCanary.isInAnalyzerProcess(this)) { return; }

		initDependencies();
		plantTrees();
	}

	public ApplicationComponent getComponent() {
		return mApplicationComponent;
	}

	// Needed to replace the component with a test specific one
	public void setComponent(ApplicationComponent applicationComponent) {
		mApplicationComponent = applicationComponent;
	}

	private void initDependencies() {
		mApplicationComponent = DaggerApplicationComponent
				                        .builder()
				                        .applicationModule(new ApplicationModule(this))
				                        .build();
		mApplicationComponent.inject(this);

		mRefWatcher = LeakCanary.install(this);

		JodaTimeAndroid.init(this);
	}

	private void plantTrees() {
		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree() {
				@Override
				protected void log(int priority, String tag, String message, Throwable t) {
					super.log(priority, "Timber_" + tag, message, t);
				}
			});
		} else {
			Bugsnag.init(this);
			Timber.plant(new Timber.Tree() {
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
			});
		}
	}

}
