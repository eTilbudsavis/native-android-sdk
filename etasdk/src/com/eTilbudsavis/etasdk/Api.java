/**
 * @fileoverview	API.
 * @author			Danny Hvam <danny@etilbudsavis.dk>
 */
package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;

import android.os.Bundle;

public class Api implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Name of latitude.
	 */
	public static final String LATITUDE = "r_lat";
	
	/**
	 * Name of longitude.
	 */
	public static final String LONGITUDE = "r_lng";
	
	/**
	 * Name of radius.
	 */
	public static final String RADIUS = "r_radius";
	
	/**
	 * Name of bounds east.
	 */
	public static final String BOUND_EAST = "b_east";
	
	/**
	 * Name of bounds north.
	 */
	public static final String BOUND_NORTH = "b_north";
	
	/**
	 * Name of bounds south.
	 */
	public static final String BOUND_SOUTH = "b_south";
	
	/**
	 * Name of bounds west.
	 */
	public static final String BOUND_WEST = "b_west";
	
	private ETA mETA;
	
	/***
	 * The type expected to return;
	 * Default is JSON, other types are not implemented yet
	 */
	public enum AcceptType {
		XML { public String toString() { return "application/xml, text/xml"; } },
		HTML { public String toString() { return "text/html"; } },
		TEXT { public String toString() { return "text/plain"; } },
		JSON { public String toString() { return "application/json"; } }
	}
	
	public enum RequestType {
		GET, POST
	}
	
	/**
	 * The content type to use in requests.
	 * This feature is not implemented yet.
	 */
	public enum ContentType {
		PLAIN { public String toString() { return "text/plain; charset=utf-8"; } },
		URLENCODED { public String toString() { return "application/x-www-form-urlencoded; charset=utf-8"; } },
		FORMDATA { public String toString() { return "multipart/form-data; charset=utf-8"; } }
	}
	
	/**
	 * Default constructor for API
	 * @param ETA object with relevant information e.g. location
	 */
	public Api(ETA eta) {
		mETA = eta;
	}
	
	/**
	 * Makes a request to the server, with the given parameters.
	 * The result will return via the RequestListener.
	 *
	 * @param url - This can be any URL, but optionalKeys are only sent if the URL points to the ETA API
	 * @param requestListener - API.RequestListener
	 */
	public HttpHelper request(String url, RequestListener requestListener) {
		return request(url, requestListener, new Bundle());
	}

	/**
	 * Makes a request to the server, with the given parameters.
	 * The result will return via the RequestListener.
	 *
	 * @param url - This can be any URL, but optionalKeys are only sent if the URL points to the ETA API
	 * @param requestListener - API.RequestListener
	 * @param optionalKeys - Bundle containing parameters specified on https://etilbudsavis.dk/developers/docs/
	 */
	public HttpHelper request(String url, RequestListener requestListener, Bundle optionalKeys) {
		return request(url, requestListener, optionalKeys, Api.RequestType.GET);
	}

	/**
	 * Make a request to the server, with the given parameters.
	 * The result will return via the RequestListener.
	 *
	 * @param url - This can be any URL, but optionalKeys are only sent if the URL points to the ETA API
	 * @param requestListener - API.RequestListener
	 * @param optionalKeys - Bundle containing parameters specified on https://etilbudsavis.dk/developers/docs/
	 * @param requestType - API.RequestType
	 */
	public HttpHelper request(String url, final RequestListener requestListener, Bundle optionalKeys, RequestType requestType) {
		
		// Prepare data.
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		// API request?
		if (url.matches("^\\/api\\/.*")) {
			
			// Required API pairs.
			Utilities.putNameValuePair(params, "api_key", mETA.getApiKey());
			Utilities.putNameValuePair(params, "api_uuid", mETA.getUUID());
			Utilities.putNameValuePair(params, "api_timestamp", Utilities.getTime());
			
			
			// Determine whether to include location info.
			if (mETA.getLocation().isLocationSet()) {
				
				Bundle loc = mETA.getLocation().getApiParams();
				Utilities.putNameValuePair(params, "api_latitude", loc.getDouble("api_latitude"));
				Utilities.putNameValuePair(params, "api_longitude", loc.getDouble("api_longitude"));
				
				// Use distance?
				if (mETA.getLocation().useDistance()) 
					Utilities.putNameValuePair(params, "api_distance", loc.getInt("api_distance"));
				
				Utilities.putNameValuePair(params, "api_locationDetermined", loc.getInt("api_locationDetermined"));
				Utilities.putNameValuePair(params, "api_geocoded", loc.getInt("api_geocoded"));
				
				// Accuracy is only to be included if the location was found using a sensor (geocoded == 0).
				if (loc.getInt("api_geocoded") == 0) 
					Utilities.putNameValuePair(params, "api_accuracy", loc.getInt("api_accuracy"));
				
			}

			// Determine whether to include bounds.
			if (mETA.getLocation().isBoundsSet()) {
				Bundle bounds = mETA.getLocation().getApiBounds();
				Utilities.putNameValuePair(params, "api_boundsNorth", bounds.getDouble("api_boundsNorth"));
				Utilities.putNameValuePair(params, "api_boundsEast", bounds.getDouble("api_boundsNorth"));
				Utilities.putNameValuePair(params, "api_boundsSouth", bounds.getDouble("api_boundsNorth"));
				Utilities.putNameValuePair(params, "api_boundsWest", bounds.getDouble("api_boundsNorth"));
				
			}
			
			// Add optional data.
			Iterator<String> iterator = optionalKeys.keySet().iterator();
			while (iterator.hasNext()) {
				String s = iterator.next();
				Utilities.putNameValuePair(params, s, optionalKeys.get(s));
			}
			
			// Build checksum.
			Utilities.putNameValuePair(params, "api_checksum", Utilities.buildChecksum(params, mETA.getApiSecret()));
			
		} else {

			// Add data to non-eta-api site.
			Iterator<String> iterator = optionalKeys.keySet().iterator();
			while (iterator.hasNext()) {
				String s = iterator.next();
				Utilities.putNameValuePair(params, s, optionalKeys.get(s));
			}
			
		}
		
		// Prefix URL?
		if (!url.matches("^http.*")) url = mETA.getMainUrl() + url;
		
		// Create a new HttpHelper.
		HttpHelper httpHelper = new HttpHelper(url, params, requestType, requestListener, mETA.getContext());
		
		// Execute the AsyncTask in HttpHelper to ensure a new thread.
		httpHelper.execute();
		
		return httpHelper;
	}

    /**
     * Callback interface for API requests, which allows for callbacks from asynchronous tasks
     * back to the UI thread.
     */
    public static interface RequestListener {
        public void onSuccess(Integer response, Object object);
        public void onError(Integer response, Object object);
    }
}