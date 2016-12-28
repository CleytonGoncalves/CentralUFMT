package com.cleytongoncalves.centralufmt.data.model;

import org.immutables.value.Value;

@Value.Immutable
abstract class AbstractStudent {
	public abstract String getFullName();

	public abstract String getRga();

	public abstract Course getCourse();

	@Value.Auxiliary
	public String getFirstName() {
		return getFullName().split(" ")[0];
	}

	@Value.Auxiliary
	public String getLastName() {
		String[] names = getFullName().split(" ");
		return names.length > 1 ? names[names.length - 1] : "";
	}
}
