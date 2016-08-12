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

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Dimension implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Dimension.class);

    public static Parcelable.Creator<Dimension> CREATOR = new Parcelable.Creator<Dimension>() {
        public Dimension createFromParcel(Parcel source) {
            return new Dimension(source);
        }

        public Dimension[] newArray(int size) {
            return new Dimension[size];
        }
    };
    private double mWidth = Double.NaN;
    private double mHeight = Double.NaN;

    public static Dimension fromBitmap(Bitmap b) {
        Dimension d = new Dimension();
        d.setWidth(1); // magic number... always one
        double h = (double)((float)b.getHeight()/(float)b.getWidth());
        d.setHeight(h);
        return d;
    }

    public Dimension() {
    }

    private Dimension(Parcel in) {
        this.mWidth = in.readDouble();
        this.mHeight = in.readDouble();
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Dimension}
     * @return A {@link List} of POJO
     */
    public static List<Dimension> fromJSON(JSONArray array) {
        List<Dimension> list = new ArrayList<Dimension>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Dimension.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Dimension}
     * @return A {@link Dimension}, or {@code null} if {@code object} is {@code null}
     */
    public static Dimension fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Dimension d = new Dimension()
                .setWidth(o.getWidth())
                .setHeight(o.getHeight());

        o.getStats().log(TAG);

        return d;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setWidth(getWidth())
                .setHeight(getHeight())
                .toJSON();
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
        return mWidth != Double.NaN && mHeight != Double.NaN;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mWidth);
        dest.writeDouble(this.mHeight);
    }


}
