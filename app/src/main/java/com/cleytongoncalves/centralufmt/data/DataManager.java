package com.cleytongoncalves.centralufmt.data;

import android.util.Log;

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

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Cookie;

@Singleton
public class DataManager {
	//TODO: Login before other net operations when operating from cache
	private static final String TAG = "DataManager";
	public static final int LOGIN_SIGA = 0;
	private static final int LOGIN_MOODLE = - 1;

	private final NetworkService mNetworkService;
	private final PreferencesHelper mPreferencesHelper;

	private LogInTask mLogInTask;
	private ScheduleTask mScheduleTask;

	private Student mStudent;
	private Cookie mMoodleCookie;

	@Inject
	public DataManager(PreferencesHelper preferencesHelper, NetworkService networkService) {
		mPreferencesHelper = preferencesHelper;
		mNetworkService = networkService;
		mStudent = preferencesHelper.getLoggedInStudent();
		EventBus.getDefault().register(this);
	}

	public PreferencesHelper getPreferencesHelper() {
		return mPreferencesHelper;
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
			mLogInTask = new MoodleLogInTask(rga, password, mNetworkService);
		} else {
			mPreferencesHelper.putCredentials(rga, password);
			mLogInTask = new SigaLogInTask(rga, password, mNetworkService);
		}

		mLogInTask.start();
	}

	public void triggerMoodleLogIn() {
		final String rga = mPreferencesHelper.getRga();
		final char[] password = mPreferencesHelper.getAuth();
		logIn(rga, password, DataManager.LOGIN_MOODLE);
	}

	public void cancelLogIn() {
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
		Log.i(TAG, "STARTING SCHEDULE FETCH");
		mScheduleTask = new ScheduleTask(mNetworkService);
		mScheduleTask.execute();
	}

	/* ----- EventBus Listeners ----- */

	@Subscribe(priority = 1)
	public void onLogInCompleted(LogInEvent logInEvent) {
		mLogInTask = null;

		if (logInEvent.isSuccessful()) {
			Object obj = logInEvent.getObjectResult();

			if (obj.getClass() == Student.class) {
				mStudent = (Student) obj;
				mPreferencesHelper.putLoggedInStudent(mStudent);
				triggerMoodleLogIn();
			} else if (obj.getClass() == Cookie.class) {
				mMoodleCookie = (Cookie) obj;
			} else {
				Log.e(TAG, "LOGIN EVENT OBJECT UNKNOWN: " + obj.getClass());
			}
		} else {
			Log.w(TAG, "LOGIN FAILED: " + logInEvent.getFailureReason());
		}
	}

	@Subscribe(priority = 1)
	public void onScheduleFetched(ScheduleFetchEvent scheduleEvent) {
		mScheduleTask = null;

		if (scheduleEvent.isSuccessful()) {
			mStudent.getCourse().setEnrolledDisciplines(scheduleEvent.getDisciplineList());
		}
	}
}
