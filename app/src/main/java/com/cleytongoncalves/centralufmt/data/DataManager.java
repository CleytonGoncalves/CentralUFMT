package com.cleytongoncalves.centralufmt.data;

import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.data.events.MenuRuFetchEvent;
import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.model.Course;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.data.remote.task.LogInTask;
import com.cleytongoncalves.centralufmt.data.remote.task.MenuRuTask;
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
	private final Lazy<NetworkService> mNetworkService;
	private final PreferencesHelper mPreferencesHelper;
	
	private LogInTask mLogInTask;
	private ScheduleTask mScheduleTask;
	private MenuRuTask mMenuRuTask;
	
	private Student mStudent;
	private Cookie mMoodleCookie;
	
	private boolean mLoggedInSiga;
	
	@Inject
	public DataManager(PreferencesHelper preferencesHelper, Lazy<NetworkService> networkService) {
		mPreferencesHelper = preferencesHelper;
		mNetworkService = networkService;
		
		mStudent = preferencesHelper.getStudent();
		
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
	
	/**
	 * Initial App LogIn
	 *
	 * @param rga      Registration number
	 * @param password Siga password
	 */
	public void initialLogIn(String rga, char[] password) {
		mPreferencesHelper.putCredentials(rga, password);
		mLogInTask = new SigaLogInTask(rga, password, mNetworkService);
		
		Timber.d("Starting Initial App LogIn");
		mLogInTask.start();
	}
	
	/**
	 * Must be called <b>once per app execution</b> before doing any update on Student info from
	 * Siga
	 */
	private void sigaLogIn() {
		final String rga = mPreferencesHelper.getRga();
		final char[] password = mPreferencesHelper.getAuth();
		mLogInTask = new SigaLogInTask(rga, password, mNetworkService);
		
		Timber.d("Starting Siga LogIn");
		mLogInTask.start();
	}
	
	/**
	 * Must be called <b>once per app execution</b> before executing anything Moodle-related
	 */
	public void moodleLogIn() {
		final String rga = mPreferencesHelper.getRga();
		final char[] password = mPreferencesHelper.getAuth();
		mLogInTask = new MoodleLogInTask(rga, password, mNetworkService);
		
		Timber.d("Starting Moodle LogIn");
		mLogInTask.start();
	}
	
	/**
	 * Cancels whichever login is occuring at the moment
	 */
	public void cancelLogIn() {
		Timber.d("Cancelling LogIn");
		if (mLogInTask != null) {
			mLogInTask.cancelTask();
			mLogInTask = null;
		}
	}
	
	/**
	 * @return true, if there is any login occurring
	 */
	public boolean isLogInHappening() {
		return mLogInTask != null;
	}
	
	/**
	 * @return true, if there is a fetched Student
	 */
	public boolean hasStudent() {
		return mStudent != null;
	}
	
	/**
	 * @return true, if it is currently logged in on Moodle
	 */
	public boolean isLoggedInMoodle() {
		return mMoodleCookie != null;
	}
	
	/**
	 * @return true, if it is currently logged in on Siga
	 */
	private boolean isLoggedInSiga() {
		return mLoggedInSiga;
	}

	/* ----- Schedule ----- */
	
	public void fetchSchedule() {
		Timber.d("Fetching Schedule");
		
		if (! isLoggedInSiga()) {
			sigaLogIn();
		}
		
		mScheduleTask = new ScheduleTask(mNetworkService);
		mScheduleTask.execute();
	}
	
	public boolean isFetchingSchedule() {
		return mScheduleTask != null;
	}
	
	private void cancelScheduleFetch() {
		Timber.d("Cancel Schedule Fetch");
		if (mScheduleTask != null) {
			mScheduleTask.cancel(true);
			mScheduleTask = null;
		}
	}

	/* ----- Menu RU ----- */
	
	public void fetchMenuRu() {
		Timber.d("Fetching Menu Ru");
		mMenuRuTask = new MenuRuTask(mNetworkService);
		mMenuRuTask.execute();
	}
	
	public boolean isFetchingMenuRu() {
		return mMenuRuTask != null;
	}
	
	private void cancelMenuRuFetch() {
		Timber.d("Cancel MenuRu Fetch");
		if (mMenuRuTask != null) {
			mMenuRuTask.cancel(true);
			mMenuRuTask = null;
		}
	}
	
	/* ----- Settings - Logout ----- */
	
	public void logOut() {
		cancelLogIn();
		cancelScheduleFetch();
		cancelMenuRuFetch();
		
		mStudent = null;
		mMoodleCookie = null;
		
		mPreferencesHelper.clear();
	}

	/* ----- EventBus Listeners ----- */
	
	@Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
	public void onLogInCompleted(LogInEvent logInEvent) {
		mLogInTask = null;
		
		if (logInEvent.isSuccessful()) {
			Object obj = logInEvent.getResult();
			
			if (Student.class.isAssignableFrom(obj.getClass())) {
				mStudent = (Student) obj;
				mPreferencesHelper.putStudent(mStudent);
				mLoggedInSiga = true;
				Timber.d("Student saved successfully");
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
			Course newCourse = mStudent.getCourse()
			                           .withEnrolledDisciplines(scheduleEvent.getResult());
			
			mStudent = mStudent.withCourse(newCourse);
			
			mPreferencesHelper.putStudent(mStudent);
			Timber.d("Enrolled disciplines saved successfully");
		}
	}
	
	@Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
	public void onMenuRuFetched(MenuRuFetchEvent menuRuEvent) {
		mMenuRuTask = null;
		
		if (menuRuEvent.isSuccessful()) {
			Timber.d("Menu RU received successfully");
		}
	}
}
