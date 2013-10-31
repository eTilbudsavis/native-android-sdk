package com.eTilbudsavis.etasdk.NetworkInterface;

import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkHelpers.SessionError;

public interface Network {
	
	public NetworkResponse performRequest(Request<?> request) throws EtaError;
	
}
