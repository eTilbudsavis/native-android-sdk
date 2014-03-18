package com.eTilbudsavis.etasdk.NetworkHelpers;


public class ParseError extends EtaError {
	
	private static final long serialVersionUID = 1L;
	
	public ParseError(Exception e, Class<?> c) {
		super(e, Code.PARSE_ERROR, 
				"Unable to parse API response into " + c.getSimpleName(),
				"The data structure returned from endpoint, does not matches the Request-type");
	}
	
}
