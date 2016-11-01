package com.cleytongoncalves.centralufmt.ui.schedule;

import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

final class SchedulePresenter implements Presenter<ScheduleMvpView>, ScheduleDataProvider {
	private final static int MINIMUM_DAYS_AMOUNT = 5;

	private final DataManager mDataManager;
	@Nullable private ScheduleMvpView mScheduleView;

	private List<ScheduleItemData> mScheduleData;
	private int mDaysOfWeekAmount;
	private int mMaxDailyClass;

	@Inject
	SchedulePresenter(DataManager dataManager) {
		mDataManager = dataManager;
		mMaxDailyClass = 0;
		mDaysOfWeekAmount = MINIMUM_DAYS_AMOUNT;
		init();
	}

	private void init() {
		PreferencesHelper prefHelper = mDataManager.getPreferencesHelper();
		List<ScheduleItemData> schedule = prefHelper.getSchedule();

		if (schedule == null) {
			//TODO: SCHEDULE NETWORK GET
			List<Discipline> disciplineList = HtmlHelper.parseSchedule(HtmlHelper.getScheduleHtml
					                                                                      ());
			//disciplineList = mDataManager.getStudent().getCourse().getEnrolledDisciplines();

			Map<Integer, SortedSet<ScheduleItemData>> hourlySchedule = parseRawSchedule
					                                                           (disciplineList);
			schedule = parseSchuleForAdapter(hourlySchedule);
			prefHelper.putSchedule(schedule);
		}
		mScheduleData = schedule;
	}

	@Override
	public void attachView(ScheduleMvpView mvpView) {
		mScheduleView = mvpView;
	}

	@Override
	public void detachView() {
		mScheduleView = null;
	}

	/* Adapter Methods */

	@Override
	public ScheduleItemData getDataForPosition(int position) {
		return mScheduleData.get(getItemPosition(position));
	}

	@Override
	public boolean isHeader(int position) {
		return position < mDaysOfWeekAmount;
	}

	@Override
	public int getItemId(int position) {
		if (isHeader(position)) {
			return position;
		}

		position = getItemPosition(position);
		return mScheduleData.get(position).hashCode();
	}

	@Override
	public int getItemCount() {
		return mScheduleData.size() + mDaysOfWeekAmount;
	}

	int getAmountOfDays() {
		return mDaysOfWeekAmount;
	}

	/* Private Helper Methods */

	private Map<Integer, SortedSet<ScheduleItemData>> parseRawSchedule(List<Discipline> discList) {
		Map<Integer, SortedSet<ScheduleItemData>> hourlySchedule = new TreeMap<>();

		for (int i = 0, disciplineListSize = discList.size(); i < disciplineListSize; i++) {
			Discipline disc = discList.get(i);
			List<Interval> classTimes = disc.getClassTimes();
			for (int j = 0, classTimesSize = classTimes.size(); j < classTimesSize; j++) {
				Interval inter = classTimes.get(j);
				DateTime start = inter.getStart();
				DateTime end = inter.getEnd();

				int dayOfWeek = start.getDayOfWeek();
				if (dayOfWeek > MINIMUM_DAYS_AMOUNT) {
					//Adds saturday and/or sunday
					mDaysOfWeekAmount = dayOfWeek;
				}

				String classTime = start.hourOfDay().getAsText() + ":" +
						                   start.minuteOfHour().getAsText() + " - " +
						                   end.hourOfDay().getAsText() + ":" +
						                   end.minuteOfHour().getAsText();

				ScheduleItemData discData = new ScheduleItemData(dayOfWeek - 1, disc.getTitle(),
						                                                classTime, disc.getRoom());

				SortedSet<ScheduleItemData> viewSet = hourlySchedule.get(start.getHourOfDay());
				if (viewSet == null) {
					viewSet = new TreeSet<>();
					hourlySchedule.put(start.getHourOfDay(), viewSet);
					mMaxDailyClass++;
				}
				viewSet.add(discData);
			}
		}

		return hourlySchedule;
	}

	private List<ScheduleItemData> parseSchuleForAdapter(Map<Integer, SortedSet<ScheduleItemData>>
			                                                     hourlySchedule) {
		List<ScheduleItemData> list = new ArrayList<>(mDaysOfWeekAmount * mMaxDailyClass);

		int position = 0;
		for (Integer hourRow : hourlySchedule.keySet()) {
			SortedSet<ScheduleItemData> hourlyData = hourlySchedule.get(hourRow);

			for (ScheduleItemData data : hourlyData) {
				int currDay = position % mDaysOfWeekAmount;
				int nextDataDay = data.getColumn();
				if (nextDataDay != currDay) {
					for (; currDay < nextDataDay; currDay++) {
						ScheduleItemData empty = new ScheduleItemData(currDay, "", "", "");
						list.add(empty);
						position++;
					}
				}

				list.add(data);
				position++;
			}
		}

		return list;
	}

	private int getItemPosition(int position) {
		return position - mDaysOfWeekAmount; //Removes the header from the count;
	}
}
