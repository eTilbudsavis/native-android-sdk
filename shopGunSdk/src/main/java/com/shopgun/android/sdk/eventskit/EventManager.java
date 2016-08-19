package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.LifecycleManager;
import com.shopgun.android.sdk.corekit.SgnPreferences;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.SgnUtils;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class EventManager {

    public static final String TAG = EventManager.class.getSimpleName();

    private static final int DISPATCH_MSG = 5738629;
    private static final long DISPATCH_INTERVAL = TimeUnit.SECONDS.toMillis(30);

    private static EventManager mInstacnce;
    private static final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISPATCH_MSG:
                    mInstacnce.flush();
                    break;
            }
        }
    };

    private Collection<WeakReference<EventTracker>> mTrackers = new HashSet<WeakReference<EventTracker>>();
    private static BlockingQueue<Event> mEventQueue = new LinkedBlockingQueue<>(1024);
    private JsonObject mJsonContext;
    private JsonObject mJsonClient;
    private EventDispatcher mEventDispatcher;
    private Gson mGson;

    public static EventManager getInstacnce() {
        if (mInstacnce == null) {
            synchronized (EventManager.class) {
                if (mInstacnce == null) {
                    mInstacnce = new EventManager(ShopGun.getInstance());
                }
            }
        }
        return mInstacnce;
    }

    private EventManager(ShopGun shopGun) {

        mEventDispatcher = new EventDispatcher(mEventQueue, shopGun.getClient());
        mJsonContext = EventUtils.getContext(shopGun.getContext());
        mJsonClient = getClient();

        LifecycleManager.Callback callback = new LifecycleManager.Callback() {

            @Override
            public void onCreate() {
                SgnLog.d(TAG, "onCreate");
                mHandler.sendEmptyMessageDelayed(DISPATCH_MSG, DISPATCH_INTERVAL);
                mEventDispatcher.start();
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onStop() {
            }

            @Override
            public void onDestroy() {
                SgnLog.d(TAG, "onDestroys");
                mHandler.removeMessages(DISPATCH_MSG);
                flush();
                mEventDispatcher.quit();
            }
        };

//        shopGun.getLifecycleManager().registerCallback(callback);


        mHandler.sendEmptyMessageDelayed(DISPATCH_MSG, DISPATCH_INTERVAL);
        mEventDispatcher.start();

    }

    public static JsonObject getClient() {
        JsonObject map = new JsonObject();
        map.addProperty("id", SgnPreferences.getInstance().getInstallationId());
        map.addProperty("trackId", SgnUtils.createUUID());
        return map;
    }

    public void registerTracker(EventTracker tracker) {
        synchronized (EventManager.class) {
            mTrackers.add(new WeakReference<EventTracker>(tracker));
        }
    }

    public void unregisterTracker(EventTracker tracker) {
        synchronized (EventManager.class) {
            Iterator<WeakReference<EventTracker>> it = mTrackers.iterator();
            while(it.hasNext()) {
                WeakReference<EventTracker> weakTracker = it.next();
                EventTracker tmp = weakTracker.get();
                if (tracker == tmp) {
                    it.remove();
                    break;
                }
                if (tmp == null) {
                    // if the weak-ref is null, remove
                    it.remove();
                }
            }
        }
    }

    public void setCampaign(JsonObject campaign) {
        mJsonContext.add("campaing", campaign);
    }

    public JsonObject getContext(boolean updateLocation) {
        if (updateLocation) {
            Context ctx = ShopGun.getInstance().getContext();
            JsonObject loc = EventUtils.location(ctx);
            mJsonContext.add("location", loc);
        }
        return mJsonContext;
    }

    public void addEvent(Event event) {
        event.setClient(mJsonClient);
        try {
            mEventQueue.add(event);
        } catch (IllegalStateException e) {
            SgnLog.d(TAG, "Queue is full", e);
        }
    }

    public void flush() {
        mEventDispatcher.flush();
    }

}
