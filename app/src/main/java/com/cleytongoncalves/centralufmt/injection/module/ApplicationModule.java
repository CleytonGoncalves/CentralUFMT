package com.cleytongoncalves.centralufmt.injection.module;

import android.app.Application;
import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.data.jobs.NetworkJob;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;
import com.cleytongoncalves.centralufmt.util.Logger;

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
		return NetworkService.builder().build();
	}
	
	@Provides
	@Singleton
	JobManager provideJobManager() {
		Configuration.Builder builder =
				new Configuration.Builder(provideContext())
						.minConsumerCount(1) // always keep at least one consumer alive
						.maxConsumerCount(3) // up to 3 consumers at a time
						.loadFactor(3) // 3 jobs per consumer
						.consumerKeepAlive(120) // wait 2 minute
						.customLogger(Logger.getJobQueueLogger())
						.injector(job -> {
							if (job instanceof NetworkJob) {
								((NetworkJob) job).inject(((CentralUfmt) mApplication).getComponent());
							}
						});
		
		return new JobManager(builder.build());
	}
}
