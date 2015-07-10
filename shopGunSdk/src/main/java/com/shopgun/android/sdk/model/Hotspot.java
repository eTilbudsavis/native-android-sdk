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

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Hotspot implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Hotspot.class);
    /**
     * The default significant area
     */
    public static final double SIGNIFICANT_AREA = 0.01d;
    private static final long serialVersionUID = 7068341225117028048L;
    public static Parcelable.Creator<Hotspot> CREATOR = new Parcelable.Creator<Hotspot>() {
        public Hotspot createFromParcel(Parcel source) {
            return new Hotspot(source);
        }

        public Hotspot[] newArray(int size) {
            return new Hotspot[size];
        }
    };
    /**
     * The top most part of the hotspot, relative to the catalog.dimensions
     */
    public double mTop = Double.MIN_VALUE;
    /**
     * The bottom most part of the hotspot, relative to the catalog.dimensions
     */
    public double mBottom = Double.MIN_VALUE;
    /**
     * The left most part of the hotspot, relative to the catalog.dimensions
     */
    public double mLeft = Double.MIN_VALUE;
    /**
     * The top most part of the hotspot, relative to the catalog.dimensions
     */
    public double mRight = Double.MIN_VALUE;
    /**
     * The top most part of the hotspot. This is the absolute value
     */
    public double mAbsTop = Double.MIN_VALUE;
    /**
     * The bottom most part of the hotspot. This is the absolute value
     */
    public double mAbsBottom = Double.MIN_VALUE;
    /**
     * The left most part of the hotspot. This is the absolute value
     */
    public double mAbsLeft = Double.MIN_VALUE;
    /**
     * The top most part of the hotspot. This is the absolute value
     */
    public double mAbsRight = Double.MIN_VALUE;
    private String mType;
    private int mPage = 0;
    private Offer mOffer;
    private boolean mIsSpanningTwoPages = false;
    private int mColor = Color.TRANSPARENT;

    public Hotspot() {

    }

    private Hotspot(Parcel in) {
        this.mPage = in.readInt();
        this.mOffer = in.readParcelable(Offer.class.getClassLoader());
        this.mIsSpanningTwoPages = in.readByte() != 0;
        this.mTop = in.readDouble();
        this.mBottom = in.readDouble();
        this.mLeft = in.readDouble();
        this.mRight = in.readDouble();
        this.mAbsTop = in.readDouble();
        this.mAbsBottom = in.readDouble();
        this.mAbsLeft = in.readDouble();
        this.mAbsRight = in.readDouble();
        this.mColor = in.readInt();
    }

    public static Hotspot fromJSON(JSONArray jHotspot) {
        Hotspot h = new Hotspot();
        if (jHotspot == null) {
            return h;
        }

        // We expect the first JSONArray to have an additional 4 JSONArray's
        if (jHotspot.length() != 4) {
            SgnLog.w(TAG, "Expected jHotspot.length == 4, actual length: " + jHotspot.length());
            return h;
        }

        for (int i = 0; i < jHotspot.length(); i++) {

            try {

                JSONArray point = jHotspot.getJSONArray(i);
                if (point.length() != 2) {
                    SgnLog.w(TAG, "Expected hotspot.point.length == 2, actual length: " + point.length());
                    continue;
                }

                double x = Double.valueOf(point.getString(0));
                double y = Double.valueOf(point.getString(1));

                if (h.mAbsLeft == Double.MIN_VALUE) {
                    // Nothing set yet
                    h.mAbsLeft = x;
                } else if (h.mAbsLeft > x) {
                    // switch values
                    h.mAbsRight = h.mAbsLeft;
                    h.mAbsLeft = x;
                } else {
                    // no other options left
                    h.mAbsRight = x;
                }

                if (h.mAbsTop == Double.MIN_VALUE) {
                    // Nothing set yet
                    h.mAbsTop = y;
                } else if (h.mAbsTop > y) {
                    // switch values
                    h.mAbsBottom = h.mAbsTop;
                    h.mAbsTop = y;
                } else {
                    // no other options left
                    h.mAbsBottom = y;
                }

            } catch (JSONException e) {
                SgnLog.e(TAG, e.getMessage(), e);
            }
        }

        return h;
    }

    public void normalize(Dimension d) {
        mTop = mAbsTop / d.getHeight();
        mRight = mAbsRight / d.getWidth();
        mBottom = mAbsBottom / d.getHeight();
        mLeft = mAbsLeft / d.getWidth();
    }

    public boolean inBounds(double x, double y, double minArea, boolean landscape) {
        return inBounds(x, y) && isAreaSignificant(landscape);
    }

    public boolean inBounds(double x, double y) {
        return mTop < y && y < mBottom && mLeft < x && x < mRight;
    }

    public boolean isAreaSignificant(boolean landscape) {
        return isAreaSignificant(SIGNIFICANT_AREA, landscape);
    }

    public boolean isAreaSignificant(double minArea, boolean landscape) {
        if (!landscape && mIsSpanningTwoPages) {
            return getArea() > minArea;
        }
        return true;
    }

    public double getArea() {
        return Math.abs(mTop - mBottom) * Math.abs(mLeft - mRight);
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put("left", mLeft);
            o.put("top", mTop);
            o.put("right", mRight);
            o.put("bottom", mBottom);
            String offer = (mOffer == null ? "null" : mOffer.getHeading());
            o.put("offer", offer);
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return o;
    }

    public boolean isDualPage() {
        return mIsSpanningTwoPages;
    }

    public void setDualPage(boolean isDualPage) {
        mIsSpanningTwoPages = isDualPage;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        this.mPage = page;
    }

    public Offer getOffer() {
        return mOffer;
    }

    public void setOffer(Offer offer) {
        mOffer = offer;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    @Override
    public String toString() {
        String offer = (mOffer == null ? "null" : mOffer.getHeading());
        String text = "hotspot[offer:%s, t:%.2f, r:%.2f, b:%.2f, l:%.2f, absT:%.2f, absR:%.2f, absB:%.2f, absL:%.2f]";
        return String.format(text, offer, mTop, mRight, mBottom, mLeft, mAbsTop, mAbsRight, mAbsBottom, mAbsLeft);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(mAbsBottom);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mAbsLeft);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mAbsRight);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mAbsTop);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mBottom);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + mColor;
        result = prime * result + (mIsSpanningTwoPages ? 1231 : 1237);
        temp = Double.doubleToLongBits(mLeft);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((mOffer == null) ? 0 : mOffer.hashCode());
        result = prime * result + mPage;
        temp = Double.doubleToLongBits(mRight);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mTop);
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
        Hotspot other = (Hotspot) obj;
        if (Double.doubleToLongBits(mAbsBottom) != Double
                .doubleToLongBits(other.mAbsBottom))
            return false;
        if (Double.doubleToLongBits(mAbsLeft) != Double
                .doubleToLongBits(other.mAbsLeft))
            return false;
        if (Double.doubleToLongBits(mAbsRight) != Double
                .doubleToLongBits(other.mAbsRight))
            return false;
        if (Double.doubleToLongBits(mAbsTop) != Double
                .doubleToLongBits(other.mAbsTop))
            return false;
        if (Double.doubleToLongBits(mBottom) != Double
                .doubleToLongBits(other.mBottom))
            return false;
        if (mColor != other.mColor)
            return false;
        if (mIsSpanningTwoPages != other.mIsSpanningTwoPages)
            return false;
        if (Double.doubleToLongBits(mLeft) != Double
                .doubleToLongBits(other.mLeft))
            return false;
        if (mOffer == null) {
            if (other.mOffer != null)
                return false;
        } else if (!mOffer.equals(other.mOffer))
            return false;
        if (mPage != other.mPage)
            return false;
        if (Double.doubleToLongBits(mRight) != Double
                .doubleToLongBits(other.mRight))
            return false;
        if (Double.doubleToLongBits(mTop) != Double
                .doubleToLongBits(other.mTop))
            return false;
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPage);
        dest.writeParcelable(this.mOffer, flags);
        dest.writeByte(mIsSpanningTwoPages ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.mTop);
        dest.writeDouble(this.mBottom);
        dest.writeDouble(this.mLeft);
        dest.writeDouble(this.mRight);
        dest.writeDouble(this.mAbsTop);
        dest.writeDouble(this.mAbsBottom);
        dest.writeDouble(this.mAbsLeft);
        dest.writeDouble(this.mAbsRight);
        dest.writeInt(this.mColor);
    }

}