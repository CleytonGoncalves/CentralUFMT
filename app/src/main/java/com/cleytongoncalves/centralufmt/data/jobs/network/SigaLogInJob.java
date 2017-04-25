package com.cleytongoncalves.centralufmt.data.jobs.network;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.cleytongoncalves.centralufmt.data.events.SigaLogInEvent;
import com.cleytongoncalves.centralufmt.data.jobs.JobExitingException;
import com.cleytongoncalves.centralufmt.data.local.DatabaseHelper;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Course;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.data.model.Subject;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import okhttp3.FormBody;
import timber.log.Timber;

import static com.birbit.android.jobqueue.CancelReason.CANCELLED_VIA_SHOULD_RE_RUN;
import static com.birbit.android.jobqueue.CancelReason.CANCELLED_WHILE_RUNNING;
import static com.birbit.android.jobqueue.CancelReason.REACHED_RETRY_LIMIT;
import static com.cleytongoncalves.centralufmt.data.remote.NetworkService.CHARSET_ISO;

public final class SigaLogInJob extends NetworkJob {
	public static final String TAG = SigaLogInJob.class.getName();
	private static final int RETRY_LIMIT = 5;
	private static final int RETRY_DELAY = 200;
	
	private static final String BASE_SIGA_URL = "http://academico-siga.ufmt.br/www-siga/dll/";
	private static final String GET_SIGA_URL = "LoginUnicoIDBUFMT.dll/chamalogin";
	private static final String POST_SIGA_URL = "LoginUnicoIDBUFMT.dll/logar";
	private static final String EXACAO_SIGA_URL = "PConferencia_EXACAO.dll/listaEstrutura";
	
	@Inject Lazy<NetworkService> mLazyNetworkService;
	@Inject DatabaseHelper mDatabaseHelper;
	
	private final String mRga;
	private char[] mAuthKey;
	
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
		Timber.i("Siga login started");
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
		
		String exacaoHtml = exacaoGet.getResponseBody();
		
		Student student = parseStudent(exacaoHtml);
		Course course = parseCourse(exacaoHtml);
		List<Subject> curriculum = parseCurriculum(exacaoHtml);
		
		student.setCourseCode(course.getCode());
		for (Subject subj : curriculum) {
			subj.setCourseCode(course.getCode());
		}
		
		mDatabaseHelper.insertCourse(course);
		mDatabaseHelper.insertStudent(student);
		mDatabaseHelper.insertSubjectList(curriculum);
		
		assertNotCancelled();
		clearAuthKey();
		EventBus.getDefault().post(new SigaLogInEvent());
		Timber.i("Siga login successful");
	}
	
	@Override
	protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
		clearAuthKey();
		
		String msg = "Siga login cancelled";
		switch (cancelReason) {
			case REACHED_RETRY_LIMIT:
				EventBus.getDefault().post(new SigaLogInEvent(SigaLogInEvent.GENERAL_ERROR));
				Timber.i("%s - Reached Retry Limit", msg);
				break;
			case CANCELLED_VIA_SHOULD_RE_RUN:
				if (isAuthenticationException(throwable)) {
					EventBus.getDefault().post(new SigaLogInEvent(SigaLogInEvent.ACCESS_DENIED));
					Timber.i("%s - Authentication error (Wrong user/pass or server error)", msg);
				} else {
					EventBus.getDefault().post(new SigaLogInEvent(SigaLogInEvent.GENERAL_ERROR));
					Timber.i("%s - HTTP Status 400 (Client Error)", msg);
				}
				break;
			case CANCELLED_WHILE_RUNNING:
				EventBus.getDefault().post(new SigaLogInEvent(SigaLogInEvent.USER_CANCELLED));
				Timber.i("%s - Job Cancelled", msg);
				break;
		}
	}
	
	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount,
	                                                 int maxRunCount) {
		if (! shouldRetry(throwable) || isAuthenticationException(throwable)) {
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
	
	private FormBody createFormParams(String html) throws JobExitingException {
		final String loginField = "txt_login";
		final String passwordField = "txt_senha";
		
		Map<String, String> paramsMap;
		try {
			paramsMap = HtmlHelper.parseSigaFormParams(html);
		} catch (Exception e) {
			throw new ParsingErrorException(e, html);
		}
		
		paramsMap.put(loginField, mRga);
		paramsMap.put(passwordField, String.valueOf(mAuthKey));
		
		FormBody.Builder formBody = new FormBody.Builder();
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			try {
				formBody.addEncoded(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Timber.wtf(e, "Encoding error on Siga LogIn Form Params");
				throw new ParsingErrorException(e, html);
			}
		}
		
		paramsMap.clear();
		
		return formBody.build();
	}
	
	private List<Subject> parseCurriculum(String htmlResponse) throws ParsingErrorException {
		try {
			return HtmlHelper.parseCurriculum(htmlResponse);
		} catch (Exception e) {
			throw new ParsingErrorException(e, htmlResponse);
		}
	}
	
	private Course parseCourse(String htmlResponse) throws ParsingErrorException {
		try {
			return HtmlHelper.parseCourse(htmlResponse);
		} catch (Exception e) {
			throw new ParsingErrorException(e, htmlResponse);
		}
	}
	
	private Student parseStudent(String htmlResponse) throws ParsingErrorException {
		try {
			return HtmlHelper.parseStudent(htmlResponse);
		} catch (Exception e) {
			throw new ParsingErrorException(e, htmlResponse);
		}
	}
	
	private void assertLoginSuccess(NetworkOperation operation) {
		if (! operation.getResponseHeaders().containsKey("Set-Cookie")) {
			throw new AuthenticationErrorException(operation.getResponseBody());
		}
	}
	
	/**
	 * Helper method to check if the throwable is caused by having the access denied on Siga
	 * @return True, if it is an AuthenticationError
	 */
	private boolean isAuthenticationException(Throwable throwable) {
		return throwable instanceof AuthenticationErrorException;
	}
	
	/**
	 * Purges the AuthKey from memory for security reasons
	 */
	private void clearAuthKey() {
		//Try to clear the password from memory after using it
		Arrays.fill(mAuthKey, '0');
		mAuthKey = null;
	}
	
}
