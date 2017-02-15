package com.cleytongoncalves.centralufmt.injection.component;

import android.app.Application;
import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.jobs.MenuRuFetchJob;
import com.cleytongoncalves.centralufmt.data.jobs.MoodleLogInJob;
import com.cleytongoncalves.centralufmt.data.jobs.ScheduleFetchJob;
import com.cleytongoncalves.centralufmt.data.jobs.SigaLogInJob;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;
import com.cleytongoncalves.centralufmt.injection.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
	
	@ApplicationContext
	Context context();
	
	Application application();
	
	NetworkService networkService();
	
	PreferencesHelper preferencesHelper();
	
	JobManager jobManager();
	
	DataManager dataManager();
	
	void inject(CentralUfmt centralUfmt);

	void inject(SigaLogInJob sigaLogInJob);
	
	void inject(MoodleLogInJob moodleLogInJob);
	
	void inject(ScheduleFetchJob scheduleFetchJob);
	
	void inject(MenuRuFetchJob menuRuFetchJob);
}
