package com.cleytongoncalves.centralufmt.data;

import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.data.remote.SigaLogInTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataManager {
	private static final String TAG = "DataManager";

	public final NetworkService mNetworkService;
	private final PreferencesHelper mPreferencesHelper;
	private SigaLogInTask mSigaLoginTask;
	private Student mStudent;

	@Inject
	public DataManager(PreferencesHelper preferencesHelper, NetworkService networkService) {
		mPreferencesHelper = preferencesHelper;
		mNetworkService = networkService;
		mStudent = preferencesHelper.getLoggedInStudent();
	}

	public PreferencesHelper getPreferencesHelper() {
		return mPreferencesHelper;
	}

	public Student getStudent() {
		return mStudent;
	}

	/**
	 * Parses recurse-intensive Student info on a background thread.
	 */
	public void doHeavyStudentParsing(final Student student) {
		//TODO: Maybe make this a Service?
		new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(android.os.Process
						                                     .THREAD_PRIORITY_BACKGROUND);
				//mHtmlHelper.parseCompleteCourse();
				mPreferencesHelper.putLoggedInStudent(student);
			}
		}).start();
	}

	/* ----- LogIn Methods ----- */
	public void logIn(String rga, char[] password) {
		EventBus.getDefault().register(this);
		mPreferencesHelper.putCredentials(rga, password);
		mSigaLoginTask = new SigaLogInTask(rga, password, mNetworkService);
		mSigaLoginTask.execute();
	}

	public void cancelLogIn() {
		if (mSigaLoginTask != null) {
			mSigaLoginTask.cancelTask();
		}
	}

	public boolean isLogInHappening() {
		return mSigaLoginTask != null;
	}

	public boolean isLoggedIn() {
		return mStudent != null;
	}

	@Subscribe
	public void onLogInCompleted(LogInEvent event) {
		mSigaLoginTask = null;
		if (event.isSuccessful()) {
			mStudent = (Student) event.getObjectResult();
			mPreferencesHelper.putLoggedInStudent(mStudent);
		}
		EventBus.getDefault().unregister(this);
	}
}
