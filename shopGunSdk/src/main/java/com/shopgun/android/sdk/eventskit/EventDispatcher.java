package com.shopgun.android.sdk.eventskit;

import android.os.Process;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.utils.TextUtils;
import com.shopgun.android.utils.log.L;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class EventDispatcher extends Thread {

    public static final String TAG = Constants.getTag(EventDispatcher.class);

    private static final String DISPATCH_EVENT = "dispatch-event-queue-id";
    public static final int DEF_MAX_QUEUE_SIZE = 20;
    public static final int DEF_MAX_RETRY_COUNT = 5;

    /** The queue of requests to service. */
    private final BlockingQueue<Event> mQueue;
    /** The http client of choice. */
    private final OkHttpClient mClient;
    /** Used for telling us to die. */
    private volatile boolean mQuit = false;
    private final int mMaxQueueSize;
    private final int mMaxRetryCount;
    private Realm mRealm;

    public EventDispatcher(BlockingQueue<Event> queue, OkHttpClient client) {
        this(queue, client, DEF_MAX_QUEUE_SIZE, DEF_MAX_RETRY_COUNT);
    }

    public EventDispatcher(BlockingQueue<Event> queue, OkHttpClient client, int maxQueueSize, int maxRetryCount) {
        mQueue = queue;
        mClient = client;
        mMaxQueueSize = maxQueueSize;
        mMaxRetryCount = maxRetryCount;
    }

    /**
     * Terminate this NetworkDispatcher. Once terminated, no further requests will be processed.
     */
    public void quit() {
        mQuit = true;
    }

    @Override
    public void run() {
        // low priority on posting mEvents to atta
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mRealm = ShopGun.getInstance().getRealmInstance();
        Event event;
        while (true) {
            try {
                // Take an event from the queue.
                event = mQueue.take();

            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    // break the loop so we'll close the connection to Realm
                    break;
                }
                continue;
            }

            if (DISPATCH_EVENT.equals(event.getId())) {
                dispatchEventQueue(true);
            } else {
                mRealm.executeTransaction(new InsertTransaction(event));
                dispatchEventQueue(false);
            }

        }
        mRealm.close();

    }

    private void dispatchEventQueue(boolean force) {

        if (!force && !ShopGun.getInstance().getLifecycleManager().isActive()) {
            // Ship network is we aren't active
            return;
        }

        int count = (int) mRealm.where(Event.class).count();
        if (count == 0) {
            // Nothing to dispatch
            return;
        }

        if (!force && count < mMaxQueueSize) {
            // Wait until we have a decent amount of mEvents
            return;
        }

        try {

            mRealm.beginTransaction();
            List<Event> events = getEvents(100);
            Date now = new Date();
            for (Event event : events) {
                event.setSentAt(now);
            }
            mRealm.commitTransaction();

            Call call = EventRequest.postEvents(mClient, events);
            Response response = call.execute();

            if (response.isSuccessful()) {

                String responseBody = response.body().string();

                Gson gson = new GsonBuilder().create();
                EventResponse resp = gson.fromJson(responseBody, EventResponse.class);

                mRealm.beginTransaction();

                Set<String> removeIds = resp.getRemovableItems();
                getEvents(removeIds).deleteAllFromRealm();

                Set<String> nackIds = resp.getNackItems();

                RealmResults<Event> nack = getEvents(nackIds);
                for (Event e : nack) {
                    e.incrementRetryCount();
                }
                mRealm.where(Event.class).greaterThan("mRetryCount", mMaxRetryCount).findAll().deleteAllFromRealm();
                mRealm.commitTransaction();

                SgnLog.i(TAG, removeIds.size() + " events successfully shipped, " + nackIds.size() + " failed.");
            }

        } catch (Exception e) {
            SgnLog.e(TAG, "Networking failed", e);
            if (mRealm.isInTransaction()) {
                mRealm.cancelTransaction();
            }
        }

    }

    private List<Event> getEvents(int limit) {
        RealmResults<Event> events = mRealm.where(Event.class).findAll();
        List<Event> dispatchEvents = new ArrayList<>(100);
        for (int i = 0; i < Math.min(limit, events.size()); i++) {
            dispatchEvents.add(events.get(i));
        }
        return dispatchEvents;
    }

    private RealmResults<Event> getEvents(Set<String> ids) {
        RealmQuery<Event> query = mRealm.where(Event.class);
        boolean first = true;
        for (String id : ids) {
            if (!first) {
                query.or();
            }
            first = false;
            query = query.equalTo("mId", id);
        }
        return query.findAll();
    }

    public void flush() {
        Event event = new Event();
        event.setId(DISPATCH_EVENT);
        mQueue.add(event);
    }

    private static class InsertTransaction implements Realm.Transaction {

        Event mEvent;

        InsertTransaction(Event event) {
            mEvent = event;
        }

        @Override
        public void execute(Realm realm) {
            realm.insert(mEvent);
        }
    }

}
