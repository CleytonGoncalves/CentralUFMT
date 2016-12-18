package com.cleytongoncalves.centralufmt.util.converter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.Interval;

import java.lang.reflect.Type;

public final class IntervalConverter implements JsonSerializer<Interval>,
		                                                JsonDeserializer<Interval> {

	public IntervalConverter() {
	}

	public JsonElement serialize(Interval src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.toString());
	}

	public Interval deserialize(JsonElement json, Type typeOfT,
	                            JsonDeserializationContext context) throws JsonParseException {
		if (json.isJsonNull()) { return null; }

		String jsonString = json.getAsString();
		if (jsonString != null && ! jsonString.isEmpty()) {
			return new Interval(json.getAsString());
		} else {
			return null;
		}
	}
}