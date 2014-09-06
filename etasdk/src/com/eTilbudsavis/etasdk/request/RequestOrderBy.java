package com.eTilbudsavis.etasdk.request;

import java.util.LinkedHashSet;

import android.text.TextUtils;

public class RequestOrderBy {
	
	LinkedHashSet<String> mOrderBy;
	
	public boolean set(String order) {
		mOrderBy.add(order);
		return true;
	}
	
	public String build() {
		return TextUtils.join(",", mOrderBy);
	}
}
