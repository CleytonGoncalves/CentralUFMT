package com.cleytongoncalves.centralufmt.ui.schedule;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
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
	private static final int MINIMUM_DAYS_AMOUNT = 5;
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

	void refreshSchedule(boolean forceUpdate) {
		ScheduleData schedule = mDataManager.getPreferencesHelper().getSchedule();
		if (schedule == null || forceUpdate) {
			if (! isParseRunning()) {
				EventBus.getDefault().register(this);
				mParserTask = new DataParserTask();
				mParserTask.execute();
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
		return mSchedule != null ? mSchedule.getScheduleSize() + mSchedule.getAmountOfDays() : 0;
	}

	/* Private Helper Methods */

	private int getTrueItemPosition(int position) {
		return position - mSchedule.getAmountOfDays(); //Removes the header from the count;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onDataChanged(ScheduleData schedule) {
		mSchedule = schedule;

		mScheduleView.setGridSpanCount(mSchedule.getAmountOfDays());
		mScheduleAdapter.notifyDataSetChanged();
		mScheduleView.stopProgressBar();

		if (isParseRunning()) {
			EventBus.getDefault().unregister(this);
			mDataManager.getPreferencesHelper().putSchedule(mSchedule);
			mParserTask = null;
		}
	}

	private boolean isParseRunning() {
		return mParserTask != null;
	}

	private static class DataParserTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			//TODO: SCHEDULE NETWORK GET
			List<Discipline> disciplineList = HtmlHelper.parseSchedule(HtmlHelper.getScheduleHtml
					                                                                      ());
			//disciplineList = mDataManager.getStudent().getCourse().getEnrolledDisciplines();

			ScheduleData schedule = parseToSchedule(disciplineList);
			if (schedule != null && ! isCancelled()) { EventBus.getDefault().post(schedule); }
			return null;
		}

		private ScheduleData parseToSchedule(List<Discipline> discList) {
		/* Parses the discipline list to a sorted *map* (dayOfWeek -> SortedSet(Starting Hour)) */

			SparseArray<SortedSet<ScheduleItemData>> rawSchedule =
					new SparseArray<>(discList.size()); //Max # of different hours

			int amountOfDays = MINIMUM_DAYS_AMOUNT;
			int maxDailyClasses = 0;
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
