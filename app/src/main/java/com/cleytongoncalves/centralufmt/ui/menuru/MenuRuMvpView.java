package com.cleytongoncalves.centralufmt.ui.menuru;

import com.cleytongoncalves.centralufmt.ui.base.MvpView;

interface MenuRuMvpView extends MvpView {
	void showRecyclerView(boolean enabled);

	void showProgressBar(boolean enabled);

	void showDataUpdatedSnack();

	void showGeneralErrorSnack();

	void hideSnackIfShown();
}
