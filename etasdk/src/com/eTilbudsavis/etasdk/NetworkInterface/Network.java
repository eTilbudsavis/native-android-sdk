package com.eTilbudsavis.etasdk.NetworkInterface;

import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;

/**
 * TODO: Write documentation on how this interface should be implemented
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public interface Network {
	
	/**
	 * Method performing the Request
	 * @param request to perform
	 * @return a NetworkResponse, fulfilling the Request
	 * @throws EtaError in the case of an unexpected error
	 */
	public NetworkResponse performRequest(Request<?> request) throws EtaError;
	
}
