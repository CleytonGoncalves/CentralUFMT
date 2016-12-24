package com.cleytongoncalves.centralufmt.ui.schedule;

import com.cleytongoncalves.centralufmt.ui.base.MvpView;

interface ScheduleMvpView extends MvpView {
	void setGridSpanCount(int gridSpan);

	void showRecyclerView(boolean enabled);

	void showProgressBar(boolean enabled);

	void showDataUpdatedSnack();

	void showEmptyScheduleSnack();

	void showGeneralErrorSnack();

	void hideSnackIfShown();
}
