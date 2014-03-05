package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

public abstract class EtaListObject<T> extends EtaErnObject<T> implements Comparable<T>, Serializable {
	
	public static final String TAG = "EtaListObject";

	private static final long serialVersionUID = 8166712456946780878L;
	
	/**
	 * The state an EtaListObject can be in, this is an indication of
	 * whether the item needs synchronization or not.
	 */
	public interface State {
		int TO_SYNC	= 0;
		int SYNCING	= 1;
		int SYNCED	= 2;
		int DELETE	= 4;
		int ERROR	= 5;
	}
	
	/** A string indication the first item in a list of items */
	public final static String FIRST_ITEM = "00000000-0000-0000-0000-000000000000";
	
	private int mState = State.TO_SYNC;
	
	public int getState() {
		return mState;
	}
	
	@SuppressWarnings("unchecked")
	public T setState(int state) {
		if (State.TO_SYNC <= state && state <= State.ERROR)
			mState = state;
		return (T)this;
	}
	
}
