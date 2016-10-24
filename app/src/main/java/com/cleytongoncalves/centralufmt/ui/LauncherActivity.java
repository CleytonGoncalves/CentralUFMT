package com.cleytongoncalves.centralufmt.ui;

import android.content.Intent;
import android.os.Bundle;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;
import com.cleytongoncalves.centralufmt.ui.login.LogInActivity;
import com.cleytongoncalves.centralufmt.ui.main.MainActivity;
import com.cleytongoncalves.centralufmt.util.NetworkUtil;

import javax.inject.Inject;

public class LauncherActivity extends BaseActivity {
	private static final boolean FORCE_LOGIN = true; //For debug purposes

	@Inject DataManager mDataManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityComponent().inject(this);
		Intent intent;

		if (mDataManager.isLoggedInSiga() && ! FORCE_LOGIN) {
			intent = MainActivity.getStartIntent(this, false);
			if (NetworkUtil.isNetworkConnected(this)) {
				mDataManager.triggerMoodleLogIn();
			}
		} else if (mDataManager.getPreferencesHelper().getAnonymousLogIn() && ! FORCE_LOGIN) {
			intent = MainActivity.getStartIntent(this, false);
		} else {
			intent = LogInActivity.getStartIntent(this, false);
		}
		startActivity(intent);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		finish();
	}
}
