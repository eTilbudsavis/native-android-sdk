package com.eTilbudsavis.etasdk.Database;


public abstract class DbQuery<T> implements Comparable<DbQuery<T>> {

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called when a response is received. */
        public void onResponse(T response);
    }

	String mQuery;
	boolean mChanges = false;
	T mResult;
	int mSequence;
	
	public void setQuery(String query) {
		mQuery = query;
	}
	
	public String getQuery() {
		return mQuery;
	}
	
	public void setSequence(int seq) {
		mSequence = seq;
	}
	
	public void setChanges(boolean getChangesInDb) {
		mChanges = getChangesInDb;
	}
	
	public boolean getChangesInDb() {
		return mChanges;
	}
	
	public void setResult(T result) {
		mResult = result;
	}
	
	public T getResult() {
		return mResult;
	}
	
	public abstract void run();

    /**
     * Our comparator sorts from high to low priority, and secondarily by
     * sequence number to provide FIFO ordering.
     */
    public int compareTo(DbQuery<T> other) {
    	
        // High-priority requests are "lesser" so they are sorted to the front.
        // Equal priorities are sorted by sequence number to provide FIFO ordering.
        return this.mSequence - other.mSequence;
    }

	public void deliverResponse() {
		// TODO Auto-generated method stub
		
	}

}
