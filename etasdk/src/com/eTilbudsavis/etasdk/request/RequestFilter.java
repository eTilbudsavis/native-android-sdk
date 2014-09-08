package com.eTilbudsavis.etasdk.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.text.TextUtils;

public abstract class RequestFilter<T extends Set<String>> implements IRequestParameter {
	
	private static final String DELIMITER = ",";
	
	Map<String, T> mFilters = new HashMap<String, T>();
	
	protected boolean add(String filter, T ids) {
		Set<String> set = mFilters.get(filter);
		if (set != null) {
			set.addAll(ids);
		} else {
			mFilters.put(filter, ids);
		}
		return true;
	}

	public boolean remove(String filter, String id) {
		if (mFilters.get(filter) != null) {
			return mFilters.get(filter).remove(id);
		}
		return false;
	}
	
	public boolean remove(String filter) {
		mFilters.remove(filter);
		return true;
	}
	
	protected Map<String, T> getFilters() {
		return mFilters;
	}
	
	public Map<String, String> getParameter() {
		Map<String, String> map = new HashMap<String, String>();
		for (String key : mFilters.keySet()) {
			map.put(key, TextUtils.join(DELIMITER, mFilters.get(key)));
		}
		return map;
	}
	
}
