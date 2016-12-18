package com.cleytongoncalves.centralufmt.util.converter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

public final class LocalTimeConverter implements JsonSerializer<LocalTime>,
		                                                 JsonDeserializer<LocalTime> {
	private static final DateTimeFormatter FORMATTER = (new DateTimeFormatterBuilder()).append(
			ISODateTimeFormat.time().getPrinter(), ISODateTimeFormat.localTimeParser().getParser())
	                                                                                   .toFormatter();

	public LocalTimeConverter() {
	}

	public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.toString(FORMATTER));
	}

	public LocalTime deserialize(JsonElement json, Type typeOfT,
	                             JsonDeserializationContext context) throws JsonParseException {
		if (json.isJsonNull()) { return null; }

		String jsonString = json.getAsString();
		if (jsonString != null && ! jsonString.isEmpty()) {
			return LocalTime.parse(jsonString, FORMATTER);
		} else {
			return null;
		}
	}
}
