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

package com.shopgun.android.sdk.bus;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;

import java.lang.reflect.Field;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class SgnBus {

    public static final String TAG = Constants.getTag(SgnBus.class);

    private static final WeakEventBus BUS = new WeakEventBus();

    private SgnBus() {
        // empty
    }

    public static EventBus getInstance() {
        return BUS;
    }

    public static class WeakEventBus extends EventBus {

        private static final boolean DEBUG = false;

        private WeakEventBus() {
            enableLogging(false);
        }

        public void enableLogging(boolean enable) {
            setBooleanField("logSubscriberExceptions", enable);
            setBooleanField("logNoSubscriberMessages", enable);
            setBooleanField("sendSubscriberExceptionEvent", enable);
            setBooleanField("sendNoSubscriberEvent", enable);
        }

        private void setBooleanField(String field, boolean value) {
            try {
                Field f = getClass().getSuperclass().getDeclaredField(field);
                f.setAccessible(true);
                f.set(this, value);
            } catch (Exception ex) {
                SgnLog.d(TAG, ex.getMessage(), ex);
            }
        }

        @Override
        public void register(Object subscriber) {
            try {
                super.register(subscriber);
            } catch (EventBusException e) {
                log(e);
            }
        }

        @Override
        public void registerSticky(Object subscriber) {
            try {
                super.registerSticky(subscriber);
            } catch (EventBusException e) {
                log(e);
            }
        }

        @Override
        public void register(Object subscriber, int priority) {
            try {
                super.register(subscriber, priority);
            } catch (EventBusException e) {
                log(e);
            }
        }

        @Override
        public void registerSticky(Object subscriber, int priority) {
            try {
                super.registerSticky(subscriber, priority);
            } catch (EventBusException e) {
                log(e);
            }
        }

        @Override
        public synchronized void unregister(Object subscriber) {
            try {
                super.unregister(subscriber);
            } catch (EventBusException e) {
                log(e);
            }
        }

        private static final String MSG_FORMAT = "An %s was caught in %s, and suppressed.";
        private void log(Exception ex) {
            SgnLog.e(TAG, String.format(MSG_FORMAT, ex.getClass().getSimpleName(), TAG), ex);
        }

    }

}
