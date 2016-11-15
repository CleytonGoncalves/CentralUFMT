package com.cleytongoncalves.centralufmt.ui.schedule;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
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
	private static final String TAG = SchedulePresenter.class.getSimpleName();

	static final int MINIMUM_AMOUNT_OF_DAYS = 5;
	private static final int MAXIMUM_TITLE_LENGTH = 25;
	private static final int MAXIMUM_ROOM_LENGTH = 10;

	private final DataManager mDataManager;
	private ScheduleMvpView mView;
	private ScheduleAdapter mScheduleAdapter;
	private ScheduleData mSchedule;

	@Nullable private DataParserTask mParserTask;

	@Inject
	SchedulePresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(ScheduleMvpView mvpView) {
		mView = mvpView;
	}

	@Override
	public void detachView() {
		mView = null;
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

		mView.hideRecyclerView();
		mView.showProgressBar();

		if (schedule == null) {
			if (! isLoading()) {
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
		return mSchedule == null || position < mSchedule.getAmountOfDays();
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onScheduleFetched(ScheduleFetchEvent scheduleEvent) {
		if (scheduleEvent.isSuccessful()) {
			onFetchSuccess(scheduleEvent.getDisciplineList());
		} else {
			onFetchFailure(scheduleEvent.getFailureReason());
		}
	}

	private void onFetchSuccess(List<Discipline> disciplineList) {
		Log.i(TAG, "INITIATING SCHEDULE PARSING");
		mParserTask = new DataParserTask(disciplineList);
		mParserTask.execute();
	}

	private void onFetchFailure(String reason) {
		Log.i(TAG, "ON FETCH FAILURE: " + reason);
		EventBus.getDefault().unregister(this);

		mView.hideProgressBar();
		mView.showGeneralErrorSnack();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onDataChanged(ScheduleData schedule) {
		mSchedule = schedule;

		int gridSpan = mSchedule.getAmountOfDays();
		if (gridSpan > MINIMUM_AMOUNT_OF_DAYS) {
			mView.setGridSpanCount(gridSpan);
		}

		mScheduleAdapter.notifyDataSetChanged();

		mView.hideSnackIfShown();
		mView.hideProgressBar();
		mView.showRecyclerView();

		if (isLoading()) {
			EventBus.getDefault().unregister(this);
			mDataManager.getPreferencesHelper().putSchedule(mSchedule);
			mParserTask = null;
			Log.i(TAG, "DATA CHANGED SUCCESSFULLY");
		}

		if (! schedule.containsData()) {
			mView.showEmptyScheduleSnack();
		}
	}

	/* Private Helper Methods */

	private int getTrueItemPosition(int position) {
		return position - mSchedule.getAmountOfDays(); //Removes the header from the count;
	}

	private boolean isLoading() {
		return EventBus.getDefault().isRegistered(this);
	}

	private static class DataParserTask extends AsyncTask<Void, Void, Void> {
		private final List<Discipline> mEnrolled;

		DataParserTask(List<Discipline> enrolled) {
			mEnrolled = enrolled;
		}

		@Override
		protected Void doInBackground(Void... params) {
			ScheduleData schedule;
			if (mEnrolled.isEmpty()) {
				schedule = ScheduleData.emptySchedule();
			} else {
				schedule = parseToSchedule();
			}

			if (schedule != null && ! isCancelled()) { EventBus.getDefault().post(schedule); }
			return null;
		}

		private ScheduleData parseToSchedule() {
		/* Parses the discipline list to a sorted *map* (dayOfWeek -> SortedSet(Starting Hour)) */
			SparseArray<SortedSet<ScheduleItemData>> rawSchedule =
					new SparseArray<>(mEnrolled.size()); //Max # of different hours

			int amountOfDays = MINIMUM_AMOUNT_OF_DAYS;
			int maxDailyClasses = 0;
			for (int i = 0, discListSize = mEnrolled.size(); i < discListSize; i++) {
				Discipline disc = mEnrolled.get(i);
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

			if (isCancelled()) {
				return null;
			}

		/* Creates a list sorted by the necessary adapter order, with emptyItem items on emptyItem
		classes */
			List<ScheduleItemData> schedule = new ArrayList<>(amountOfDays * maxDailyClasses);

			int lastDay = 0;
			int scheduleSize = rawSchedule.size();
			for (int i = 0, position = 0; i < scheduleSize; i++) {
				SortedSet<ScheduleItemData> hourlyData = rawSchedule.valueAt(i);

				int currColumn = 0;
				for (ScheduleItemData data : hourlyData) {
					currColumn = position % amountOfDays;
					int nextDataColumn = data.getColumn();

					//Creates fillers until it gets to the correct position
					if (currColumn < nextDataColumn) {
						for (; currColumn < nextDataColumn; currColumn++) {
							schedule.add(ScheduleItemData.emptyItem());
							position++;
						}
					} else if (currColumn > nextDataColumn) {
						int amountOfFillers = (amountOfDays - 1) - currColumn; //Finish the line
						amountOfFillers += nextDataColumn; //plus the amount to get to the next
						for (int j = 0; j < amountOfFillers; j++) {
							schedule.add(ScheduleItemData.emptyItem());
							position++;
						}
					}

					schedule.add(data);
					position++;
				}

				lastDay = currColumn;
			}

			//fills the last line
			for (; lastDay < amountOfDays; ++ lastDay) {
				schedule.add(ScheduleItemData.emptyItem());
			}

			return new ScheduleData(maxDailyClasses, amountOfDays, schedule);
		}
	}
}
