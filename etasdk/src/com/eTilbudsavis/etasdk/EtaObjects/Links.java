package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.Utils;

public class Links extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Links";

	private String mWebshop;
	
	public Links() { }
	
	public static Links fromJSON(String links) {
		Links l = new Links();
		try {
			l = fromJSON(l, new JSONObject(links));
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		return l;
	}
	
	@SuppressWarnings("unchecked")
	public static Links fromJSON(JSONObject links) {
		return fromJSON(new Links(), links);
	}
	
	public static Links fromJSON(Links l, JSONObject links) {
		if (l == null) l = new Links();
		if (links == null) return l;
		
		l.setWebshop(getJsonString(links, S_WEBSHOP));
		
		return l;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Links l) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_WEBSHOP, l.getWebshop());
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		return o;
	}
	
	public void setWebshop(String url) {
		mWebshop = url;
	}
	
	public String getWebshop() {
		return mWebshop;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Links))
			return false;

		Links l = (Links)o;
		return stringCompare(mWebshop, l.getWebshop());
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("webshop=").append(mWebshop)
		.append("]").toString();
	}
	
}
