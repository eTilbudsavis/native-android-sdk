package com.shopgun.android.sdk.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.eventskit.Event;
import com.shopgun.android.sdk.eventskit.EventTracker;
import com.shopgun.android.utils.log.L;
import com.shopgun.android.utils.log.LogUtil;

import io.realm.Realm;

public class EventsKitActivity extends AppCompatActivity {

    public static final String TAG = EventsKitActivity.class.getSimpleName();

    static final String jProperties = "{\"offerId\":\"kkqS23412\",\"searchQuery\":{\"query\":\"cola\",\"filters\":[{\"price\":{\"currency\":\"DKK\",\"max\":12}}]}}";
    static final JsonObject PROPERTIES = (JsonObject) new JsonParser().parse(jProperties);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventskit);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        realmUpdateTest(3);
        fullStackTest();
    }

    private void fullStackTest() {
        LogUtil.printMethod();

        EventTracker tracker = EventTracker.globalTracker();

        Event nullVersion = new Event("x-custom-event", PROPERTIES);
        nullVersion.setVersion(null);
        tracker.track(nullVersion);

        Event badType = new Event("bad-type", PROPERTIES);
        tracker.track(badType);

        Event perfect = new Event("x-custom-event", PROPERTIES);
        tracker.track(perfect);

        JsonObject o = new JsonObject();
        o.addProperty("offerId", "kkqS23412");
        tracker.track("x-custom-event", o);

        o.addProperty("offerId", "54gtr445h");
        tracker.track("x-custom-event", o);

        o.addProperty("offerId", "e5gtretre");
        tracker.track("x-custom-event", o);

        tracker.flush();

    }

    private void clearEventsFromRealm() {

        Realm realm = ShopGun.getInstance().getRealmInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.close();

    }

    private void realmUpdateTest(int events) {
        LogUtil.printMethod();
        clearEventsFromRealm();

        EventTracker tracker = EventTracker.globalTracker();
        for (int i = 0; i < events; i++) {
            Event event = new Event("x-custom-event", PROPERTIES);
            tracker.track(event);
        }

    }

}
