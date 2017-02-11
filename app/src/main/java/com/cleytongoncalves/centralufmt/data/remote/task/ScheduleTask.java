package com.cleytongoncalves.centralufmt.data.remote.task;

import android.os.AsyncTask;

import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import dagger.Lazy;
import timber.log.Timber;

public final class ScheduleTask extends AsyncTask<Void, Void, Void> {
	private static final String URL =
			"http://academico-siga.ufmt.br/www-siga/dll/PlanilhaRgaAutenticada.dll/listaalunos";

	private final Lazy<NetworkService> mNetworkService;

	public ScheduleTask(Lazy<NetworkService> networkService) {
		mNetworkService = networkService;
	}

	@Override
	protected Void doInBackground(Void... params) {
		NetworkService networkService = mNetworkService.get();
		
		if (isCancelled()) { return null; }
		NetworkOperation scheduleGet = networkService.get(URL, NetworkService.CHARSET_ISO);
		if (isCancelled()) { return null; }
		
		ScheduleFetchEvent event;
		if (! scheduleGet.isSuccessful()) {
			event = new ScheduleFetchEvent(ScheduleFetchEvent.GENERAL_ERROR);
		} else {
			List<Discipline> disciplineList =
					HtmlHelper.parseSchedule(scheduleGet.getResponseBody());

			event = new ScheduleFetchEvent(disciplineList);
		}
		
		if (isCancelled()) { return null; }
		
		Timber.d("Schedule Fetch - Successful: %s, Error: %s", event.isSuccessful(),
		         event.getFailureReason());
		EventBus.getDefault().post(event);
		return null;
	}
}
