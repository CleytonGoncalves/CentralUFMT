package com.cleytongoncalves.centralufmt.data.remote.task;

import android.os.AsyncTask;
import android.util.Log;

import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

import dagger.Lazy;
import okhttp3.FormBody;
import timber.log.Timber;

public final class SigaLogInTask extends AsyncTask<Void, Void, LogInEvent> implements LogInTask {
	private static final String TAG = SigaLogInTask.class.getSimpleName();
	private static final String BASE_SIGA_URL = "http://academico-siga.ufmt.br/www-siga/dll/";
	private static final String GET_SIGA_URL = "LoginUnicoIDBUFMT.dll/chamalogin";
	private static final String POST_SIGA_URL = "LoginUnicoIDBUFMT.dll/logar";
	private static final String EXACAO_SIGA_URL = "PConferencia_EXACAO.dll/listaEstrutura";

	private final Lazy<NetworkService> mNetworkService;
	private final String mRga;
	private char[] mPassword;

	public SigaLogInTask(String rga, char[] password, Lazy<NetworkService> networkService) {
		mRga = rga;
		mPassword = password;
		mNetworkService = networkService;
	}

	@Override
	public void start() {
		//Wrapper on execute, to make this class a child of LogInTask interface
		this.execute();
	}

	public void cancelTask() {
		this.cancel(true);
	}

	@Override
	protected LogInEvent doInBackground(Void... voids) {
		NetworkService networkService = mNetworkService.get();

		NetworkOperation logInPageGet =
				networkService.get(BASE_SIGA_URL + GET_SIGA_URL, NetworkService.CHARSET_ISO);

		if (isCancelled()) {
			return userCanceled();
		} else if (! logInPageGet.isSuccessful()) {
			return generalFailure();
		}

		FormBody params = createFormParams(logInPageGet.getResponseBody());
		NetworkOperation logInPost = networkService.post(BASE_SIGA_URL + POST_SIGA_URL, params);

		if (isCancelled()) {
			return userCanceled();
		} else if (! logInPost.isSuccessful()) {
			return generalFailure();
		} else if (! isLogInSuccessful(logInPost)) {
			return accessDenied();
		}

		NetworkOperation exacaoPageGet =
				networkService.get(BASE_SIGA_URL + EXACAO_SIGA_URL, NetworkService.CHARSET_ISO);

		if (isCancelled()) {
			return userCanceled();
		} else if (! exacaoPageGet.isSuccessful()) {
			return generalFailure();
		}
		
		Student student = HtmlHelper.parseStudent(exacaoPageGet.getResponseBody());
		return new LogInEvent(student);
	}

	@Override
	protected void onPostExecute(LogInEvent event) {
		Timber.d("LogIn on Siga - Successful: %s, Error: %s", event.isSuccessful(),
		         event.getFailureReason());
		EventBus.getDefault().post(event);
	}

	private FormBody createFormParams(String html) {
		final String loginField = "txt_login";
		final String passwordField = "txt_senha";

		Map<String, String> paramsMap = HtmlHelper.createFormParams(html);
		paramsMap.put(loginField, mRga);
		paramsMap.put(passwordField, String.valueOf(mPassword));

		FormBody.Builder formBody = new FormBody.Builder();
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			try {
				formBody.addEncoded(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "ENCODING ERROR: " + e.getMessage());
			}
		}

		//Try to clear the password from memory immediately after using it
		Arrays.fill(mPassword, '0');
		mPassword = null;
		paramsMap.clear();

		return formBody.build();
	}

	/********
	 * Helper methods
	 *******/
	private boolean isLogInSuccessful(NetworkOperation operation) {
		return operation.getResponseHeaders().containsKey("Set-Cookie");
	}

	private LogInEvent accessDenied() {
		return new LogInEvent(LogInEvent.ACCESS_DENIED);
	}

	private LogInEvent userCanceled() {
		return new LogInEvent(LogInEvent.USER_CANCELED);
	}

	private LogInEvent generalFailure() {
		return new LogInEvent(LogInEvent.GENERAL_ERROR);
	}
}
