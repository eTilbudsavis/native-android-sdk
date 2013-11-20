package com.eTilbudsavis.etasdk.NetworkHelpers;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;

public class ParseError extends EtaError {

	public ParseError(NetworkResponse r) {
		super(r);
	}

	public ParseError(Throwable t) {
		super(t);
	}
	
}
