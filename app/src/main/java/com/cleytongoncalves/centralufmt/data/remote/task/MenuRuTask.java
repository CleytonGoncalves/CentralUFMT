package com.cleytongoncalves.centralufmt.data.remote.task;

import android.os.AsyncTask;

import com.cleytongoncalves.centralufmt.data.events.MenuRuFetchEvent;
import com.cleytongoncalves.centralufmt.data.local.MenuParser;
import com.cleytongoncalves.centralufmt.data.model.MenuRu;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;

import org.greenrobot.eventbus.EventBus;

import dagger.Lazy;
import timber.log.Timber;

public final class MenuRuTask extends AsyncTask<Void, Void, Void> {
	private static final String CARDAPIO_URL = "http://www.ufmt.br/ufmt/unidade/index" +
			                                           ".php/secao/visualizar/3793/RU";

	private final Lazy<NetworkService> mLazyNetworkService;

	public MenuRuTask(Lazy<NetworkService> networkService) {
		mLazyNetworkService = networkService;
	}

	@Override
	protected Void doInBackground(Void... voids) {
		NetworkService networkService = mLazyNetworkService.get();

		NetworkOperation menuGet = networkService.get(CARDAPIO_URL, NetworkService.CHARSET_UTF8);

		MenuRuFetchEvent event;
		//if (menuGet.isSuccessful()) {
		if (! menuGet.isSuccessful()) {
			//MenuRu menu = MenuParser.parse(menuGet.getResponseBody());
			MenuRu menu = MenuParser.parse(MenuParser.getHtml());
			event = new MenuRuFetchEvent(menu);
		} else {
			event = new MenuRuFetchEvent(MenuRuFetchEvent.GENERAL_ERROR);
		}

		Timber.d("Menu RU Fetch - Successful: %s, Error: %s", event.isSuccessful(),
		         event.getFailureReason());
		EventBus.getDefault().post(event);
		return null;
	}
}
