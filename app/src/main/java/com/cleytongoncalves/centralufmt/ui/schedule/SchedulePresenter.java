package com.cleytongoncalves.centralufmt.ui.schedule;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;
import com.cleytongoncalves.centralufmt.util.TextUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import timber.log.Timber;

final class SchedulePresenter implements Presenter<ScheduleMvpView>, ScheduleDataPresenter {
	static final int MINIMUM_AMOUNT_OF_DAYS = 5;
	private static final int MAXIMUM_TITLE_LENGTH = 25;
	private static final int MAXIMUM_ROOM_LENGTH = 10;

	private final DataManager mDataManager;
	@Nullable private ScheduleMvpView mView;
	@Nullable private ScheduleAdapter mAdapter;

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

		if (mView != null) {
			mView.showRecyclerView(false);
			mView.showProgressBar(true);
		}

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
		mAdapter = adapter;
	}

	@Override
	public void detachAdapter() {
		mAdapter = null;
	}

	@Override
	public DisciplineModelView getDataForPosition(int pos) {
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

	/* Data Methods */

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onScheduleFetched(ScheduleFetchEvent scheduleEvent) {
		if (scheduleEvent.isSuccessful()) {
			onFetchSuccess(scheduleEvent.getResult());
		} else {
			onFetchFailure();
		}
	}

	private void onFetchSuccess(List<Discipline> disciplineList) {
		if (mView == null || mAdapter == null) { return; }

		mParserTask = new DataParserTask(disciplineList);
		mParserTask.execute();
	}

	private void onFetchFailure() {
		EventBus.getDefault().unregister(this);

		if (mView != null) {
			mView.showProgressBar(false);
			mView.showGeneralErrorSnack();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onDataChanged(ScheduleData schedule) {
		if (mView == null || mAdapter == null) { return; }

		mSchedule = schedule;

		int gridSpan = mSchedule.getAmountOfDays();
		if (gridSpan > MINIMUM_AMOUNT_OF_DAYS) {
			mView.setGridSpanCount(gridSpan);
		}

		mAdapter.notifyDataSetChanged();

		mView.hideSnackIfShown();
		mView.showProgressBar(false);
		mView.showRecyclerView(true);

		if (isLoading()) {
			EventBus.getDefault().unregister(this);
			mDataManager.getPreferencesHelper().putSchedule(mSchedule);
			mParserTask = null;
			Timber.d("Schedule updated successfully");
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
		private int mAmountOfDays;
		private int mMaxDailyClasses;

		DataParserTask(List<Discipline> enrolled) {
			mEnrolled = enrolled;
		}

		@Override
		protected Void doInBackground(Void... params) {
			ScheduleData schedule = null;

			if (mEnrolled.isEmpty()) {
				schedule = ScheduleData.emptySchedule();
			} else {
				SparseArray<SortedSet<DisciplineModelView>> rawSchedule = parseToRawSchedule();

				if (! isCancelled()) {
					List<DisciplineModelView> scheduleList = createListForDisplay(rawSchedule);
					schedule = ScheduleData.of(mMaxDailyClasses, mAmountOfDays, scheduleList);
				}
			}

			Timber.d("Schedule parsed - Canceled: %s", schedule == null);
			if (schedule != null && ! isCancelled()) { EventBus.getDefault().post(schedule); }
			return null;
		}

		private SparseArray<SortedSet<DisciplineModelView>> parseToRawSchedule() {
		/* Parses the discipline list to a sorted *map* (dayOfWeek -> SortedSet(Starting Hour)) */
			SparseArray<SortedSet<DisciplineModelView>> rawSchedule =
					new SparseArray<>(mEnrolled.size()); //Max # of different hours

			mAmountOfDays = MINIMUM_AMOUNT_OF_DAYS;
			mMaxDailyClasses = 0;
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
						mAmountOfDays = dayOfWeek;
					}

					String title = TextUtil.capsMeaningfulWords(disc.getTitle());
					title = TextUtil.ellipsizeString(title, MAXIMUM_TITLE_LENGTH);

					String time = start.toString("HH:mm") + " - " + end.toString("HH:mm");

					String room = TextUtil.ellipsizeString(disc.getRoom(), MAXIMUM_ROOM_LENGTH);

					DisciplineModelView discData =
							DisciplineModelView.of(dayOfWeek - 1, title, time, room);

					SortedSet<DisciplineModelView> viewSet = rawSchedule.get(start.getHourOfDay());
					if (viewSet == null) {
						viewSet = new TreeSet<>();
						rawSchedule.put(start.getHourOfDay(), viewSet);
						mMaxDailyClasses++;
					}
					viewSet.add(discData);
				}
			}

			return rawSchedule;
		}

		private List<DisciplineModelView> createListForDisplay
				(SparseArray<SortedSet<DisciplineModelView>> rawSchedule) {
			/* Creates a list sorted by the necessary adapter order, with emptyItem items on
			emptyItem
		classes */
			List<DisciplineModelView> scheduleList = new ArrayList<>(mAmountOfDays *
					                                                      mMaxDailyClasses);

			int lastDay = 0;
			int scheduleSize = rawSchedule.size();
			for (int i = 0, position = 0; i < scheduleSize; i++) {
				SortedSet<DisciplineModelView> hourlyData = rawSchedule.valueAt(i);

				int currColumn = 0;
				for (DisciplineModelView data : hourlyData) {
					currColumn = position % mAmountOfDays;
					int nextDataColumn = data.getColumn();

					//Creates fillers until it gets to the correct position
					if (currColumn < nextDataColumn) {
						for (; currColumn < nextDataColumn; currColumn++) {
							scheduleList.add(DisciplineModelView.emptyItem());
							position++;
						}
					} else if (currColumn > nextDataColumn) {
						int amountOfFillers = (mAmountOfDays - 1) - currColumn; //Finish the line
						amountOfFillers += nextDataColumn; //plus the amount to get to the next
						for (int j = 0; j < amountOfFillers; j++) {
							scheduleList.add(DisciplineModelView.emptyItem());
							position++;
						}
					}

					scheduleList.add(data);
					position++;
				}

				lastDay = currColumn;
			}

			//fills the last line
			for (; lastDay < mAmountOfDays; ++ lastDay) {
				scheduleList.add(DisciplineModelView.emptyItem());
			}

			return Collections.unmodifiableList(scheduleList);
		}
	}
}
