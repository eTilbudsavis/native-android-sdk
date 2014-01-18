package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache.Item;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Param;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class JsonArrayRequest extends JsonRequest<JSONArray> {
	
	/**  
	 * The default limit for API calls.<br>
	 * By using this limit, queries are more likely to hit a cache on the server, hence making queries faster */
    public static final int DEFAULT_LIMIT = 25;
    
    private static final long CACHE_TTL = 3 * Utils.MINUTE_IN_MILLIS;
    
	public JsonArrayRequest(String url, Listener<JSONArray> listener) {
		super(Method.GET, url, null, listener);
	}

	public JsonArrayRequest(int method, String url, Listener<JSONArray> listener) {
		super(method, url, null, listener);
		
	}
	
	public JsonArrayRequest(int method, String url, JSONArray requestBody, Listener<JSONArray> listener) {
		super(method, url, requestBody == null ? null : requestBody.toString(), listener);
		
	}
	
	public JsonArrayRequest(int method, String url, JSONObject requestBody, Listener<JSONArray> listener) {
		super(method, url, requestBody == null ? null : requestBody.toString(), listener);
		
	}
	
	@Override
	protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
		
		try {
            String jsonString = new String(response.data);
    		JSONArray jArray = new JSONArray(jsonString);
    		putJsonArray(jArray);
    		
            return Response.fromSuccess(jArray, null, false);
        } catch (Exception e) {
            return Response.fromError(new ParseError(this, response));
        }
	}
	
	@Override
	public Response<JSONArray> parseCache(Cache c) {

		// if last element is a type, then we'll expect a list
		String type = path[path.length-1];
		
		Set<String> keys = getQueryParameters().keySet();
		
		boolean hasFilter = keys.contains(Param.FILTER_CATALOG_IDS) || 
				keys.contains(Param.FILTER_DEALER_IDS) || 
				keys.contains(Param.FILTER_OFFER_IDS) || 
				keys.contains(Param.FILTER_STORE_IDS);
		
		boolean hasOrder = keys.contains(Param.ORDER_BY);
		
		Set<String> ids = new HashSet<String>(0);
		String filter = getFilter(type, getQueryParameters());
		if (apiParams.containsKey(filter)) {
			ids = getFilter(filter, apiParams);
		}
		
		// No ids? no catchable items...
		if (ids.size() == 0)
			return resp;
		
		// Get all possible items requested from cache
		JSONArray jArray = new JSONArray();
		for (String id : ids) {
			String ern = buildErn(type, id);
			Item c = getItem(ern);
			if (c != null) {
				jArray.put((JSONObject)c.object);
			}
		}
		
		// If cache had ALL items, then return the list.
		int size = jArray.length();
		if (size == ids.size()) {
			
//			resp.set(200, jArray.toString());
		}
		
	}
	
	@Override
	public long getCacheTTL() {
		return CACHE_TTL;
	}
	
	public Request<?> setOrderBy(String order) {
		getQueryParameters().putString(Sort.ORDER_BY, order);
		return this;
	}
	
	public Request<?> setOrderBy(List<String> order) {
		String tmp = TextUtils.join(",",order);
		getQueryParameters().putString(Sort.ORDER_BY, tmp);
		return this;
	}
	
	public String getOrderBy() {
		return getQueryParameters().getString(Sort.ORDER_BY);
	}

	public Request<?> setOffset(int offset) {
		getQueryParameters().putInt(Param.OFFSET, offset);
		return this;
	}
	
	public int getOffset() {
		return getQueryParameters().getInt(Param.OFFSET);
	}

	public Request<?> setLimit(int limit) {
		getQueryParameters().putInt(Param.LIMIT, limit);
		return this;
	}
	
	public int getLimit() {
		return getQueryParameters().getInt(Param.LIMIT);
	}
	
	/**
	 * Set a parameter for what specific id's to get from a given endpoint.<br><br>
	 * 
	 * E.g.: setIds(Catalog.PARAM_IDS, new String[]{"eecdA5g","b4Aea5h"});
	 * @param	type of the endpoint parameter e.g. Catalog.PARAM_IDS
	 * @param	ids to filter by
	 * @return	this object
	 */
	public Request<?> setIds(String type, Set<String> ids) {
		String idList = TextUtils.join(",",ids);
		getQueryParameters().putString(type, idList);
		return this;
	}
	
}
