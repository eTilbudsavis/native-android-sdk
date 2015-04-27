/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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
 ******************************************************************************/

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
import com.eTilbudsavis.etasdk.utils.Json;

public class Quantity implements IJson<JSONObject>, Serializable, Parcelable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = Constants.getTag(Quantity.class);
	
	private Unit mUnit;
	private Size mSize;
	private Pieces mPieces;

	public static Parcelable.Creator<Quantity> CREATOR = new Parcelable.Creator<Quantity>(){
		public Quantity createFromParcel(Parcel source) {
			return new Quantity(source);
		}
		public Quantity[] newArray(int size) {
			return new Quantity[size];
		}
	};

	public Quantity() {
		
	}
	
	public static Quantity fromJSON(JSONObject quantity) {
		Quantity q = new Quantity();
		if (quantity == null) {
			return q;
		}
		
		try {
			q.setUnit(quantity.isNull(JsonKey.UNIT) ? null : Unit.fromJSON(quantity.getJSONObject(JsonKey.UNIT)));
			q.setSize(quantity.isNull(JsonKey.SIZE) ? null : Size.fromJSON(quantity.getJSONObject(JsonKey.SIZE)));
			q.setPieces(quantity.isNull(JsonKey.PIECES) ? null : Pieces.fromJSON(quantity.getJSONObject(JsonKey.PIECES)));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return q;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.UNIT, Json.toJson(getUnit()));
			o.put(JsonKey.SIZE, Json.toJson(getSize()));
			o.put(JsonKey.PIECES, Json.toJson(getPieces()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
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
		int result = 1;
		result = prime * result + ((mPieces == null) ? 0 : mPieces.hashCode());
		result = prime * result + ((mSize == null) ? 0 : mSize.hashCode());
		result = prime * result + ((mUnit == null) ? 0 : mUnit.hashCode());
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

	private Quantity(Parcel in) {
		this.mUnit = in.readParcelable(Unit.class.getClassLoader());
		this.mSize = in.readParcelable(Size.class.getClassLoader());
		this.mPieces = in.readParcelable(Pieces.class.getClassLoader());
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.mUnit, flags);
		dest.writeParcelable(this.mSize, flags);
		dest.writeParcelable(this.mPieces, flags);
	}
	
}
