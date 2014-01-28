package com.eTilbudsavis.etasdk.NetworkHelpers;


public class SessionError extends EtaError {
	
	private static final long serialVersionUID = 1L;

	public SessionError(Throwable t) {
		super(t, Code.SDK_SESSION, "Unrecoverable session error occoured.");
	}
	
}
