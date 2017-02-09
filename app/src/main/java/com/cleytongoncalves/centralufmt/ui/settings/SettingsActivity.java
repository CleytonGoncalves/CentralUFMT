package com.cleytongoncalves.centralufmt.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;
import com.cleytongoncalves.centralufmt.ui.login.LogInActivity;

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
		
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			//Sets the SharedPref to be the same as the one already in use (PreferencesHelper one)
			PreferenceManager prefMgr = getPreferenceManager();
			prefMgr.setSharedPreferencesName(PreferencesHelper.PREF_FILE_NAME);
			prefMgr.setSharedPreferencesMode(MODE_PRIVATE);
			
			createLeaveAccountButton(prefMgr);
			
			addPreferencesFromResource(R.xml.preferences);
		}
		
		private void createLeaveAccountButton(PreferenceManager prefMgr) {
			Preference leaveButton = prefMgr.findPreference("LEAVEACCOUNT");
			
			if (leaveButton != null) {
				leaveButton.setOnPreferenceClickListener(
						pref -> {
							pref.getEditor().clear().apply(); //ALL preferences (PrefHelper as well)
							Intent intent = LogInActivity.getStartIntent(getActivity(), true);
							startActivity(intent);
							return true;
						});
			}
		}
	}
}
