package com.cleytongoncalves.centralufmt.data.jobs.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.cleytongoncalves.centralufmt.data.events.MoodleLogInEvent;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import okhttp3.Cookie;
import okhttp3.FormBody;
import timber.log.Timber;

import static com.birbit.android.jobqueue.CancelReason.CANCELLED_VIA_SHOULD_RE_RUN;
import static com.birbit.android.jobqueue.CancelReason.CANCELLED_WHILE_RUNNING;
import static com.birbit.android.jobqueue.CancelReason.REACHED_RETRY_LIMIT;

public final class MoodleLogInJob extends NetworkJob {
	public static final String TAG = MoodleLogInJob.class.getName();
	private static final int RETRY_LIMIT = 2;
	private static final int RETRY_DELAY = 150;
	
	private static final String BASE_AVA_URL = "https://www.ava.ufmt.br";
	private static final String POST_AVA_URL = "/index.php?pag=login";
	
	@Inject Lazy<NetworkService> mLazyNetworkService;
	private final String mRga;
	private char[] mAuthKey;
	
	public MoodleLogInJob(String rga, char[] authKey) {
		super(new Params(BACKGROUND)
		     .addTags(TAG)
		     .singleInstanceBy(TAG));
		
		mRga = rga;
		mAuthKey = authKey;
	}
	
	@Override
	public void inject(ApplicationComponent appComponent) {
		appComponent.inject(this);
	}
	
	@Override
	public void onAdded() {
		Timber.i("Moodle login started");
	}
	
	@Override
	public void onRun() throws Throwable {
		assertNetworkConnected();
		NetworkService networkService = mLazyNetworkService.get();
		
		FormBody params = createAvaFormParams();
		
		assertNotCancelled();
		
		NetworkOperation logInPost = networkService.post(BASE_AVA_URL + POST_AVA_URL, params);
		assertNetworkSuccess(logInPost);
		
		List<Cookie> cookies = networkService.getCookieFromJar(BASE_AVA_URL);
		
		assertLogInSuccess(cookies, logInPost.getResponseBody());
		
		assertNotCancelled();
		clearAuthKey();
		EventBus.getDefault().postSticky(new MoodleLogInEvent(cookies.get(0)));
		Timber.i("Moodle login successful");
	}
	
	@Override
	protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
		clearAuthKey();
		
		String msg = "Moodle login cancelled";
		switch (cancelReason) {
			case REACHED_RETRY_LIMIT:
				EventBus.getDefault().post(new MoodleLogInEvent(MoodleLogInEvent.GENERAL_ERROR));
				Timber.i("%s - Reached Retry Limit", msg);
				break;
			case CANCELLED_VIA_SHOULD_RE_RUN:
				EventBus.getDefault().post(new MoodleLogInEvent(MoodleLogInEvent.GENERAL_ERROR));
				Timber.i("%s - HTTP Status 400 (Client Error)", msg);
				break;
			case CANCELLED_WHILE_RUNNING:
				Timber.i("%s - Job Cancelled", msg);
				break;
		}
	}
	
	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount,
	                                                 int maxRunCount) {
		if (shouldRetry(throwable)) {
			//exponential delay in ms before trying again
			RetryConstraint constraint = RetryConstraint
					                             .createExponentialBackoff(runCount, RETRY_DELAY);
			constraint.setApplyNewDelayToGroup(true);
			return constraint;
		}
		
		return RetryConstraint.CANCEL;
	}
	
	@Override
	protected int getRetryLimit() {
		return RETRY_LIMIT;
	}
	
	/* Helper Methods */
	
	private FormBody createAvaFormParams() {
		FormBody.Builder formBody = new FormBody.Builder();
		formBody.add("userLogar", mRga)
		        .add("senha", String.valueOf(mAuthKey))
		        .add("envio", "login")
		        .add("x", "0")
		        .add("y", "0");
		
		return formBody.build();
	}
	
	/**
	 * Should be called to free the AuthKey from memory after it is done using it
	 */
	private void clearAuthKey() {
		//Try to clear the password from memory after using it
		Arrays.fill(mAuthKey, '0');
		mAuthKey = null;
	}
	
	private void assertLogInSuccess(List<Cookie> cookies, String html) {
		if (cookies.isEmpty()) {
			throw new AuthenticationErrorException(html);
		}
	}
	
}
