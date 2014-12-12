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
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IErn;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IJson;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Utils;

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
public class Dealer implements IErn<Dealer>, IJson<JSONObject>, Serializable, Parcelable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = Eta.TAG_PREFIX + Dealer.class.getSimpleName();
	
	private static final String ERN_CLASS = "dealer";

	private String mId;
	private String mErn;
	private String mName;
	private String mUrlName;
	private String mWebsite;
	private String mLogo;
	private int mColor;
	private Pageflip mPageflip;
	
	public Dealer() {
		
	}
	
	public static ArrayList<Dealer> fromJSON(JSONArray dealers) {
		ArrayList<Dealer> list = new ArrayList<Dealer>();
		try {
			for (int i = 0 ; i < dealers.length() ; i++ )
				list.add(Dealer.fromJSON((JSONObject)dealers.get(i)));
			
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		
		return list;
	}
	
	public static Dealer fromJSON(JSONObject dealer) {
		Dealer d = new Dealer();
		if (dealer == null) {
			return d;
		}
		
		try {
			d.setId(Json.valueOf(dealer, JsonKey.ID));
			d.setErn(Json.valueOf(dealer, JsonKey.ERN));
			d.setName(Json.valueOf(dealer, JsonKey.NAME));
			d.setUrlName(Json.valueOf(dealer, JsonKey.URL_NAME));
			d.setWebsite(Json.valueOf(dealer, JsonKey.WEBSITE));
			d.setLogo(Json.valueOf(dealer, JsonKey.LOGO));
			d.setColor(Json.colorValueOf(dealer, JsonKey.COLOR));
			d.setPageflip(Pageflip.fromJSON(dealer.getJSONObject(JsonKey.PAGEFLIP)));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return d;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.ID, Json.nullCheck(getId()));
			o.put(JsonKey.ERN, Json.nullCheck(getErn()));
			o.put(JsonKey.NAME, Json.nullCheck(getName()));
			o.put(JsonKey.URL_NAME, Json.nullCheck(getUrlName()));
			o.put(JsonKey.WEBSITE, Json.nullCheck(getWebsite()));
			o.put(JsonKey.LOGO, Json.nullCheck(getLogo()));
			o.put(JsonKey.COLOR, Json.nullCheck(Utils.colorToString(getColor())));
			o.put(JsonKey.PAGEFLIP, Json.nullCheck(getPageflip().toJSON()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public Dealer setId(String id) {
		mId = id;
		mErn = String.format("ern:%s:%s", ERN_CLASS, id);
		return this;
	}
	
	public String getId() {
		return mId;
	}
	
	public Dealer setErn(String ern) {
		if (ern != null) {
			mErn = ern;
			String[] parts = mErn.split(":");
			mId = parts[parts.length-1];
		}
		return this;
	}
	
	public String getErn() {
		return mErn;
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
	
	public Dealer setPageflip(Pageflip pageflip) {
		mPageflip = pageflip;
		return this;
	}

	public Pageflip getPageflip() {
		return mPageflip;
	}

	/**
	 * Compare object, that uses {@link Dealer#getName() name} to compare two lists.
	 */
	public static Comparator<Dealer> NAME_COMPARATOR  = new Comparator<Dealer>() {

		public int compare(Dealer item1, Dealer item2) {

			if (item1 == null || item2 == null) {
				return item1 == null ? (item2 == null ? 0 : 1) : -1;
			} else {
				String t1 = item1.getName();
				String t2 = item2.getName();
				if (t1 == null || t2 == null) {
					return t1 == null ? (t2 == null ? 0 : 1) : -1;
				}
				
				//ascending order
				return t1.compareToIgnoreCase(t2);
			}
			
		}

	};

	public static Parcelable.Creator<Dealer> CREATOR = new Parcelable.Creator<Dealer>(){
		public Dealer createFromParcel(Parcel source) {
			return new Dealer(source);
		}
		public Dealer[] newArray(int size) {
			return new Dealer[size];
		}
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mColor;
		result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
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
		Dealer other = (Dealer) obj;
		if (mColor != other.mColor)
			return false;
		if (mErn == null) {
			if (other.mErn != null)
				return false;
		} else if (!mErn.equals(other.mErn))
			return false;
		if (mId == null) {
			if (other.mId != null)
				return false;
		} else if (!mId.equals(other.mId))
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

	private Dealer(Parcel in) {
		this.mId = in.readString();
		this.mErn = in.readString();
		this.mName = in.readString();
		this.mUrlName = in.readString();
		this.mWebsite = in.readString();
		this.mLogo = in.readString();
		this.mColor = in.readInt();
		this.mPageflip = in.readParcelable(Pageflip.class.getClassLoader());
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mId);
		dest.writeString(this.mErn);
		dest.writeString(this.mName);
		dest.writeString(this.mUrlName);
		dest.writeString(this.mWebsite);
		dest.writeString(this.mLogo);
		dest.writeInt(this.mColor);
		dest.writeParcelable(this.mPageflip, flags);
	}

}
