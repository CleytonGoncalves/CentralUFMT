package com.cleytongoncalves.centralufmt.data.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class Course {
	public abstract String getTitle();

	public abstract String getCode();

	public abstract String getType();

	public abstract String getCurrentTerm();

	public abstract List<Discipline> getEnrolledDisciplines();
}