package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Links implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Links";
	
	public static final String S_WEBSHOP = "webshop";
	
	private String mWebshop;
	
	public Links() {
	}
	
	public static Links fromJSON(String links) {
		try {
			return fromJSON(new Links(), new JSONObject(links));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Links fromJSON(JSONObject links) {
		return fromJSON(new Links(), links);	
	}
	
	public static Links fromJSON(Links l, JSONObject links) {
		if (l == null) l = new Links();
		if (links == null) return l;
		
		try {
			l.setWebshop(links.getString(S_WEBSHOP));
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
			o = null;
			e.printStackTrace();
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
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("webshop=").append(mWebshop)
		.append("]").toString();
	}
	
}
