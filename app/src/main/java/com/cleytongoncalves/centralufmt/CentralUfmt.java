package com.cleytongoncalves.centralufmt;

import android.app.Application;
import android.content.Context;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.component.DaggerApplicationComponent;
import com.cleytongoncalves.centralufmt.injection.module.ApplicationModule;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

public class CentralUfmt extends Application {
	@Inject DataManager mDataManager;

	ApplicationComponent mApplicationComponent;

	public static CentralUfmt get(Context context) {
		return (CentralUfmt) context.getApplicationContext();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mApplicationComponent = DaggerApplicationComponent.builder()
		                                                  .applicationModule(new ApplicationModule
				                                                                     (this))
		                                                  .build();
		mApplicationComponent.inject(this);
		JodaTimeAndroid.init(this);
	}

	public ApplicationComponent getComponent() {
		return mApplicationComponent;
	}

	// Needed to replace the component with a test specific one
	public void setComponent(ApplicationComponent applicationComponent) {
		mApplicationComponent = applicationComponent;
	}


}
