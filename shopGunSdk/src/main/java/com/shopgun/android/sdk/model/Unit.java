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
import android.support.annotation.Keep;

import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Keep
public class Unit implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Unit.class);
    public static final Parcelable.Creator<Unit> CREATOR = new Parcelable.Creator<Unit>() {
        public Unit createFromParcel(Parcel source) {
            return new Unit(source);
        }

        public Unit[] newArray(int size) {
            return new Unit[size];
        }
    };
    private String mSymbol;
    private Si mSi;

    public Unit() {

    }

    private Unit(Parcel in) {
        this.mSymbol = in.readString();
        this.mSi = in.readParcelable(Si.class.getClassLoader());
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Unit}
     * @return A {@link List} of POJO
     */
    public static List<Unit> fromJSON(JSONArray array) {
        List<Unit> list = new ArrayList<Unit>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Unit.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Unit}
     * @return A {@link Unit}, or {@code null} if {@code object} is {@code null}
     */
    public static Unit fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }
        SgnJson o = new SgnJson(object);
        Unit u = new Unit()
                .setSymbol(o.getSymbol())
                .setSi(o.getSi());

        o.getStats().log(TAG);
        return u;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setSymbol(getSymbol())
                .setSi(getSi())
                .toJSON();
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
        int result = 1;
        result = prime * result + ((mSi == null) ? 0 : mSi.hashCode());
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSymbol);
        dest.writeParcelable(mSi, flags);
    }

}
