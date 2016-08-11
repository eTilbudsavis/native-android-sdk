package com.shopgun.android.sdk.eventskit;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class EventTracker {

    public static final String TAG = EventTracker.class.getSimpleName();

    private static EventTracker mGlobalInstance;

    private String mTrackerId;
    private JSONObject mView;

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
        EventManager.getInstacnce().registerTracker(tracker);
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
        JsonMap map = new JsonMap();
        List<String> tmp = Arrays.asList(path);
        map.put("path", tmp);
        if (previousPath != null) {
            tmp = Arrays.asList(previousPath);
            map.put("previousPath", tmp);
        }
        if (uri != null) {
            map.put("uri", uri);
        }
        setView(new JSONObject(map));
    }

    /**
     * Meta-data about "where" the event was triggered visually in the app.
     * So, what "page" did the event come from.
     * "path": ["some", "namespaced", "path"], // required
     * "previousPath": ["the", "previous", "path"], // optional
     * "uri": "sgn://offers/sg32rmfsd", // optional
     */
    public void setView(JSONObject view) {
        mView = view;
    }

    public void setCampaign(JSONObject campaign) {
        EventManager.getInstacnce().setCampaign(campaign);
    }

    public void track(String type, JSONObject properties) {
        track(new Event(type, properties));
    }

    public void track(Event event) {
        EventManager manager = EventManager.getInstacnce();
        JSONObject context = manager.getContext(true);
        try {
            context.put("view", mView);
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        event.setContext(context);
        manager.addEvent(event);
    }

    public void flush() {
        EventManager.getInstacnce().flush();
    }

}
