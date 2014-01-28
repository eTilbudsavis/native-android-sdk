package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.LinkedList;

public class LinkedListFixedLength<T> extends LinkedList<T> {
	
	private static final long serialVersionUID = 7054222965424406843L;

	private Object LOCK = new Object();
	
	int mMaxSize = 0;
	
	public LinkedListFixedLength(int maxSize) {
		mMaxSize = maxSize;
	}
	
	@Override
	public boolean add(T object) {
		
		synchronized (LOCK) {
			
			if (mMaxSize == 0) {
				return false;
			}
			
			if (size() == mMaxSize) {
				remove(0);
			}
			
			return super.add(object);
		}
		
	}
	
}
