package com.cleytongoncalves.centralufmt.data.events;

import com.cleytongoncalves.centralufmt.data.model.MenuRu;

public final class MenuRuFetchEvent extends BusEvent<MenuRu> {
	
	public MenuRuFetchEvent(MenuRu result) {
		super(result);
	}
	
	public MenuRuFetchEvent(int failureReason) {
		super(failureReason);
	}
}
