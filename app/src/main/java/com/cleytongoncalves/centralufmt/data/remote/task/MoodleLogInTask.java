package com.cleytongoncalves.centralufmt.data.remote.task;

import android.os.AsyncTask;

import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

import dagger.Lazy;
import okhttp3.Cookie;
import okhttp3.FormBody;
import timber.log.Timber;

public final class MoodleLogInTask extends AsyncTask<Void, Void, Void> implements LogInTask {
	private static final String BASE_AVA_URL = "http://www.ava.ufmt.br";
	private static final String POST_AVA_URL = "/index.php?pag=login";

	private final Lazy<NetworkService> mNetworkService;
	private final String mRga;
	private char[] mPassword;

	public MoodleLogInTask(String rga, char[] password, Lazy<NetworkService> networkService) {
		mRga = rga;
		mPassword = password;
		mNetworkService = networkService;
	}

	@Override
	public void start() {
		//Wrapper on execute, to make this class a child of LogInTask interface
		this.execute();
	}

	@Override
	public void cancelTask() {
		this.cancel(true);
	}

	@Override
	protected Void doInBackground(Void... voids) {
		NetworkService networkService = mNetworkService.get();
		
		//MUST have this check before params, bcs the password is cleared from memory when cancelled
		if (isCancelled()) { return null; }
		FormBody params = createAvaFormParams();
		
		if (isCancelled()) { return null; }
		NetworkOperation logInPost = networkService.post(BASE_AVA_URL + POST_AVA_URL, params);

		LogInEvent event = null;
		if (logInPost.isSuccessful() && ! isCancelled()) {
			List<Cookie> cookies = networkService.getCookieFromJar(BASE_AVA_URL);
			
			if (! cookies.isEmpty()) { event = new LogInEvent(cookies.get(0)); }
		}
		
		if (isCancelled()) { return null; }
		else if (event == null) { event = new LogInEvent(LogInEvent.GENERAL_ERROR); }
		
		Timber.d("LogIn on Moodle - Successful: %s", event.isSuccessful());
		EventBus.getDefault().post(event);
		return null;
	}

	private FormBody createAvaFormParams() {
		FormBody.Builder formBody = new FormBody.Builder();
		formBody.add("userLogar", mRga)
		        .add("senha", String.valueOf(mPassword))
		        .add("envio", "login")
		        .add("x", "0")
		        .add("y", "0");

		//Try to clear the password from memory immediately after using it
		Arrays.fill(mPassword, '0');
		mPassword = null;

		return formBody.build();
	}
}
