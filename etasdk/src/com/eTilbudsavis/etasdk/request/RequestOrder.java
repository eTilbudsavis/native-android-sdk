package com.eTilbudsavis.etasdk.request;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import android.text.TextUtils;

import com.eTilbudsavis.etasdk.Utils.Api;

public abstract class RequestOrder implements IRequestParameter {
	
	private String mDefault = null;
	
	private LinkedHashSet<String> mOrder = new LinkedHashSet<String>();
	
	public RequestOrder(String defaultOrder) {
		mDefault = defaultOrder;
	}
	
	protected boolean set(LinkedHashSet<String> orders) {
		return mOrder.addAll(orders);
	}
	
	/**
	 * Adds the order to the set. Use with caution, as this method does
	 * not remove negated values in the set.
	 * @param order the order to add
	 * @return true when this {@link RequestOrder} did not already contain the order, false otherwise
	 */
	protected boolean add(String order) {
		return mOrder.add(order);
	}
	
	/**
	 * Removed the given order (checks for both ascending, and descending order).
	 * @param order the order to remove
	 * @return true if the order was removed, otherwise false
	 */
	protected boolean remove(String order) {
		// removing the negated value of the given order
		if (order.startsWith("-")) {
			mOrder.remove(order.replaceFirst("-", ""));
		} else {
			mOrder.remove("-" + order);
		}
		return mOrder.remove(order);
	}
	
	/**
	 * Adds the given order to the set of orders, removing any order
	 * currently in the set, that may be in conflict with the new order.
	 * 
	 * @param order the string to add
	 * @param descending true if the string "-" should be prepended, indicating descending order. else false
	 * @return true when this {@link RequestOrder} did not already contain the order, false otherwise
	 */
	public boolean add(String order, boolean descending) {
		String tmp = (descending ? "-" : "") + order;
		// Performing cleanup, ensuring only ONE of each 'order' is added to the set
		if (mOrder.contains(order)) {
			mOrder.remove(order);
		}
		return add(tmp);
	}
	
	public Map<String, String> getParameter() {
		// Default a default list order if one was given
		Map<String, String> map = new HashMap<String, String>();
		if (!mOrder.isEmpty()) {
			map.put(Api.Param.ORDER_BY, TextUtils.join(Api.DELIMITER, mOrder));
		} else if (mDefault != null) {
			map.put(Api.Param.ORDER_BY, mDefault);
		}
		return map;
	}
	
}
