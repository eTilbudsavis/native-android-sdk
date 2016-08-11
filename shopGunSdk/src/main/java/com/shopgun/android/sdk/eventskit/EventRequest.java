package com.shopgun.android.sdk.eventskit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EventRequest {
    
    public static final String TAG = EventRequest.class.getSimpleName();
    
    public static final String URL = "https://events-staging.shopgun.com/track";
    private static final HttpUrl URL_PARSED = HttpUrl.parse(URL);
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
    private static final Headers HEADERS = new Headers.Builder()
            .add("Content-Type", "application/json")
            .add("Accept", "application/json")
            .build();


    public static Call post(OkHttpClient client, List<Event> events) {
        String jsonBody = json(events).toString();
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, jsonBody);
        Request request = new Request.Builder()
                .url(URL_PARSED)
                .post(body)
                .headers(HEADERS)
                .build();
        return client.newCall(request);
    }

    private static JSONObject json(List<Event> events) {
        JSONArray jEvents = new JSONArray();
        for (Event event : events) {
            jEvents.put(event.toJson());
        }
        Map<String, JSONArray> map = new HashMap<>();
        map.put("events", jEvents);
        return new JSONObject(map);
    }

}
