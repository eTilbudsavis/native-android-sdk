/*******************************************************************************
 * Copyright 2015 ShopGun
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

package com.shopgun.android.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.api.JsonKeys;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Quantity implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Quantity.class);
    public static Parcelable.Creator<Quantity> CREATOR = new Parcelable.Creator<Quantity>() {
        public Quantity createFromParcel(Parcel source) {
            return new Quantity(source);
        }

        public Quantity[] newArray(int size) {
            return new Quantity[size];
        }
    };
    private Unit mUnit;
    private Size mSize;
    private Pieces mPieces;

    public Quantity() {

    }

    private Quantity(Parcel in) {
        this.mUnit = in.readParcelable(Unit.class.getClassLoader());
        this.mSize = in.readParcelable(Size.class.getClassLoader());
        this.mPieces = in.readParcelable(Pieces.class.getClassLoader());
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Quantity}
     * @return A {@link List} of POJO
     */
    public static List<Quantity> fromJSON(JSONArray array) {
        List<Quantity> list = new ArrayList<Quantity>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = Json.getObject(array, i);
            if (o != null) {
                list.add(Quantity.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Quantity}
     * @return A {@link Quantity}, or {@link null} if {@code object is null}
     */
    public static Quantity fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        Quantity q = new Quantity();
        JSONObject jUnit = Json.getObject(object, JsonKeys.UNIT);
        q.setUnit(Unit.fromJSON(jUnit));
        JSONObject jSize = Json.getObject(object, JsonKeys.UNIT);
        q.setSize(Size.fromJSON(jSize));
        JSONObject jPieces = Json.getObject(object, JsonKeys.UNIT);
        q.setPieces(Pieces.fromJSON(jPieces));
        return q;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(JsonKeys.UNIT, Json.toJson(getUnit()));
            o.put(JsonKeys.SIZE, Json.toJson(getSize()));
            o.put(JsonKeys.PIECES, Json.toJson(getPieces()));
        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mUnit, flags);
        dest.writeParcelable(this.mSize, flags);
        dest.writeParcelable(this.mPieces, flags);
    }

}
