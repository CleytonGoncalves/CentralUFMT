package com.cleytongoncalves.centralufmt.data.remote;

import com.cleytongoncalves.centralufmt.data.events.LogInEvent;

public interface LogInCallback {
	void OnLogInCompleted(LogInEvent logInEvent);
}
