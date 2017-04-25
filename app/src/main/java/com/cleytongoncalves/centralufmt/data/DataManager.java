package com.cleytongoncalves.centralufmt.data;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.cleytongoncalves.centralufmt.data.events.SigaLogInEvent;
import com.cleytongoncalves.centralufmt.data.jobs.network.MenuRuFetchJob;
import com.cleytongoncalves.centralufmt.data.jobs.network.MoodleLogInJob;
import com.cleytongoncalves.centralufmt.data.jobs.network.ScheduleFetchJob;
import com.cleytongoncalves.centralufmt.data.jobs.network.SigaLogInJob;
import com.cleytongoncalves.centralufmt.data.local.DatabaseHelper;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.model.Student;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataManager {
	private final PreferencesHelper mPreferencesHelper;
	private final DatabaseHelper mDbHelper;
	private final JobManager mJobManager;
	
	private boolean mLoggedInSiga;
	
	@Inject
	public DataManager(PreferencesHelper prefHelper, DatabaseHelper dbHelper, JobManager jobMngr) {
		mPreferencesHelper = prefHelper;
		mJobManager = jobMngr;
		mDbHelper = dbHelper;
		
		init();
	}
	
	private void init() {
		EventBus.getDefault().register(this);
	}
	
	/* ----- Student ----- */
	
	public Student getStudent() {
		return mDbHelper.getStudent();
	}
	
	/**
	 * @return true, if there is a fetched Student
	 */
	public boolean hasStudent() {
		return mDbHelper.hasStudent();
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
		final char[] authKey = mPreferencesHelper.getAuthKey();
		
		mJobManager.addJobInBackground(new SigaLogInJob(rga, authKey));
	}
	
	public void cancelSigaLogIn() {
		mJobManager.cancelJobsInBackground(null, TagConstraint.ANY, SigaLogInJob.TAG);
	}
	
	/**
	 * @return true, if it is currently logged in on Siga
	 */
	public boolean isLoggedInSiga() {
		return mLoggedInSiga;
	}
	
	/* ----- Moodle ----- */
	
	/**
	 * Must be called <b>once per app execution</b> before executing anything Moodle-related
	 */
	public void moodleLogIn() {
		final String rga = mPreferencesHelper.getRga();
		final char[] authKey = mPreferencesHelper.getAuthKey();
		
		mJobManager.addJobInBackground(new MoodleLogInJob(rga, authKey));
	}
	
	public void cancelMoodleLogIn() {
		mJobManager.cancelJobsInBackground(null, TagConstraint.ANY, MoodleLogInJob.TAG);
	}

	/* ----- Schedule ----- */
	
	public void fetchSchedule() {
		if (! isLoggedInSiga()) {
			sigaLogIn();
		}
		
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
		
		mPreferencesHelper.clear();
		mDbHelper.clearDb();
	}

	/* ----- EventBus Listeners ----- */
	
	@Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
	public void onSigaLogInCompleted(SigaLogInEvent sigaEvent) {
		if (sigaEvent.isSuccessful()) {
			mLoggedInSiga = true;
			
			//If there is more on the queue, cancel them (special case on Single Instance Jobs)
			cancelSigaLogIn();
		}
	}
	
}
