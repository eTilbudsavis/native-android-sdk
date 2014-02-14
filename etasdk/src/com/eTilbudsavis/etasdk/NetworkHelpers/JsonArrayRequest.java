package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.util.HashMap;
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
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.Utils.Sort;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class JsonArrayRequest extends JsonRequest<JSONArray> {
	
	/**  
	 * The default limit for API calls.<br>
	 * By using this limit, queries are more likely to hit a cache on the server, hence making queries faster */
    public static final int DEFAULT_LIMIT = 24;
    
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
            
			Response<JSONArray> r = null;
            if (Utils.isSuccess(response.statusCode)) {

                r = Response.fromSuccess(new JSONArray(jsonString), getCache());
        		putJSON(r.result);
        		
            } else {
            	r = Response.fromError(EtaError.fromJSON(new JSONObject(jsonString)));
            }
            
            log(response.statusCode, response.headers, r.result, r.error);
            
            return r;
            
        } catch (Exception e) {
            return Response.fromError(new ParseError(e));
        }
	}
	
	@Override
	public Response<JSONArray> parseCache(Cache c) {
		Response<JSONArray> cache = getJSONArray(c);
		if (cache != null) {
			log(0, new HashMap<String, String>(), cache.result, cache.error);
		}
		return cache;
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
