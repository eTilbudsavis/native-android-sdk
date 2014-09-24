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

public class Unit implements EtaObject<JSONObject>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = Unit.class.getSimpleName();
	
	private String mSymbol;
	private Si mSi;
	
	public static Unit fromJSON(JSONObject unit) {
		Unit u = new Unit();
		if (unit == null) {
			return u;
		}

		u.setSymbol(Json.valueOf(unit, JsonKey.SYMBOL));
		try {
			u.setSi(Si.fromJSON(unit.getJSONObject(JsonKey.SI)));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		
		return u;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.SYMBOL, Json.nullCheck(getSymbol()));
			o.put(JsonKey.SI, Json.toJson(getSi()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public String getSymbol() {
		return mSymbol;
	}

	public Unit setSymbol(String symbol) {
		mSymbol = symbol;
		return this;
	}

	public Si getSi() {
		return mSi;
	}

	public Unit setSi(Si si) {
		mSi = si;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mSi == null) ? 0 : mSi.hashCode());
		result = prime * result + ((mSymbol == null) ? 0 : mSymbol.hashCode());
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
		Unit other = (Unit) obj;
		if (mSi == null) {
			if (other.mSi != null)
				return false;
		} else if (!mSi.equals(other.mSi))
			return false;
		if (mSymbol == null) {
			if (other.mSymbol != null)
				return false;
		} else if (!mSymbol.equals(other.mSymbol))
			return false;
		return true;
	}
	
	
}
