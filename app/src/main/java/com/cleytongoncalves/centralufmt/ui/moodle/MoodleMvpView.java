package com.cleytongoncalves.centralufmt.ui.moodle;

import com.cleytongoncalves.centralufmt.ui.base.MvpView;

interface MoodleMvpView extends MvpView {
	void setCookieString(String cookieString);

	void loadStartPage();

	void showProgressBar(boolean enabled);

	void showWebView(boolean enabled);

	void showLoadingTitle();

	void showDefaultTitle();

	void showDownloadStart();

	void showGeneralLogInError();
}
