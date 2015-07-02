package com.eTilbudsavis.etasdk.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class created for easily counting number of start/stop events from activities
 *
 * @author Danny Hvam - danny@eTilbudsavis.dk
 */
public class ActivityCounter {

    private final AtomicInteger mCounter = new AtomicInteger(0);

    /**
     * Reset the counter.
     * @return Current count
     */
    public int reset() {
        mCounter.set(0);
        return 0;
    }

    /**
     * Increments the counter by one. And returns <code>true</code> if this event was the start event of this counter.
     * @return <code>true</code> if this was the start event, else <code>false</code>
     */
    public boolean increment() {
        return mCounter.getAndIncrement() == 0;
    }

    /**
     * Decrements the counter by one. And returns <code>true</code> if this event was the stop event of this counter.
     * @return <code>true</code> if this was the stop event, else <code>false</code>
     */
    public boolean decrement() {
        return mCounter.decrementAndGet() == 0;
    }

    /**
     * Test if the counter is started
     * @return <code>true</code> if <code>counter > 0</code> else <code>false</code>
     */
    public boolean isStarted() {
        return mCounter.get() > 0;
    }

}
