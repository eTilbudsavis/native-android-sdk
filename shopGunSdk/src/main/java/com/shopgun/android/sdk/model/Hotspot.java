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
import android.util.SparseArray;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.utils.PolygonF;
import com.shopgun.android.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Hotspot implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Hotspot.class);

    public static final double SIGNIFICANT_AREA = 0.02d;

    public static final String TYPE = "offer";

    SparseArray<PolygonF> mLocations = new SparseArray<>();
    private String mType;
    private Offer mOffer;

    public static Hotspot fromJSON(JSONObject hotspot) {
        Hotspot h = new Hotspot();
        if (hotspot == null) {
            return h;
        }

        try {

            String type = hotspot.optString(SgnJson.TYPE, null);
            h.setType(type);

            JSONObject offer = hotspot.getJSONObject(SgnJson.OFFER);
            Offer o = Offer.fromJSON(offer);
            h.setOffer(o);

            JSONObject locations = hotspot.getJSONObject(SgnJson.LOCATIONS);
            Iterator<String> it = locations.keys();
            while (it.hasNext()) {
                String page = it.next();
                int intPage = Integer.valueOf(page)-1;
                JSONArray location = locations.getJSONArray(page);
                PolygonF poly = new PolygonF(location.length());
                for (int i = 0; i < location.length(); i++) {
                    JSONArray point = location.getJSONArray(i);
                    float x = Float.valueOf(point.getString(0));
                    float y = Float.valueOf(point.getString(1));
                    poly.addPoint(x, y);
                }
                h.mLocations.append(intPage, poly);
            }

        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }

        return h;
    }

    public Hotspot() {
    }

    public void normalize(double width, double height) {
        for (PolygonF p : getLocations()) {
            for (int i = 0; i < p.npoints; i++) {
                p.ypoints[i] = p.ypoints[i] / (float) height;
                p.xpoints[i] = p.xpoints[i] / (float) width;
            }
        }
    }

    public int[] getPages() {
        int[] pages = new int[mLocations.size()];
        for(int i = 0; i < mLocations.size(); i++) {
            pages[i] = mLocations.keyAt(i);
        }
        return pages;
    }

    public List<PolygonF> getLocations() {
        return getLocationsForPages(getPages());
    }

    public List<PolygonF> getLocationsForPages(int[] pages) {
        List<PolygonF> locs = new ArrayList<>(mLocations.size());
        for (int p : pages) {
            PolygonF poly = mLocations.get(p);
            locs.add(poly);
        }
        return locs;
    }

    public boolean hasLocationAt(int[] visiblePages, int clickedPage, float x, float y) {
        PolygonF p = mLocations.get(clickedPage);
        return p != null && p.contains(x, y) && isAreaSignificant(visiblePages, clickedPage);
    }

    private boolean isAreaSignificant(int[] visiblePages, int clickedPage) {
        return isAreaSignificant(visiblePages, clickedPage, SIGNIFICANT_AREA);
    }

    private boolean isAreaSignificant(int[] visiblePages, int clickedPage, double minArea) {
        return !(visiblePages.length == 1 && mLocations.size() > 1) || getArea(clickedPage) > minArea;
    }

    private double getArea(int page) {
        PolygonF p = mLocations.get(page);
        return p == null ? 0 : Math.abs(p.getBounds().height()) * Math.abs(p.getBounds().width());
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

    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (PolygonF poly : getLocations()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(poly.toString());
        }
        String offer = (mOffer == null ? "null" : (mOffer.getHeading()));
        return "Hotspot[ type:" + mType + ", pages:" + TextUtils.join(",", getPages()) + ", locations:" + sb.toString() + ", offer:" + offer + " ]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSparseArray((SparseArray) this.mLocations);
        dest.writeString(this.mType);
        dest.writeParcelable(this.mOffer, flags);
    }

    protected Hotspot(Parcel in) {
        this.mLocations = in.readSparseArray(PolygonF.class.getClassLoader());
        this.mType = in.readString();
        this.mOffer = in.readParcelable(Offer.class.getClassLoader());
    }

    public static final Creator<Hotspot> CREATOR = new Creator<Hotspot>() {
        @Override
        public Hotspot createFromParcel(Parcel source) {
            return new Hotspot(source);
        }

        @Override
        public Hotspot[] newArray(int size) {
            return new Hotspot[size];
        }
    };
}