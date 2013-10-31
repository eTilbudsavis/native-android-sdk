package com.eTilbudsavis.etasdk.NetworkHelpers;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;

public class SessionError extends EtaError {

	public SessionError(NetworkResponse r) {
		super(r);
	}
	
	public SessionError(Throwable t) {
		super(t);
	}
	
}
