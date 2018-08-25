package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.lang.reflect.Type;


/**
 * Translate an event fetched from the database into a json object
 */
public class AnonymousEventSerializer implements JsonSerializer<AnonymousEventWrapper> {
    
    public static final String TAG = AnonymousEventSerializer.class.getSimpleName();
    
    @Override
    public JsonElement serialize(AnonymousEventWrapper src, Type typeOfSrc, JsonSerializationContext context) {

        String eventData = src.getEvent();

        return parse(eventData);
    }

    private JsonObject parse(String json) {
        try {
            return (JsonObject) new JsonParser().parse(json);
        } catch (Exception e) {
            return new JsonObject();
        }
    }

}
