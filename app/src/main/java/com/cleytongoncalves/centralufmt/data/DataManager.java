package com.cleytongoncalves.centralufmt.data;

import android.os.AsyncTask;

import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.data.remote.task.LogInTask;
import com.cleytongoncalves.centralufmt.data.remote.task.MoodleLogInTask;
import com.cleytongoncalves.centralufmt.data.remote.task.ScheduleTask;
import com.cleytongoncalves.centralufmt.data.remote.task.SigaLogInTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import okhttp3.Cookie;
import timber.log.Timber;

@Singleton
public class DataManager {
	//TODO: Login before other net operations when operating from cache
	public static final int LOGIN_SIGA = 0;
	private static final int LOGIN_MOODLE = - 1;

	private final Lazy<NetworkService> mNetworkService;
	private final PreferencesHelper mPreferencesHelper;

	private LogInTask mLogInTask;
	private ScheduleTask mScheduleTask;

	private Student mStudent;
	private Cookie mMoodleCookie;

	@Inject
	public DataManager(final PreferencesHelper preferencesHelper,
	                   final Lazy<NetworkService> networkService) {
		mPreferencesHelper = preferencesHelper;
		mNetworkService = networkService;
		mStudent = preferencesHelper.getLoggedInStudent();
		EventBus.getDefault().register(this);
	}

	public PreferencesHelper getPreferencesHelper() {
		return mPreferencesHelper;
	}

	/**
	 * Initializes the Network Service if it hasn't been used yet.
	 */
	public void initializeNetwork() {
		AsyncTask.execute(mNetworkService::get); //Singleton instantiation is thread-safe on Dagger
	}

	public Student getStudent() {
		return mStudent;
	}

	public Cookie getMoodleCookie() {
		return mMoodleCookie;
	}

	/* ----- LogIn ----- */

	public void logIn(String rga, char[] password, int platform) {
		if (platform == LOGIN_MOODLE) {
			mLogInTask = new MoodleLogInTask(rga, password, mNetworkService.get());
		} else {
			mPreferencesHelper.putCredentials(rga, password);
			mLogInTask = new SigaLogInTask(rga, password, mNetworkService.get());
		}

		Timber.d("LogIn - %s", platform == LOGIN_MOODLE ? "Moodle" : "Siga");
		mLogInTask.start();
	}

	public void triggerMoodleLogIn() {
		final String rga = mPreferencesHelper.getRga();
		final char[] password = mPreferencesHelper.getAuth();
		logIn(rga, password, DataManager.LOGIN_MOODLE);
	}

	public void cancelLogIn() {
		Timber.d("Cancel LogIn");
		if (mLogInTask != null) {
			mLogInTask.cancelTask();
		}
	}

	public boolean isLogInHappening() {
		return mLogInTask != null;
	}

	public boolean isLoggedInSiga() {
		return mStudent != null;
	}

	public boolean isLoggedInMoodle() {
		return mMoodleCookie != null;
	}

	/* ----- Schedule ----- */

	public void fetchSchedule() {
		Timber.d("Fetching Schedule");
		mScheduleTask = new ScheduleTask(mNetworkService.get());
		mScheduleTask.execute();
	}

	/* ----- EventBus Listeners ----- */

	@Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
	public void onLogInCompleted(LogInEvent logInEvent) {
		mLogInTask = null;

		if (logInEvent.isSuccessful()) {
			Object obj = logInEvent.getObjectResult();

			if (obj.getClass() == Student.class) {
				mStudent = (Student) obj;
				mPreferencesHelper.putLoggedInStudent(mStudent);
				Timber.d("Student saved successfully");
				triggerMoodleLogIn();
			} else if (obj.getClass() == Cookie.class) {
				mMoodleCookie = (Cookie) obj;
				Timber.d("Cookie saved successfully");
			} else {
				Timber.wtf("LogInEvent object unknown: %s", obj.getClass());
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
	public void onScheduleFetched(ScheduleFetchEvent scheduleEvent) {
		mScheduleTask = null;

		if (scheduleEvent.isSuccessful()) {
			mStudent.getCourse().setEnrolledDisciplines(scheduleEvent.getDisciplineList());
			Timber.d("Enrolled disciplines saved successfully");
		}
	}
}
