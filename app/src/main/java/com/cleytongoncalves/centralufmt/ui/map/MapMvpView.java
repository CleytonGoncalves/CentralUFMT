package com.cleytongoncalves.centralufmt.ui.map;

import com.cleytongoncalves.centralufmt.ui.base.MvpView;

interface MapMvpView extends MvpView {
	void setPoiMenuState(boolean state);

	void setRouteMenuState(boolean state);
}
