package com.cleytongoncalves.centralufmt.ui.schedule;

import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;

import java.util.List;

import javax.inject.Inject;

final class SchedulePresenter implements Presenter<ScheduleMvpView> {
	private final DataManager mDataManager;
	@Nullable private ScheduleMvpView mScheduleView;

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
	}

	public List<Discipline> getDisciplineList() {
		//TODO: SCHEDULE GET
		return HtmlHelper.parseSchedule(HtmlHelper.getScheduleHtml());
	}
}
