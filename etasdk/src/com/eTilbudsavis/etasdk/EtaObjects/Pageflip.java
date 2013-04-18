package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Pageflip implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String mLogo;
	private String mColor;
	
	public Pageflip(JSONObject pageflip) {
		try {
			mLogo = pageflip.getString("logo");
			mColor = pageflip.getString("color");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public String getLogo() {
		return mLogo;
	}
	
	public String getColor() {
		return mColor;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Pageflip))
			return false;

		Pageflip p = (Pageflip)o;
		return mLogo.equals(p.getLogo()) &&
				mColor.equals(p.getColor());
	}
	
}