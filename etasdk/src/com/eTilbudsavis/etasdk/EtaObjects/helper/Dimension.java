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

import android.os.Parcel;
import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Dimension implements EtaObject<JSONObject>, Serializable, Parcelable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = Eta.TAG_PREFIX + Dimension.class.getSimpleName();
	
	public static final double DEF_DIMENSION = -1d;
	
	private double mWidth = DEF_DIMENSION;
	private double mHeight = DEF_DIMENSION;

	public static Parcelable.Creator<Dimension> CREATOR = new Parcelable.Creator<Dimension>(){
		public Dimension createFromParcel(Parcel source) {
			return new Dimension(source);
		}
		public Dimension[] newArray(int size) {
			return new Dimension[size];
		}
	};
	
	public Dimension() {
		
	}
	
	public static Dimension fromJSON(JSONObject dimension) {
		Dimension d = new Dimension();
		if (dimension == null) return d;
		
		d.setWidth(Json.valueOf(dimension, JsonKey.WIDTH, DEF_DIMENSION));
		d.setHeight(Json.valueOf(dimension, JsonKey.HEIGHT, DEF_DIMENSION));
		
		return d;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.HEIGHT, Json.nullCheck(getHeight()));
			o.put(JsonKey.WIDTH, Json.nullCheck(getWidth()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public Double getWidth() {
		return mWidth;
	}

	public Dimension setWidth(double width) {
		mWidth = width;
		return this;
	}

	public Double getHeight() {
		return mHeight;
	}

	public Dimension setHeight(double height) {
		mHeight = height;
		return this;
	}
	
	public boolean isSet() {
		return mWidth>DEF_DIMENSION && mHeight>DEF_DIMENSION;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(mHeight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mWidth);
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
		Dimension other = (Dimension) obj;
		if (Double.doubleToLongBits(mHeight) != Double
				.doubleToLongBits(other.mHeight))
			return false;
		if (Double.doubleToLongBits(mWidth) != Double
				.doubleToLongBits(other.mWidth))
			return false;
		return true;
	}

	private Dimension(Parcel in) {
		this.mWidth = in.readDouble();
		this.mHeight = in.readDouble();
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(this.mWidth);
		dest.writeDouble(this.mHeight);
	}
	
	
}
