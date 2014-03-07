package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache.Item;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.Utils.Utils;

public abstract class JsonRequest<T> extends Request<T> {
	
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
    
    public JsonRequest(int method, String url, String requestBody, Listener<T> listener) {
		super(method, url, listener);
		boolean nonBodyRequest = (method == Method.GET || method == Method.DELETE);
		if (nonBodyRequest && requestBody != null) {
			EtaLog.d(TAG, "GET and DELETE requests doesn't take a body, and will be ignored.\n"
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
    
	protected void putJSON(JSONArray a) {
		
		LinkedList<String> ernlist = new LinkedList<String>();
		try {
			
			for (int i = 0; i < a.length() ; i++) {
				Object o = a.get(i);
				if (o instanceof JSONObject) {
					String ern = putJSON((JSONObject)o);
					if (ern != null) {
						ernlist.add(ern);
					}
				}
				
			}
			
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
		if (ernlist.isEmpty()) {
			return;
		}
		
		getCache().put(Utils.buildQueryString(this), new Cache.Item(ernlist, getCacheTTL()));
		
	}
	
	protected String putJSON(JSONObject o) {
		
		try {
			if (o.has(EtaObject.ServerKey.ERN)) {
				String ern = o.getString(EtaObject.ServerKey.ERN);
				Cache.Item i = new Item(o, getCacheTTL());
				getCache().put(ern, i);
				
				return ern;
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
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
	

	protected void log(int statusCode, Map<String, String> headers, JSONObject jObject, EtaError error) {

		String data = null;
		
		if (jObject != null) {
			
			JSONArray tmp = new JSONArray();
			
			if (jObject.has(Param.ERN)) {
				
				try {
					tmp.put(jObject.getString(Param.ERN));
				} catch (JSONException e) {
					EtaLog.d(TAG, e);
				}
				
			} else {
				data = jObject.toString();
			}
			
			data = tmp.toString();
			
		}
		
		setLogDebug(statusCode, headers, data, error);
		
	}
	
	protected void log(int statusCode, Map<String, String> headers, JSONArray jArray, EtaError error) {
		
		String data = null;
		
		if (jArray != null) {
			
			try {
				
				if (0 < jArray.length() && jArray.get(0) instanceof JSONObject && jArray.getJSONObject(0).has(Param.ERN) ) {
					
					JSONArray tmp = new JSONArray();
					for (int i = 0 ; i < jArray.length() ; i++ ) {
						JSONObject o = jArray.getJSONObject(i);
						if (o.has(Param.ERN)) {
							tmp.put(o.getString(Param.ERN));
						} else {
							tmp.put("non-ern-object-in-list");
						}
					}
					data = tmp.toString();
					
				} else {
					data = jArray.toString();
				}
				
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
			
		}
		
		setLogDebug(statusCode, headers, data, error);
		
	}
	
	private void setLogDebug(int statusCode, Map<String, String> headers, String successData, EtaError error) {
		
		try {
			
			// Server Response
			JSONObject response = new JSONObject();
			response.put("statuscode", statusCode);
			response.put("success", successData);
			response.put("error", error == null ? null : error.toJSON());
			response.put("headers", headers == null ? new JSONObject() : new JSONObject(headers));
			
			// Client request
			JSONObject request = new JSONObject();
			request.put("method", getMethodString());
			request.put("url", Utils.buildQueryString(this));
			request.put(HTTP.CONTENT_TYPE, getBodyContentType());
			request.put("headers", new JSONObject(getHeaders()));
			request.put("time", Utils.parseDate(new Date()));
			request.put("response", response);
			request.put("body", mRequestBody);
			
			getLog().setSummary(request);
			
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
	}
	
}
