package com.eTilbudsavis.etasdk.NetworkHelpers;


public class ParseError extends EtaError {
	
	private static final long serialVersionUID = 1L;

	public ParseError(Exception e) {
		super(e, Code.SDK_PARSE, "Unable to parse response.");
	}

}
