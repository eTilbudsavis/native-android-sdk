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
import com.shopgun.android.sdk.model.interfaces.IErn;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.palette.MaterialColor;
import com.shopgun.android.sdk.palette.SgnColor;
import com.shopgun.android.sdk.utils.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <p>This class is a representation of a dealer as the API v2 exposes it</p>
 *
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/dealers.html">Dealer Reference</a>
 * documentation, on the engineering blog.
 * </p>
 */
public class Dealer implements IErn<Dealer>, IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Dealer.class);
    /**
     * Compare object, that uses {@link Dealer#getName() name} to compare two lists.
     */
    public static Comparator<Dealer> NAME_COMPARATOR = new Comparator<Dealer>() {

        public int compare(Dealer item1, Dealer item2) {

            if (item1 == null || item2 == null) {
                return item1 == null ? (item2 == null ? 0 : 1) : -1;
            } else {
                String t1 = item1.getName();
                String t2 = item2.getName();
                if (t1 == null || t2 == null) {
                    return t1 == null ? (t2 == null ? 0 : 1) : -1;
                }

                //ascending order
                return t1.compareToIgnoreCase(t2);
            }

        }

    };

    private String mErn;
    private String mName;
    private String mWebsite;
    private String mLogo;
    private MaterialColor mColor;
    private Pageflip mPageflip;

    public Dealer() {

    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a dealer
     * @return A {@link List} of POJO
     */
    public static List<Dealer> fromJSON(JSONArray array) {
        ArrayList<Dealer> list = new ArrayList<Dealer>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = Json.getObject(array, i);
            if (o != null) {
                list.add(Dealer.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a dealer
     * @return A {@link Dealer}, or {@code null} if {@code object} is {@code null}
     */
    public static Dealer fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        Dealer d = new Dealer();
        d.setId(Json.valueOf(object, JsonKeys.ID));
        d.setErn(Json.valueOf(object, JsonKeys.ERN));
        d.setName(Json.valueOf(object, JsonKeys.NAME));
        d.setWebsite(Json.valueOf(object, JsonKeys.WEBSITE));
        d.setLogo(Json.valueOf(object, JsonKeys.LOGO));
        d.setColor(Json.colorValueOf(object, JsonKeys.COLOR));
        JSONObject jPageflip = Json.getObject(object, JsonKeys.PAGEFLIP);
        d.setPageflip(Pageflip.fromJSON(jPageflip));
        return d;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(JsonKeys.ID, Json.nullCheck(getId()));
            o.put(JsonKeys.ERN, Json.nullCheck(getErn()));
            o.put(JsonKeys.NAME, Json.nullCheck(getName()));
            o.put(JsonKeys.WEBSITE, Json.nullCheck(getWebsite()));
            o.put(JsonKeys.LOGO, Json.nullCheck(getLogo()));
            o.put(JsonKeys.COLOR, Json.colorToSgnJson(getMaterialColor()));
            o.put(JsonKeys.PAGEFLIP, Json.nullCheck(getPageflip().toJSON()));
        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }
        return o;
    }

    public String getId() {
        if (mErn == null) {
            return null;
        }
        String[] parts = mErn.split(":");
        return parts[parts.length - 1];
    }

    public Dealer setId(String id) {
        setErn((id == null) ? null : String.format("ern:%s:%s", getErnType(), id));
        return this;
    }

    public String getErn() {
        return mErn;
    }

    public Dealer setErn(String ern) {
        if (ern == null || (ern.startsWith("ern:") && ern.split(":").length == 3 && ern.contains(getErnType()))) {
            mErn = ern;
        }
        return this;
    }

    public String getErnType() {
        return IErn.TYPE_DEALER;
    }

    public String getName() {
        return mName;
    }

    public Dealer setName(String name) {
        mName = name;
        return this;
    }

    public String getWebsite() {
        return mWebsite;
    }

    public Dealer setWebsite(String website) {
        mWebsite = website;
        return this;
    }

    public String getLogo() {
        return mLogo;
    }

    public Dealer setLogo(String logo) {
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

    public Dealer setColor(int color) {
        setColor(new SgnColor(color));
        return this;
    }

    public Dealer setColor(SgnColor color) {
        mColor = color;
        return this;
    }

    public Pageflip getPageflip() {
        return mPageflip;
    }

    public Dealer setPageflip(Pageflip pageflip) {
        mPageflip = pageflip;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dealer dealer = (Dealer) o;

        if (mErn != null ? !mErn.equals(dealer.mErn) : dealer.mErn != null) return false;
        if (mName != null ? !mName.equals(dealer.mName) : dealer.mName != null) return false;
        if (mWebsite != null ? !mWebsite.equals(dealer.mWebsite) : dealer.mWebsite != null) return false;
        if (mLogo != null ? !mLogo.equals(dealer.mLogo) : dealer.mLogo != null) return false;
        if (mColor != null ? !mColor.equals(dealer.mColor) : dealer.mColor != null) return false;
        return !(mPageflip != null ? !mPageflip.equals(dealer.mPageflip) : dealer.mPageflip != null);

    }

    @Override
    public int hashCode() {
        int result = mErn != null ? mErn.hashCode() : 0;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mWebsite != null ? mWebsite.hashCode() : 0);
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
        dest.writeString(this.mErn);
        dest.writeString(this.mName);
        dest.writeString(this.mWebsite);
        dest.writeString(this.mLogo);
        dest.writeParcelable(this.mColor, 0);
        dest.writeParcelable(this.mPageflip, 0);
    }

    protected Dealer(Parcel in) {
        this.mErn = in.readString();
        this.mName = in.readString();
        this.mWebsite = in.readString();
        this.mLogo = in.readString();
        this.mColor = in.readParcelable(MaterialColor.class.getClassLoader());
        this.mPageflip = in.readParcelable(Pageflip.class.getClassLoader());
    }

    public static final Creator<Dealer> CREATOR = new Creator<Dealer>() {
        public Dealer createFromParcel(Parcel source) {
            return new Dealer(source);
        }

        public Dealer[] newArray(int size) {
            return new Dealer[size];
        }
    };
}
