package com.cleytongoncalves.centralufmt.util.converter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

public final class DateTimeConverter implements JsonSerializer<DateTime>,
		                                                JsonDeserializer<DateTime> {
	public DateTimeConverter() {
	}

	public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		return new JsonPrimitive(fmt.print(src));
	}

	public DateTime deserialize(JsonElement json, Type typeOfT,
	                            JsonDeserializationContext context) throws JsonParseException {
		if (json.isJsonNull()) { return null; }

		String jsonString = json.getAsString();
		if (jsonString != null && ! jsonString.isEmpty()) {
			DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser();
			return fmt.parseDateTime(json.getAsString());
		} else {
			return null;
		}
	}
}
