package com.eTilbudsavis.etasdk.NetworkInterface;

public interface Network {
	
	public NetworkResponse performRequest(Request<?> request);
	
}
