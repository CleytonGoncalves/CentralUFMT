package com.cleytongoncalves.centralufmt.data.events;

import com.cleytongoncalves.centralufmt.data.model.menuru.MenuRu;

public final class MenuRuFetchEvent extends AbstractEvent<MenuRu> {
	
	public MenuRuFetchEvent(MenuRu result) {
		super(result);
	}
	
	public MenuRuFetchEvent(int failureReason) {
		super(failureReason);
	}
}
