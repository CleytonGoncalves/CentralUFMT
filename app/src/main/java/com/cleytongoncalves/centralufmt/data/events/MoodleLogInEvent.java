package com.cleytongoncalves.centralufmt.data.events;

import okhttp3.Cookie;

public final class MoodleLogInEvent extends AbstractEvent<Cookie> {
	public static final int ACCESS_DENIED = 3;
	
	public MoodleLogInEvent(Cookie result) {
		super(result);
	}
	
	public MoodleLogInEvent(int reason) {
		super(reason);
	}
}
