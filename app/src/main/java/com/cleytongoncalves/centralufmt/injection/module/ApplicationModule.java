package com.cleytongoncalves.centralufmt.injection.module;

import android.app.Application;
import android.content.Context;

import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provide application-level dependencies. Mainly singleton object that can be injected from
 * anywhere in the app.
 */
@Module @SuppressWarnings("WeakerAccess")
public class ApplicationModule {
	protected final Application mApplication;

	public ApplicationModule(Application application) {
		mApplication = application;
	}

	@Provides
	Application provideApplication() {
		return mApplication;
	}

	@Provides
	@ApplicationContext
	Context provideContext() {
		return mApplication;
	}

	@Provides
	@Singleton
	NetworkService provideNetworkService() {
		return NetworkService.Factory.make();
	}
}
