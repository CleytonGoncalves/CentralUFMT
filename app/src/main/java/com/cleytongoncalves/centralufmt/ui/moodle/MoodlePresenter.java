package com.cleytongoncalves.centralufmt.ui.moodle;

import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import okhttp3.Cookie;

public final class MoodlePresenter implements Presenter<MoodleMvpView> {
	private final DataManager mDataManager;
	@Nullable private MoodleMvpView mMoodleMvpView;

	@Inject
	MoodlePresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(MoodleMvpView mvpView) {
		mMoodleMvpView = mvpView;
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
		mMoodleMvpView = null;
	}

	private void onLogInSuccessful(Cookie cookie) {
		if (mMoodleMvpView != null) {
			mMoodleMvpView.onLogInSuccessful(getCookieString(cookie));
			mMoodleMvpView.showWebView(true);
			mMoodleMvpView.showProgressBar(false);
		}
	}

	private void doLogIn() {
		mDataManager.triggerMoodleLogIn();

		EventBus.getDefault().register(this);

		if (mMoodleMvpView != null) {
			mMoodleMvpView.showProgressBar(true);
			mMoodleMvpView.showWebView(false);
		}
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

	@SuppressWarnings("UnusedParameters")
	private void onLogInFailure(String reason) {
		if (mMoodleMvpView != null) {
			mMoodleMvpView.showGeneralLogInError();
			mMoodleMvpView.showProgressBar(false);
		}
	}

	void onDownloadStart() {
		if (mMoodleMvpView != null) {
			mMoodleMvpView.showDownloadStart();
		}
	}

	void onLoadingPage() {
		if (mMoodleMvpView != null) {
			mMoodleMvpView.showLoadingTitle();
		}
	}

	void onLoadComplete() {
		if (mMoodleMvpView != null) {
			mMoodleMvpView.showDefaultTitle();
		}
	}
}
