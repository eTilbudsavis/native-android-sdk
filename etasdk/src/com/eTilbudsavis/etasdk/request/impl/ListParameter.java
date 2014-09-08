package com.eTilbudsavis.etasdk.request.impl;

import com.eTilbudsavis.etasdk.request.RequestParameter;

public class ListParameter extends RequestParameter {

	/** String identifying the offset parameter for all list calls to the API */
	public static final String OFFSET = "offset";
	
	/** String identifying the limit parameter for all list calls to the API */
	public static final String LIMIT = "limit";
	
	/**
	 * The API relies on pagination for retrieving data. Therefore you need to
	 * define the offset to the first item in the requested list, when querying for data.
	 * If no offset is set it will default to 0.
	 * @param offset to first item in list
	 * @return this object
	 */
	public ListParameter setOffset(int offset) {
		put(OFFSET, String.valueOf(offset));
		return this;
	}
	
	/**
	 * Get the offset parameter used for the query.
	 * @return offset
	 */
	public int getOffset() {
		String offset = getFilter().get(OFFSET);
		if (offset != null) {
			return Integer.valueOf(offset);
		}
		return 0;
	}
	
	/**
	 * The API relies on pagination for retrieving data. Therefore you need to
	 * define a limit for the data you want to retrieve. If no limit is set
	 * this will default to {@link #DEFAULT_LIMIT DEFAULT_LIMIT} if no limit is set.
	 * @param limit
	 * @return
	 */
	public ListParameter setLimit(int limit) {
		put(LIMIT, String.valueOf(limit));
		return this;
	}
	
	/**
	 * Get the upper limit on how many items the API should return.
	 * @return max number of items API should return
	 */
	public int getLimit() {
		String offset = getFilter().get(LIMIT);
		if (offset != null) {
			return Integer.valueOf(offset);
		}
		return 0;
	}
	
}
