package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

import android.graphics.Color;

public class Pageflip implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Pageflip";
	
	public static final String S_LOGO = "logo";
	public static final String S_COLOR = "color";

	private String mLogo;
	private int mColor = 0;

	public Pageflip() {
	}

	public Pageflip(int color) {
		mColor = color;
	}
	
	public static Pageflip fromJSON(String pageflip) {
		Pageflip p = new Pageflip();
		try {
			p = fromJSON(p, new JSONObject(pageflip));
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		return p;
	}

	public static Pageflip fromJSON(JSONObject pageflip) {
		return fromJSON(new Pageflip(), pageflip);
	}
	
	public static Pageflip fromJSON(Pageflip p, JSONObject pageflip) {
		if (p == null) p = new Pageflip();
		if (pageflip == null) return p;
			
		try {
			p.setLogo(pageflip.getString(S_LOGO));
			p.setColor(Color.parseColor("#"+pageflip.getString(S_COLOR)));
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		return p;
	}

	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Pageflip p) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_LOGO, p.getLogo());
			o.put(S_COLOR, p.getColorString());
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		return o;
	}
	
	public String getLogo() {
		return mLogo;
	}
	
	public Pageflip setLogo(String url) {
		mLogo = url;
		return this;
	}
	
	public int getColor() {
		return mColor;
	}

	public String getColorString() {
		return String.format("%06X", 0xFFFFFF & mColor);
	}

	public Pageflip setColor(int color) {
		mColor = color;
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Pageflip))
			return false;

		Pageflip p = (Pageflip)o;
		return mLogo.equals(p.getLogo()) &&
				mColor == p.getColor();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("logo=").append(mLogo)
		.append(", color=").append(mColor)
		.append("]").toString();
	}
	
}