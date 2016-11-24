package com.cleytongoncalves.centralufmt;

import android.app.Application;
import android.content.Context;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.component.DaggerApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.module.ApplicationModule;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

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
	public void onCreate() {
		super.onCreate();
		if (LeakCanary.isInAnalyzerProcess(this)) {
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}
		mRefWatcher = LeakCanary.install(this);

		mApplicationComponent = DaggerApplicationComponent.builder()
		                                                  .applicationModule(new ApplicationModule
				                                                                     (this))
		                                                  .build();
		mApplicationComponent.inject(this);
		JodaTimeAndroid.init(this);

		if (BuildConfig.DEBUG) { Timber.plant(new Timber.DebugTree()); }


	}

	public ApplicationComponent getComponent() {
		return mApplicationComponent;
	}

	// Needed to replace the component with a test specific one
	public void setComponent(ApplicationComponent applicationComponent) {
		mApplicationComponent = applicationComponent;
	}


}
