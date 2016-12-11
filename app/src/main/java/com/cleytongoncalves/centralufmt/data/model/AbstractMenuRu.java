package com.cleytongoncalves.centralufmt.data.model;

import com.cleytongoncalves.centralufmt.util.TimeInterval;

import org.immutables.value.Value;
import org.joda.time.LocalDate;

import java.util.List;

@Value.Immutable
public abstract class AbstractMenuRu {
	public static final TimeInterval HORARIO_CAFE = new TimeInterval("07:00", "08:00");
	public static final TimeInterval HORARIO_ALMOCO = new TimeInterval("11:00", "13:30");
	public static final TimeInterval HORARIO_JANTA = new TimeInterval("17:00", "19:30");
	
	public abstract LocalDate getDate();

	public abstract List<String> getBreakfast();

	public abstract Meal getLunch();

	public abstract Meal getDinner();
}
