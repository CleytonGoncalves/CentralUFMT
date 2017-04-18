package com.cleytongoncalves.centralufmt;

import android.app.Application;
import android.content.Context;

import com.bugsnag.android.Bugsnag;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.component.DaggerApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.module.ApplicationModule;
import com.cleytongoncalves.centralufmt.util.Logger;
import com.facebook.stetho.Stetho;
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
		
		Stetho.initializeWithDefaults(this);
	}

	private void plantTrees() {
		if (BuildConfig.DEBUG) {
			Timber.plant(Logger.getDebugTree());
		} else {
			Bugsnag.init(this);
			Timber.plant(Logger.getBugsnagTree());
		}
	}

}
