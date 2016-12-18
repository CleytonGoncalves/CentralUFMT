package com.cleytongoncalves.centralufmt.ui.menuru;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.events.MenuRuFetchEvent;
import com.cleytongoncalves.centralufmt.data.model.Meal;
import com.cleytongoncalves.centralufmt.data.model.MenuRu;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;
import com.cleytongoncalves.centralufmt.util.TextUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

final class MenuRuPresenter implements Presenter<MenuRuMvpView>, DataPresenter {
	private final DataManager mDataManager;

	@Nullable private MenuRuMvpView mView;
	@Nullable private MenuRuAdapter mAdapter;

	@Nullable private DataParserTask mParserTask;

	//TODO: SERVICE TO AUTO-UPDATE AT CERTAIN TIME OF THE DAY, AND TO PUSH A NOTIFICATION

	@Inject
	MenuRuPresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(MenuRuMvpView mvpView) {
		mView = mvpView;
	}

	@Override
	public void detachView() {
		mView = null;
		detachAdapter();
		if (EventBus.getDefault().isRegistered(this)) { EventBus.getDefault().unregister(this); }

		if (mParserTask != null) {
			mParserTask.cancel(true);
			mParserTask = null;
		}
	}

	/* MenuRuAdapter Methods */

	@Override
	public void attachAdapter(MenuRuAdapter adapter) {
		mAdapter = adapter;
	}

	@Override
	public void detachAdapter() {
		mAdapter = null;
	}

	/* Data Methods */

	void loadMenuRu(boolean forceUpdate) {
		if (isLoadingData()) { return; }

		MenuRu menuRu = null;
		if (! forceUpdate) { menuRu = mDataManager.getPreferencesHelper().getMenuRu(); }

		if (mView != null) {
			mView.showRecyclerView(false);
			mView.showProgressBar(true);
		}

		EventBus.getDefault().register(this);
		if (menuRu == null || ! isTodaysMenu(menuRu)) { mDataManager.fetchMenuRu(); } else {
			onFetchSuccessful(menuRu);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMenuFetched(MenuRuFetchEvent event) {
		if (event.isSuccessful()) {
			MenuRu menuRu = event.getResult();
			onFetchSuccessful(menuRu);
			mDataManager.getPreferencesHelper().putMenuRu(menuRu);
		} else {
			onFetchFailure();
		}
	}

	private void onFetchSuccessful(MenuRu menuRu) {
		if (mView == null || mAdapter == null) { return; }

		mParserTask = new DataParserTask(menuRu);
		mParserTask.execute();
	}

	private void onFetchFailure() {
		if (mView == null) { return; }

		EventBus.getDefault().unregister(this);

		mView.showProgressBar(false);
		mView.showRecyclerView(true);
		mView.showGeneralErrorSnack();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onDataChanged(List<MealModelView> mealList) {
		if (mView == null || mAdapter == null) { return; }
		EventBus.getDefault().unregister(this);

		mAdapter.setMealList(mealList);
		mAdapter.notifyDataSetChanged();

		mView.hideSnackIfShown();
		mView.showProgressBar(false);
		mView.showRecyclerView(true);

		if (isLoadingData()) {
			mParserTask = null;
			Timber.d("MenuRu updated successfully");
		}
	}

	/* Private Helper Methods */

	private boolean isLoadingData() {
		return mParserTask != null;
	}

	private boolean isTodaysMenu(MenuRu menuRu) {
		return ! menuRu.getDate().isEqual(LocalDate.now());
	}

	private static class DataParserTask extends AsyncTask<Void, Void, Void> {
		private final MenuRu mMenuRu;

		DataParserTask(MenuRu menuRu) {
			mMenuRu = menuRu;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			List<MealModelView> mealList = new ArrayList<>();

			final String lunchHeader = "ALMOÇO";
			final String dinnerHeader = "JANTA";

			MealModelView lunch = parseToModelView(lunchHeader, mMenuRu.getLunch());
			MealModelView dinner = parseToModelView(dinnerHeader, mMenuRu.getDinner());

			mealList.add(lunch);
			mealList.add(dinner);

			Timber.d("MealModelViews parsed - Size: %s - Cancelled: %s", mealList.size(),
			         isCancelled());
			if (! isCancelled()) { EventBus.getDefault().post(mealList); }
			return null;
		}

		private MealModelView parseToModelView(String header, Meal meal) {
			LocalDate date = mMenuRu.getDate();
			String timeDate =
					String.format("%s - %s/%s/%s", date.toString("EEE").toUpperCase(),
					              date.toString("dd"), date.toString("MM"), date.toString("YY"));

			if (meal.isEmpty()) { return MealModelView.emptyMeal(header, timeDate); }

			String main = parseToString(meal.getMeat());
			String vegetarian = parseToString(meal.getVegetarian());
			String garnish = parseToString(meal.getGarnishes());
			String salad = parseToString(meal.getSalad());
			String acompaniment = parseToString(meal.getAcompaniment());
			String dessert = parseToString(meal.getDessert());

			return MealModelView.of(header, timeDate, main, vegetarian, garnish, salad,
			                        acompaniment, dessert, false);
		}

		private String parseToString(List<String> stringList) {
			StringBuilder sb = new StringBuilder();

			for (int i = 0, size = stringList.size(); i < size; i++) {
				String each = TextUtil.capsSentenceFirstLetter(stringList.get(i));
				int toRemoveIdx = each.indexOf("(");
				if (toRemoveIdx != - 1) { each = each.substring(0, toRemoveIdx); }
				sb.append(each);
				if (i < size - 1) { sb.append(" / "); }
			}

			return sb.toString();
		}
	}
}
