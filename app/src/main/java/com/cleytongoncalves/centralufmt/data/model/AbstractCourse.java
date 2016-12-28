package com.cleytongoncalves.centralufmt.data.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
abstract class AbstractCourse {
	public abstract String getTitle();

	public abstract String getCode();

	public abstract String getType();

	public abstract String getCurrentTerm();

	public abstract List<EnrolledDiscipline> getEnrolledDisciplines();

	public abstract List<Discipline> getAllDisciplines();
}