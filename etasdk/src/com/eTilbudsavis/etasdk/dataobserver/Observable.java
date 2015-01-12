package com.eTilbudsavis.etasdk.dataobserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;

public class Observable<T> {
	
	public static final String TAG = Eta.TAG_PREFIX + Observable.class.getSimpleName();
	
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
            } else {
                mObservers.add(observer);
            }
        }
        
    }

    /**
     * Removes a previously registered observer. The observer must not be null and it
     * must already have been registered.
     * @param observer the observer to unregister
     * @throws IllegalArgumentException the observer is  null
     * @throws IllegalStateException the observer is not yet registered
     */
    public void unregisterObserver(T observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized(mObservers) {
            if (!mObservers.contains(observer)) {
            	EtaLog.w(TAG, "Observer " + observer + " not registered.");
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
