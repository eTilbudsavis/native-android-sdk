package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collection;

public class FixedArrayList<E> extends ArrayList<E> {
	
	private static final long serialVersionUID = -2709268219112197508L;
	
	int mMaxSize = 16;
	
	public FixedArrayList(int size) { 
		mMaxSize = size <= 0 ? 1 : size; 
	}
	
	@Override
	public boolean addAll(Collection<? extends E> collection) {
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> collection) {
		return false;
	}
	
	@Override
	public boolean add(E object) {
		cleanUp();
		return super.add(object);
	}

	@Override
	public void add(int index, E object) {
		cleanUp();
		super.add(index, object);
	}
	
	private void cleanUp() {
		while (size() >= mMaxSize) {
			remove(0);
		}
	}
	
}
