package com.cleytongoncalves.centralufmt.ui.login;

import com.cleytongoncalves.centralufmt.ui.base.MvpView;

interface LogInMvpView extends MvpView {
	void onLogInSuccessful();

	void onUserCanceled();
	
	void showProgressBar(boolean show);
	
	void showLoginForm(boolean show);

	void showAccessDenied();

	void showGeneralLogInError();
}
