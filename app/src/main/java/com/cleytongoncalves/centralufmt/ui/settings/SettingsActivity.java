package com.cleytongoncalves.centralufmt.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;
import com.cleytongoncalves.centralufmt.ui.login.LogInActivity;

import javax.inject.Inject;

public class SettingsActivity extends BaseActivity {

	public static Intent getStartIntent(Context context) {
		return new Intent(context, SettingsActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
		                    .replace(android.R.id.content, new SettingsFragment())
		                    .commit();
	}

	public static class SettingsFragment extends PreferenceFragment {
		
		@Inject DataManager mDataManager;
		
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			((BaseActivity) getActivity()).activityComponent().inject(this);
			
			//Sets the SharedPref to be the same as the one already in use (PreferencesHelper one)
			PreferenceManager prefMgr = getPreferenceManager();
			prefMgr.setSharedPreferencesName(PreferencesHelper.PREF_FILE_NAME);
			prefMgr.setSharedPreferencesMode(MODE_PRIVATE);
			
			addPreferencesFromResource(R.xml.preferences);
			
			createLeaveAccountButton(prefMgr);
		}
		
		private void createLeaveAccountButton(PreferenceManager prefMgr) {
			Preference leaveButton = prefMgr.findPreference("LEAVEACCOUNT");
			
			if (leaveButton != null) {
				leaveButton.setOnPreferenceClickListener(
						pref -> {
							mDataManager.logOut();
							Intent intent = LogInActivity.getStartIntent(getActivity(), true);
							startActivity(intent);
							getActivity().finish();
							return true;
						});
			}
		}
	}
}
