package com.cleytongoncalves.centralufmt.data.remote;

import android.os.AsyncTask;
import android.util.Log;

import com.cleytongoncalves.centralufmt.data.events.LogInEvent;
import com.cleytongoncalves.centralufmt.data.events.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Student;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

import okhttp3.FormBody;

public final class SigaLogInTask extends AsyncTask<Void, Void, LogInEvent> {
	public static final int SIGA_LOGIN = - 1;
	public static final int AVA_LOGIN = - 2;
	private static final String TAG = SigaLogInTask.class.getSimpleName();
	private static final String BASE_SIGA_URL = "http://sia.ufmt.br/www-siga/dll/";
	private static final String GET_SIGA_URL = "autenticacao_unica/LoginUnicoIDBUFMT" +
			                                           ".dll/chamalogin";
	private static final String POST_SIGA_URL = "autenticacao_unica/LoginUnicoIDBUFMT.dll/logar";
	private static final String EXACAO_SIGA_URL = "PConferencia_EXACAO.exe/consultaExacao";

	private final String mRga;
	private final NetworkService mNetworkService;
	private char[] mPassword;
	private boolean mCancelTask;

	public SigaLogInTask(String rga, char[] password, NetworkService networkService) {
		mRga = rga;
		mPassword = password;
		mNetworkService = networkService;
		mCancelTask = false;
	}

	@Override
	protected LogInEvent doInBackground(Void... Void) {
		NetworkOperation logInPageGet = mNetworkService.get(BASE_SIGA_URL + GET_SIGA_URL);

		if (mCancelTask) {
			return userCanceled();
		} else if (logInPageGet.hasFailed()) {
			return generalFailure();
		}

		FormBody params = createFormParams(logInPageGet.getResponseBody());
		NetworkOperation logInPost = mNetworkService.post(BASE_SIGA_URL + POST_SIGA_URL, params);

		if (mCancelTask) {
			return userCanceled();
		} else if (logInPost.hasFailed()) {
			return generalFailure();
		} else if (! isLogInSuccessful(logInPost)) {
			return accessDenied();
		}

		NetworkOperation infoPageGet = mNetworkService.get(BASE_SIGA_URL + EXACAO_SIGA_URL);

		if (mCancelTask) {
			return userCanceled();
		} else if (infoPageGet.hasFailed()) {
			return generalFailure();
		}

		HtmlHelper htmlHelper = new HtmlHelper(infoPageGet.getResponseBody());
		Student student = htmlHelper.parseBasicStudent();
		return new LogInEvent(student);
	}

	@Override
	protected void onPostExecute(LogInEvent result) {
		EventBus.getDefault().post(result);
	}

	private LogInEvent userCanceled() {
		return new LogInEvent(LogInEvent.USER_CANCELED);
	}

	private LogInEvent generalFailure() {
		return new LogInEvent(LogInEvent.GENERAL_ERROR);
	}

	private FormBody createFormParams(String html) {
		final String login_field = "txt_login";
		final String password_field = "txt_senha";

		Map<String, String> paramsMap = HtmlHelper.createFormParams(html);
		paramsMap.put(login_field, mRga);
		paramsMap.put(password_field, String.valueOf(mPassword));

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

	public void cancelTask() {
		mCancelTask = true;
	}
}
