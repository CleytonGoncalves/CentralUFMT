package com.cleytongoncalves.centralufmt.data.remote.task;

import android.os.AsyncTask;

import com.cleytongoncalves.centralufmt.data.events.ScheduleFetchEvent;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.data.remote.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.remote.NetworkService;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;

public final class ScheduleTask extends AsyncTask<Void, Void, Void> {
	private static final String URL =
			"http://sia.ufmt.br/www-siga/WebSnap/C_PlanilhaHorario/planilhaHorariodoAluno" +
					".dll/HorarioAluno";

	private final NetworkService mNetworkService;

	public ScheduleTask(NetworkService networkService) {
		mNetworkService = networkService;
	}

	@Override
	protected Void doInBackground(Void... params) {
		NetworkOperation scheduleGet = mNetworkService.get(URL);

		ScheduleFetchEvent event;
		if (scheduleGet.hasFailed()) {
			event = new ScheduleFetchEvent(ScheduleFetchEvent.GENERAL_ERROR);
		} else {
			List<Discipline> disciplineList =
					HtmlHelper.parseSchedule(scheduleGet.getResponseBody());

			event = new ScheduleFetchEvent(disciplineList);
		}

		Timber.d("Schedule Fetch - Successful: %s, Error: %s", event.isSuccessful(),
		         event.getFailureReason());
		EventBus.getDefault().post(event);
		return null;
	}
}
