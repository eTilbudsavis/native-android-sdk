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

public class Pageflip implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Pageflip.class);
    private String mLogo;
    private MaterialColor mColor;

    public Pageflip() {

    }

    public Pageflip(int color) {
        mColor = new SgnColor(color);
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Pageflip}
     * @return A {@link List} of POJO
     */
    public static List<Pageflip> fromJSON(JSONArray array) {
        List<Pageflip> list = new ArrayList<Pageflip>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Pageflip.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Pageflip}
     * @return A {@link Pageflip}, or {@code null} if {@code object} is {@code null}
     */
    public static Pageflip fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }
        SgnJson o = new SgnJson(object);

        Pageflip p = new Pageflip()
                .setLogo(o.getLogo())
                .setColor(o.getColor());

        o.getStats().log(TAG);

        return p;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setLogo(getLogo())
                .setColor(getColor())
                .toJSON();
    }

    public String getLogo() {
        return mLogo;
    }

    public Pageflip setLogo(String url) {
        mLogo = url;
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

    public Pageflip setColor(int color) {
        setColor(new SgnColor(color));
        return this;
    }

    public Pageflip setColor(MaterialColor color) {
        mColor = color;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pageflip pageflip = (Pageflip) o;

        if (mLogo != null ? !mLogo.equals(pageflip.mLogo) : pageflip.mLogo != null) return false;

        return !(mColor != null ? !mColor.equals(pageflip.mColor) : pageflip.mColor != null);

    }

    @Override
    public int hashCode() {
        int result = mLogo != null ? mLogo.hashCode() : 0;
        result = 31 * result + (mColor != null ? mColor.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLogo);
        dest.writeParcelable(this.mColor, 0);
    }

    protected Pageflip(Parcel in) {
        this.mLogo = in.readString();
        this.mColor = in.readParcelable(MaterialColor.class.getClassLoader());
    }

    public static final Creator<Pageflip> CREATOR = new Creator<Pageflip>() {
        public Pageflip createFromParcel(Parcel source) {
            return new Pageflip(source);
        }

        public Pageflip[] newArray(int size) {
            return new Pageflip[size];
        }
    };
}
