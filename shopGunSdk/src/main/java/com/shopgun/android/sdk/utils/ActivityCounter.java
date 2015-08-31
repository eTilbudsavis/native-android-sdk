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

package com.shopgun.android.sdk.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class created for easily counting number of start/stop events from activities
 *
 * @author Danny Hvam - danny@eTilbudsavis.dk
 */
public class ActivityCounter {

    private final AtomicInteger mCounter = new AtomicInteger(0);
    private boolean mAwaitingTermination = false;

    /**
     * Reset the counter.
     *
     * @return Current count
     */
    public int reset() {
        mCounter.set(0);
        return 0;
    }

    /**
     * Increments the counter by one. And returns <code>true</code> if this event was the start event of this counter.
     *
     * @return <code>true</code> if this was the start event, else <code>false</code>
     */
    public boolean increment() {
        return mCounter.getAndIncrement() == 0;
    }

    /**
     * Decrements the counter by one. And returns <code>true</code> if this event was the stop event of this counter.
     *
     * @return <code>true</code> if this was the stop event, else <code>false</code>
     */
    public boolean decrement() {
        return mCounter.decrementAndGet() == 0;
    }

    /**
     * Test if the counter is started
     *
     * @return <code>true</code> if <code>counter > 0</code> else <code>false</code>
     */
    public boolean isStarted() {
        return mCounter.get() > 0;
    }

    public void setAwaitingTermination(boolean waiting) {
        mAwaitingTermination = waiting;
    }

    public boolean isAwaitingTermination() {
        return mAwaitingTermination;
    }

    public boolean shouldPerformStart() {
        return isStarted() && !mAwaitingTermination;
    }

}
