package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.utils.SgnUtils;

public class EventTracker {

    public static final String TAG = EventTracker.class.getSimpleName();

    private static EventTracker mGlobalInstance;

    private String mTrackerId;
    private JsonObject mView;

    public static EventTracker globalTracker() {
        if (mGlobalInstance == null) {
            synchronized (EventTracker.class) {
                if (mGlobalInstance == null) {
                    mGlobalInstance = new EventTracker(SgnUtils.createUUID());
                }
            }
        }
        return mGlobalInstance;
    }

    public static EventTracker newTracker() {
        return newTracker(SgnUtils.createUUID());
    }

    public static EventTracker newTracker(String trackerId) {
        EventTracker tracker = new EventTracker(trackerId);
        EventManager.getInstance().registerTracker(tracker);
        return tracker;
    }

    private EventTracker(String trackerId) {
        SgnUtils.isValidUuidOrThrow(trackerId);
        mTrackerId = trackerId;
    }

    public void setView(String[] path) {
        setView(path, null, null);
    }

    public void setView(String[] path, String[] previousPath, String uri) {
        JsonObject map = new JsonObject();
        map.add("path", toJson(path));
        if (previousPath != null) {
            map.add("previousPath", toJson(previousPath));
        }
        if (uri != null) {
            map.addProperty("uri", uri);
        }
        setView(map);
    }

    private JsonArray toJson(String[] array) {
        JsonArray jsonArray = null;
        if (array != null) {
            jsonArray = new JsonArray();
            for (String s : array) {
                jsonArray.add(s);
            }
        }
        return jsonArray;
    }

    /**
     * Meta-data about "where" the event was triggered visually in the app.
     * So, what "page" did the event come from.
     * "path": ["some", "namespaced", "path"], // required
     * "previousPath": ["the", "previous", "path"], // optional
     * "uri": "sgn://offers/sg32rmfsd", // optional
     */
    public void setView(JsonObject view) {
        mView = view;
    }

    public void setCampaign(JsonObject campaign) {
        EventManager.getInstance().setCampaign(campaign);
    }

    public void track(String type, JsonObject properties) {
        track(new Event(type, properties));
    }

    public void track(Event event) {
        EventManager manager = EventManager.getInstance();
        JsonObject context = manager.getContext(true);
        context.add("view", mView);
        event.setContext(context);
        manager.addEvent(event);
    }

    public void flush() {
        EventManager.getInstance().flush();
    }

}
