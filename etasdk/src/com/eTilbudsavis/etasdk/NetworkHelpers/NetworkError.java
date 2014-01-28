package com.eTilbudsavis.etasdk.NetworkHelpers;


public class NetworkError extends EtaError {
	
	private static final long serialVersionUID = 1L;
	
	public NetworkError(Throwable t) {
		super(t, Code.SDK_NETWORK, "Networking error");
	}

}
