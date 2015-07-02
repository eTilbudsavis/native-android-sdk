package com.eTilbudsavis.etasdk.bus;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;

import java.lang.reflect.Field;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class Bus {

    public static final String TAG = Constants.getTag(Bus.class);

    private static final WeakEventBus BUS = new WeakEventBus();

    private Bus() {
        // empty
    }

    public static EventBus getInstance() {
        return BUS;
    }

    public static class WeakEventBus extends EventBus {

        private boolean mLogcatEnabled = false;

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
                EtaLog.d(TAG, ex.getMessage(), ex);
            }
        }

        public void setLogcatEnabled(boolean enableLogcat) {
            mLogcatEnabled = enableLogcat;
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

        private void log(Exception ex) {
            if (mLogcatEnabled) {
                EtaLog.e(TAG, ex.getMessage(), ex);
            }
        }

    }

}
