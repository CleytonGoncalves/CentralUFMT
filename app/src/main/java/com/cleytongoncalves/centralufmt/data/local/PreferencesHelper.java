package com.cleytongoncalves.centralufmt.data.local;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.model.menuru.MenuRu;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PreferencesHelper {
	public static final String PREF_FILE_NAME = "central_ufmt_pref_file";
	private static final String PREF_KEY_RGA = "PREF_KEY_RGA";
	private static final String PREF_KEY_AUTH = "PREF_KEY_AUTH";
	private static final String PREF_KEY_ROUTE_OPTION = "PREF_KEY_ROUTE_OPTION";
	private static final String PREF_KEY_POI_OPTION = "PREF_KEY_POI_OPTION";
	private static final String PREF_KEY_MENURU = "PREF_KEY_MENURU";
	private static final String PREF_KEY_SCHEDULE_SATURDAY = "PREF_KEY_SCHEDULE_SATURDAY";
	private static final String PREF_KEY_SCHEDULE_SUNDAY = "PREF_KEY_SCHEDULE_SUNDAY";
	
	private final SharedPreferences mSharedPref;
	private final Gson mGson;

	@Inject
	PreferencesHelper(SharedPreferences sharedPreferences, Gson gson) {
		mSharedPref = sharedPreferences;
		mGson = gson;
	}
	
	/* LogOut*/
	
	public void clear() {
		mSharedPref.edit().clear().apply();
	}
	
	/* Credentials */
	
	public void putCredentials(String rga, char[] authKey) {
		mSharedPref.edit().putString(PREF_KEY_RGA, rga).apply();
		mSharedPref.edit().putString(PREF_KEY_AUTH, String.valueOf(authKey)).apply();
	}

	@Nullable
	public String getRga() {
		return mSharedPref.getString(PREF_KEY_RGA, null);
	}

	@Nullable
	public char[] getAuthKey() {
		char[] auth = mSharedPref.getString(PREF_KEY_AUTH, "").toCharArray();
		return auth.length > 0 ? auth : null;
	}
	
	/* Schedule */
	
	public void putScheduleShowSaturday(boolean enabled) {
		mSharedPref.edit().putBoolean(PREF_KEY_SCHEDULE_SATURDAY, enabled).apply();
	}
	
	public boolean getScheduleShowSaturday() {
		return mSharedPref.getBoolean(PREF_KEY_SCHEDULE_SATURDAY, false);
	}
	
	public void putScheduleShowSunday(boolean enabled) {
		mSharedPref.edit().putBoolean(PREF_KEY_SCHEDULE_SUNDAY, enabled).apply();
	}
	
	public boolean getScheduleShowSunday() {
		return mSharedPref.getBoolean(PREF_KEY_SCHEDULE_SUNDAY, false);
	}
	
	/* MenuRu */

	public void putMenuRu(MenuRu menuRu) {
		mSharedPref.edit().putString(PREF_KEY_MENURU, mGson.toJson(menuRu)).apply();
	}

	@Nullable
	public MenuRu getMenuRu() {
		String menuRuJson = mSharedPref.getString(PREF_KEY_MENURU, null);
		if (menuRuJson == null) { return null; }

		return mGson.fromJson(menuRuJson, MenuRu.class);
	}
	
	/* Map Preferences */
	
	public void putMapBusRouteDisplayState(boolean enabled) {
		mSharedPref.edit().putBoolean(PREF_KEY_ROUTE_OPTION, enabled).apply();
	}
	
	public boolean getMapRouteDisplayState() {
		return mSharedPref.getBoolean(PREF_KEY_ROUTE_OPTION, true);
	}
	
	public void putMapPoiDisplayState(boolean enabled) {
		mSharedPref.edit().putBoolean(PREF_KEY_POI_OPTION, enabled).apply();
	}
	
	public boolean getMapPoiDisplayState() {
		return mSharedPref.getBoolean(PREF_KEY_POI_OPTION, true);
	}
}
