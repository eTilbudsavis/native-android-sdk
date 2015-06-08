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

public class Size implements IJson<JSONObject>, Parcelable {

	public static final String TAG = Constants.getTag(Size.class);
	
	private double mFrom = 1.0d;
	private double mTo = 1.0d;
	
	public static Parcelable.Creator<Size> CREATOR = new Parcelable.Creator<Size>(){
		public Size createFromParcel(Parcel source) {
			return new Size(source);
		}
		public Size[] newArray(int size) {
			return new Size[size];
		}
	};
	
	public Size() {
		
	}
	
	public static Size fromJSON(JSONObject size) {
		Size s = new Size();
		if (size == null) {
			return s;
		}
		
		s.setFrom(Json.valueOf(size, JsonKey.FROM, 1.0d));
		s.setTo(Json.valueOf(size, JsonKey.TO, 1.0d));
		
		return s;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.FROM, getFrom());
			o.put(JsonKey.TO, getTo());
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public double getFrom() {
		return mFrom;
	}
	
	public Size setFrom(double from) {
		mFrom = from;
		return this;
	}
	
	public double getTo() {
		return mTo;
	}
	
	public Size setTo(double to) {
		mTo = to;
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(mFrom);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mTo);
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
		Size other = (Size) obj;
		if (Double.doubleToLongBits(mFrom) != Double
				.doubleToLongBits(other.mFrom))
			return false;
		if (Double.doubleToLongBits(mTo) != Double.doubleToLongBits(other.mTo))
			return false;
		return true;
	}

	private Size(Parcel in) {
		this.mFrom = in.readDouble();
		this.mTo = in.readDouble();
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(this.mFrom);
		dest.writeDouble(this.mTo);
	}
	
}
