package net.mafiascum.json;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateUnixTimestampSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {

  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
    if(src == null) {
      return new JsonNull();
    }
    return new JsonPrimitive(src.getTime());
  }

  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if(json.isJsonNull()) {
      return null;
    }
    if(!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber()) {
      throw new JsonParseException("Invalid type for Date, must be a numeric timestamp!");
    }

    return new Date(json.getAsLong());
  }
}