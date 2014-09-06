package com.eTilbudsavis.etasdk.request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RequestFilter implements RequestParameter {
	
	Map<String, Set<String>> mFilters = new HashMap<String, Set<String>>();
	
	private boolean set(String filter, Set<String> ids) {
		if (mFilters.containsKey(filter) && mFilters.get(filter) != null) {
			mFilters.get(filter).addAll(ids);
		} else {
			mFilters.put(filter, ids);
		}
		return true;
	}
	
	private boolean set(String filter, String id) {
		if (mFilters.containsKey(filter) && mFilters.get(filter) != null) {
			mFilters.get(filter).add(id);
		} else {
			HashSet<String> set = new HashSet<String>();
			set.add(id);
			mFilters.put(filter, set);
		}
		return true;
	}

	public String buildParameter() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
