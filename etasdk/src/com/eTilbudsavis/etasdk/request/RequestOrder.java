package com.eTilbudsavis.etasdk.request;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import android.text.TextUtils;

public abstract class RequestOrder implements IRequestParameter {

	private static final String DELIMITER = ",";
	
	public static final String ORDER_BY = "order_by";
	public static final String POPULARITY = "popularity";
	public static final String DISTANCE = "distance";
	public static final String NAME = "name";
	public static final String PUBLICATION_DATE = "publication_date";
	public static final String EXPIRATION_DATE = "expiration_date";
	public static final String CREATED = "created";
	public static final String DEALER = "dealer";
	public static final String PAGE = "page";
	public static final String SCORE = "score";
	
	private String mDefault = null;
	
	private LinkedHashSet<String> mOrder = new LinkedHashSet<String>();
	
	public RequestOrder(String defaultOrder) {
		mDefault = defaultOrder;
	}
	
	protected boolean set(LinkedHashSet<String> orders) {
		return mOrder.addAll(orders);
	}
	
	protected boolean set(String order) {
		mOrder.add(order);
		return true;
	}
	
	protected boolean setOrder(boolean orderBy, boolean descending, String order) {
		// TODO this probably won't cut it, please remember to fix the descending filter
		if (orderBy) {
			return set((descending ? "-" : "") + order);
		} else {
			return remove(order);
		}
	}
	
	public boolean remove(String id) {
		return mOrder.remove(id);
	}
	
	public Map<String, String> getParameter() {
		// Default a default list order if one was given
		Map<String, String> map = new HashMap<String, String>();
		if (!mOrder.isEmpty()) {
			map.put(ORDER_BY, TextUtils.join(DELIMITER, mOrder));
		} else if (mDefault != null) {
			map.put(ORDER_BY, mDefault);
		}
		return map;
	}
	
}
