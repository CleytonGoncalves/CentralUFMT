package com.cleytongoncalves.centralufmt.ui.moodle;

import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.MoodleLogInEvent;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import okhttp3.Cookie;

public final class MoodlePresenter implements Presenter<MoodleMvpView> {
	private final DataManager mDataManager;

	@Nullable private MoodleMvpView mView;

	@Inject
	MoodlePresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	/* View Methods */
	
	@Override
	public void attachView(MoodleMvpView mvpView) {
		mView = mvpView;
	}

	@Override
	public void detachView() {
		mView = null;
		if (EventBus.getDefault().isRegistered(this)) { EventBus.getDefault().unregister(this); }
	}
	
	void init() {
		if (mDataManager.isLoggedInMoodle()) {
			//Already logged in before by self, or after the app login screen.
			onLogInSuccessful(mDataManager.getMoodleCookie());
		} else {
			logIn();
		}
	}

	void logIn() {
		if (mView != null) {
			mView.showProgressBar(true);
			mView.showWebView(false);
		}

		EventBus.getDefault().register(this);
		mDataManager.moodleLogIn();
	}

	/* Data Methods */
	
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMoodleLogInEvent(MoodleLogInEvent event) {
		EventBus.getDefault().unregister(this);

		if (event.isSuccessful()) {
			onLogInSuccessful(event.getResult());
		} else {
			onLogInFailure(event.getFailureReason());
		}
	}

	private void onLogInSuccessful(Cookie cookie) {
		if (mView == null) { return; }

		mView.setCookieString(getCookieString(cookie));
		mView.loadStartPage();
		mView.showWebView(true);
	}

	@SuppressWarnings("UnusedParameters")
	private void onLogInFailure(int reason) {
		if (mView == null) { return; }

		mView.showGeneralLogInError();
		mView.showWebView(true);
		mView.showProgressBar(false);
	}

	/* Private Helper Methods */
	
	private String getCookieString(Cookie cookie) {
		return cookie.name() + "=" + cookie.value() + "; domain=" + cookie.domain();
	}
}
