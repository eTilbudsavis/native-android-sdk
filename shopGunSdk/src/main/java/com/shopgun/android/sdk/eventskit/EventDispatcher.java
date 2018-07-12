package com.shopgun.android.sdk.eventskit;

import android.os.Process;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.gson.JsonNullExclusionStrategy;
import com.shopgun.android.sdk.corekit.gson.RealmObjectExclusionStrategy;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EventDispatcher extends Thread {

    public static final String TAG = Constants.getTag(EventDispatcher.class);

    private static final String FLUSH_EVENT_UUID = "00000000-0000-0000-0000-000000000000";

    private static final int DEF_EVENT_BATCH_SIZE = 100;
    private static final int DEF_MAX_RETRY_COUNT = 5;

    /** The queue of requests to service. */
    private final BlockingQueue<Event> mQueue;
    /** The http client of choice. */
    private final OkHttpClient mClient;
    /** Used for telling us to die. */
    private volatile boolean mQuit = true;
    private final int mEventBatchSize;
    private final int mMaxRetryCount;
    private final Event mFlushEvent;
    private Realm mRealm;

    private final HttpUrl mUrl;
    private final MediaType mMediatype;
    private final Headers mHeaders;
    private final Gson mGson;

    public EventDispatcher(BlockingQueue<Event> queue, OkHttpClient client, String url) {
        this(queue, client, url, DEF_EVENT_BATCH_SIZE, DEF_MAX_RETRY_COUNT);
    }

    public EventDispatcher(BlockingQueue<Event> queue, OkHttpClient client, String url, int eventBatchSize, int maxRetryCount) {
        mQueue = queue;
        mClient = client;
        mEventBatchSize = eventBatchSize;
        mMaxRetryCount = maxRetryCount;
        mFlushEvent = new Event();
        mFlushEvent.setId(FLUSH_EVENT_UUID); // ensure an identifiable ID
        mFlushEvent.doNotTrack(true);
        mUrl = HttpUrl.parse(url);
        mMediatype = MediaType.parse("application/json");
        mHeaders = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .build();
        mGson = getGson();
    }

    private Gson getGson() {

        try {
            Class clazz = Class.forName("io.realm.EventRealmProxy");
            return new GsonBuilder()
                    .setExclusionStrategies(
                            new RealmObjectExclusionStrategy(),
                            new JsonNullExclusionStrategy())
                    .registerTypeAdapter(clazz, new EventSerializer())
                    .create();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Gson not instantiated due to missing RealmProxy class", e);
        }

    }

    public void quit() {
        flush();
        mQuit = true;
    }

    @Override
    public synchronized void start() {
        if (mQuit) {
            mQuit = false;
            if (!isAlive()) {
                try {
                    super.start();
                } catch (IllegalThreadStateException e) {
                    // ignore
                }
            }
            flush();
        }
    }

    @Override
    public void run() {
        // low priority on posting mEvents to atta
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mRealm = ShopGun.getInstance().getRealmInstance();
        Event event;
        while (!mQuit || !mQueue.isEmpty()) {
            try {
                // Take an event from the queue.
                event = mQueue.take();
            } catch (InterruptedException e) {
                // We were interrupted, likely because we want to quit
                continue;
            }
            if (event.doNotTrack()) {
                SgnLog.i(TAG, event.toString());
            } else {
                mRealm.executeTransaction(new InsertTransaction(event));
            }
            boolean flush = FLUSH_EVENT_UUID.equals(event.getId());
            dispatchEventQueue(flush);
        }
        mRealm.close();
        interrupt();
    }

    private void dispatchEventQueue(boolean force) {

        if (!force && !ShopGun.getInstance().getLifecycleManager().isActive()) {
            // Ship network is we aren't active
            return;
        }

        int count = (int) mRealm.where(Event.class).count();

        if (!force && count < mEventBatchSize) {
            // Wait until we have a decent amount of mEvents
            return;
        }

        Response response = null;

        try {

            List<Event> events = getEvents(mEventBatchSize);
            if (events.isEmpty()) {
                return;
            }

            Call call = buildCallFromEvents(events);
            response = call.execute();

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

                List<EventResponse.Item> errors = resp.getErrors();
                SgnLog.v(TAG, events.size() + " events successfully shipped. " + resp.getAckItems().size() + " ack, " + nackIds.size() + " nack, " + errors.size() + " error.");

                if (!errors.isEmpty()) {
                    for (EventResponse.Item i : resp.getErrors()) {
                        SgnLog.d(TAG, " - " + i.getErrors().toString());
                    }
                }

            } else {
                SgnLog.d(TAG, response.toString() + ", " + response.body().string());
            }

        } catch (Exception e) {
            SgnLog.e(TAG, "Network failed", e);
        } finally {
            if (mRealm.isInTransaction()) {
                mRealm.cancelTransaction();
            }
            if (response != null) {
                response.close();
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
        // we can't flush if we've exceeded the capacity
        if (mQueue.remainingCapacity() > 0) {
            mQueue.add(mFlushEvent);
        }
    }

    private static class InsertTransaction implements Realm.Transaction {

        Event mEvent;

        InsertTransaction(Event event) {
            mEvent = event;
        }

        @Override
        public void execute(Realm realm) {
            try {
                realm.insert(mEvent);
            } catch (RealmPrimaryKeyConstraintException e) {
                throw new IllegalStateException("Realm duplicate key on event: " + mEvent.toString(), e);
            }
        }
    }

    private Call buildCallFromEvents(List<Event> events) {
        JsonElement eventArray = mGson.toJsonTree(events);
        JsonObject eventWrapper = new JsonObject();
        eventWrapper.add("events", eventArray);
        RequestBody body = RequestBody.create(mMediatype, eventWrapper.toString());
        Request request = new Request.Builder()
                .url(mUrl)
                .post(body)
                .headers(mHeaders)
                .build();
        return mClient.newCall(request);
    }

}
