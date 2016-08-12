package com.shopgun.android.sdk.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.shopgun.android.sdk.eventskit.Event;
import com.shopgun.android.sdk.eventskit.EventTracker;
import com.shopgun.android.sdk.eventskit.JsonMap;
import com.shopgun.android.sdk.eventskit.database.EventDb;
import com.shopgun.android.utils.log.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventsKitActivity extends AppCompatActivity {

    public static final String TAG = EventsKitActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventskit);
    }

    @Override
    protected void onResume() {
        super.onResume();
        localDBTest();
        fullStackTest();
    }

    private void fullStackTest() {

        EventTracker tracker = EventTracker.globalTracker();

        Event nullVersion = new Event("x-custom-event", getJson());
        nullVersion.setVersion(null);
        tracker.track(nullVersion);

        Event badType = new Event("bad-type", getJson());
        tracker.track(badType);

        Event perfect = new Event("x-custom-event", getJson());
        tracker.track(perfect);

        JsonMap map = new JsonMap();
        map.put("offerId", "kkqS23412");
        tracker.track("x-custom-event", map.toJson());

        map.put("offerId", "54gtr445h");
        tracker.track("x-custom-event", map.toJson());

        map.put("offerId", "e5gtretre");
        tracker.track("x-custom-event", map.toJson());

        tracker.flush();

    }

    static final String jProperties = "{\"offerId\":\"kkqS23412\",\"searchQuery\":{\"query\":\"cola\",\"filters\":[{\"price\":{\"currency\":\"DKK\",\"max\":12}}]}}";
    private JSONObject getJson() {
        try {
            return new JSONObject(jProperties);
        } catch (JSONException e) {
            return null;
        }
    }

    private void localDBTest() {

        Logg l = new Logg();

        l.intermediateStart();
//        L.d(TAG, "### Clear database");
        EventDb db = EventDb.getInstance();
        db.clear();
        l.intermediateStop("db cleared");

//        L.d(TAG, "### Generate events");
        JsonMap testMap = new JsonMap();
        testMap.put("offerId", "firstEvent");
        Event first = new Event("x-custom-event", testMap.toJson());
        testMap.put("offerId", "secondEvent");
        Event second = new Event("x-custom-event", testMap.toJson());

//        L.d(TAG, "### Insert events");
        l.intermediateStart();
        db.insert(first);
        db.insert(second);
        l.intermediateStop("db insert");

//        L.d(TAG, "### Update sentAt");
        List<Event> events = db.getEvents();
        Date now = new Date();
        for (Event event : events) {
            event.setSentAt(now);
        }
        l.intermediateStart();
        db.update(events);
        l.intermediateStop("db update");

//        L.d(TAG, "### UpdateRetryCount");
        l.intermediateStart();
        events = db.getEvents();
        db.updateRetryCount(toIds(events), 3);
        db.updateRetryCount(toIds(events), 3);
        db.updateRetryCount(toIds(events), 3);
        // Should delete
        db.updateRetryCount(toIds(events), 3);
        l.intermediateStop("db retry count");


//        L.d(TAG, "### Re-Insert events");
        db.insert(first);
        db.insert(second);

//        L.d(TAG, "### Delete events");
        l.intermediateStart();
        db.delete(toIds(events));
        l.intermediateStop("db deleted");

//        L.d(TAG, "db.event.count: " + db.getEventCount());

        db.clear();

        l.stop("done");

    }

    public static class Logg {

        long globstart = System.currentTimeMillis();
        long intermediate;

        public void intermediateStart() {
            intermediate = System.currentTimeMillis();
        }

        public void intermediateStop(String msg) {
            L.d(TAG, msg + ": " + (System.currentTimeMillis()-intermediate));
        }

        public void stop(String msg) {
            L.d(TAG, msg + ": " + (System.currentTimeMillis()-globstart));
        }

    }

    private Set<String> toIds(List<Event> events) {
        Set<String> ids = new HashSet<>();
        for(Event event : events) {
            ids.add(event.getId());
        }
        return  ids;
    }

    private void dumpTable(EventDb db) {
        L.d(TAG, "### Table dump");
        try {
            L.d(TAG, db.dump().toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
