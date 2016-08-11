package com.shopgun.android.sdk.eventskit;

import android.content.Context;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.SgnPreferences;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventManager {

    public static final String TAG = EventManager.class.getSimpleName();
    
    private static EventManager mInstacnce;

    private Collection<WeakReference<EventTracker>> mTrackers = new HashSet<WeakReference<EventTracker>>();
    private static BlockingQueue<Event> mEventQueue = new LinkedBlockingQueue<>(128);
    private JSONObject mContext;
    private JSONObject mClient;
    private EventDispatcher mEventDispatcher;

    public static EventManager getInstacnce() {
        if (mInstacnce == null) {
            synchronized (EventManager.class) {
                if (mInstacnce == null) {
                    mInstacnce = new EventManager();
                }
            }
        }
        return mInstacnce;
    }

    private EventManager() {
        mEventDispatcher = new EventDispatcher(mEventQueue, ShopGun.getInstance().getClient());
        mEventDispatcher.start();
        mContext = EventUtils.getContext(ShopGun.getInstance().getContext());
        mClient = getClient();
    }

    public static JSONObject getClient() {
        JsonMap map = new JsonMap();
        map.put("id", SgnPreferences.getInstance().getInstallationId());
        map.put("trackId", SgnUtils.createUUID());
        return map.toJson();
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

    public void setCampaign(JSONObject campaign) {
        try {
            mContext.put("campaing", campaign);
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
    }

    public JSONObject getContext(boolean updateLocation) {
        if (updateLocation) {
            try {
                Context ctx = ShopGun.getInstance().getContext();
                JSONObject loc = EventUtils.location(ctx);
                mContext.put("location", loc);
            } catch (JSONException e) {
                SgnLog.e(TAG, e.getMessage(), e);
            }
        }
        return mContext;
    }

    public void addEvent(Event event) {
        event.setClient(mClient);
        mEventQueue.add(event);
    }

    public void flush() {
        mEventDispatcher.flush();
    }

}
