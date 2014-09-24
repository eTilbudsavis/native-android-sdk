/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.Network.Impl;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.Cache;
import com.eTilbudsavis.etasdk.Network.Cache.Item;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.RequestQueue;
import com.eTilbudsavis.etasdk.Network.Response;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.Utils.Api.Param;
import com.eTilbudsavis.etasdk.Utils.Utils;

public abstract class JsonRequest<T> extends Request<T> {

	public static final String TAG = Eta.TAG_PREFIX + JsonRequest.class.getSimpleName();
	
	/** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", DEFAULT_PARAMS_ENCODING);

    private String mRequestBody;
    
    private Priority mPriority = Priority.MEDIUM;
    
	// Define catchable types
	private static Map<String, String> mFilterTypes = new HashMap<String, String>();
	
	private static Set<String> mErnTypes = new HashSet<String>();
	
    public JsonRequest(String url, Listener<T> listener) {
		super(Method.GET, url, listener);
		
	}
    
    public JsonRequest(Method method, String url, String requestBody, Listener<T> listener) {
		super(method, url, listener);
		boolean nonBodyRequest = (method == Method.GET || method == Method.DELETE);
		if (nonBodyRequest && requestBody != null) {
			EtaLog.i(TAG, "GET and DELETE requests doesn't take a body, and will be ignored.\n"
					+ "Please append any GET and DELETE parameters to Request.putQueryParameters()");
		}
		mRequestBody = requestBody;
	}
    
    /**
     * Append single query parameter to the given request.
     * @param key - a API v2 parameter key
     * @param value - The value matching the key
     * @return this object, for easy chaining
     */
    public Request<?> putQueryParam(String key, String value) {
    	getQueryParameters().putString(key, value);
    	return this;
    }
    
    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }
    
    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(DEFAULT_PARAMS_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }
    
    public Request<T> setPriority(Priority p) {
    	mPriority = p;
    	return this;
    }

    @Override
    public Priority getPriority() {
    	return mPriority;
    }
    
    protected Response<JSONArray> getJSONArray(Cache c) {
    	
		JSONArray jArray = new JSONArray();
		// Check if we've previously done this exact call
		Cache.Item cacheList = c.get(Utils.buildQueryString(this));
		if (cacheList != null && cacheList.object instanceof LinkedList<?>) {
			
			LinkedList<?> cacheListLinkedList = (LinkedList<?>)cacheList.object;
			if (!cacheListLinkedList.isEmpty() && cacheListLinkedList.get(0) instanceof String) {
				LinkedList<String> erns = (LinkedList<String>)cacheListLinkedList;
				for (String string : erns) {
					Cache.Item jObject = c.get(string);
					if (jObject != null && jObject.object instanceof JSONObject) {
						jArray.put((JSONObject)jObject.object);
					}
				}
				
				if (jArray.length() == erns.size()) {
					return Response.fromSuccess(jArray, null);
				}
			}
			
		}
		
		// Lets try to see if it's possible to create a response from 
		// previously cached items
		Set<String> keys = getQueryParameters().keySet();
		boolean hasFilter = keys.contains(Param.FILTER_CATALOG_IDS) || 
				keys.contains(Param.FILTER_DEALER_IDS) || 
				keys.contains(Param.FILTER_OFFER_IDS) || 
				keys.contains(Param.FILTER_STORE_IDS);
		
		if (!hasFilter) {
			// Nothing to work with
			return null;
		}
		
		String[] path = getUrl().split("/");
		
		// if last element is a type, then we'll expect a list
		String type = path[path.length-1];
		
		Set<String> ids = getIdsFromFilter(type, getQueryParameters());
		
		// No ids? no catchable items...
		if (ids.size() == 0) {
			return null;
		}
		
		// Get all possible items requested from cache
		for (String id : ids) {
			String ern = buildErn(type, id);
			Cache.Item cacheId = c.get(ern);
			if (cacheId != null) {
				jArray.put((JSONObject)cacheId.object);
			}
		}
		
		// If cache had ALL items, then return the list.
		if (jArray.length() == ids.size()) {
			return Response.fromSuccess(jArray, null);
		}
		
		return null;
    }
    
    protected Response<JSONObject> getJSONObject(Cache cache) {

		String url = getUrl();
		String[] path = url.split("/");

		// Test all paths with for, to make better checks
		String id = path[path.length-1];
		String type = path[path.length-2];
		
		String ern = buildErn(type, id);
		Item ci = cache.get(ern);
		if (ci != null && ci.object instanceof JSONObject) {
			return Response.fromSuccess((JSONObject)ci.object, null);
		}
		
//		for (int i = path.length-1; i == 0 ; i-- ) {
//
//			// Test all paths with for, to make better checks
//			String id = path[i];
//			String type = path[i-1];
//			
//			String ern = buildErn(type, id);
//			Item ci = c.get(ern);
//			if (c != null && ci.object instanceof JSONObject) {
//				return Response.fromSuccess((JSONObject)ci.object, null);
//			}
//			
//		}
		
		return null;
		
    }
    
	protected void cacheJSONArray(JSONArray a) {
		
		LinkedList<String> ernlist = new LinkedList<String>();
		try {
			
			for (int i = 0; i < a.length() ; i++) {
				Object o = a.get(i);
				if (o instanceof JSONObject) {
					String ern = cacheJSONObject((JSONObject)o);
					if (ern != null) {
						ernlist.add(ern);
					}
				}
				
			}
			
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		
		if (ernlist.isEmpty()) {
			return;
		}
		
		getCache().put(Utils.buildQueryString(this), new Cache.Item(ernlist, getCacheTTL()));
		
	}
	
	protected String cacheJSONObject(JSONObject o) {
		
		try {
			
			if (o.has(Api.ServerKey.ERN)) {
				String ern = o.getString(Api.ServerKey.ERN);
				Cache.Item i = new Item(o, getCacheTTL());
				getCache().put(ern, i);
				return ern;
			}
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return null;
	}
	
	protected Map<String, String> getFilterTypes() {

		synchronized (mFilterTypes) {
			if (mFilterTypes.isEmpty()) {
				mFilterTypes.put("catalogs", Param.FILTER_CATALOG_IDS);
				mFilterTypes.put("offers", Param.FILTER_OFFER_IDS);
				mFilterTypes.put("dealers", Param.FILTER_DEALER_IDS);
				mFilterTypes.put("stores", Param.FILTER_STORE_IDS);
			}
		}
		return mFilterTypes;
		
	}
	
	protected Set<String> getErnTypes() {

		synchronized (mErnTypes) {
			if (mErnTypes.isEmpty()) {
				mErnTypes.add("catalogs");
				mErnTypes.add("offers");
				mErnTypes.add("dealers");
				mErnTypes.add("stores");
				mErnTypes.add("shoppinglists");
				mErnTypes.add("items");
			}
		}
		return mErnTypes;
	}
	
	protected Set<String> getIdsFromFilter(String filterName, Bundle apiParams) {
		
		String tmp = apiParams.getString(filterName);
		Set<String> list = new HashSet<String>();
		if (tmp != null) {
			Collections.addAll(list, TextUtils.split(tmp, ","));
		}
		return list;
	}
	
	private String buildErn(String type, String id) {
		
		if (getErnTypes().contains(type)) {
			type = type.substring(0, type.length()-1);
			return String.format("ern:%s:%s", type, id);
		}
		return type;
	}
	
	/**
	 * Returns a complete printable representation of this Request, e.g:
	 * 
	 * <li>GET: https://api.etilbudsavis.dk/v2/catalogs/{catalog_id}?param1=value1&amp;param2=value2</li>
	 * <li>PUT: https://api.etilbudsavis.dk/v2/catalogs/{catalog_id}?param1=value1&amp;param2=value2&amp;body=[json_string]</li>
	 * 
	 * <p>Body data is appended as the last query parameter for convenience, as
	 * seen in the examples above. The SDK/API parameters are not added to the 
	 * {@link Request#getQueryParameters() query parameters}, before the request
	 * is handed to the {@link RequestQueue}. So if you want to have the SDK/API
	 * parameters appended as well in the string do:</p>
	 * <li>Eta.getInstance().add(Request)</li>
	 * <p>and then call: </p>
	 * <li>toString()</li>
	 */
	@Override
	public String toString() {
		if (mRequestBody != null) {
			return super.toString() + "&body=[" + mRequestBody + "]";
		} else {
			return super.toString();
		}
	}
	
}
