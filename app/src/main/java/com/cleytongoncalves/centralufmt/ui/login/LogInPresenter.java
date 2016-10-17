package com.cleytongoncalves.centralufmt.ui.login;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

public final class LogInPresenter implements Presenter<LogInMvpView> {
	private final DataManager mDataManager;
	private LogInMvpView mMvpView;

	@Inject
	LogInPresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(LogInMvpView mvpView) {
		mMvpView = mvpView;
		if (mDataManager.isLoggedInSiga()) {
			//Check if it isn't already logged in when attaching the view
			onLogInSuccess(false);
		}
	}

	@Override
	public void detachView() {
		mMvpView = null;
	}

	private void onLogInSuccess(boolean anonymous) {
		mDataManager.getPreferencesHelper().setAnonymousLogIn(anonymous);
		mMvpView.showProgress(false);
		mMvpView.onLogInSuccessful();
	}

	void doLogIn(String rga, char[] password) {
		EventBus.getDefault().register(this);
		mDataManager.logIn(rga, password, DataManager.LOGIN_SIGA);

		mMvpView.setLogInButtonEnabled(false);
		mMvpView.setAnonymousLogInEnabled(false);
		mMvpView.showProgress(true);
	}

	void doAnonymousLogIn() {
		onLogInSuccess(true); //Goes directly to the result
		mMvpView.setLogInButtonEnabled(false);
		mMvpView.setAnonymousLogInEnabled(false);
		mMvpView.showProgress(true);
	}

	void cancelLogin() {
		mDataManager.cancelLogIn();
	}

	boolean isLogInHappening() {
		return mDataManager.isLogInHappening();
	}

	@Subscribe
	public void onLogInEvent(LogInEvent event) {
		if (event.isSuccessful()) {
			onLogInSuccess(false);
		} else {
			onLogInFailure(event.getFailureReason());
		}
		EventBus.getDefault().unregister(this);
	}

	private void onLogInFailure(String reason) {
		mMvpView.showProgress(false);
		mMvpView.setLogInButtonEnabled(true);
		mMvpView.setAnonymousLogInEnabled(true);

		switch (reason) {
			case LogInEvent.ACCESS_DENIED:
				mMvpView.showAccessDenied();
				break;
			case LogInEvent.USER_CANCELED:
				mMvpView.onUserCanceled();
				break;
			default:
				mMvpView.showGeneralLogInError();
		}
	}
}
