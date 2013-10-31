package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.util.List;
import java.util.Set;

import android.text.TextUtils;

import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;

public abstract class ListRequest extends Request<List<?>>{

	public static final String TAG = "Request";
	
	/**  The default offset for API calls  */
    public static final int DEFAULT_OFFSET = 0;

	/**  The default limit for API calls. 
	 * Note that this number is best for performance on the server. */
    public static final int DEFAULT_LIMIT = 25;
	
	public ListRequest(int method, String url, Listener<List<?>> listener) {
		super(method, url, listener);
		// TODO Auto-generated constructor stub
	}

	/**
	 * TODO: Write proper JavaDoc<br>
	 * <code>new String[] {Api.SORT_DISTANCE, Api.SORT_PUBLISHED}</code>
	 * @param order
	 * @return This {@link com.eTilbudsavis.etasdk.Api Api} object to allow for chaining of calls to set methods
	 */
	public Api setOrderBy(String order) {
		mApiParams.putString(Sort.ORDER_BY, order);
		return this;
	}
	
	public Api setOrderBy(List<String> order) {
		String tmp = TextUtils.join(",",order);
		mApiParams.putString(Sort.ORDER_BY, tmp);
		return this;
	}
	
	public String getOrderBy() {
		return mApiParams.getString(Sort.ORDER_BY);
	}

	public Api setOffset(int offset) {
		mApiParams.putInt(Param.OFFSET, offset);
		return this;
	}
	
	public int getOffset() {
		return mApiParams.getInt(Param.OFFSET);
	}

	public Api setLimit(int limit) {
		mApiParams.putInt(Param.LIMIT, limit);
		return this;
	}
	
	public int getLimit() {
		return mApiParams.getInt(Param.LIMIT);
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
		mApiParams.putString(type, idList);
		return ListRequest.this;
	}
	
}
