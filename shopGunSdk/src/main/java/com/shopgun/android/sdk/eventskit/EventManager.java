package com.shopgun.android.sdk.eventskit;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.LifecycleManager;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Receives events from the {@link EventTracker} and insert them into the "to be dispatched" queue.
 */
public class EventManager {

    public static final String TAG = Constants.getTag(EventManager.class);

    private static final int DISPATCH_MSG = 5738629;
    private static final long DISPATCH_INTERVAL = TimeUnit.SECONDS.toMillis(120);
    public static final int MAX_QUEUE_SIZE = 1024;

    private static EventManager mInstance;

    // Every 120 sec the flush will be triggered and all the events will be sent to the server
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

    private static BlockingQueue<AnonymousEvent> mEventQueue;
    private EventDispatcher mEventDispatcher;
    private LegacyEventDispatcher mLegacyEventDispatcher;
    private long mDispatchInterval = DISPATCH_INTERVAL;
    private final List<EventListener> mEventListeners;
    private String mCountryCode;

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
        mEventListeners = new ArrayList<>();
        mEventQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        mEventDispatcher = new EventDispatcher(mEventQueue, shopGun.getClient(), shopGun.getEventEnvironment());
        mCountryCode = "";

        checkLegacyEvents(shopGun);

        EventLifecycle lifecycleCallback = new EventLifecycle();
        shopGun.getLifecycleManager().registerCallback(lifecycleCallback);
        if (shopGun.getLifecycleManager().isActive()) {
            lifecycleCallback.onCreate(shopGun.getLifecycleManager().getActivity());
        }
    }

    private void checkLegacyEvents(ShopGun shopGun) {
        mLegacyEventDispatcher = null;
        if (shopGun.legacyEventsDetected()) {
            mLegacyEventDispatcher = new LegacyEventDispatcher(shopGun.getClient(), shopGun.getLegacyEventEnvironment());
        }
    }

    /**
     * Add an event to the queue after adding the country code to it (optional field)
     * @param event to be added
     */
    public void addEvent(AnonymousEvent event) {

        event.addUserCountry(mCountryCode);

        if (!event.doNotTrack()) {
            SgnLog.d(TAG, "Adding tracked event: " + event.toString());
        }

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

    /**
     * Optional: the current country of the device user as an ISO 3166-1 alpha-2 encoded string that will
     * be added to every event.
     * @param userCountry code
     */
    public void setUserCountry(String userCountry) {
        if (userCountry != null && userCountry.length() == 2) {
            mCountryCode = userCountry.toUpperCase(Locale.ENGLISH);
        }
    }

    private void resetTimer() {
        mHandler.removeMessages(DISPATCH_MSG);
        // next flush in 120 sec
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

        if (mLegacyEventDispatcher != null) {
            mLegacyEventDispatcher.start();
        }

        // start flushing events every 120 sec
        mHandler.sendEmptyMessageDelayed(DISPATCH_MSG, mDispatchInterval);
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
            if (mLegacyEventDispatcher != null) {
                mLegacyEventDispatcher.quit();
            }
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

    private void dispatchOnEvent(AnonymousEvent event) {
        for (EventListener tracker : mEventListeners) {
            tracker.onEvent(event);
        }
    }

}
