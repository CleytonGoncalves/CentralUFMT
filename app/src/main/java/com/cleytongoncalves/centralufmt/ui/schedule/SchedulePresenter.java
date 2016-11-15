package com.cleytongoncalves.centralufmt.ui.schedule;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;
import com.cleytongoncalves.centralufmt.ui.schedule.ScheduleData.ScheduleItemData;
import com.cleytongoncalves.centralufmt.util.TextUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

final class SchedulePresenter implements Presenter<ScheduleMvpView>, ScheduleDataPresenter {
	//TODO: FIX WRONG STUDENT SCHEDULE
	private static final String TAG = SchedulePresenter.class.getSimpleName();

	public static final int MINIMUM_AMOUNT_OF_DAYS = 5;
	private static final int MAXIMUM_TITLE_LENGTH = 25;
	private static final int MAXIMUM_ROOM_LENGTH = 10;

	private final DataManager mDataManager;
	private ScheduleMvpView mScheduleView;
	private ScheduleAdapter mScheduleAdapter;
	private ScheduleData mSchedule;
	@Nullable private DataParserTask mParserTask;

	@Inject
	SchedulePresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(ScheduleMvpView mvpView) {
		mScheduleView = mvpView;
	}

	@Override
	public void detachView() {
		mScheduleView = null;
		detachAdapter();
		if (EventBus.getDefault().isRegistered(this)) { EventBus.getDefault().unregister(this); }

		if (mParserTask != null) {
			mParserTask.cancel(true);
			mParserTask = null;
		}
	}

	void loadSchedule(boolean forceUpdate) {
		ScheduleData schedule = null;
		if (! forceUpdate) { schedule = mDataManager.getPreferencesHelper().getSchedule(); }

		mScheduleView.hideRecyclerView();
		mScheduleView.showProgressBar();

		if (schedule == null) {
			if (! isParseRunning()) {
				EventBus.getDefault().register(this);
				mDataManager.fetchSchedule();
			}
		} else {
			onDataChanged(schedule);
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
	public ScheduleItemData getDataForPosition(int pos) {
		//Not called on header positions
		return mSchedule.getItem(getTrueItemPosition(pos));
	}

	@Override
	public boolean isHeader(int position) {
		return position < mSchedule.getAmountOfDays();
	}

	@Override
	public int getItemId(int position) {
		if (isHeader(position)) {
			return position;
		}

		int realPos = getTrueItemPosition(position);
		return mSchedule.getItem(realPos).hashCode();
	}

	@Override
	public int getItemCount() {
		if (mSchedule == null) {
			return MINIMUM_AMOUNT_OF_DAYS;
		}

		return mSchedule.getScheduleSize() + mSchedule.getAmountOfDays();
	}

	@Subscribe
	public void onScheduleFetched(List<Discipline> enrolled) {
		Log.i(TAG, "INITIATING SCHEDULE PARSING");
		mParserTask = new DataParserTask(enrolled);
		mParserTask.execute();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onDataChanged(ScheduleData schedule) {
		mSchedule = schedule;

		int gridSpan = mSchedule.getAmountOfDays();
		if (gridSpan > MINIMUM_AMOUNT_OF_DAYS) {
			mScheduleView.setGridSpanCount(gridSpan);
		}

		mScheduleAdapter.notifyDataSetChanged();

		mScheduleView.hideProgressBar();
		mScheduleView.showRecyclerView();

		if (isParseRunning()) {
			EventBus.getDefault().unregister(this);
			mDataManager.getPreferencesHelper().putSchedule(mSchedule);
			mParserTask = null;
			Log.i(TAG, "DATA CHANGED SUCCESSFULLY");
		}
	}

	/* Private Helper Methods */

	private int getTrueItemPosition(int position) {
		return position - mSchedule.getAmountOfDays(); //Removes the header from the count;
	}

	private boolean isParseRunning() {
		return mParserTask != null;
	}

	private static class DataParserTask extends AsyncTask<Void, Void, Void> {
		private final List<Discipline> mEnrolled;

		DataParserTask(List<Discipline> enrolled) {
			mEnrolled = enrolled;
		}

		@Override
		protected Void doInBackground(Void... params) {
			List<Discipline> disciplineList = mEnrolled;
			ScheduleData schedule = parseToSchedule(disciplineList);

			if (schedule != null && ! isCancelled()) { EventBus.getDefault().post(schedule); }
			return null;
		}

		private static ScheduleData parseToSchedule(List<Discipline> discList) {
		/* Parses the discipline list to a sorted *map* (dayOfWeek -> SortedSet(Starting Hour)) */
			SparseArray<SortedSet<ScheduleItemData>> rawSchedule =
					new SparseArray<>(discList.size()); //Max # of different hours

			int amountOfDays = MINIMUM_AMOUNT_OF_DAYS;
			int maxDailyClasses = 0;
			for (int i = 0, discListSize = discList.size(); i < discListSize; i++) {
				Discipline disc = discList.get(i);
				List<Interval> classTimes = disc.getClassTimes();

				for (int j = 0, classTimesSize = classTimes.size(); j < classTimesSize; j++) {
					Interval inter = classTimes.get(j);
					DateTime start = inter.getStart();
					DateTime end = inter.getEnd();

					int dayOfWeek = start.getDayOfWeek();
					if (dayOfWeek > MINIMUM_AMOUNT_OF_DAYS) {
						//Adds saturday and/or sunday
						amountOfDays = dayOfWeek;
					}

					String title = TextUtil.capsMeaningfulWords(disc.getTitle());
					title = TextUtil.ellipsizeString(title, MAXIMUM_TITLE_LENGTH);

					String time = start.toString("HH:mm") + " - " + end.toString("HH:mm");

					String room = TextUtil.ellipsizeString(disc.getRoom(), MAXIMUM_ROOM_LENGTH);

					ScheduleItemData discData =
							new ScheduleItemData(dayOfWeek - 1, title, time, room);

					SortedSet<ScheduleItemData> viewSet = rawSchedule.get(start.getHourOfDay());
					if (viewSet == null) {
						viewSet = new TreeSet<>();
						rawSchedule.put(start.getHourOfDay(), viewSet);
						maxDailyClasses++;
					}
					viewSet.add(discData);
				}
			}

			//if (isCancelled()) {
			//	return null;
			//}

		/* Creates a list sorted by the necessary adapter order, with empty items on empty
		classes */
			List<ScheduleItemData> schedule = new ArrayList<>(amountOfDays * maxDailyClasses);

			int scheduleSize = rawSchedule.size();
			for (int i = 0, position = 0; i < scheduleSize; i++) {
				SortedSet<ScheduleItemData> hourlyData = rawSchedule.valueAt(i);

				for (ScheduleItemData data : hourlyData) {
					int currDay = position % amountOfDays;
					int nextDataDay = data.getColumn();
					if (nextDataDay != currDay) {
						for (; currDay < nextDataDay; currDay++) {
							ScheduleItemData empty = new ScheduleItemData(currDay, "", "", "");
							schedule.add(empty);
							position++;
						}
					}

					schedule.add(data);
					position++;
				}
			}

			return new ScheduleData(maxDailyClasses, amountOfDays, schedule);
		}
	}
}
