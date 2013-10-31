package com.eTilbudsavis.etasdk.NetworkHelpers;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;

public class AuthError extends EtaError {
	
	public AuthError(NetworkResponse r) {
		super(r);
	}
	
	public AuthError(Throwable t) {
		super(t);
	}
	
}
