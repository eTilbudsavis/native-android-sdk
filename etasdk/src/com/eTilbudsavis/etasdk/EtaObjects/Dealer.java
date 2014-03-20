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
package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;

/**
 * <p>This class is a representation of a dealer as the API v2 exposes it</p>
 * 
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/dealers.html">Dealer Reference</a>
 * documentation, on the engineering blog.
 * </p>
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class Dealer extends EtaErnObject<Dealer> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "Dealer";
	
	private String mName;
	private String mUrlName;
	private String mWebsite;
	private String mLogo;
	private Integer mColor;
	private Pageflip mPageflip;

	public Dealer() { }
	
	public static ArrayList<Dealer> fromJSON(JSONArray dealers) {
		ArrayList<Dealer> list = new ArrayList<Dealer>();
		try {
			for (int i = 0 ; i < dealers.length() ; i++ )
				list.add(Dealer.fromJSON((JSONObject)dealers.get(i)));
			
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}
	
	public static Dealer fromJSON(JSONObject dealer) {
		return fromJSON(new Dealer(), dealer);
	}
	
	public static Dealer fromJSON(Dealer d, JSONObject dealer) {
		if (d == null) d = new Dealer();
		if (dealer == null) return d;
		
		try {
			d.setId(Json.valueOf(dealer, ServerKey.ID));
			d.setErn(Json.valueOf(dealer, ServerKey.ERN));
			d.setName(Json.valueOf(dealer, ServerKey.NAME));
			d.setUrlName(Json.valueOf(dealer, ServerKey.URL_NAME));
			d.setWebsite(Json.valueOf(dealer, ServerKey.WEBSITE));
			d.setLogo(Json.valueOf(dealer, ServerKey.LOGO));
			d.setColor(Color.parseColor("#"+Json.valueOf(dealer, ServerKey.COLOR)));
			d.setPageflip(Pageflip.fromJSON(dealer.getJSONObject(ServerKey.PAGEFLIP)));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return d;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = super.toJSON();
		try {
			o.put(ServerKey.NAME, Json.nullCheck(getName()));
			o.put(ServerKey.URL_NAME, Json.nullCheck(getUrlName()));
			o.put(ServerKey.WEBSITE, Json.nullCheck(getWebsite()));
			o.put(ServerKey.LOGO, Json.nullCheck(getLogo()));
			o.put(ServerKey.COLOR, Json.nullCheck(getColorString()));
			o.put(ServerKey.PAGEFLIP, Json.nullCheck(getPageflip().toJSON()));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o; 
	}
	
	@Override
	public String getErnPrefix() {
		return ERN_DEALER;
	}
	
	public Dealer setName(String name) {
		mName = name;
		return this;
	}

	public String getName() {
		return mName;
	}

	public Dealer setUrlName(String url) {
		mUrlName = url;
		return this;
	}

	public String getUrlName() {
		return mUrlName;
	}

	public Dealer setWebsite(String website) {
		mWebsite = website;
		return this;
	}

	public String getWebsite() {
		return mWebsite;
	}

	public Dealer setLogo(String logo) {
		mLogo = logo;
		return this;
	}

	public String getLogo() {
		return mLogo;
	}

	public Dealer setColor(int color) {
		mColor = color;
		return this;
	}

	public int getColor() {
		return mColor;
	}
	
	public String getColorString() {
		return String.format("%06X", 0xFFFFFF & mColor);
	}

	public Dealer setPageflip(Pageflip pageflip) {
		mPageflip = pageflip;
		return this;
	}

	public Pageflip getPageflip() {
		return mPageflip;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dealer other = (Dealer) obj;
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
