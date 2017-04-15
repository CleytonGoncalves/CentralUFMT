package com.cleytongoncalves.centralufmt.data.jobs;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

import static com.birbit.android.jobqueue.CancelReason.CANCELLED_VIA_SHOULD_RE_RUN;
import static com.birbit.android.jobqueue.CancelReason.CANCELLED_WHILE_RUNNING;
import static com.birbit.android.jobqueue.CancelReason.REACHED_RETRY_LIMIT;

public final class ScheduleFetchJob extends NetworkJob {
	public static final String TAG = ScheduleFetchJob.class.getName();
	private static final int RETRY_LIMIT = 3;
	private static final int RETRY_DELAY = 150;
	
	private static final String URL =
			"http://academico-siga.ufmt.br/www-siga/dll/PlanilhaRgaAutenticada.dll/listaalunos";
	
	@Inject DataManager mDataManager;
	@Inject Lazy<NetworkService> mNetworkService;
	
	public ScheduleFetchJob() {
		super(new Params(BACKGROUND)
				      .addTags(TAG)
				      .singleInstanceBy(TAG)
				      .groupBy(SIGA));
	}
	
	@Override
	public void inject(ApplicationComponent appComponent) {
		appComponent.inject(this);
	}
	
	@Override
	public void onAdded() {
		Timber.d("Schedule fetch started");
	}
	
	@Override
	public void onRun() throws Throwable {
		assertLoggedInSiga();
		assertNetworkConnected();
		NetworkService networkService = mNetworkService.get();
		
		assertNotCancelled();
		
		NetworkOperation scheduleGet = networkService.get(URL, NetworkService.CHARSET_ISO);
		assertNetworkSuccess(scheduleGet);
		
		assertNotCancelled();
		
		List<Discipline> disciplineList;
		try {
			disciplineList = HtmlHelper.parseSchedule(scheduleGet.getResponseBody());
		} catch (Exception e) {
			throw new ParsingErrorException(e);
		}
		
		ScheduleFetchEvent event = new ScheduleFetchEvent(disciplineList);
		
		assertNotCancelled();
		EventBus.getDefault().post(event);
		Timber.d("ScheduleFetchJob fetch successful");
	}
	
	@Override
	protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
		String msg = "Schedule fetch cancelled";
		
		switch (cancelReason) {
			case REACHED_RETRY_LIMIT:
				EventBus.getDefault()
				        .post(new ScheduleFetchEvent(ScheduleFetchEvent.GENERAL_ERROR));
				Timber.d("%s - Reached Retry Limit", msg);
				break;
			case CANCELLED_VIA_SHOULD_RE_RUN:
				EventBus.getDefault()
				        .post(new ScheduleFetchEvent(ScheduleFetchEvent.GENERAL_ERROR));
				if (isAuthenticationException(throwable)) {
					Timber.d("%s - Not logged in on Siga", msg);
				} else {
					Timber.d("%s - HTTP Status 400 (Client Error)", msg);
				}
				break;
			case CANCELLED_WHILE_RUNNING:
				Timber.d("%s - Job Cancelled", msg);
				break;
		}
	}
	
	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount,
	                                                 int maxRunCount) {
		if (! shouldRetry(throwable) || isAuthenticationException(throwable)) {
			return RetryConstraint.CANCEL;
		}
		
		RetryConstraint constraint = RetryConstraint
				                             .createExponentialBackoff(runCount, RETRY_DELAY);
		constraint.setApplyNewDelayToGroup(true);
		return constraint;
	}
	
	@Override
	protected int getRetryLimit() {
		return RETRY_LIMIT;
	}
	
	private void assertLoggedInSiga() {
		if (! mDataManager.isLoggedInSiga()) {
			throw new AuthenticationErrorException();
		}
	}
	
	/**
	 * Helper method to check if the throwable is caused by not being logged in on Siga
	 *
	 * @param throwable
	 * @return True, if it is an AuthenticationError
	 */
	private boolean isAuthenticationException(Throwable throwable) {
		return throwable instanceof AuthenticationErrorException;
	}
}
