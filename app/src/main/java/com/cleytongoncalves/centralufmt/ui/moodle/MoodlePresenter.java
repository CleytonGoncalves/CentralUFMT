package com.cleytongoncalves.centralufmt.ui.moodle;

import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
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

	@Override
	public void attachView(MoodleMvpView mvpView) {
		mView = mvpView;

		if (mDataManager.isLoggedInMoodle()) {
			//Already logged in before by self, or after the app login screen.
			onLogInSuccessful(mDataManager.getMoodleCookie());
		} else if (mDataManager.isLogInHappening()) {
			//The app login screen fired the Moodle LogIn,
			//but this got instantiated before its conclusion.
			EventBus.getDefault().register(this);
		} else {
			//First time instantiating this
			doLogIn();
		}
	}

	@Override
	public void detachView() {
		mView = null;
		if (EventBus.getDefault().isRegistered(this)) { EventBus.getDefault().unregister(this); }
	}

	void doLogIn() {
		if (mView != null) {
			mView.showProgressBar(true);
			mView.showWebView(false);
		}

		EventBus.getDefault().register(this);
		mDataManager.moodleLogIn();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onLogInEvent(LogInEvent event) {
		EventBus.getDefault().unregister(this);

		if (event.isSuccessful()) {
			Cookie cookie = (Cookie) event.getResult();
			onLogInSuccessful(cookie);
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
	private void onLogInFailure(String reason) {
		if (mView == null) { return; }

		mView.showGeneralLogInError();
		mView.showWebView(true);
		mView.showProgressBar(false);
	}

	private String getCookieString(Cookie cookie) {
		return cookie.name() + "=" + cookie.value() + "; domain=" + cookie.domain();
	}
}
