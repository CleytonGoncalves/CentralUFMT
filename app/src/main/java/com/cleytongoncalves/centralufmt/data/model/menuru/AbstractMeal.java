package com.cleytongoncalves.centralufmt.data.model.menuru;

import com.cleytongoncalves.centralufmt.util.TimeInterval;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class AbstractMeal {
	public abstract TimeInterval getTimeInterval();

	public abstract List<String> getSalad();

	public abstract List<String> getMeat();

	public abstract List<String> getVegetarian();

	public abstract List<String> getGarnishes();

	public abstract List<String> getAcompaniment();

	public abstract List<String> getDessert();

	public abstract List<String> getJuice();

	@Value.Default
	public boolean isEmpty() {
		return false;
	}

	public static Meal emptyMeal(TimeInterval timeInterval) {
		return Meal.builder().timeInterval(timeInterval).isEmpty(true).build();
	}
}