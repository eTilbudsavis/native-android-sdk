package com.shopgun.android.sdk.eventskit;

import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class EventTrackerImpl extends EventTracker {

    EventTrackerImpl(String trackerId) {
        super(trackerId);
    }

    public void setView(String[] path) {
        setView(path, null, null);
    }

    public void setView(String[] path, String[] previousPath, Uri uri) {
        JsonArray jPath = pathToJson(path);
        if (jPath == null) {
            setView((JsonObject) null);
            return;
        }
        JsonObject map = new JsonObject();
        map.add("path", jPath);
        if (previousPath != null) {
            map.add("previousPath", pathToJson(previousPath));
        }
        if (uri != null) {
            map.addProperty("uri", uri.toString());
        }
        setView(map);
    }

    private JsonArray pathToJson(String[] array) {
        if (array == null) {
            return null;
        }
        JsonArray jsonArray = new JsonArray();
        for (String s : array) {
            jsonArray.add(s);
        }
        return jsonArray;
    }

    public void track(Event event) {
        EventManager manager = EventManager.getInstance();
        JsonObject context = manager.getContext(true);
        JsonObject view = getView();
        if (view != null) {
            context.add("view", getView());
        }
        manager.addEvent(event);
    }

}
