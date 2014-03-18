package com.eTilbudsavis.etasdk.NetworkHelpers;


public class NetworkError extends EtaError {
	
	private static final long serialVersionUID = 1L;
	
	public NetworkError(Throwable t) {
		super(t, Code.NETWORK_ERROR, "Networking error", "There was an error "
				+ "establishing a connection to the API. Please check that the "
				+ "device has a working internet connection.");
	}

}
