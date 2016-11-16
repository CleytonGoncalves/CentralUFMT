package com.cleytongoncalves.centralufmt.data.remote.task;

import android.os.AsyncTask;

import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.FormBody;

public final class MoodleLogInTask extends AsyncTask<Void, Void, LogInEvent> implements LogInTask {
	private static final String TAG = MoodleLogInTask.class.getSimpleName();
	private static final String BASE_AVA_URL = "http://www.ava.ufmt.br";
	private static final String POST_AVA_URL = "/index.php?pag=login";

	private final NetworkService mNetworkService;
	private final String mRga;
	private char[] mPassword;

	public MoodleLogInTask(String rga, char[] password, NetworkService networkService) {
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
	protected LogInEvent doInBackground(Void... voids) {
		FormBody params = createAvaFormParams();
		NetworkOperation logInPost = mNetworkService.post(BASE_AVA_URL + POST_AVA_URL, params);

		LogInEvent event;
		if (logInPost.hasFailed()) {
			event = new LogInEvent(LogInEvent.GENERAL_ERROR);
		} else {
			List<Cookie> cookies = mNetworkService.getCookieFromJar(BASE_AVA_URL);

			if (cookies.isEmpty()) {
				event = new LogInEvent(LogInEvent.GENERAL_ERROR);
			} else {
				event = new LogInEvent(cookies.get(0));
			}
		}

		return event;
	}

	@Override
	protected void onPostExecute(LogInEvent logInEvent) {
		EventBus.getDefault().post(logInEvent);
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
