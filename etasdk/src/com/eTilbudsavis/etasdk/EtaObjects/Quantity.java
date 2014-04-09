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

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Quantity extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Quantity";
	
	private Unit mUnit;
	private Size mSize;
	private Pieces mPieces;
	
	public Quantity() {
		
	}
	
	public static Quantity fromJSON(JSONObject quantity) {
		return fromJSON(new Quantity(), quantity);
	}
	
	public static Quantity fromJSON(Quantity q, JSONObject quantity) {
		if (q == null) q = new Quantity();
		if (quantity == null) return q;
		
		try {
			q.setUnit(quantity.isNull(ServerKey.UNIT) ? null : Unit.fromJSON(quantity.getJSONObject(ServerKey.UNIT)));
			q.setSize(quantity.isNull(ServerKey.SIZE) ? null : Size.fromJSON(quantity.getJSONObject(ServerKey.SIZE)));
			q.setPieces(quantity.isNull(ServerKey.PIECES) ? null : Pieces.fromJSON(quantity.getJSONObject(ServerKey.PIECES)));
		} catch (JSONException e) {
			EtaLog.e(TAG, e);
		}
		return q;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.UNIT, Json.toJson(getUnit()));
			o.put(ServerKey.SIZE, Json.toJson(getSize()));
			o.put(ServerKey.PIECES, Json.toJson(getPieces()));
		} catch (JSONException e) {
			EtaLog.e(TAG, e);
		}
		return o;
	}
	
	public Unit getUnit() {
		return mUnit;
	}

	public Quantity setUnit(Unit unit) {
		mUnit = unit;
		return this;
	}

	public Size getSize() {
		return mSize;
	}

	public Quantity setSize(Size size) {
		mSize = size;
		return this;
	}
	
	public Pieces getPieces() {
		return mPieces;
	}

	public Quantity setPieces(Pieces pieces) {
		mPieces = pieces;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mPieces == null) ? 0 : mPieces.hashCode());
		result = prime * result + ((mSize == null) ? 0 : mSize.hashCode());
		result = prime * result + ((mUnit == null) ? 0 : mUnit.hashCode());
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
		Quantity other = (Quantity) obj;
		if (mPieces == null) {
			if (other.mPieces != null)
				return false;
		} else if (!mPieces.equals(other.mPieces))
			return false;
		if (mSize == null) {
			if (other.mSize != null)
				return false;
		} else if (!mSize.equals(other.mSize))
			return false;
		if (mUnit == null) {
			if (other.mUnit != null)
				return false;
		} else if (!mUnit.equals(other.mUnit))
			return false;
		return true;
	}
	
	
}
