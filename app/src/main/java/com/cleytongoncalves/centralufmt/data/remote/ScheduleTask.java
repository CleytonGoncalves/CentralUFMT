package com.cleytongoncalves.centralufmt.data.remote;

import android.os.AsyncTask;

import com.cleytongoncalves.centralufmt.data.events.NetworkOperation;
import com.cleytongoncalves.centralufmt.data.local.HtmlHelper;
import com.cleytongoncalves.centralufmt.data.model.Discipline;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public final class ScheduleTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = ScheduleTask.class.getSimpleName();
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

		List<Discipline> disciplineList;
		if (scheduleGet.hasFailed()) {
			disciplineList = new ArrayList<>();
		} else {
			disciplineList = HtmlHelper.parseSchedule(scheduleGet.getResponseBody());
		}

		EventBus.getDefault().post(disciplineList);
		return null;
	}
}
