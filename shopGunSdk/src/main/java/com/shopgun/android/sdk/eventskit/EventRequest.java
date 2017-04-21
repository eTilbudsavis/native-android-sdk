package com.shopgun.android.sdk.eventskit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.corekit.gson.JsonNullExclusionStrategy;
import com.shopgun.android.sdk.corekit.gson.RealmObjectExclusionStrategy;
import com.shopgun.android.utils.log.L;

import java.util.List;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EventRequest {

    public static final String TAG = EventRequest.class.getSimpleName();

    public static final String URL = "https://events.service.shopgun.com/track";
    private static final HttpUrl URL_PARSED = HttpUrl.parse(URL);
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
    private static final Headers HEADERS = new Headers.Builder()
            .add("Content-Type", "application/json")
            .add("Accept", "application/json")
            .build();
    private static Gson mGson;

    public static Call postEvents(OkHttpClient client, List<Event> events) {
        JsonElement eventArray = gson().toJsonTree(events);
        JsonObject eventWrapper = new JsonObject();
        eventWrapper.add("events", eventArray);
        return postEvents(client, eventWrapper);
    }

    public static Call postEvents(OkHttpClient client, JsonElement json) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json.toString());
        Request request = new Request.Builder()
                .url(URL_PARSED)
                .post(body)
                .headers(HEADERS)
                .build();
        return client.newCall(request);
    }

    private static Gson gson() {
        if (mGson == null) {
            synchronized (EventRequest.class) {
                if (mGson == null) {
                    try {
                        Class clazz = Class.forName("io.realm.EventRealmProxy");
                        mGson = new GsonBuilder()
                                .setExclusionStrategies(
                                        new RealmObjectExclusionStrategy(),
                                        new JsonNullExclusionStrategy())
                                .registerTypeAdapter(clazz, new EventSerializer())
                                .create();
                    } catch (ClassNotFoundException e) {
                        L.w(TAG, "Gson not instantiated due to missing RealmProxy class", e);
                    }
                }
            }
        }
        return mGson;
    }

}
