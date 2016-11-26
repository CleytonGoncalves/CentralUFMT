package com.cleytongoncalves.centralufmt;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.component.DaggerApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.module.ApplicationModule;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class CentralUfmt extends Application {
	@Inject DataManager mDataManager;

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

		CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
		Fabric.with(this, new Crashlytics.Builder().core(core).build());
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
			Timber.plant(new CrashlyticsTree());
		}
	}

	private static class CrashlyticsTree extends Timber.Tree {
		private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
		private static final String CRASHLYTICS_KEY_TAG = "tag";
		private static final String CRASHLYTICS_KEY_MESSAGE = "message";

		@Override
		protected void log(int priority, @Nullable String tag, @Nullable String message,
		                   @Nullable Throwable t) {
			if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
				return;
			}

			Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
			Crashlytics.setString(CRASHLYTICS_KEY_TAG, "Timber_" + tag);
			Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

			if (t == null) {
				Crashlytics.logException(new Exception(message));
			} else {
				Crashlytics.logException(t);
			}
		}
	}

}
