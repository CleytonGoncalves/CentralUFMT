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
	@Value.Default
	public String getHeader() {
		return "";
	}
	
	@Value.Default
	public String getTimeDate() {
		return "";
	}
	
	@Value.Default
	public String getMainCourse() {
		return "";
	}
	
	@Value.Default
	public String getVegetarian() {
		return "";
	}
	
	@Value.Default
	public String getGarnish() {
		return "";
	}
	
	@Value.Default
	public String getSalad() {
		return "";
	}
	
	@Value.Default
	public String getAcompaniment() {
		return "";
	}
	
	@Value.Default
	public String getDessert() {
		return "";
	}

	@Value.Default
	public boolean isEmpty() {
		return false;
	}

	static MealModelView emptyMeal(String header, String date) {
		return MealModelView.builder().header(header).timeDate(date).build();
	}
}
