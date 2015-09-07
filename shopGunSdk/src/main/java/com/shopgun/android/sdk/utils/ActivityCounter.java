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

import android.os.Handler;
import android.os.Looper;

import com.shopgun.android.sdk.Constants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class created for easily counting number of start/stop events from activities
 *
 */
public class ActivityCounter implements Runnable {

    public static final String TAG = Constants.getTag(ActivityCounter.class);

    public static final int DELAY = 1000;

    private final Handler mHandler;
    private final int mDelay;
    private final AtomicInteger mCounter = new AtomicInteger(0);
    private final OnLifecycleEvent mListener;
    private boolean mAwaitingTermination = false;
    private final Object LOCK = new Object();

    public ActivityCounter(OnLifecycleEvent listener) {
        this(listener, DELAY, new Handler(Looper.getMainLooper()));
    }

    public ActivityCounter(OnLifecycleEvent listener, int delay) {
        this(listener, delay, new Handler(Looper.getMainLooper()));
    }

    public ActivityCounter(OnLifecycleEvent listener, int delay, Handler handler) {
        this.mListener = listener;
        this.mDelay = delay < 0 ? 0 : delay;
        this.mHandler = handler;
    }

    /**
     * Increment counter
     */
    public void start() {
        synchronized (LOCK) {
            if (mCounter.getAndIncrement() == 0 && !mAwaitingTermination) {
                mListener.onPerformStart();
            }
            mHandler.removeCallbacks(this);
            mAwaitingTermination = false;
        }
    }

    /**
     * Decrements counter
     */
    public void stop() {
        synchronized (LOCK) {
            mHandler.removeCallbacks(this);
            if (mCounter.decrementAndGet() == 0 ) {
                mAwaitingTermination = true;
                if (mDelay <= 0) {
                    run();
                } else {
                    mHandler.postDelayed(this, mDelay);
                }
            }
        }
    }

    /**
     * Test if there is currently an active activity.
     * @return <code>true</code> if SDK is started else <code>false</code>
     */
    public boolean isStarted() {
        synchronized (LOCK) {
            return mCounter.get() > 0;
        }
    }

    public void run() {
        synchronized (LOCK) {
            mHandler.removeCallbacks(this);
            mAwaitingTermination = false;
            mCounter.set(0);
            mListener.onPerformStop();
        }
    }

    public interface OnLifecycleEvent {
        void onPerformStart();
        void onPerformStop();
    }

}
