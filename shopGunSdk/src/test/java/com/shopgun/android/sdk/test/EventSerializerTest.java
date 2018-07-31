package com.shopgun.android.sdk.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.eventskit.Event;
import com.shopgun.android.sdk.eventskit.LegacyEventSerializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class EventSerializerTest {

    @Test
    public void testSerializer() throws Exception {
        Event event = new Event();

        // create a fully set event
        event.setGeoHash("u5r3", System.currentTimeMillis());
        event.setCountry("DK");
        event.setViewToken("viewToken");

        JsonObject payload = new JsonObject();
        payload.addProperty("a.p1", "AB54R");
        payload.addProperty("a.p2", "5");
        event.setPayload(payload);

        LegacyEventSerializer serializer = new LegacyEventSerializer();

        JsonElement result = serializer.serialize(event, null, null);

        // print the serialized event to see how it looks like
        System.out.println(result.toString());
    }

}