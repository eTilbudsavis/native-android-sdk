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
package com.eTilbudsavis.etasdk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.interfaces.IJson;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.utils.ColorUtils;
import com.eTilbudsavis.etasdk.utils.Json;

public class Pageflip implements IJson<JSONObject>, Serializable, Parcelable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = Constants.getTag(Pageflip.class);
	
	private String mLogo;
	private Integer mColor = 0;
	
	public static Parcelable.Creator<Pageflip> CREATOR = new Parcelable.Creator<Pageflip>(){
		public Pageflip createFromParcel(Parcel source) {
			return new Pageflip(source);
		}
		public Pageflip[] newArray(int size) {
			return new Pageflip[size];
		}
	};

	public Pageflip() {
		
	}
	
	public Pageflip(int color) {
		mColor = ColorUtils.stripAlpha(color);
	}
	
	public static Pageflip fromJSON(JSONObject pageflip) {
		Pageflip p = new Pageflip();
		if (pageflip == null) {
			return p;
		}
		
		p.setLogo(Json.valueOf(pageflip, JsonKey.LOGO));
		p.setColor(Json.colorValueOf(pageflip, JsonKey.COLOR));
		
		return p;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.LOGO, Json.nullCheck(getLogo()));
			o.put(JsonKey.COLOR, Json.nullCheck(ColorUtils.toString(mColor)));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
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
	
	public Pageflip setColor(Integer color) {
		mColor = ColorUtils.stripAlpha(color);
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mColor == null) ? 0 : mColor.hashCode());
		result = prime * result + ((mLogo == null) ? 0 : mLogo.hashCode());
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
		Pageflip other = (Pageflip) obj;
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
		return true;
	}

	private Pageflip(Parcel in) {
		this.mLogo = in.readString();
		this.mColor = (Integer)in.readValue(Integer.class.getClassLoader());
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mLogo);
		dest.writeValue(this.mColor);
	}
	
}
