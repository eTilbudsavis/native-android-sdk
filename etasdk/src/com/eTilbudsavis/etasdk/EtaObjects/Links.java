package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

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
			EtaLog.d(TAG, e);
		}
		return l;
	}
	
	public static Links fromJSON(JSONObject links) {
		return fromJSON(new Links(), links);
	}
	
	public static Links fromJSON(Links l, JSONObject links) {
		if (l == null) l = new Links();
		if (links == null) return l;
		
		l.setWebshop(getJsonString(links, ServerKey.WEBSHOP));
		
		return l;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Links l) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.WEBSHOP, l.getWebshop());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mWebshop == null) ? 0 : mWebshop.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Links other = (Links) obj;
		if (mWebshop == null) {
			if (other.mWebshop != null)
				return false;
		} else if (!mWebshop.equals(other.mWebshop))
			return false;
		return true;
	}

	
}
