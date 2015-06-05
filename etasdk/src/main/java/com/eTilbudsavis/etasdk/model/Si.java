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

import java.io.Serializable;

public class Si implements IJson<JSONObject>, Parcelable {

	public static final String TAG = Constants.getTag(Si.class);
	
	private String mSymbol;
	private double mFactor = 1.0d;

	public static Parcelable.Creator<Si> CREATOR = new Parcelable.Creator<Si>(){
		public Si createFromParcel(Parcel source) {
			return new Si(source);
		}
		public Si[] newArray(int size) {
			return new Si[size];
		}
	};
	
	public Si() {
		
	}
	
	public static Si fromJSON(JSONObject si) {
		Si s = new Si();
		if (si == null) return s;
		
		s.setSymbol(Json.valueOf(si, JsonKey.SYMBOL));
		s.setFactor(Json.valueOf(si, JsonKey.FACTOR, 1.0d));
		
		return s;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.SYMBOL, Json.nullCheck(getSymbol()));
			o.put(JsonKey.FACTOR, Json.nullCheck(getFactor()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public String getSymbol() {
		return mSymbol;
	}

	public Si setSymbol(String symbol) {
		mSymbol = symbol;
		return this;
	}

	public double getFactor() {
		return mFactor;
	}
	
	public Si setFactor(double factor) {
		mFactor = factor;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(mFactor);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((mSymbol == null) ? 0 : mSymbol.hashCode());
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
		Si other = (Si) obj;
		if (Double.doubleToLongBits(mFactor) != Double
				.doubleToLongBits(other.mFactor))
			return false;
		if (mSymbol == null) {
			if (other.mSymbol != null)
				return false;
		} else if (!mSymbol.equals(other.mSymbol))
			return false;
		return true;
	}

	private Si(Parcel in) {
		this.mSymbol = in.readString();
		this.mFactor = in.readDouble();
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mSymbol);
		dest.writeDouble(this.mFactor);
	}
	
	
}
