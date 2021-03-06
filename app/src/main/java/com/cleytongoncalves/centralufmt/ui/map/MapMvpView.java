package com.cleytongoncalves.centralufmt.ui.map;

import com.cleytongoncalves.centralufmt.ui.base.MvpView;

interface MapMvpView extends MvpView {
	void setPoiMenuState(boolean state);

	void setBusRouteMenuState(boolean state);

	void showBusRouteError();

	void showPoiError();
}
