package com.shopgun.android.sdk.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.shopgun.android.sdk.eventskit.AnonymousEvent;
import com.shopgun.android.sdk.eventskit.EventTracker;
import com.shopgun.android.sdk.eventskit.EventUtils;
import com.shopgun.android.utils.log.LogUtil;

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
        fullStackTest();
    }

    private void fullStackTest() {
        LogUtil.printMethod();

        EventTracker tracker = EventTracker.globalTracker();

        AnonymousEvent empty_event = new AnonymousEvent(AnonymousEvent.DEFAULT_TYPE);
        empty_event.track();

        AnonymousEvent search_event = new AnonymousEvent(AnonymousEvent.SEARCHED)
                .addSearch("coca cola", "dk");
        search_event.track();

        AnonymousEvent offer_event = new AnonymousEvent(AnonymousEvent.OFFER_OPENED)
                .addOfferOpened("kkqS23412")
                .addViewToken(EventUtils.generateViewToken("kkqS23412".getBytes(), "MyAwesomeApp"))
                .addUserCountry("DK");
        offer_event.track();

        tracker.flush();

    }

}
