package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

public class Branding implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "Branding";
	
	private String mName;
	private String mUrlName;
	private String mWebsite;
	private String mUrl;
	private String mLogo;
	private Integer mColor;
	private Pageflip mPageflip;
	
	public Branding(JSONObject branding) {
		try {
			mName = branding.getString("name");
			mUrlName = branding.getString("url_name");
			mWebsite = branding.getString("website");
			mUrl = branding.getString("website");
			mLogo = branding.getString("logo");
			mColor = Color.parseColor("#"+branding.getString("color"));
			mPageflip = new Pageflip(branding.getJSONObject("pageflip"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

	public Branding setUrl(String url) {
		mUrl = url;
		return this;
	}

	public String getUrl() {
		return mUrl;
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
				mUrl.equals(b.getUrl()) &&
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
			sb.append(", url=").append(mUrl)
			.append(", logo=").append(mLogo)
			.append(", color=").append(mColor)
			.append(", pageflip=").append(mPageflip.toString());
		}
		
		return sb.append("]").toString();
	}

}