package com.cleytongoncalves.centralufmt.data.events;

interface BusEvent<T> {
	String GENERAL_ERROR = "Network/IO Error";
	String USER_CANCELED = "User Canceled";
	
	boolean isSuccessful();

	T getResult();

	String getFailureReason();
}
