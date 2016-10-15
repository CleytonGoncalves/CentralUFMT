package com.cleytongoncalves.centralufmt.ui.moodle;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.data.remote.MoodleLogInTask;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import okhttp3.Cookie;

public final class MoodlePresenter implements Presenter<MoodleMvpView> {
	private final DataManager mDataManager;
	private MoodleMvpView mMoodleMvpView;

	@Inject
	MoodlePresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(MoodleMvpView mvpView) {
		mMoodleMvpView = mvpView;
	}

	@Override
	public void detachView() {
		mMoodleMvpView = null;
	}

	void doLogIn() {
		final String rga = mDataManager.getPreferencesHelper().getRga();
		final char[] password = mDataManager.getPreferencesHelper().getAuth();

		EventBus.getDefault().register(this);
		new MoodleLogInTask(rga, password, mDataManager.mNetworkService).execute();
		mMoodleMvpView.showProgressBar(true);
		mMoodleMvpView.showWebView(false);
	}

	@Subscribe
	public void onLogIn(LogInEvent event) {
		if (event.isSuccessful()) {
			Cookie cookie = ((List<Cookie>) event.getObjectResult()).get(0);
			String cookieString = cookie.name() + "=" + cookie.value() + "; domain=" + cookie
					                                                                           .domain();
			mMoodleMvpView.onLogInSuccessful(cookieString);
		}
		mMoodleMvpView.showProgressBar(false);
		mMoodleMvpView.showWebView(true);
		EventBus.getDefault().unregister(this);
	}
}
