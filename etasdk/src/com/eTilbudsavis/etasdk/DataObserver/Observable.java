package com.eTilbudsavis.etasdk.DataObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.eTilbudsavis.etasdk.Log.EtaLog;

public class Observable<T> {
	
	public static final String TAG = Observable.class.getSimpleName();
	
	protected final List<T> mObservers = Collections.synchronizedList(new ArrayList<T>());
	
    /**
     * Adds an observer to the list. The observer cannot be null and it must not already
     * be registered.
     * @param observer the observer to register
     * @throws IllegalArgumentException the observer is null
     * @throws IllegalStateException the observer is already registered
     */
    public void registerObserver(T observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized(mObservers) {
            if (mObservers.contains(observer)) {
            	EtaLog.w(TAG, "Observer " + observer + " is already registered. Ignoring.");
//                throw new IllegalStateException("Observer " + observer + " is already registered.");
            } else {
                mObservers.add(observer);
            }
        }
        
    }

    /**
     * Removes a previously registered observer. The observer must not be null and it
     * must already have been registered.
     * @param observer the observer to unregister
     * @throws IllegalArgumentException the observer is null
     * @throws IllegalStateException the observer is not yet registered
     */
    public void unregisterObserver(T observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized(mObservers) {
            if (!mObservers.contains(observer)) {
            	EtaLog.w(TAG, "Observer " + observer + " is already registered. Ignoring.");
//                throw new IllegalStateException("Observer " + observer + " was not registered.");
            } else {
                mObservers.remove(observer);
            }
        }
    }
    
    /**
     * Remove all registered observers.
     */
    public void unregisterAll() {
        synchronized(mObservers) {
            mObservers.clear();
        }
    }
    
}
