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
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Api.JsonKey;
import com.shopgun.android.sdk.utils.Json;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static Links fromJSON(JSONObject links) {
        Links l = new Links();
        if (links == null) {
            return l;
        }

        l.setWebshop(Json.valueOf(links, JsonKey.WEBSHOP));

        return l;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(JsonKey.WEBSHOP, Json.nullCheck(getWebshop()));
        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }
        return o;
    }

    public String getWebshop() {
        return mWebshop;
    }

    public void setWebshop(String url) {
        mWebshop = url;
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
