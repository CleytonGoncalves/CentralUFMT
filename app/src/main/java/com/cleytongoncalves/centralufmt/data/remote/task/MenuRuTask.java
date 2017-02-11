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
		
		if (isCancelled()) { return null; }
		NetworkOperation menuGet = networkService.get(CARDAPIO_URL, NetworkService.CHARSET_UTF8);
		
		MenuRuFetchEvent event = null;
		if (menuGet.isSuccessful() && !isCancelled()) {
			try {
				MenuRu menu = MenuParser.parse(menuGet.getResponseBody());
				event = new MenuRuFetchEvent(menu);
			} catch (Exception e) {
				Timber.w(e, "*** Error parsing MenuRu ***");
			}
		}
		
		if (isCancelled()) { return null; }
		else if (event == null) { event = new MenuRuFetchEvent(MenuRuFetchEvent.GENERAL_ERROR); }
			
		Timber.d("Menu RU Fetch - Successful: %s", event.isSuccessful());
		EventBus.getDefault().post(event);
		return null;
	}
}
