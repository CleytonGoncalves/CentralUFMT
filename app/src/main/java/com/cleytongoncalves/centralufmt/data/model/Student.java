package com.cleytongoncalves.centralufmt.data.model;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Student {
	public abstract String getFullName();

	public abstract String getRga();

	public abstract Course getCourse();

	@Value.Derived
	public String getFirstName() {
		return getFullName().split(" ")[0];
	}

	@Value.Derived
	public String getLastName() {
		String[] names = getFullName().split(" ");
		return names.length > 1 ? names[names.length - 1] : "";
	}
}
