package com.eTilbudsavis.etasdk.request;

import java.util.HashMap;
import java.util.Map;

public abstract class ParameterBuilder implements RequestParameter {
	
	private Map<String, String> mParam = new HashMap<String, String>();
	
	public ParameterBuilder() {
		
	}
	
	public ParameterBuilder(Map<String, String> parameters) {
		mParam.putAll(parameters);
	}
	
	public ParameterBuilder(RequestParameter parameters) {
		mParam.putAll(parameters.getParameters());
	}

	protected void put(Map<String, String> parameters) {
		mParam.putAll(parameters);
	}
	
	protected String put(String parameter, String value) {
		return mParam.put(parameter, value);
	}
	
	protected void clear() {
		mParam.clear();
	}
	
	protected String remove(String parameter) {
		return mParam.remove(parameter);
	}
	
	public Map<String, String> getParameters() {
		return mParam;
	}
	
}
