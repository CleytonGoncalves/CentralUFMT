package com.cleytongoncalves.centralufmt.ui.moodle;

import com.cleytongoncalves.centralufmt.ui.base.MvpView;

interface MoodleMvpView extends MvpView {
	void onLogInSuccessful(String cookieString);

	void showProgressBar(boolean enabled);

	void showWebView(boolean enabled);
}
