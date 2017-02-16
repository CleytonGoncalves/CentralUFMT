package com.cleytongoncalves.centralufmt.data.events;

import com.cleytongoncalves.centralufmt.data.model.Student;

public final class SigaLogInEvent extends BusEvent<Student> {
	public static final int ACCESS_DENIED = 3;
	
	public SigaLogInEvent(Student result) {
		super(result);
	}
	
	public SigaLogInEvent(int failureReason) {
		super(failureReason);
	}
}
