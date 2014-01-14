package com.eTilbudsavis.etasdk.NetworkHelpers;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;

public class SessionError extends EtaError {
	
	private static final long serialVersionUID = 1L;

	public SessionError(Request<?> request, NetworkResponse response) {
		super(request, response);
	}
	
	public SessionError(Throwable t) {
		super(t);
	}
	
}
