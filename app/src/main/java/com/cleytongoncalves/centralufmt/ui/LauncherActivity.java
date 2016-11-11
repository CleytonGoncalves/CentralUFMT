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
	@Inject DataManager mDataManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityComponent().inject(this);
		Intent intent;

		if (mDataManager.isLoggedInSiga()) {
			intent = MainActivity.getStartIntent(this, true);
			if (NetworkUtil.isNetworkConnected(this)) {
				mDataManager.triggerMoodleLogIn();
			}
		} else if (mDataManager.getPreferencesHelper().getAnonymousLogIn()) {
			intent = MainActivity.getStartIntent(this, true);
		} else {
			intent = LogInActivity.getStartIntent(this, true);
		}
		startActivity(intent);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		finish();
	}
}
