package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.Utils.Utils;

public class Branding extends EtaObject  implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Branding";
	
	private String mName;
	private String mUrlName;
	private String mWebsite;
	private String mLogo;
	private Integer mColor;
	private Pageflip mPageflip;
	
	public Branding() { }
	
	@SuppressWarnings("unchecked")
	public static Branding fromJSON(JSONObject branding) {
		return fromJSON(new Branding(), branding);
	}

	private static Branding fromJSON(Branding b, JSONObject branding) {
		if (b == null) b = new Branding();
		if (branding == null) return b;
		
		try {
			b.setName(getJsonString(branding, S_NAME));
			b.setUrlName(getJsonString(branding, S_URL_NAME));
			b.setWebsite(getJsonString(branding, S_WEBSITE));
			b.setLogo(getJsonString(branding, S_LOGO));
			b.setColor(Color.parseColor("#"+branding.getString(EtaObject.S_COLOR)));
			b.setPageflip(Pageflip.fromJSON(branding.getJSONObject(EtaObject.S_PAGEFLIP)));
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		return b;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Branding b) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_NAME, b.getName());
			o.put(S_URL_NAME, b.getUrlName());
			o.put(S_WEBSITE, b.getWebsite());
			o.put(S_LOGO, b.getLogo());
			o.put(S_COLOR, b.getColorString());
			o.put(S_PAGEFLIP, b.getPageflip().toJSON());
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		return o;
	}
	
	public Branding setName(String name) {
		mName = name;
		return this;
	}

	public String getName() {
		return mName;
	}

	public Branding setUrlName(String urlName) {
		mUrlName = urlName;
		return this;
	}

	public String getUrlName() {
		return mUrlName;
	}

	public Branding setWebsite(String website) {
		mWebsite = website;
		return this;
	}

	public String getWebsite() {
		return mWebsite;
	}

	public Branding setLogo(String logo) {
		mLogo = logo;
		return this;
	}

	public String getLogo() {
		return mLogo;
	}

	public Branding setColor(Integer color) {
		mColor = color;
		return this;
	}

	public Integer getColor() {
		return mColor;
	}

	public String getColorString() {
		return String.format("%06X", 0xFFFFFF & mColor);
	}

	public Branding setPageflip(Pageflip pageflip) {
		mPageflip = pageflip;
		return this;
	}

	public Pageflip getPageflip() {
		return mPageflip;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Branding))
			return false;

		Branding b = (Branding)o;
		return stringCompare(mName, b.getName()) &&
				stringCompare(mUrlName, b.getUrlName()) &&
				stringCompare(mWebsite, b.getWebsite()) &&
				stringCompare(mLogo, b.getLogo()) &&
				mColor.equals(b.getColor()) &&
				mPageflip.equals(b.getPageflip());
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append("name=").append(mName)
		.append(", urlName=").append(mUrlName)
		.append(", website=").append(mWebsite);
				
		if (everything) {
			sb.append(", logo=").append(mLogo)
			.append(", color=").append(mColor)
			.append(", pageflip=").append(mPageflip == null ? null : mPageflip.toString());
		}
		
		return sb.append("]").toString();
	}

}