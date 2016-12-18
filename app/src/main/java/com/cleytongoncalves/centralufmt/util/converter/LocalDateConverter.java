package com.cleytongoncalves.centralufmt.util.converter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;

public final class LocalDateConverter implements JsonSerializer<LocalDate>,
		                                                 JsonDeserializer<LocalDate> {
	private static final String PATTERN = "yyyy-MM-dd";

	public LocalDateConverter() {
	}

	public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		return new JsonPrimitive(fmt.print(src));
	}

	public LocalDate deserialize(JsonElement json, Type typeOfT,
	                             JsonDeserializationContext context) throws JsonParseException {
		if (json.isJsonNull()) { return null; }

		String jsonString = json.getAsString();
		if (jsonString != null && ! jsonString.isEmpty()) {
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
			return fmt.parseLocalDate(jsonString);
		} else {
			return null;
		}
	}
}
