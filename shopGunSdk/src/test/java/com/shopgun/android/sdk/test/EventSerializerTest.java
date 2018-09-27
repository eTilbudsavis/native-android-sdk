package com.shopgun.android.sdk.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.eventskit.AnonymousEvent;
import com.shopgun.android.sdk.eventskit.AnonymousEventSerializer;
import com.shopgun.android.sdk.eventskit.AnonymousEventWrapper;
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
    public void testSerializer() {

        // create a fully set event
        AnonymousEvent event = new AnonymousEvent(0)
                .addUserLocation("u5r3", System.currentTimeMillis())
                .addUserCountry("DK")
                .addViewToken("viewToken");

        AnonymousEventWrapper wrappedEvent =
                new AnonymousEventWrapper(event.getId(),event.getVersion(), event.getTimestamp(), event.toString());

        AnonymousEventSerializer serializer = new AnonymousEventSerializer();

        JsonElement result = serializer.serialize(wrappedEvent, null, null);

        // print the serialized event to see how it looks like
        System.out.println(result.toString());
    }

}