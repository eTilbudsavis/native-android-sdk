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
import com.shopgun.android.sdk.utils.ColorUtils;
import com.shopgun.android.sdk.utils.Json;

import org.json.JSONException;
import org.json.JSONObject;

public class Branding implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Branding.class);
    public static Parcelable.Creator<Branding> CREATOR = new Parcelable.Creator<Branding>() {
        public Branding createFromParcel(Parcel source) {
            return new Branding(source);
        }

        public Branding[] newArray(int size) {
            return new Branding[size];
        }
    };
    private String mName;
    private String mWebsite;
    private String mLogo;
    private Integer mColor;
    private Pageflip mPageflip;

    public Branding() {

    }

    private Branding(Parcel in) {
        this.mName = in.readString();
        this.mWebsite = in.readString();
        this.mLogo = in.readString();
        this.mColor = (Integer) in.readValue(Integer.class.getClassLoader());
        this.mPageflip = in.readParcelable(Pageflip.class.getClassLoader());
    }

    public static Branding fromJSON(JSONObject branding) {
        Branding b = new Branding();
        if (branding == null) {
            return b;
        }

        try {
            b.setName(Json.valueOf(branding, JsonKey.NAME));
            b.setWebsite(Json.valueOf(branding, JsonKey.WEBSITE));
            b.setLogo(Json.valueOf(branding, JsonKey.LOGO));
            b.setColor(Json.colorValueOf(branding, JsonKey.COLOR));
            b.setPageflip(Pageflip.fromJSON(branding.getJSONObject(JsonKey.PAGEFLIP)));
        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }
        return b;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(JsonKey.NAME, Json.nullCheck(getName()));
            o.put(JsonKey.WEBSITE, Json.nullCheck(getWebsite()));
            o.put(JsonKey.LOGO, Json.nullCheck(getLogo()));
            o.put(JsonKey.COLOR, Json.nullCheck(ColorUtils.toString(getColor())));
            o.put(JsonKey.PAGEFLIP, Json.nullCheck(getPageflip().toJSON()));
        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }
        return o;
    }

    public String getName() {
        return mName;
    }

    public Branding setName(String name) {
        mName = name;
        return this;
    }

    public String getWebsite() {
        return mWebsite;
    }

    public Branding setWebsite(String website) {
        mWebsite = website;
        return this;
    }

    public String getLogo() {
        return mLogo;
    }

    public Branding setLogo(String logo) {
        mLogo = logo;
        return this;
    }

    public Integer getColor() {
        return mColor;
    }

    public Branding setColor(Integer color) {
        mColor = ColorUtils.stripAlpha(color);
        return this;
    }

    public Pageflip getPageflip() {
        return mPageflip;
    }

    public Branding setPageflip(Pageflip pageflip) {
        mPageflip = pageflip;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mColor == null) ? 0 : mColor.hashCode());
        result = prime * result + ((mLogo == null) ? 0 : mLogo.hashCode());
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        result = prime * result
                + ((mPageflip == null) ? 0 : mPageflip.hashCode());
        result = prime * result
                + ((mWebsite == null) ? 0 : mWebsite.hashCode());
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
        Branding other = (Branding) obj;
        if (mColor == null) {
            if (other.mColor != null)
                return false;
        } else if (!mColor.equals(other.mColor))
            return false;
        if (mLogo == null) {
            if (other.mLogo != null)
                return false;
        } else if (!mLogo.equals(other.mLogo))
            return false;
        if (mName == null) {
            if (other.mName != null)
                return false;
        } else if (!mName.equals(other.mName))
            return false;
        if (mPageflip == null) {
            if (other.mPageflip != null)
                return false;
        } else if (!mPageflip.equals(other.mPageflip))
            return false;
        if (mWebsite == null) {
            if (other.mWebsite != null)
                return false;
        } else if (!mWebsite.equals(other.mWebsite))
            return false;
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeString(this.mWebsite);
        dest.writeString(this.mLogo);
        dest.writeValue(this.mColor);
        dest.writeParcelable(this.mPageflip, flags);
    }

}
