package com.cleytongoncalves.centralufmt.injection.component;

import com.cleytongoncalves.centralufmt.injection.PerActivity;
import com.cleytongoncalves.centralufmt.injection.module.ActivityModule;
import com.cleytongoncalves.centralufmt.ui.LauncherActivity;
import com.cleytongoncalves.centralufmt.ui.login.LogInActivity;
import com.cleytongoncalves.centralufmt.ui.main.MainActivity;
import com.cleytongoncalves.centralufmt.ui.map.MapFragment;
import com.cleytongoncalves.centralufmt.ui.moodle.MoodleFragment;
import com.cleytongoncalves.centralufmt.ui.schedule.ScheduleFragment;

import dagger.Component;


@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

	void inject(LauncherActivity launcherActivity);

	void inject(LogInActivity logInActivity);

	void inject(MainActivity mainActivity);

	void inject(MapFragment mapFragment);

	void inject(MoodleFragment moodleFragment);

	void inject(ScheduleFragment scheduleFragment);

}
