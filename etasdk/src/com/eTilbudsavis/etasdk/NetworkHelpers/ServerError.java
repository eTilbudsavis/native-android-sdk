package com.eTilbudsavis.etasdk.NetworkHelpers;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;

public class ServerError extends EtaError {

	public ServerError(NetworkResponse r) {
		super(r);
	}

	public ServerError(Throwable t) {
		super(t);
	}
	
}
