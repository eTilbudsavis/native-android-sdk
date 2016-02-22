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
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Json;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Size implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Size.class);
    public static Parcelable.Creator<Size> CREATOR = new Parcelable.Creator<Size>() {
        public Size createFromParcel(Parcel source) {
            return new Size(source);
        }

        public Size[] newArray(int size) {
            return new Size[size];
        }
    };
    private double mFrom = 1.0d;
    private double mTo = 1.0d;

    public Size() {

    }

    private Size(Parcel in) {
        this.mFrom = in.readDouble();
        this.mTo = in.readDouble();
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Size}
     * @return A {@link List} of POJO
     */
    public static List<Size> fromJSON(JSONArray array) {
        List<Size> list = new ArrayList<Size>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = Json.getObject(array, i);
            if (o != null) {
                list.add(Size.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Links}
     * @return A {@link Links}, or {@code null} if {@code object} is {@code null}
     */
    public static Size fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Size s = new Size()
                .setFrom(o.getFrom())
                .setTo(o.getTo());
        o.getStats().log(TAG);
        return s;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setFrom(getFrom())
                .setTo(getTo())
                .toJSON();
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mFrom);
        dest.writeDouble(this.mTo);
    }

}
