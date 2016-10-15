package com.cleytongoncalves.centralufmt.ui.login;

import com.cleytongoncalves.centralufmt.ui.base.MvpView;

interface LogInMvpView extends MvpView {
	void onLogInSuccessful();

	void onUserCanceled();

	void showProgress(boolean show);

	void setLogInButtonEnabled(boolean enabled);

	void setAnonymousLogInEnabled(boolean enabled);

	void showAccessDenied();

	void showGeneralLogInError();
}
