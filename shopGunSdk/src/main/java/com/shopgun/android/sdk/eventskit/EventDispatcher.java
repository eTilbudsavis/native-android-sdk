package com.shopgun.android.sdk.eventskit;

import android.os.Process;

import com.shopgun.android.sdk.eventskit.database.EventDb;
import com.shopgun.android.sdk.log.SgnLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class EventDispatcher extends Thread {

    public static final String TAG = EventDispatcher.class.getSimpleName();

    /** The queue of requests to service. */
    private final BlockingQueue<Event> mQueue;
    /** The http client of choice. */
    private final OkHttpClient mClient;
    /** Used for telling us to die. */
    private volatile boolean mQuit = false;
    private final int mMaxQueueSize;
    private final int mMaxRetryCount;
    private int mEventCounter;

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

            if (event instanceof FlushEvent) {
                dispatchEventQueue(true);
            } else {
                EventDb.getInstance().insert(event);
                mEventCounter++;
                dispatchEventQueue(false);
            }

        }
    }

    private void dispatchEventQueue(boolean force) {

        if (!force && mEventCounter < mMaxQueueSize) {
            return;
        }

        EventDb db = EventDb.getInstance();
        List<Event> events = db.getEvents();

        try {

            Date now = new Date();
            for (Event ew : events) {
                ew.setSentAt(now);
            }
            db.update(events);

            Call call = EventRequest.post(mClient, events);
            Response response = call.execute();

            if (response.isSuccessful()) {

                String responseBody = response.body().string();
                JSONObject jResponseBody = new JSONObject(responseBody);
                EventResponse jResponse = EventResponse.fromJson(jResponseBody);

                List<String> delete = jResponse.getAckIds();
                delete.addAll(jResponse.getErrorIds());
                db.delete(delete);
                db.updateRetryCount(jResponse.getNackIds(), mMaxRetryCount);

            }

        } catch (IOException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }

    }

    public void flush() {
        mQueue.add(new FlushEvent());
    }

    private static class FlushEvent extends Event {

    }

}
