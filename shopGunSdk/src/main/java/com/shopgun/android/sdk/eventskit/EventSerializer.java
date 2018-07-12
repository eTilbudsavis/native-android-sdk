package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Map;

public class EventSerializer implements JsonSerializer<Event> {
    
    public static final String TAG = EventSerializer.class.getSimpleName();
    
    @Override
    public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Mandatory fields
        jsonObject.addProperty("_v", src.getVersion());
        jsonObject.addProperty("_i", src.getId());
        jsonObject.addProperty("_e", src.getType());
        jsonObject.addProperty("_t", src.getTimestamp());
        jsonObject.addProperty("_a", src.getApplication());

        // Optional common fields
        if (src.hasLocationFields()) {
            jsonObject.addProperty("l.h", src.getGeoHash());
            jsonObject.addProperty("l.ht", src.getLocationTimestamp());
        }

        if (src.hasCountryField()) {
            jsonObject.addProperty("l.c", src.getCountry());
        }

        if (src.hasViewToken()) {
            jsonObject.addProperty("vt", src.getViewToken());
        }

        // Event defined fields
        if (src.hasAdditionalPayload()) {
            JsonObject payload = src.getPayload();
            for (Map.Entry<String, JsonElement> set : payload.entrySet()) {
                jsonObject.add(set.getKey(), set.getValue());
            }
        }
        return jsonObject;
    }

}
