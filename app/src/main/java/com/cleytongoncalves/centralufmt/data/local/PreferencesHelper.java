package com.cleytongoncalves.centralufmt.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.model.GsonAdaptersModel;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;
import com.cleytongoncalves.centralufmt.ui.schedule.ScheduleData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;

public class PreferencesHelper {
	private static final String PREF_FILE_NAME = "central_ufmt_pref_file";
	private static final String PREF_KEY_LOGGED_IN = "PREF_KEY_LOGGED_IN";
	private static final String PREF_KEY_RGA = "PREF_KEY_RGA";
	private static final String PREF_KEY_AUTH = "PREF_KEY_AUTH";
	private static final String PREF_KEY_ANONYMOUS_LOGIN = "PREF_KEY_ANONYMOUS_LOGIN";
	private static final String PREF_KEY_SCHEDULE_DATA = "PREF_KEY_SCHEDULE_DATA";
	private static final String PREF_KEY_ROUTE_OPTION = "PREF_KEY_ROUTE_OPTION";
	private static final String PREF_KEY_POI_OPTION = "PREF_KEY_POI_OPTION";

	private final SharedPreferences mSharedPref;
	private final Gson mGson;

	@Inject
	PreferencesHelper(@ApplicationContext Context context) {
		mSharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

		mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz")
		                         .registerTypeAdapterFactory(new GsonAdaptersModel())
		                         .create();
	}

	public void clear() {
		mSharedPref.edit().clear().apply();
	}

	public void putCredentials(String rga, char[] password) {
		mSharedPref.edit().putString(PREF_KEY_RGA, rga).apply();
		mSharedPref.edit().putString(PREF_KEY_AUTH, String.valueOf(password)).apply();
	}

	@Nullable
	public String getRga() {
		return mSharedPref.getString(PREF_KEY_RGA, null);
	}

	@Nullable
	public char[] getAuth() {
		char[] auth = mSharedPref.getString(PREF_KEY_AUTH, "").toCharArray();
		return auth.length > 0 ? auth : null;
	}

	public void putLoggedInStudent(Student student) {
		mSharedPref.edit().putString(PREF_KEY_LOGGED_IN, mGson.toJson(student)).apply();
	}

	@Nullable
	public Student getLoggedInStudent() {
		String studentJson = mSharedPref.getString(PREF_KEY_LOGGED_IN, null);

		if (studentJson == null) {
			return null;
		}
		return mGson.fromJson(studentJson, Student.class);
	}

	public void setAnonymousLogIn(boolean enabled) {
		mSharedPref.edit().putBoolean(PREF_KEY_ANONYMOUS_LOGIN, enabled).apply();
	}

	public boolean getAnonymousLogIn() {
		return mSharedPref.getBoolean(PREF_KEY_ANONYMOUS_LOGIN, false);
	}

	public void putSchedule(ScheduleData schedule) {
		mSharedPref.edit().putString(PREF_KEY_SCHEDULE_DATA, mGson.toJson(schedule)).apply();
	}

	@Nullable
	public ScheduleData getSchedule() {
		String scheduleJson = mSharedPref.getString(PREF_KEY_SCHEDULE_DATA, null);

		if (scheduleJson == null) {
			return null;
		}
		return mGson.fromJson(scheduleJson, ScheduleData.class);
	}

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
