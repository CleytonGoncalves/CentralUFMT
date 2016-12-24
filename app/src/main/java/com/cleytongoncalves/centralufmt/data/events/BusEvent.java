package com.cleytongoncalves.centralufmt.data.events;

interface BusEvent<T> {

	boolean isSuccessful();

	T getResult();

	String getFailureReason();
}
