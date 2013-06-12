package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

public class Branding implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final String S_NAME = "name";
	private static final String S_URL_NAME = "url_name";
	private static final String S_WEBSITE = "website";
	private static final String S_LOGO = "logo";
	private static final String S_COLOR = "color";
	private static final String S_PAGEFLIP = "pageflip";
	
	public static final String TAG = "Branding";
	
	private String mName;
	private String mUrlName;
	private String mWebsite;
	private String mLogo;
	private Integer mColor;
	private Pageflip mPageflip;
	
	public Branding() {
	}
	
	public static Branding fromJSON(String branding) {
		try {
			return fromJSON(new Branding(), new JSONObject(branding));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Branding fromJSON(JSONObject branding) {
		return fromJSON(new Branding(), branding);
	}

	private static Branding fromJSON(Branding b, JSONObject branding) {
		if (b == null) b = new Branding();
		if (branding == null) return b;
		
		try {
			b.setName(branding.getString(S_NAME));
			b.setUrlName(branding.getString(S_URL_NAME));
			b.setWebsite(branding.getString(S_WEBSITE));
			b.setLogo(branding.getString(S_LOGO));
			b.setColor(Color.parseColor("#"+branding.getString(S_COLOR)));
			b.setPageflip(Pageflip.fromJSON(branding.getJSONObject(S_PAGEFLIP)));
		} catch (JSONException e) {
			e.printStackTrace();
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
			o = null;
			e.printStackTrace();
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
		return mName.equals(b.getName()) &&
				mUrlName.equals(b.getUrlName()) &&
				mWebsite.equals(b.getWebsite()) &&
				mLogo.equals(b.getLogo()) &&
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
			.append(", pageflip=").append(mPageflip.toString());
		}
		
		return sb.append("]").toString();
	}

}