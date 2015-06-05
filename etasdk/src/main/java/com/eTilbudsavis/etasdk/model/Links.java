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

import android.os.Parcel;
import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.interfaces.IJson;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.utils.Json;

import org.json.JSONException;
import org.json.JSONObject;

public class Links implements IJson<JSONObject>, Parcelable {

	public static final String TAG = Constants.getTag(Links.class);

	private String mWebshop;

	public static Parcelable.Creator<Links> CREATOR = new Parcelable.Creator<Links>(){
		public Links createFromParcel(Parcel source) {
			return new Links(source);
		}
		public Links[] newArray(int size) {
			return new Links[size];
		}
	};
	
	public Links() {
		
	}
	
	public static Links fromJSON(JSONObject links) {
		Links l = new Links();
		if (links == null) {
			return l;
		}
		
		l.setWebshop(Json.valueOf(links, JsonKey.WEBSHOP));
		
		return l;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.WEBSHOP, Json.nullCheck(getWebshop()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public void setWebshop(String url) {
		mWebshop = url;
	}
	
	public String getWebshop() {
		return mWebshop;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mWebshop == null) ? 0 : mWebshop.hashCode());
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
		Links other = (Links) obj;
		if (mWebshop == null) {
			if (other.mWebshop != null)
				return false;
		} else if (!mWebshop.equals(other.mWebshop))
			return false;
		return true;
	}

	private Links(Parcel in) {
		this.mWebshop = in.readString();
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mWebshop);
	}

}
