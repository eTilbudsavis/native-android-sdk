package com.eTilbudsavis.etasdk.DataObserver;

import android.database.Observable;

import com.eTilbudsavis.etasdk.Log.EtaLog;

public class DataObservable extends Observable<DataObserver> {
	
	public static final String TAG = DataObservable.class.getSimpleName();
	
	/**
     * Invokes {@link DataObservable#onChanged} on each observer.
     * Called when the contents of the data set have changed.  The recipient
     * will obtain the new contents the next time it queries the data set.
     */
    public void notifyChanged() {
        synchronized(mObservers) {
            // since onChanged() is implemented by the app, it could do anything, including
            // removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }
    }
    
    @Override
    public void unregisterObserver(DataObserver observer) {
    	try {
        	super.unregisterObserver(observer);
    	} catch (Exception e) {
    		EtaLog.e(TAG, "Observer not registered", e);
    	}
    }
    
}
