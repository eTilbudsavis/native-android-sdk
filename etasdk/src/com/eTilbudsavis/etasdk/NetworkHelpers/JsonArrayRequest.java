package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import android.text.TextUtils;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;

public class JsonArrayRequest extends JsonRequest<JSONArray> {

	/**  The default offset for API calls  */
    public static final int DEFAULT_OFFSET = 0;

	/**  The default limit for API calls. 
	 * Note that this number is best for performance on the server. */
    public static final int DEFAULT_LIMIT = 25;
    
	public <T> Request<T> setOrderBy(String order) {
		getQueryParameters().putString(Sort.ORDER_BY, order);
		return (Request<T>) this;
	}
	
	public Request setOrderBy(List<String> order) {
		String tmp = TextUtils.join(",",order);
		getQueryParameters().putString(Sort.ORDER_BY, tmp);
		return this;
	}
	
	public String getOrderBy() {
		return getQueryParameters().getString(Sort.ORDER_BY);
	}

	public Request setOffset(int offset) {
		getQueryParameters().putInt(Param.OFFSET, offset);
		return this;
	}
	
	public int getOffset() {
		return getQueryParameters().getInt(Param.OFFSET);
	}

	public Request setLimit(int limit) {
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
	public Request getIds(String type, Set<String> ids) {
		String idList = TextUtils.join(",",ids);
		getQueryParameters().putString(type, idList);
		return this;
	}
	
	public JsonArrayRequest(int method, String url, JSONArray requestBody, Listener<JSONArray> listener) {
		super(method, url, requestBody == null ? null : requestBody.toString(), listener);
		
	}
	
	@Override
	protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
		try {
            String jsonString = new String(response.data);
            return Response.fromSuccess(new JSONArray(jsonString), null, false);
        } catch (Exception e) {
            return Response.fromError(new ParseError(response));
        }
	}

	@Override
	protected void deliverResponse(JSONArray response) {
		// TODO Auto-generated method stub
		
	}
	
	
}
