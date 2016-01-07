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

package com.shopgun.android.sdk.pageflip.stats;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PageEvent {

    public static final String TAG = Constants.getTag(PageEvent.class);

    private final EventType mEventType;
    private final String mViewSession;
    private final int[] mPages;
    private final Orientation mOrientation;
    private final Clock mClock;
    private boolean mCollected = false;
    private long mStart = 0;
    private long mStop = 0;
    private ArrayList<PageEvent> mSubEvents = new ArrayList<PageEvent>();

    public static PageEvent view(String viewSession, int[] pages, Clock c) {
        return new PageEvent(EventType.VIEW, viewSession, Orientation.fromPages(pages), pages, c);
    }

    public static PageEvent zoom(String viewSession, int[] pages, Clock c) {
        return new PageEvent(EventType.ZOOM, viewSession, Orientation.fromPages(pages), pages, c);
    }

    public PageEvent(EventType type, String viewSession, Orientation orientation, int[] pages, Clock c) {
        mEventType = type;
        mViewSession = viewSession;
        mPages = pages;
        mOrientation = orientation;
        mClock = c;
    }

    /**
     * Start the timer for this event.
     * This method can be called multiple times, only the first time will set the start time.
     */
    public void start() {
        if (!isStarted()) {
            mStart = mClock.now();
        }
    }

    /**
     * Stop the time for this event. If the event haven't been started yet, the start and stop time will be the same.
     */
    public void stop() {
        if (isActive()) {
            mStop = mClock.now();
        } else if (!isStarted()) {
            long now = mClock.now();
            mStart = now;
            mStop = now;
            SgnLog.w(TAG, "Timer haven't been started, start and stop time set");
        }
    }

    /**
     * Get the relative duration of this event, this will subtract the duration of sub-events.
     * If the event is still active, the duration up till this point in time is returned, else
     * it's the duration from {@link #start()} till {@link #stop()}'
     * @return The relative duration of this event
     */
    public long getDuration() {
        long delta = 0;
        for (PageEvent e : getSubEventsRecursive()) {
            delta += e.getDuration();
        }
        return getDurationAbsolute() - delta;
    }

    /**
     * Get the absolute duration of this event, this does not subtract duration of sub-events.
     * If the event is still active, the duration up till this point in time is returned, else
     * it's the duration from {@link #start()} till {@link #stop()}'
     * @return The absolute duration of this event
     */
    public long getDurationAbsolute() {
        return (isActive() ? mClock.now() : mStop) - mStart;
    }

    /**
     * @return {@code true} if the event has been started, but not yet stopped, else {@code false}
     */
    public boolean isActive() {
        return isStarted() && !isStopped();
    }

    /**
     * @return {@code true} if the event has been started, else {@code false}
     */
    public boolean isStarted() {
        return mStart > 0;
    }

    /**
     * @return {@code true} if the event has been stopped, else {@code false}
     */
    public boolean isStopped() {
        return mStop > 0;
    }

    public void reset() {
        mStart = 0;
        mStop = 0;
        for (PageEvent e : mSubEvents) {
            e.reset();
        }
        mSubEvents.clear();
    }

    public EventType getType() {
        return mEventType;
    }

    public String getViewSession() {
        return mViewSession;
    }

    public int[] getPages() {
        return mPages;
    }

    public long getStart() {
        return mStart;
    }

    public Clock getClock() {
        return mClock;
    }

    private Orientation getOrientation() {
        return mOrientation;
    }

    public boolean isCollected() {
        return mCollected;
    }

    public void setCollected(boolean collected) {
        mCollected = collected;
    }

    public void addSubEvent(PageEvent e) {
        mSubEvents.add(e);
    }

    public List<PageEvent> getSubEvents() {
        return mSubEvents;
    }

    public List<PageEvent> getSubEventsRecursive() {
        if (mSubEvents.isEmpty()) {
            return mSubEvents;
        }
        ArrayList<PageEvent> tmp = new ArrayList<PageEvent>(mSubEvents);
        for (PageEvent e : mSubEvents) {
            tmp.addAll(e.getSubEventsRecursive());
        }
        return tmp;
    }

    @Override
    public String toString() {
        JSONObject o = toJSON();
        try {
            o.put("start", mStart);
            o.put("stop", mStop);
            o.put("clock", mClock.getClass().getSimpleName());
            o.put("subevent.count", mSubEvents.size());
            return o.toString();
        } catch (JSONException e) {
            SgnLog.d(TAG, e.getMessage(), e);
        }
        return "";
    }

    public JSONObject toJSON() {
        try {
            JSONObject o = new JSONObject();
            o.put("type", mEventType.toString());
            o.put("ms", getDuration());
            o.put("orientation", mOrientation.toString());
            o.put("pages", PageflipUtils.join(",", mPages));
            o.put("view_session", mViewSession);
            return o;
        } catch (JSONException e) {
            SgnLog.d(TAG, e.getMessage(), e);
        }
        return new JSONObject();
    }

}
