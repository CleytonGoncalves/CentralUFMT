package com.cleytongoncalves.centralufmt.data.jobs;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.cleytongoncalves.centralufmt.data.events.SigaLogInEvent;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import okhttp3.FormBody;
import timber.log.Timber;

import static com.birbit.android.jobqueue.CancelReason.CANCELLED_VIA_SHOULD_RE_RUN;
import static com.birbit.android.jobqueue.CancelReason.CANCELLED_WHILE_RUNNING;
import static com.birbit.android.jobqueue.CancelReason.REACHED_RETRY_LIMIT;
import static com.cleytongoncalves.centralufmt.data.remote.NetworkService.*;

public final class SigaLogInJob extends NetworkJob {
	public static final String TAG = SigaLogInJob.class.getName();
	private static final int RETRY_LIMIT = 5;
	private static final int RETRY_DELAY = 100;
	
	private static final String BASE_SIGA_URL = "http://academico-siga.ufmt.br/www-siga/dll/";
	private static final String GET_SIGA_URL = "LoginUnicoIDBUFMT.dll/chamalogin";
	private static final String POST_SIGA_URL = "LoginUnicoIDBUFMT.dll/logar";
	private static final String EXACAO_SIGA_URL = "PConferencia_EXACAO.dll/listaEstrutura";
	
	@Inject Lazy<NetworkService> mLazyNetworkService;
	private final String mRga;
	private char[] mAuthKey;
	private boolean mAccessDenied;
	
	public SigaLogInJob(String rga, char[] authKey) {
		super(new Params(BACKGROUND_HIGH)
				      .addTags(TAG)
				      .singleInstanceBy(TAG)
				      .groupBy(SIGA));
		
		mRga = rga;
		mAuthKey = authKey;
	}
	
	@Override
	public void inject(ApplicationComponent appComponent) {
		appComponent.inject(this);
	}
	
	@Override
	public void onAdded() {
		Timber.d("Siga login started");
	}
	
	@Override
	public void onRun() throws Throwable {
		assertNetworkConnected();
		NetworkService networkService = mLazyNetworkService.get();
		
		assertNotCancelled();
		NetworkOperation logInGet = networkService.get(BASE_SIGA_URL + GET_SIGA_URL, CHARSET_ISO);
		assertNetworkSuccess(logInGet);
		
		FormBody params = createFormParams(logInGet.getResponseBody());
		
		assertNotCancelled();
		NetworkOperation logInPost = networkService.post(BASE_SIGA_URL + POST_SIGA_URL, params);
		assertNetworkSuccess(logInPost);
		
		assertLoginSuccess(logInPost);
		
		assertNotCancelled();
		NetworkOperation exacaoGet =
				networkService.get(BASE_SIGA_URL + EXACAO_SIGA_URL, CHARSET_ISO);
		assertNetworkSuccess(exacaoGet);
		
		Student student = parseStudent(exacaoGet);
		
		assertNotCancelled();
		clearAuthKey();
		EventBus.getDefault().post(new SigaLogInEvent(student));
		Timber.d("Siga login successful");
	}
	
	@Override
	protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
		clearAuthKey();
		
		String msg = "Siga login cancelled";
		switch (cancelReason) {
			case REACHED_RETRY_LIMIT:
				EventBus.getDefault().post(new SigaLogInEvent(SigaLogInEvent.GENERAL_ERROR));
				Timber.d("%s - Reached Retry Limit", msg);
				break;
			case CANCELLED_VIA_SHOULD_RE_RUN:
				if (mAccessDenied) {
					Timber.d("%s - Access Denied (Wrong User/Auth)", msg);
					EventBus.getDefault().post(new SigaLogInEvent(SigaLogInEvent.ACCESS_DENIED));
				} else {
					Timber.d("%s - HTTP Status 400 (Client Error)", msg);
					EventBus.getDefault().post(new SigaLogInEvent(SigaLogInEvent.GENERAL_ERROR));
				}
				break;
			case CANCELLED_WHILE_RUNNING:
				EventBus.getDefault().post(new SigaLogInEvent(SigaLogInEvent.USER_CANCELLED));
				Timber.d("%s - Job Cancelled", msg);
				break;
		}
	}
	
	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount,
	                                                 int maxRunCount) {
		if (mAccessDenied || ! shouldRetry(throwable)) {
			return RetryConstraint.CANCEL;
		}
		
		//exponential delay in ms before trying again
		RetryConstraint constraint = RetryConstraint
				                             .createExponentialBackoff(runCount, RETRY_DELAY);
		constraint.setApplyNewDelayToGroup(true);
		return constraint;
	}
	
	@Override
	protected int getRetryLimit() {
		return RETRY_LIMIT;
	}
	
	/* Helper Methods */
	
	private FormBody createFormParams(String html) {
		final String loginField = "txt_login";
		final String passwordField = "txt_senha";
		
		Map<String, String> paramsMap = HtmlHelper.parseSigaFormParams(html);
		paramsMap.put(loginField, mRga);
		paramsMap.put(passwordField, String.valueOf(mAuthKey));
		
		FormBody.Builder formBody = new FormBody.Builder();
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			try {
				formBody.addEncoded(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Timber.wtf(e, "*** Encoding error on Siga LogIn Form Params ***");
			}
		}
		
		paramsMap.clear();
		
		return formBody.build();
	}
	
	private Student parseStudent(NetworkOperation operation) throws Exception {
		try {
			return HtmlHelper.parseStudent(operation.getResponseBody());
		} catch (Exception e) {
			throw new ParsingErrorException(e);
		}
	}
	
	private void assertLoginSuccess(NetworkOperation operation) {
		if (! operation.getResponseHeaders().containsKey("Set-Cookie")) {
			mAccessDenied = true;
			throw new AuthenticationErrorException();
		}
	}
	
	/**
	 * Should be called to free the AuthKey from memory after it is done using it
	 */
	private void clearAuthKey() {
		//Try to clear the password from memory after using it
		Arrays.fill(mAuthKey, '0');
		mAuthKey = null;
	}
	
}
