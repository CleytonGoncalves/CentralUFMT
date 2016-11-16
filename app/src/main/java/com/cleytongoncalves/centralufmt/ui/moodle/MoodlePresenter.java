package com.cleytongoncalves.centralufmt.ui.moodle;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import okhttp3.Cookie;
import timber.log.Timber;

public final class MoodlePresenter implements Presenter<MoodleMvpView> {
	private final DataManager mDataManager;
	private MoodleMvpView mView;

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
	}

	private void doLogIn() {
		EventBus.getDefault().register(this);
		mDataManager.triggerMoodleLogIn();

		mView.showProgressBar(true);
		mView.showWebView(false);
	}

	@Subscribe
	public void onLogInEvent(LogInEvent event) {
		EventBus.getDefault().unregister(this);

		if (event.isSuccessful()) {
			Cookie cookie = (Cookie) event.getObjectResult();
			onLogInSuccessful(cookie);
		} else {
			onLogInFailure(event.getFailureReason());
		}
	}

	private void onLogInSuccessful(Cookie cookie) {
		mView.onLogInSuccessful(getCookieString(cookie));
		mView.showWebView(true);
		mView.showProgressBar(false);
	}

	@SuppressWarnings("UnusedParameters")
	private void onLogInFailure(String reason) {
		mView.showGeneralLogInError();
		mView.showProgressBar(false);
	}

	private String getCookieString(Cookie cookie) {
		return cookie.name() + "=" + cookie.value() + "; domain=" + cookie.domain();
	}

	void onDownloadStart() {
		Timber.d("Downloading file");
		mView.showDownloadStart();
	}

	void onLoadingPage() {
		mView.showLoadingTitle();
	}

	void onLoadComplete() {
		mView.showDefaultTitle();
	}
}
