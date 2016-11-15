package com.cleytongoncalves.centralufmt.ui.login;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

public final class LogInPresenter implements Presenter<LogInMvpView> {
	private final DataManager mDataManager;
	private LogInMvpView mView;

	@Inject
	LogInPresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(LogInMvpView mvpView) {
		mView = mvpView;
		if (mDataManager.isLoggedInSiga()) {
			//Check if it isn't already logged in when attaching the view
			onLogInSuccess(false);
		}
	}

	@Override
	public void detachView() {
		mView = null;
	}

	void doAnonymousLogIn() {
		onLogInSuccess(true); //Goes directly to the result
		mView.setLogInButtonEnabled(false);
		mView.setAnonymousLogInEnabled(false);
		mView.showProgress(true);
	}

	void doLogIn(String rga, char[] password) {
		mView.setLogInButtonEnabled(false);
		mView.setAnonymousLogInEnabled(false);
		mView.showProgress(true);

		EventBus.getDefault().register(this);
		mDataManager.logIn(rga, password, DataManager.LOGIN_SIGA);
	}

	void cancelLogin() {
		mDataManager.cancelLogIn();
	}

	@Subscribe
	public void onLogInEvent(LogInEvent event) {
		EventBus.getDefault().unregister(this);

		if (event.isSuccessful()) {
			onLogInSuccess(false);
		} else {
			onLogInFailure(event.getFailureReason());
		}
	}

	private void onLogInSuccess(boolean anonymous) {
		mDataManager.getPreferencesHelper().setAnonymousLogIn(anonymous);
		mView.showProgress(false);
		mView.onLogInSuccessful();
	}

	private void onLogInFailure(String reason) {
		mView.showProgress(false);
		mView.setLogInButtonEnabled(true);
		mView.setAnonymousLogInEnabled(true);

		switch (reason) {
			case LogInEvent.ACCESS_DENIED:
				mView.showAccessDenied();
				break;
			case LogInEvent.USER_CANCELED:
				mView.onUserCanceled();
				break;
			default:
				mView.showGeneralLogInError();
		}
	}

	boolean isLogInHappening() {
		return mDataManager.isLogInHappening();
	}
}
