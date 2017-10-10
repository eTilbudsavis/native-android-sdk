package com.shopgun.android.sdk.eventskit;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.JsonObject;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.LifecycleManager;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.utils.LocationUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class EventManager {

    public static final String TAG = Constants.getTag(EventManager.class);

    private static final int DISPATCH_MSG = 5738629;
    private static final long DISPATCH_INTERVAL = TimeUnit.SECONDS.toMillis(120);
    public static final int MAX_QUEUE_SIZE = 1024;

    private static EventManager mInstance;
    private static final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISPATCH_MSG:
                    mInstance.flush();
                    break;
            }
        }
    };

    private Collection<WeakReference<EventTracker>> mTrackers;
    private static BlockingQueue<Event> mEventQueue;
    private JsonObject mJsonContext;
    private Location mLastKnownLocation;
    private JsonObject mJsonLocation;
    private EventDispatcher mEventDispatcher;
    private long mDispatchInterval = DISPATCH_INTERVAL;
    private final List<EventListener> mEventListeners;

    public static EventManager getInstance() {
        if (mInstance == null) {
            synchronized (EventManager.class) {
                if (mInstance == null) {
                    mInstance = new EventManager(ShopGun.getInstance());
                }
            }
        }
        return mInstance;
    }

    private EventManager(ShopGun shopGun) {
        mTrackers = new HashSet<>();
        mEventListeners = new ArrayList<>();
        mEventQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        mEventDispatcher = new EventDispatcher(mEventQueue, shopGun.getClient(), shopGun.getEventEnvironment());
        mJsonContext = EventUtils.getContext(shopGun.getContext());
        EventLifecycle lifecycleCallback = new EventLifecycle();
        shopGun.getLifecycleManager().registerCallback(lifecycleCallback);
        if (shopGun.getLifecycleManager().isActive()) {
            lifecycleCallback.onCreate(shopGun.getLifecycleManager().getActivity());
        }
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
        mJsonContext.add("campaign", campaign);
    }

    public JsonObject getContext(boolean updateLocation) {
        if (updateLocation || mLastKnownLocation == null) {
            Context ctx = ShopGun.getInstance().getContext();
            Location currentLoc = LocationUtils.getLastKnownLocation(ctx);
            if (LocationUtils.isBetterLocation(currentLoc, mLastKnownLocation)) {
                mLastKnownLocation = currentLoc;
                mJsonLocation = EventUtils.location(mLastKnownLocation);
            }
            mJsonContext.add("location", mJsonLocation);
        }
        return mJsonContext;
    }

    public void addEvent(Event event) {
        boolean isActive = ShopGun.getInstance().getLifecycleManager().isActive();
        if (!isActive) {
            mEventDispatcher.start();
        }
        if (mEventQueue.remainingCapacity() > 0) {
            dispatchOnEvent(event);
            mEventQueue.add(event);
        }
        if (!isActive) {
            mEventDispatcher.quit();
        }
    }

    private void resetTimer() {
        mHandler.removeMessages(DISPATCH_MSG);
        mHandler.sendEmptyMessageDelayed(DISPATCH_MSG, mDispatchInterval);
    }

    public void flush() {
        resetTimer();
        mEventDispatcher.flush();
    }

    private void startDispatcher() {
        if (mEventDispatcher == null || mEventDispatcher.getState() == Thread.State.TERMINATED) {
            ShopGun sgn = ShopGun.getInstance();
            mEventDispatcher = new EventDispatcher(mEventQueue, sgn.getClient(), sgn.getEventEnvironment());
        }
        mEventDispatcher.start();
    }

    private class EventLifecycle extends LifecycleManager.SimpleCallback {

        @Override
        public void onCreate(Activity activity) {
            startDispatcher();
        }

        @Override
        public void onDestroy(Activity activity) {
            mHandler.removeMessages(DISPATCH_MSG);
            mEventDispatcher.quit();
        }

    }

    public void addEventListener(EventListener tracker) {
        mEventListeners.add(tracker);
    }

    public void removeEventListener(EventListener tracker) {
        mEventListeners.remove(tracker);
    }

    public void removeAllEventListeners() {
        mEventListeners.clear();
    }

    private void dispatchOnEvent(Event event) {
        for (EventListener tracker : mEventListeners) {
            tracker.onEvent(event);
        }
    }

}
