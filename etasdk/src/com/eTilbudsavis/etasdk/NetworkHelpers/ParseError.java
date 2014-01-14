package com.eTilbudsavis.etasdk.NetworkHelpers;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;

public class ParseError extends EtaError {
	
	private static final long serialVersionUID = 1L;

	public ParseError(Request<?> request, NetworkResponse response) {
		super(request, response);
	}

	public ParseError(Throwable t) {
		super(t);
	}
	
}
