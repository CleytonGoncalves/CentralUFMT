package com.cleytongoncalves.centralufmt.data;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.data.events.MenuRuFetchEvent;
import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
import com.cleytongoncalves.centralufmt.data.jobs.MenuRuFetchJob;
import com.cleytongoncalves.centralufmt.data.jobs.MoodleLogInJob;
import com.cleytongoncalves.centralufmt.data.jobs.ScheduleFetchJob;
import com.cleytongoncalves.centralufmt.data.jobs.SigaLogInJob;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.model.Course;
import com.cleytongoncalves.centralufmt.data.model.Student;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Cookie;
import timber.log.Timber;

@Singleton
public class DataManager {
	private final PreferencesHelper mPreferencesHelper;
	private final JobManager mJobManager;
	
	private Student mStudent;
	private Cookie mMoodleCookie;
	
	private boolean mLoggedInSiga;
	
	@Inject
	public DataManager(PreferencesHelper preferencesHelper, JobManager jobManager) {
		mPreferencesHelper = preferencesHelper;
		mJobManager = jobManager;
		
		mStudent = preferencesHelper.getStudent();
		
		EventBus.getDefault().register(this);
	}
	
	public PreferencesHelper getPreferencesHelper() {
		return mPreferencesHelper;
	}
	
	public Student getStudent() {
		return mStudent;
	}
	
	/**
	 * @return true, if there is a fetched Student
	 */
	public boolean hasStudent() {
		return mStudent != null;
	}

	/* ----- Siga ----- */
	
	/**
	 * Initial App LogIn
	 * @param rga      Registration number
	 * @param authKey  Siga authentication key
	 */
	public void initialLogIn(String rga, char[] authKey) {
		mPreferencesHelper.putCredentials(rga, authKey);
		mJobManager.addJobInBackground(new SigaLogInJob(rga, authKey));
	}
	
	/**
	 * Must be called <b>once per app execution</b> before acessing anything on Siga
	 */
	private void sigaLogIn() {
		final String rga = mPreferencesHelper.getRga();
		final char[] password = mPreferencesHelper.getAuth();
		
		mJobManager.addJobInBackground(new SigaLogInJob(rga, password));
	}
	
	public boolean isSigaLogInHappening() {
		return false;
	}
	
	public void cancelSigaLogIn() {
		mJobManager.cancelJobsInBackground(null, TagConstraint.ANY, SigaLogInJob.TAG);
	}
	
	/**
	 * @return true, if it is currently logged in on Siga
	 */
	private boolean isLoggedInSiga() {
		return mLoggedInSiga;
	}
	
	/* ----- Moodle ----- */
	
	/**
	 * Must be called <b>once per app execution</b> before executing anything Moodle-related
	 */
	public void moodleLogIn() {
		final String rga = mPreferencesHelper.getRga();
		final char[] authKey = mPreferencesHelper.getAuth();
		
		mJobManager.addJobInBackground(new MoodleLogInJob(rga, authKey));
	}
	
	public boolean isMoodleLogInHappening() {
		return false;
	}
	
	private void cancelMoodleLogIn() {
		mJobManager.cancelJobsInBackground(null, TagConstraint.ANY, MoodleLogInJob.TAG);
	}
	
	public boolean isLoggedInMoodle() {
		return mMoodleCookie != null;
	}
	
	public Cookie getMoodleCookie() {
		return mMoodleCookie;
	}

	/* ----- Schedule ----- */
	
	public void fetchSchedule() {
		if (!isLoggedInSiga()) { sigaLogIn(); }
		
		mJobManager.addJobInBackground(new ScheduleFetchJob());
	}
	
	public boolean isFetchingSchedule() {
		return false;
	}
	
	private void cancelScheduleFetch() {
		mJobManager.cancelJobsInBackground(null, TagConstraint.ANY, ScheduleFetchJob.TAG);
	}

	/* ----- Menu RU ----- */
	
	public void fetchMenuRu() {
		mJobManager.addJobInBackground(new MenuRuFetchJob());
	}
	
	public boolean isFetchingMenuRu() {
		return false;
	}
	
	private void cancelMenuRuFetch() {
		mJobManager.cancelJobsInBackground(null, TagConstraint.ANY, MenuRuFetchJob.TAG);
	}
	
	/* ----- Settings - LogOut ----- */
	
	public void logOut() {
		cancelSigaLogIn();
		cancelMoodleLogIn();
		cancelScheduleFetch();
		cancelMenuRuFetch();
		
		mStudent = null;
		mMoodleCookie = null;
		
		mPreferencesHelper.clear();
	}

	/* ----- EventBus Listeners ----- */
	
	@Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
	public void onLogInCompleted(LogInEvent logInEvent) {
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
		//TODO: Move save menuru to prefs here
		if (menuRuEvent.isSuccessful()) {
			Timber.d("Menu RU received successfully");
		}
	}
}
