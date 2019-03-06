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
import androidx.annotation.Keep;

import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Keep
public class Si implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Si.class);
    public static final Parcelable.Creator<Si> CREATOR = new Parcelable.Creator<Si>() {
        public Si createFromParcel(Parcel source) {
            return new Si(source);
        }

        public Si[] newArray(int size) {
            return new Si[size];
        }
    };
    private String mSymbol;
    private double mFactor = 1.0d;

    public Si() {

    }

    private Si(Parcel in) {
        this.mSymbol = in.readString();
        this.mFactor = in.readDouble();
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Si}
     * @return A {@link List} of POJO
     */
    public static List<Si> fromJSON(JSONArray array) {
        List<Si> list = new ArrayList<Si>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Si.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Si}
     * @return A {@link Links}, or {@code null} if {@code object} is {@code null}
     */
    public static Si fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Si s = new Si()
                .setSymbol(o.getSymbol())
                .setFactor(o.getFactor());

        o.getStats().log(TAG);

        return s;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setSymbol(getSymbol())
                .setFactor(getFactor())
                .toJSON();
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSymbol);
        dest.writeDouble(this.mFactor);
    }


}
