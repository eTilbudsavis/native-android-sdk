package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
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
    		putJSON(jArray);
    		
            return Response.fromSuccess(jArray, getCache());
        } catch (Exception e) {
        	EtaLog.d(TAG, e);
        	
            return Response.fromError(new ParseError(this, response));
        }
	}
	
	@Override
	public Response<JSONArray> parseCache(Cache c) {
		return getJSONArray(c);
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
