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
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.utils.palette.MaterialColor;
import com.shopgun.android.utils.palette.SgnColor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Branding implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Branding.class);

    private String mName;
    private String mWebsite;
    private String mDescription;
    private String mLogo;
    private MaterialColor mColor;
    private Pageflip mPageflip;

    public Branding() {

    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a branding
     * @return A {@link List} of POJO
     */
    public static List<Branding> fromJSON(JSONArray array) {
        List<Branding> list = new ArrayList<Branding>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Branding.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a branding
     * @return A {@link Branding}, or {@code null} if {@code object} is {@code null}
     */
    public static Branding fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }
        SgnJson o = new SgnJson(object);
        Branding branding = new Branding();
        branding.setName(o.getName())
                .setDescription(o.getDescription())
                .setWebsite(o.getWebsite())
                .setLogo(o.getLogo())
                .setColor(o.getMaterialColor())
                .setPageflip(o.getPageflip());

        o.getStats().log(TAG);

        return branding;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setName(getName())
                .setWebsite(getWebsite())
                .setDescription(getDescription())
                .setLogo(getLogo())
                .setColor(getColor())
                .setPageflip(getPageflip())
                .toJSON();
    }

    public String getName() {
        return mName;
    }

    public Branding setName(String name) {
        mName = name;
        return this;
    }

    public String getDescription() {
        return mDescription;
    }

    public Branding setDescription(String description) {
        mDescription = description;
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

    public int getColor() {
        return getMaterialColor().getValue();
    }

    public MaterialColor getMaterialColor() {
        if (mColor == null) {
            mColor = new SgnColor();
        }
        return mColor;
    }

    public Branding setColor(int color) {
        setColor(new SgnColor(color));
        return this;
    }

    public Branding setColor(MaterialColor color) {
        mColor = color;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Branding branding = (Branding) o;

        if (mName != null ? !mName.equals(branding.mName) : branding.mName != null) return false;
        if (mWebsite != null ? !mWebsite.equals(branding.mWebsite) : branding.mWebsite != null) return false;
        if (mDescription != null ? !mDescription.equals(branding.mDescription) : branding.mDescription != null)
            return false;
        if (mLogo != null ? !mLogo.equals(branding.mLogo) : branding.mLogo != null) return false;
        if (mColor != null ? !mColor.equals(branding.mColor) : branding.mColor != null) return false;
        return mPageflip != null ? mPageflip.equals(branding.mPageflip) : branding.mPageflip == null;

    }

    @Override
    public int hashCode() {
        int result = mName != null ? mName.hashCode() : 0;
        result = 31 * result + (mWebsite != null ? mWebsite.hashCode() : 0);
        result = 31 * result + (mDescription != null ? mDescription.hashCode() : 0);
        result = 31 * result + (mLogo != null ? mLogo.hashCode() : 0);
        result = 31 * result + (mColor != null ? mColor.hashCode() : 0);
        result = 31 * result + (mPageflip != null ? mPageflip.hashCode() : 0);
        return result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeString(this.mWebsite);
        dest.writeString(this.mDescription);
        dest.writeString(this.mLogo);
        dest.writeParcelable(this.mColor, 0);
        dest.writeParcelable(this.mPageflip, 0);
    }

    protected Branding(Parcel in) {
        this.mName = in.readString();
        this.mWebsite = in.readString();
        this.mDescription = in.readString();
        this.mLogo = in.readString();
        this.mColor = in.readParcelable(MaterialColor.class.getClassLoader());
        this.mPageflip = in.readParcelable(Pageflip.class.getClassLoader());
    }

    public static final Creator<Branding> CREATOR = new Creator<Branding>() {
        public Branding createFromParcel(Parcel source) {
            return new Branding(source);
        }

        public Branding[] newArray(int size) {
            return new Branding[size];
        }
    };
}
