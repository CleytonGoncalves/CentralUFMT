package com.cleytongoncalves.centralufmt.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.model.GsonAdaptersModel;
import com.cleytongoncalves.centralufmt.data.model.MenuRu;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;
import com.cleytongoncalves.centralufmt.ui.schedule.GsonAdaptersAbstractScheduleData;
import com.cleytongoncalves.centralufmt.ui.schedule.ScheduleData;
import com.cleytongoncalves.centralufmt.util.converter.DateTimeConverter;
import com.cleytongoncalves.centralufmt.util.converter.IntervalConverter;
import com.cleytongoncalves.centralufmt.util.converter.LocalDateConverter;
import com.cleytongoncalves.centralufmt.util.converter.LocalTimeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.inject.Inject;

public class PreferencesHelper {
	public static final String PREF_FILE_NAME = "central_ufmt_pref_file";
	private static final String PREF_KEY_STUDENT = "PREF_KEY_STUDENT";
	private static final String PREF_KEY_RGA = "PREF_KEY_RGA";
	private static final String PREF_KEY_AUTH = "PREF_KEY_AUTH";
	private static final String PREF_KEY_SCHEDULE_DATA = "PREF_KEY_SCHEDULE_DATA";
	private static final String PREF_KEY_ROUTE_OPTION = "PREF_KEY_ROUTE_OPTION";
	private static final String PREF_KEY_POI_OPTION = "PREF_KEY_POI_OPTION";
	private static final String PREF_KEY_MENURU = "PREF_KEY_MENURU";
	
	private final SharedPreferences mSharedPref;
	private final Gson mGson;

	@Inject
	PreferencesHelper(@ApplicationContext Context context) {
		mSharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

		mGson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeConverter())
		                         .registerTypeAdapter(LocalDate.class, new LocalDateConverter())
		                         .registerTypeAdapter(LocalTime.class, new LocalTimeConverter())
		                         .registerTypeAdapter(Interval.class, new IntervalConverter())
		                         .registerTypeAdapterFactory(new GsonAdaptersModel())
		                         .registerTypeAdapterFactory(new GsonAdaptersAbstractScheduleData())
		                         .create();
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
	
	/* Student */

	public void putStudent(Student student) {
		mSharedPref.edit().putString(PREF_KEY_STUDENT, mGson.toJson(student)).apply();
	}

	@Nullable
	public Student getStudent() {
		String studentJson = mSharedPref.getString(PREF_KEY_STUDENT, null);

		if (studentJson == null) {
			return null;
		}
		return mGson.fromJson(studentJson, Student.class);
	}
	
	/* Schedule */

	public void putSchedule(ScheduleData schedule) {
		mSharedPref.edit().putString(PREF_KEY_SCHEDULE_DATA, mGson.toJson(schedule)).apply();
	}

	@Nullable
	public ScheduleData getSchedule() {
		String scheduleJson = mSharedPref.getString(PREF_KEY_SCHEDULE_DATA, null);
		if (scheduleJson == null) { return null; }

		return mGson.fromJson(scheduleJson, ScheduleData.class);
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
