/**
 * @fileoverview	Pageflip.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * 					Danny Hvam <danny@etilbudsavid.dk>
 * @version			0.3.0
 */
package com.etilbudsavis.etasdk;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import android.os.Bundle;

public class API implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ETA mEta;
	
	public enum AcceptType {
		XML{ public String toString() { return "application/xml, text/xml"; } },
		HTML{ public String toString() { return "text/html"; } },
		TEXT{ public String toString() { return "text/plain"; } },
		JSON{ public String toString() { return "application/json"; } }
	}
	
	public enum RequestType {
		GET, POST
	}
	
	/**
	 * Default constructor for API
	 * 
	 * @param ETA object with relevant information e.g. location
	 */
	public API(ETA eta) {
		mEta = eta;
	}
	
	// Method overloading see request() below
	public void request(String url, RequestListener requestListener) {
		request(url, requestListener, new Bundle());
	}

	// Method overloading see request() below	
	public void request(String url, RequestListener requestListener, Bundle optionalKeys) {
		request(url, requestListener, optionalKeys, API.RequestType.POST, API.AcceptType.JSON);
	}

	public void request(String url, RequestListener requestListener, Bundle optionalKeys, RequestType requestType, AcceptType dataType) {
		// Needs to be final in order to complete callbacks.
		final RequestListener r = requestListener;

		// Prepare data.
		LinkedHashMap<String, Object> mData = new LinkedHashMap<String, Object>();

		// API request?
		if (url.matches("^\\/api\\/.*")) {
			
			// Required API pairs.
			mData.put("api_key", mEta.getApiKey());
			mData.put("api_uuid", mEta.getUuid());
			mData.put("api_timestamp", Utilities.getTime());

			// Determine whether to include location info.
			if (mEta.location.useLocation() && mEta.location.isLocationSet()) {
				Bundle loc = mEta.location.getLocation();
				mData.put("api_latitude", loc.getDouble("api_latitude"));
				mData.put("api_longitude", loc.getDouble("api_longitude"));
				
				if (mEta.location.useDistance()) mData.put("api_distance", loc.getInt("api_distance"));
				
				mData.put("api_locationDetermined", loc.getInt("api_locationDetermined"));
				mData.put("api_geocoded", loc.getInt("api_geocoded"));
				
				if (loc.getInt("api_geocoded") == 0) mData.put("api_accuracy", loc.getInt("api_accuracy"));
			}

			// Determine whether to include bounds info.
			if (mEta.location.isBoundsSet()) {
				Bundle bounds = mEta.location.getBounds();
				mData.put("api_boundsNorth", bounds.getDouble("api_boundsNorth"));
				mData.put("api_boundsEast", bounds.getDouble("api_boundsNorth"));
				mData.put("api_boundsSouth", bounds.getDouble("api_boundsNorth"));
				mData.put("api_boundsWest", bounds.getDouble("api_boundsNorth"));
			}
			
			// Add optional data
			Set<String> ks = optionalKeys.keySet();
			Iterator<String> iterator = ks.iterator();
			while (iterator.hasNext()) {
				String s = iterator.next();
				mData.put(s, optionalKeys.get(s));
			}
			
			// Build checksum.
			mData.put("api_checksum", Utilities.buildChecksum(mData, mEta.getApiSecret()));
		}
		
		// Prefix URL?
		if (!url.matches("^http.*")) url = mEta.getMainUrl() + url;
		
		// Build query string.
		String query = mData.isEmpty() ? "" : Utilities.buildParams(mData);

		// create a new HttpHelper
		HttpHelper httpHelper = new HttpHelper(url, query, requestType, dataType, r);
		
		// Execute the AsyncTask in HttpHelper
		// This makes connection in a new thread
		httpHelper.execute();
	}

    /**
     * Callback interface for API requests.
     * Allows for callback's from asynchronous tasks, 
     * back to the UI thread.
     */
    public static interface RequestListener {
        public void onSuccess(String response, Object object);
        public void onError(String response, Object object);
    }
}