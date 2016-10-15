package com.cleytongoncalves.centralufmt.injection.component;

import android.app.Application;
import android.content.Context;

import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;
import com.cleytongoncalves.centralufmt.injection.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

	void inject(CentralUfmt centralUfmt);

	@ApplicationContext
	Context context();

	Application application();

	NetworkService networkService();

	PreferencesHelper preferencesHelper();

	DataManager dataManager();
}
