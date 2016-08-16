package com.shopgun.android.sdk.eventskit;

import android.os.Process;

import com.shopgun.android.sdk.log.SgnLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class EventDispatcher extends Thread {

    public static final String TAG = EventDispatcher.class.getSimpleName();

    private static final String DISPATCH_EVENT = "dispatch_event-queue";

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
        this(queue, client, 20, 5);
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

        // low priority on posting events to atta
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        mRealm = Realm.getDefaultInstance();

        dispatchEventQueue(true);

        Event event;
        while (true) {
            try {
                // Take an event from the queue.
                event = mQueue.take();

            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }

            if (DISPATCH_EVENT.equals(event.getId())) {
                dispatchEventQueue(true);
            } else {
                mRealm.insert(event);
                dispatchEventQueue(false);
            }

        }
    }

    private RealmResults<Event> getIds(Set<String> ids) {
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

    private void dispatchEventQueue(boolean force) {

        if (!force && mRealm.where(Event.class).count() < mMaxQueueSize) {
            return;
        }

        try {

            RealmResults<Event> events = mRealm.where(Event.class).findAll();
            mRealm.beginTransaction();
            Date now = new Date();
            for (Event ew : events) {
                ew.setSentAt(now);
            }
            mRealm.insertOrUpdate(events);
            mRealm.commitTransaction();

            Call call = EventRequest.post(mClient, events);
            Response response = call.execute();

            if (response.isSuccessful()) {

                String responseBody = response.body().string();
                JSONObject jResponseBody = new JSONObject(responseBody);
                EventResponse jResponse = EventResponse.fromJson(jResponseBody);

                Set<String> delete = jResponse.getAckIds();
                delete.addAll(jResponse.getErrorIds());
                getIds(delete).deleteAllFromRealm();

                RealmResults<Event> nack = getIds(jResponse.getNackIds());
                for (Event e : nack) {
                    e.incrementRetryCount();
                }
                mRealm.insertOrUpdate(nack);
                mRealm.where(Event.class).greaterThan("mRetryCount", mMaxRetryCount).findAll().deleteAllFromRealm();

            }

        } catch (IOException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }

    }

    public void flush() {
        Event event = new Event();
        event.setId(DISPATCH_EVENT);
        mQueue.add(event);
    }

}
