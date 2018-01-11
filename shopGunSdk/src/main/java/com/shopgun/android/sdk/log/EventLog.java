/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk.log;


import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.FixedArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EventLog class have been created to simplify logging within the ETA SDK.
 * You will notice that this class is being used extensively throughout the SDK.
 * And have been especially useful, while debugging networking issues.
 */
public class EventLog {

    public static final String TAG = Constants.getTag(EventLog.class);

    private List<Event> mEvents;

    /**
     * Create a new EventLog, with no size limitations. Please be aware that
     * these logs can grow to a considerable size, and may use an unreasonable
     * amount of memory, and is therefore only recommended under development.
     */
    public EventLog() {
        mEvents = Collections.synchronizedList(new ArrayList<Event>());
    }

    /**
     * Create a new log with a fixed size. The EventLig will be using FIFO ordering of events.
     *
     * @param logSize the desired size of the log
     */
    public EventLog(int logSize) {
        mEvents = Collections.synchronizedList(new FixedArrayList<Event>(logSize));
    }

    /**
     * Add a new Event to the log, based simply on a name. This is nice for tracing
     * when, where and in what order the events have occurred.
     *
     * @param name of the log entry
     */
    public void add(String name) {
        mEvents.add(new Event(name));
    }

    /**
     * Add a new Event to the log. Adding a special type, is nice for saving generic error correcting data
     * to a log. This can be {@link Event#TYPE_VIEW view}, {@link Event#TYPE_EXCEPTION exception}, or {@link Event#TYPE_REQUEST request} events.
     * but essentially any string will do.
     *
     * @param type of event to add
     * @param data data accompanying the event
     */
    public void add(String type, JSONObject data) {
        mEvents.add(new Event(type, type).setData(data));
    }

    /**
     * Add a new Event to the log.
     *
     * @param e event to add
     */
    public void add(Event e) {
        mEvents.add(e);
    }

    /**
     * Get the current list of events
     *
     * @return A list of {@link com.shopgun.android.sdk.log.Event}
     */
    public List<Event> getEvents() {
        return mEvents;
    }

    /**
     * Clear the current list of events in the EventLog
     */
    public void clear() {
        mEvents.clear();
    }

    /**
     * Get all logs of a given type
     *
     * @param type of event to get
     * @return a list of events
     */
    public List<Event> getType(String type) {
        List<Event> tmp = new ArrayList<Event>();
        for (Event e : mEvents) {
            if (e.getType().equals(type)) {
                tmp.add(e);
            }
        }
        return tmp;
    }

    /**
     * Prints the timing of events in this EventLog
     *
     * @param name to use as print prefix
     * @return A human readable string representation of this log
     */
    public String getString(String name) {

        if (mEvents.isEmpty()) {
            SgnLog.d(TAG, String.format("[%+6d ms] %s", 0, name));
        }

        StringBuilder sb = new StringBuilder();
        long prevTime = mEvents.get(0).getTime();
        sb.append(String.format("     [%+6d ms] %s", getTotalDuration(), name)).append("\n");
        for (int i = 0; i < mEvents.size(); i++) {
            Event e = mEvents.get(i);
            sb.append(String.format("[%2d] [%+6d ms] %s", i, (e.getTime() - prevTime), e.getName())).append("\n");
            prevTime = e.getTime();
        }

        return sb.toString();

    }

    public JSONArray toJSON(boolean rawTime) {
        return Event.toJSON(mEvents, rawTime);
    }

    /**
     * Get the total duration from the first Event were recorded till the last one.
     *
     * @return a non-negative number
     */
    public long getTotalDuration() {
        if (mEvents.isEmpty()) {
            return 0;
        }

        long first = mEvents.get(0).getTime();
        long last = mEvents.get(mEvents.size() - 1).getTime();
        return last - first;

    }

}