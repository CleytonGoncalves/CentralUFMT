package com.cleytongoncalves.centralufmt.ui.moodle;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
		if (mDataManager.isLoggedInMoodle()) {
			onLogInSuccessful(mDataManager.getMoodleCookie());
		} else {
			doLogIn();
		}
	}

	@Override
	public void detachView() {
		mMoodleMvpView = null;
	}

	private void onLogInSuccessful(Cookie cookie) {
		mMoodleMvpView.onLogInSuccessful(getCookieString(cookie));
		mMoodleMvpView.showWebView(true);
		mMoodleMvpView.showProgressBar(false);
	}

	private void doLogIn() {
		final String rga = mDataManager.getPreferencesHelper().getRga();
		final char[] password = mDataManager.getPreferencesHelper().getAuth();

		EventBus.getDefault().register(this);
		mDataManager.logIn(rga, password, DataManager.LOGIN_MOODLE);

		mMoodleMvpView.showProgressBar(true);
		mMoodleMvpView.showWebView(false);
	}

	private String getCookieString(Cookie cookie) {
		return cookie.name() + "=" + cookie.value() + "; domain=" + cookie.domain();
	}

	@Subscribe
	public void onLogInEvent(LogInEvent event) {
		if (event.isSuccessful()) {
			Cookie cookie = (Cookie) event.getObjectResult();
			onLogInSuccessful(cookie);
		} else {
			onLogInFailure(event.getFailureReason());
		}
		EventBus.getDefault().unregister(this);
	}

	private void onLogInFailure(String reason) {
		mMoodleMvpView.showGeneralLogInError();
		mMoodleMvpView.showProgressBar(false);
	}
}
