package com.eTilbudsavis.etasdk.NetworkHelpers;

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

import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache.Item;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Param;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public abstract class JsonRequest<T> extends Request<T> {
	
	/** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", DEFAULT_PARAMS_ENCODING);

    private String mRequestBody;
    
    private Priority mPriority = Priority.MEDIUM;

	// Define catchable types
	private static Map<String, String> types = new HashMap<String, String>();
	
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
    

	protected void putJsonArray(JSONArray a) {
		
		LinkedList<String> ernlist = new LinkedList<String>();
		try {
			
			for (int i = 0; i < a.length() ; i++) {
				Object o = a.get(i);
				if (o instanceof JSONObject) {
					String ern = putJsonObject((JSONObject)o);
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
		
		mCache.put(Utils.buildQueryString(this), new Cache.Item(ernlist, getCacheTTL()));
		
	}
	
	protected String putJsonObject(JSONObject o) {
		
		try {
			if (o.has(EtaObject.ServerKey.ERN)) {
				String ern = o.getString(EtaObject.ServerKey.ERN);
				Cache.Item i = new Item(o, getCacheTTL());
				mCache.put(ern, i);
				return ern;
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return null;
	}
	
	protected Set<String> getFilter(String filterName, Bundle apiParams) {
		
		synchronized (types) {
			if (types.size() == 0) {
				types.put("catalogs", Param.FILTER_CATALOG_IDS);
				types.put("offers", Param.FILTER_OFFER_IDS);
				types.put("dealers", Param.FILTER_DEALER_IDS);
				types.put("stores", Param.FILTER_STORE_IDS);			    
			}
		}
		
		String tmp = apiParams.getString(filterName);
		Set<String> list = new HashSet<String>();
		Collections.addAll(list, TextUtils.split(tmp, ","));
		return list;
	}
	
	protected String buildErn(String type, String id) {
		if (ernTypes.contains(type)) {
			type = type.substring(0, type.length()-1);
			return String.format("ern:%s:%s", type, id);
		}
		return type;
	}
	
	
}
