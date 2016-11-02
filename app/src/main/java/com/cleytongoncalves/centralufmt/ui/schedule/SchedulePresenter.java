package com.cleytongoncalves.centralufmt.ui.schedule;

import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;
import com.cleytongoncalves.centralufmt.util.TextUtil;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

final class SchedulePresenter implements Presenter<ScheduleMvpView>, ScheduleDataPresenter {
	private final static String TAG = SchedulePresenter.class.getSimpleName();
	private final static int MINIMUM_DAYS_AMOUNT = 5;
	private final static int MAXIMUM_TITLE_LENGTH = 25;
	private final static int MAXIMUM_ROOM_LENGTH = 10;

	private final DataManager mDataManager;
	@Nullable private ScheduleMvpView mScheduleView;
	@Nullable private ScheduleAdapter mScheduleAdapter;

	private List<ScheduleItemData> mScheduleData;
	private int mDaysOfWeekAmount;
	private int mMaxDailyClass;

	@Inject
	SchedulePresenter(DataManager dataManager) {
		mDataManager = dataManager;
		mMaxDailyClass = 0;
		mDaysOfWeekAmount = MINIMUM_DAYS_AMOUNT;
		init(false);
	}

	private void init(boolean forceUpdate) {
		PreferencesHelper prefHelper = mDataManager.getPreferencesHelper();
		List<ScheduleItemData> schedule = prefHelper.getSchedule();

		if (schedule == null || forceUpdate) {
			//TODO: SCHEDULE NETWORK GET
			List<Discipline> disciplineList = HtmlHelper.parseSchedule(HtmlHelper.getScheduleHtml
					                                                                      ());
			//disciplineList = mDataManager.getStudent().getCourse().getEnrolledDisciplines();

			SparseArray<SortedSet<ScheduleItemData>> hourlySchedule =
					parseRawSchedule(disciplineList);

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

	void refreshSchedule() {
		init(true);
		if (mScheduleAdapter != null) {
			mScheduleAdapter.notifyDataSetChanged();
		}
		if (mScheduleView != null) {
			mScheduleView.OnItemsLoadComplete();
		}
	}

	/* Adapter Methods */

	@Override
	public void attachAdapter(ScheduleAdapter adapter) {
		mScheduleAdapter = adapter;
	}

	@Override
	public void detachAdapter() {
		mScheduleAdapter = null;
	}

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

	private SparseArray<SortedSet<ScheduleItemData>> parseRawSchedule(List<Discipline> discList) {
		SparseArray<SortedSet<ScheduleItemData>> hourlySchedule =
				new SparseArray<>(discList.size()); //Max # of different hours

		for (int i = 0, discListSize = discList.size(); i < discListSize; i++) {
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

				String time = start.hourOfDay().getAsText() + ":" +
						              start.minuteOfHour().getAsText() + " - " +
						              end.hourOfDay().getAsText() + ":" +
						              end.minuteOfHour().getAsText();

				String title = TextUtil.capsMeaningfulWords(disc.getTitle());
				title = TextUtil.ellipsizeString(title, MAXIMUM_TITLE_LENGTH);

				String room = TextUtil.ellipsizeString(disc.getRoom(), MAXIMUM_ROOM_LENGTH);

				ScheduleItemData discData = new ScheduleItemData(dayOfWeek - 1, title, time, room);

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

	private List<ScheduleItemData> parseSchuleForAdapter(SparseArray<SortedSet<ScheduleItemData>>
			                                                     hourlySchedule) {
		List<ScheduleItemData> list = new ArrayList<>(mDaysOfWeekAmount * mMaxDailyClass);

		int scheduleSize = hourlySchedule.size();
		for (int i = 0, position = 0; i < scheduleSize; i++) {
			SortedSet<ScheduleItemData> hourlyData = hourlySchedule.valueAt(i);

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
