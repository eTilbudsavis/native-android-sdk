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

import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Pieces implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Pieces.class);
    public static Parcelable.Creator<Pieces> CREATOR = new Parcelable.Creator<Pieces>() {
        public Pieces createFromParcel(Parcel source) {
            return new Pieces(source);
        }

        public Pieces[] newArray(int size) {
            return new Pieces[size];
        }
    };
    private int mFrom = 1;
    private int mTo = 1;

    public Pieces() {

    }

    private Pieces(Parcel in) {
        this.mFrom = in.readInt();
        this.mTo = in.readInt();
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Pieces}
     * @return A {@link List} of POJO
     */
    public static List<Pieces> fromJSON(JSONArray array) {
        List<Pieces> list = new ArrayList<Pieces>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Pieces.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Pieces}
     * @return A {@link Pieces}, or {@code null} if {@code object} is {@code null}
     */
    public static Pieces fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Pieces p = new Pieces()
                .setFrom((int)o.getFrom())
                .setTo((int)o.getTo());

        o.getStats().log(TAG);

        return p;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setFrom(getFrom())
                .setTo(getTo())
                .toJSON();
    }

    public int getFrom() {
        return mFrom;
    }

    public Pieces setFrom(int from) {
        mFrom = from;
        return this;
    }

    public int getTo() {
        return mTo;
    }

    public Pieces setTo(int to) {
        mTo = to;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mFrom;
        result = prime * result + mTo;
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
        Pieces other = (Pieces) obj;
        if (mFrom != other.mFrom)
            return false;
        if (mTo != other.mTo)
            return false;
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mFrom);
        dest.writeInt(this.mTo);
    }


}
