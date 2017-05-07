package com.cleytongoncalves.centralufmt.util.converter;


import org.greenrobot.greendao.converter.PropertyConverter;

public abstract class DBConverterAsString<P> implements PropertyConverter<P, String> {
	public static final String SEPARATOR = " & ";
}
