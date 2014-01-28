package com.eTilbudsavis.etasdk.NetworkHelpers;


public class ApiError extends EtaError {
	
	private static final long serialVersionUID = 1L;
	
	public ApiError (int code, String message, String id, String details, String failedOnField) {
		super(code, message, id, details, failedOnField);
	}
	
}
