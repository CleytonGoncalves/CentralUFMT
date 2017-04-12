package com.cleytongoncalves.centralufmt.data.model.menuru;

import com.cleytongoncalves.centralufmt.util.TimeInterval;

import org.immutables.value.Value;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.List;

@Value.Immutable
public abstract class AbstractMenuRu {
	public static final TimeInterval BREAKFAST_TIME = new TimeInterval("07:00", "08:00");
	public static final TimeInterval LUNCH_TIME = new TimeInterval("11:00", "13:30");
	public static final TimeInterval LUNCH_TIME_SATURDAY = new TimeInterval("11:00", "13:00");
	public static final TimeInterval DINNER_TIME = new TimeInterval("17:00", "19:30");
	
	public abstract LocalDate getDate();
	
	@Value.Default
	public List<String> getBreakfast() {
		return Collections.emptyList();
	}

	public abstract Meal getLunch();

	@Value.Default
	public Meal getDinner() {
		return Meal.emptyMeal(DINNER_TIME);
	}
}