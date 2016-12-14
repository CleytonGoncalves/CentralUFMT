package com.cleytongoncalves.centralufmt.ui.menuru;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cleytongoncalves.centralufmt.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

final class MenuRuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private List<MealModelView> mMealList;

	MenuRuAdapter() {
		mMealList = new ArrayList<>();
		setHasStableIds(true);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new CardViewHolder(inflater.inflate(R.layout.menuru_card, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		MealModelView meal = mMealList.get(position);
		CardViewHolder viewHolder = (CardViewHolder) holder;

		viewHolder.headerView.append(meal.getHeader());
		viewHolder.dateView.append(meal.getTimeDate());
		viewHolder.mainCourseView.append(meal.getMainCourse());
		viewHolder.vegetarianView.append(meal.getVegetarian());
		viewHolder.garnishView.append(meal.getGarnish());
		viewHolder.saladView.append(meal.getSalad());
		viewHolder.acompanimentView.append(meal.getAcompaniment());
		viewHolder.dessertView.append(meal.getDessert());
	}

	@Override
	public int getItemCount() {
		return mMealList.size();
	}

	@Override
	public long getItemId(int position) {
		return mMealList.get(position).hashCode();
	}

	void setMealList(List<MealModelView> mealList) {
		mMealList.clear();
		mMealList.addAll(mealList);
	}

	static class CardViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.menuru_card_header) TextView headerView;
		@BindView(R.id.menuru_card_date) TextView dateView;
		@BindView(R.id.menuru_card_main_course) TextView mainCourseView;
		@BindView(R.id.menuru_card_vegetarian) TextView vegetarianView;
		@BindView(R.id.menuru_card_garnish) TextView garnishView;
		@BindView(R.id.menuru_card_salad) TextView saladView;
		@BindView(R.id.menuru_card_acompaniment) TextView acompanimentView;
		@BindView(R.id.menuru_card_dessert) TextView dessertView;

		CardViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}
}
