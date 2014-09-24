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

import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Dimension implements EtaObject<JSONObject>, Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = Dimension.class.getSimpleName();
	
	private Double mWidth = null;
	private Double mHeight = null;
	
	public static Dimension fromJSON(JSONObject dimension) {
		Dimension d = new Dimension();
		if (dimension == null) return d;
		
		try {
			if (!dimension.isNull(JsonKey.WIDTH)) {
				d.setWidth(dimension.getDouble(JsonKey.WIDTH));
			}
			if (!dimension.isNull(JsonKey.HEIGHT)) {
				d.setHeight(dimension.getDouble(JsonKey.HEIGHT));
			}
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (!super.equals(obj))
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
	
	
}
