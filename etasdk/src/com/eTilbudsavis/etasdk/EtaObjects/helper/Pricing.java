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

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;
import android.os.Parcelable;
import android.os.Parcel;

public class Pricing implements EtaObject<JSONObject>, Serializable, Parcelable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = Eta.TAG_PREFIX + Pricing.class.getSimpleName();
	
	private double mPrice = 1.0d;
	private Double mPrePrice;
	private String mCurrency;

	public static Parcelable.Creator<Pricing> CREATOR = new Parcelable.Creator<Pricing>(){
		public Pricing createFromParcel(Parcel source) {
			return new Pricing(source);
		}
		public Pricing[] newArray(int size) {
			return new Pricing[size];
		}
	};
	
	public Pricing() {
		
	}
	
	public static Pricing fromJSON(JSONObject pricing) {
		Pricing p = new Pricing();
		if (pricing == null) {
			return p;
		}
		
		try {
			p.setPrice(Json.valueOf(pricing, JsonKey.PRICE, 1.0d));
			p.setPrePrice(pricing.isNull(JsonKey.PREPRICE) ? null : pricing.getDouble(JsonKey.PREPRICE));
			p.setCurrency(Json.valueOf(pricing, JsonKey.CURRENCY));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return p;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.PRICE, getPrice());
			o.put(JsonKey.PREPRICE, Json.nullCheck(getPrePrice()));
			o.put(JsonKey.CURRENCY, Json.nullCheck(getCurrency()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public double getPrice() {
		return mPrice;
	}

	public Pricing setPrice(double price) {
		mPrice = price;
		return this;
	}

	public Double getPrePrice() {
		return mPrePrice;
	}

	public Pricing setPrePrice(Double prePrice) {
		mPrePrice = prePrice;
		return this;
	}

	public String getCurrency() {
		return mCurrency;
	}

	public Pricing setCurrency(String currency) {
		mCurrency = currency;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mCurrency == null) ? 0 : mCurrency.hashCode());
		result = prime * result
				+ ((mPrePrice == null) ? 0 : mPrePrice.hashCode());
		long temp;
		temp = Double.doubleToLongBits(mPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Pricing other = (Pricing) obj;
		if (mCurrency == null) {
			if (other.mCurrency != null)
				return false;
		} else if (!mCurrency.equals(other.mCurrency))
			return false;
		if (mPrePrice == null) {
			if (other.mPrePrice != null)
				return false;
		} else if (!mPrePrice.equals(other.mPrePrice))
			return false;
		if (Double.doubleToLongBits(mPrice) != Double
				.doubleToLongBits(other.mPrice))
			return false;
		return true;
	}

	private Pricing(Parcel in) {
		this.mPrice = in.readDouble();
		this.mPrePrice = (Double)in.readValue(Double.class.getClassLoader());
		this.mCurrency = in.readString();
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(this.mPrice);
		dest.writeValue(this.mPrePrice);
		dest.writeString(this.mCurrency);
	}

	
}
