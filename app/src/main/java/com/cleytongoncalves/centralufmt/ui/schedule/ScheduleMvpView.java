package com.cleytongoncalves.centralufmt.ui.schedule;

import com.cleytongoncalves.centralufmt.ui.base.MvpView;

interface ScheduleMvpView extends MvpView {
	void setGridSpanCount(int gridSpan);

	void showRecyclerView();

	void hideRecyclerView();

	void showProgressBar();

	void hideProgressBar();

	void showEmptyScheduleSnack();

	void showGeneralErrorSnack();

	void hideSnackIfShown();
}
