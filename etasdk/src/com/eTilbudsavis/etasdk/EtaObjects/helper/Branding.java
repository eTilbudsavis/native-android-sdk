/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.EtaObjects.helper;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.ServerKey;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Branding implements EtaObject<JSONObject>, Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = Branding.class.getSimpleName();
	
	private String mName;
	private String mUrlName;
	private String mWebsite;
	private String mLogo;
	private Integer mLogoBackground;
	private Integer mColor;
	private Pageflip mPageflip;
	
	public static Branding fromJSON(JSONObject branding) {
		Branding b = new Branding();
		if (branding == null) {
			return b;
		}
		
		try {
			b.setName(Json.valueOf(branding, ServerKey.NAME));
			b.setUrlName(Json.valueOf(branding, ServerKey.URL_NAME));
			b.setWebsite(Json.valueOf(branding, ServerKey.WEBSITE));
			b.setLogo(Json.valueOf(branding, ServerKey.LOGO));
			String color = Json.valueOf(branding, ServerKey.COLOR, "ffffff");
			b.setColor(Color.parseColor("#"+color));
			String logoColor = Json.valueOf(branding, ServerKey.LOGO_BACKGROUND, color);
			b.setLogoBackground(Color.parseColor("#"+logoColor));
			b.setPageflip(Pageflip.fromJSON(branding.getJSONObject(ServerKey.PAGEFLIP)));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return b;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.NAME, Json.nullCheck(getName()));
			o.put(ServerKey.URL_NAME, Json.nullCheck(getUrlName()));
			o.put(ServerKey.WEBSITE, Json.nullCheck(getWebsite()));
			o.put(ServerKey.LOGO, Json.nullCheck(getLogo()));
			o.put(ServerKey.COLOR, Json.nullCheck(getColorString()));
			o.put(ServerKey.PAGEFLIP, Json.nullCheck(getPageflip().toJSON()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
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

	public Branding setLogoBackground(Integer color) {
		mLogoBackground = color;
		return this;
	}
	
	public Integer getLogoBackground() {
		return mLogoBackground;
	}

	public String getLogoBackgroundString() {
		return String.format("%06X", 0xFFFFFF & mLogoBackground);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mColor == null) ? 0 : mColor.hashCode());
		result = prime * result + ((mLogo == null) ? 0 : mLogo.hashCode());
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		result = prime * result
				+ ((mPageflip == null) ? 0 : mPageflip.hashCode());
		result = prime * result
				+ ((mUrlName == null) ? 0 : mUrlName.hashCode());
		result = prime * result
				+ ((mWebsite == null) ? 0 : mWebsite.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Branding other = (Branding) obj;
		if (mColor == null) {
			if (other.mColor != null)
				return false;
		} else if (!mColor.equals(other.mColor))
			return false;
		if (mLogo == null) {
			if (other.mLogo != null)
				return false;
		} else if (!mLogo.equals(other.mLogo))
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		if (mPageflip == null) {
			if (other.mPageflip != null)
				return false;
		} else if (!mPageflip.equals(other.mPageflip))
			return false;
		if (mUrlName == null) {
			if (other.mUrlName != null)
				return false;
		} else if (!mUrlName.equals(other.mUrlName))
			return false;
		if (mWebsite == null) {
			if (other.mWebsite != null)
				return false;
		} else if (!mWebsite.equals(other.mWebsite))
			return false;
		return true;
	}
	
	
}
