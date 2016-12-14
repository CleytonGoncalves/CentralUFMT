package com.cleytongoncalves.centralufmt.ui.menuru;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Gson.TypeAdapters
@Value.Style(allParameters = true,
             typeAbstract = {"Abstract*"},
             typeImmutable = "*",
             defaults = @Value.Immutable(copy = false))
@Value.Immutable
abstract class AbstractMealModelView {
	public abstract String getHeader();

	public abstract String getTimeDate();

	public abstract String getMainCourse();
	
	public abstract String getVegetarian();

	public abstract String getGarnish();

	public abstract String getSalad();

	public abstract String getAcompaniment();

	public abstract String getDessert();

	@Value.Default
	public boolean isEmpty() {
		return false;
	}

	static MealModelView emptyMeal(String header, String date) {
		return MealModelView.builder().header(header).timeDate(date).build();
	}
}
