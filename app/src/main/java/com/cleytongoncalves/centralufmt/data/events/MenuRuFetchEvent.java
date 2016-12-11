package com.cleytongoncalves.centralufmt.data.events;

import com.cleytongoncalves.centralufmt.data.model.MenuRu;

public final class MenuRuFetchEvent implements BusEvent<MenuRu> {
	public static final String GENERAL_ERROR = "Network/IO Error";

	private MenuRu mMenuRu;
	private String mFailureReason;

	public MenuRuFetchEvent(MenuRu menuRu) {
		mMenuRu = menuRu;
	}

	public MenuRuFetchEvent(String failureReason) {
		mFailureReason = failureReason;
	}

	@Override
	public boolean isSuccessful() {
		return mMenuRu != null;
	}

	@Override
	public MenuRu getResult() {
		return mMenuRu;
	}

	@Override
	public String getFailureReason() {
		return mFailureReason;
	}
}
