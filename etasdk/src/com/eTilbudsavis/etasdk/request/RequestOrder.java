package com.eTilbudsavis.etasdk.request;

import java.util.LinkedHashSet;
import java.util.Map;

public abstract class RequestOrder extends RequestParameter<LinkedHashSet<String>> {

	/** String identifying the order by parameter for all list calls to the API */
	public static final String ORDER_BY = "order_by";

	private static final String ERROR_ONLY_FILTER = RequestOrder.class.getSimpleName() + 
			" only allow " + ORDER_BY + " filter";
	
	/** Sort a list by popularity in ascending order. (smallest to largest) */
	public static final String POPULARITY = "popularity";

	/** Sort a list by distance in ascending order. (smallest to largest) */
	public static final String DISTANCE = "distance";

	/** Sort a list by name in ascending order. (a-z) */
	public static final String NAME = "name";

	/** Sort a list by published in ascending order. (smallest to largest) */
	public static final String PUBLICATION_DATE = "publication_date";

	/** Sort a list by expired in ascending order. (smallest to largest) */
	public static final String EXPIRATION_DATE = "expiration_date";

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String CREATED = "created";

	/** Sort a list by page (in catalog) in ascending order. (smallest to largest) */
	public static final String PAGE = "page";

	/** Sort a list by it's internal score in ascending order. (smallest to largest) */
	public static final String SCORE = "score";
	
	private String mDefault = null;
	
	protected void setDefault(String defaultOrder) {
		mDefault = defaultOrder;
	}
	
	@Override
	protected boolean set(String filter, LinkedHashSet<String> ids) {
		if (ORDER_BY.equals(filter)) {
			return super.set(filter, ids);
		} else {
			throw new IllegalArgumentException(ERROR_ONLY_FILTER);
		}
	}
	
	@Override
	public boolean remove(String filter, String id) {
		if (ORDER_BY.equals(filter)) {
			return super.remove(filter, id);
		} else {
			throw new IllegalArgumentException(ERROR_ONLY_FILTER);
		}
	}
	
	@Override
	public boolean remove(String id) {
		Map<String, LinkedHashSet<String>> map = getFilter();
		if (map.get(ORDER_BY) != null) {
			return map.get(ORDER_BY).remove(id);
		}
		return false;
	}
	
	protected boolean set(LinkedHashSet<String> ids) {
		return set(ORDER_BY, ids);
	}
	
	protected boolean set(String id) {
		Map<String, LinkedHashSet<String>> map = getFilter();
		if (map.containsKey(ORDER_BY) && map.get(ORDER_BY) != null) {
			map.get(ORDER_BY).add(id);
		} else {
			LinkedHashSet<String> ids = new LinkedHashSet<String>();
			ids.add(id);
			map.put(ORDER_BY, ids);
		}
		return true;
	}

	@Override
	public Map<String, String> getParameter() {
		// Default a default list order if one was given
		Map<String, String> map = super.getParameter();
		if (map.isEmpty() && mDefault != null) {
			map.put(ORDER_BY, mDefault);
		}
		return map;
	}
}
