package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.lang.reflect.Type;


/**
 * Serializer for legacy events
 */
public class LegacyEventSerializer implements JsonSerializer<Event> {
    
    public static final String TAG = LegacyEventSerializer.class.getSimpleName();
    
    @Override
    public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("version", src.getVersion());
        jsonObject.addProperty("id", src.getId());
        jsonObject.addProperty("type", src.getType());
        if (src.getRecordedAt() != null) {
            jsonObject.addProperty("recordedAt", ISO8601Utils.format(src.getRecordedAt()));
        }
        if (src.getSentAt() != null) {
            jsonObject.addProperty("sentAt", ISO8601Utils.format(src.getSentAt()));
        }
        if (src.getReceivedAt() != null) {
            jsonObject.addProperty("receivedAt", ISO8601Utils.format(src.getReceivedAt()));
        }
        jsonObject.add("client", src.getClient());
        jsonObject.add("context", src.getContext());
        jsonObject.add("properties", src.getProperties());
        return jsonObject;
    }

}
