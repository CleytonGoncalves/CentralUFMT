package com.cleytongoncalves.centralufmt.ui.login;

import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import timber.log.Timber;

public final class LogInPresenter implements Presenter<LogInMvpView> {
	private final DataManager mDataManager;
	@Nullable private LogInMvpView mView;

	@Inject
	LogInPresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(LogInMvpView mvpView) {
		mView = mvpView;
	}

	@Override
	public void detachView() {
		mView = null;
		if (EventBus.getDefault().isRegistered(this)) { EventBus.getDefault().unregister(this); }
	}

	void doAnonymousLogIn() {
		if (mView != null) {
			mView.setLogInButtonEnabled(false);
			mView.setAnonymousLogInEnabled(false);
			mView.showProgress(true);
		}

		onLogInSuccess(true); //Goes directly to the result
		Timber.d("Anonymous LogIn");
	}

	void doLogIn(String rga, char[] password) {
		if (mView != null) {
			mView.setLogInButtonEnabled(false);
			mView.setAnonymousLogInEnabled(false);
			mView.showProgress(true);
		}

		EventBus.getDefault().register(this);
		mDataManager.logIn(rga, password, DataManager.LOGIN_SIGA);
	}

	void cancelLogin() {
		mDataManager.cancelLogIn();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
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

		if (mView != null) {
			mView.showProgress(false);
			mView.onLogInSuccessful();
		}
	}

	private void onLogInFailure(String reason) {
		if (mView == null) { return; }

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
