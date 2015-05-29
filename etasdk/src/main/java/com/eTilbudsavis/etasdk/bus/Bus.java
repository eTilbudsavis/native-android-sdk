package com.eTilbudsavis.etasdk.bus;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class Bus {

    private static final WeakEventBus BUS = new WeakEventBus();

    public static EventBus getInstance() {
        return BUS;
    }

    private Bus() {
        // empty
    }

    public static class WeakEventBus extends EventBus {

        private WeakEventBus() {

        }

        @Override
        public void register(Object subscriber) {
            try {
                super.register(subscriber);
            } catch (EventBusException e) {
                // we're already registered
            }
        }

        @Override
        public void register(Object subscriber, int priority) {
            try {
                super.register(subscriber, priority);
            } catch (EventBusException e) {
                // we're already registered
            }
        }

        @Override
        public synchronized void unregister(Object subscriber) {
            try {
                super.unregister(subscriber);
            } catch (EventBusException e) {
                // unregister shouldn't throw exceptions
            }
        }

    }

}
