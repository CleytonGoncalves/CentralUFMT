package com.cleytongoncalves.centralufmt.data;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.cleytongoncalves.centralufmt.data.events.SigaLogInEvent;
import com.cleytongoncalves.centralufmt.data.events.MenuRuFetchEvent;
import com.cleytongoncalves.centralufmt.data.events.MoodleLogInEvent;
import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
import com.cleytongoncalves.centralufmt.data.jobs.MenuRuFetchJob;
import com.cleytongoncalves.centralufmt.data.jobs.MoodleLogInJob;
import com.cleytongoncalves.centralufmt.data.jobs.ScheduleFetchJob;
import com.cleytongoncalves.centralufmt.data.jobs.SigaLogInJob;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
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
	
	/* ----- Student ----- */
	
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
	 * @param rga     Registration number
	 * @param authKey Siga authentication key
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
		if (! isLoggedInSiga()) { sigaLogIn(); }
		
		mJobManager.addJobInBackground(new ScheduleFetchJob());
	}
	
	private void cancelScheduleFetch() {
		mJobManager.cancelJobsInBackground(null, TagConstraint.ANY, ScheduleFetchJob.TAG);
	}

	/* ----- Menu RU ----- */
	
	public void fetchMenuRu() {
		mJobManager.addJobInBackground(new MenuRuFetchJob());
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
	public void onSigaLogInCompleted(SigaLogInEvent sigaEvent) {
		if (sigaEvent.isSuccessful()) {
			
			mStudent = sigaEvent.getResult();
			mPreferencesHelper.putStudent(mStudent);
			mLoggedInSiga = true;
			
			//If there is more on the queue, cancel them (special case on Single Instance Jobs)
			cancelSigaLogIn();
			Timber.d("Student saved successfully");
		}
	}
	
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMoodleLogInCompleted(MoodleLogInEvent moodleEvent) {
		if (moodleEvent.isSuccessful()) {
			cancelMoodleLogIn();
			mMoodleCookie = moodleEvent.getResult();
			Timber.d("Cookie saved successfully");
		}
	}
	
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onScheduleFetched(ScheduleFetchEvent scheduleEvent) {
		if (scheduleEvent.isSuccessful()) {
			/* - Not used for now -
			Course newCourse = mStudent.getCourse()
			                           .withEnrolledDisciplines(scheduleEvent.getResult());
			
			mStudent = mStudent.withCourse(newCourse);
			
			mPreferencesHelper.putStudent(mStudent);
			*/
			
			Timber.d("Enrolled disciplines saved successfully");
		}
	}
	
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMenuRuFetched(MenuRuFetchEvent menuRuEvent) {
		if (menuRuEvent.isSuccessful()) {
			Timber.d("Menu RU received successfully");
		}
	}
}
