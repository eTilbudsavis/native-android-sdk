package com.eTilbudsavis.etasdk.request;

import java.util.HashMap;
import java.util.Map;

public abstract class RequestParameter implements IRequestParameter {
	
	private Map<String, String> mParam = new HashMap<String, String>();
	
	protected boolean put(String parameter, String value) {
		mParam.put(parameter, value);
		return true;
	}
	
	public boolean remove(String filter) {
		mParam.remove(filter);
		return true;
	}
	
	protected Map<String, String> getFilter() {
		return mParam;
	}
	
	public Map<String, String> getParameter() {
		return mParam;
	}
	
}
