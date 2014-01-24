package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Pageflip extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Pageflip";
	
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
			EtaLog.d(TAG, e);
		}
		return p;
	}
	
	public static Pageflip fromJSON(JSONObject pageflip) {
		return fromJSON(new Pageflip(), pageflip);
	}
	
	public static Pageflip fromJSON(Pageflip p, JSONObject pageflip) {
		if (p == null) p = new Pageflip();
		if (pageflip == null) return p;
		
		p.setLogo(jsonToString(pageflip, ServerKey.LOGO));
		String color = jsonToString(pageflip, ServerKey.COLOR);
		if (color != null) {
			p.setColor(Color.parseColor(String.format("#%s", color)));
		}
		
		return p;
	}

	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Pageflip p) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.LOGO, p.getLogo());
			o.put(ServerKey.COLOR, p.getColorString());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + mColor;
		result = prime * result + ((mLogo == null) ? 0 : mLogo.hashCode());
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
		Pageflip other = (Pageflip) obj;
		if (mColor != other.mColor)
			return false;
		if (mLogo == null) {
			if (other.mLogo != null)
				return false;
		} else if (!mLogo.equals(other.mLogo))
			return false;
		return true;
	}
	
	
}