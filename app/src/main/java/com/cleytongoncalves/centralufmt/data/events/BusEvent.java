package com.cleytongoncalves.centralufmt.data.events;

public interface BusEvent<T> {

	boolean isSuccessful();

	T getResult();

	String getFailureReason();
}
