package com.cleytongoncalves.centralufmt.injection.module;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.data.jobs.NetworkJob;
import com.cleytongoncalves.centralufmt.data.local.DatabaseHelper;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.model.DaoMaster;
import com.cleytongoncalves.centralufmt.data.model.DaoSession;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;
import com.cleytongoncalves.centralufmt.ui.schedule.GsonAdaptersAbstractScheduleData;
import com.cleytongoncalves.centralufmt.util.Logger;
import com.cleytongoncalves.centralufmt.util.converter.DateTimeConverter;
import com.cleytongoncalves.centralufmt.util.converter.IntervalConverter;
import com.cleytongoncalves.centralufmt.util.converter.LocalDateConverter;
import com.cleytongoncalves.centralufmt.util.converter.LocalTimeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.database.Database;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

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
	SharedPreferences provideSharedPreferences() {
		return mApplication.getSharedPreferences(PreferencesHelper.PREF_FILE_NAME, Context.MODE_PRIVATE);
	}
	
	@Provides
	Gson provideGson() {
		return new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeConverter())
		                        .registerTypeAdapter(LocalDate.class, new LocalDateConverter())
		                        .registerTypeAdapter(LocalTime.class, new LocalTimeConverter())
		                        .registerTypeAdapter(Interval.class, new IntervalConverter())
		                        .registerTypeAdapterFactory(new GsonAdaptersAbstractScheduleData())
		                        .create();
	}
	
	@Provides
	@Singleton
	NetworkService provideNetworkService() {
		return NetworkService.builder().build(provideContext());
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
	
	@Provides
	@Singleton
	DaoSession provideDaoSession() {
		DatabaseHelper.DbOpenHelper helper = new DatabaseHelper.DbOpenHelper(provideContext());
		Database db = helper.getWritableDb();
		
		return new DaoMaster(db).newSession();
	}
}
