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

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Links implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Links.class);
    public static Parcelable.Creator<Links> CREATOR = new Parcelable.Creator<Links>() {
        public Links createFromParcel(Parcel source) {
            return new Links(source);
        }

        public Links[] newArray(int size) {
            return new Links[size];
        }
    };
    private String mWebshop;

    public Links() {

    }

    private Links(Parcel in) {
        this.mWebshop = in.readString();
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Links}
     * @return A {@link List} of POJO
     */
    public static List<Links> fromJSON(JSONArray array) {
        List<Links> list = new ArrayList<Links>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Links.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Links}
     * @return A {@link Links}, or {@code null} if {@code object} is {@code null}
     */
    public static Links fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Links l = new Links()
                .setWebshop(o.getWebshop());

        o.getStats().log(TAG);

        return l;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setWebshop(getWebshop())
                .toJSON();
    }

    public String getWebshop() {
        return mWebshop;
    }

    public Links setWebshop(String url) {
        mWebshop = url;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mWebshop == null) ? 0 : mWebshop.hashCode());
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
        Links other = (Links) obj;
        if (mWebshop == null) {
            if (other.mWebshop != null)
                return false;
        } else if (!mWebshop.equals(other.mWebshop))
            return false;
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mWebshop);
    }

}
