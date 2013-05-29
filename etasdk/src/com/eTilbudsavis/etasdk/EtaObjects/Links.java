package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Links implements Serializable {

	private static final long serialVersionUID = 1L;

	private String mWebshop;
	
	public Links() {
		mWebshop = "";
	}
	
	public Links(JSONObject links) {
		try {
			mWebshop = links.getString("webshop");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void setWebshop(String url) {
		mWebshop = url;
	}
	
	public String getWebshop() {
		return mWebshop;
	}
}
