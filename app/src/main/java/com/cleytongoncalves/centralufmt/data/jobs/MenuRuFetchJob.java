package com.cleytongoncalves.centralufmt.data.jobs;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.cleytongoncalves.centralufmt.data.events.MenuRuFetchEvent;
import com.cleytongoncalves.centralufmt.data.local.MenuParser;
import com.cleytongoncalves.centralufmt.data.model.menuru.MenuRu;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;
import com.cleytongoncalves.centralufmt.injection.component.ApplicationComponent;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

import static com.birbit.android.jobqueue.CancelReason.CANCELLED_VIA_SHOULD_RE_RUN;
import static com.birbit.android.jobqueue.CancelReason.CANCELLED_WHILE_RUNNING;
import static com.birbit.android.jobqueue.CancelReason.REACHED_RETRY_LIMIT;

public final class MenuRuFetchJob extends NetworkJob {
	public static final String TAG = MenuRuFetchJob.class.getName();
	private static final int RETRY_LIMIT = 3;
	private static final int RETRY_DELAY = 150;
	
	private static final String CARDAPIO_URL = "http://www.ufmt.br/ufmt/unidade/index" +
			                                           ".php/secao/visualizar/3793/RU";
	
	@Inject Lazy<NetworkService> mLazyNetworkService;
	
	public MenuRuFetchJob() {
		super(new Params(BACKGROUND)
				      .addTags(TAG)
				      .singleInstanceBy(TAG));
	}
	
	@Override
	public void inject(ApplicationComponent appComponent) {
		appComponent.inject(this);
	}
	
	@Override
	public void onAdded() {
		Timber.i("MenuRu fetch started");
	}
	
	@Override
	public void onRun() throws Throwable {
		assertNetworkConnected();
		NetworkService networkService = mLazyNetworkService.get();
		
		assertNotCancelled();
		
		NetworkOperation menuGet = networkService.get(CARDAPIO_URL, NetworkService.CHARSET_UTF8);
		assertNetworkSuccess(menuGet);
		
		assertNotCancelled();
		
		MenuRu menuRu = parseMenuRu(menuGet.getResponseBody());
		
		assertNotCancelled();
		EventBus.getDefault().post(new MenuRuFetchEvent(menuRu));
		Timber.i("MenuRu fetch successful");
	}
	
	@Override
	protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
		String msg = "MenuRu fetch cancelled";
		switch (cancelReason) {
			case REACHED_RETRY_LIMIT:
				EventBus.getDefault().post(new MenuRuFetchEvent(MenuRuFetchEvent.GENERAL_ERROR));
				Timber.i("%s - Reached Retry Limit", msg);
				break;
			case CANCELLED_VIA_SHOULD_RE_RUN:
				EventBus.getDefault().post(new MenuRuFetchEvent(MenuRuFetchEvent.GENERAL_ERROR));
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
	
	private MenuRu parseMenuRu(String html) {
		try {
			return MenuParser.parse(html);
		} catch (Exception e) {
			throw new ParsingErrorException(e, html);
		}
	}
}
