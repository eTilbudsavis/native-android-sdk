package com.eTilbudsavis.etasdk.NetworkInterface;

import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;

public interface Network {
	
	public NetworkResponse performRequest(Request<?> request) throws EtaError;
	
}
