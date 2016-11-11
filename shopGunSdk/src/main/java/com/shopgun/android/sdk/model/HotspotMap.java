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
import android.support.annotation.NonNull;

import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;
import com.shopgun.android.sdk.pagedpublicationkit.apiv2.CatalogHotspot;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.utils.TextUtils;
import com.shopgun.android.utils.log.L;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HotspotMap implements IJson<JSONArray>, Parcelable {

    public static final String TAG = Constants.getTag(HotspotMap.class);

    private List<Hotspot> mHotspots = new ArrayList<>();
    private boolean mNormalised;

    public static HotspotMap fromJSON(Dimension dimension, JSONArray hotspots) {
        HotspotMap list = new HotspotMap();
        if (hotspots == null) {
            return list;
        }

        Hotspot tmp = null;
        for (int i = 0; i < hotspots.length(); i++) {
            JSONObject hotspot = hotspots.optJSONObject(i);
            String type = hotspot.optString(SgnJson.TYPE, null);
            // We all know that someone is going to introduce a new type at some point, so might as well check now
            if (Hotspot.TYPE.equals(type)) {
                Hotspot h = Hotspot.fromJSON(hotspot);
                list.mHotspots.add(h);
                if (i == 0) {
                    tmp = h;
                }
            }
        }

        L.d(TAG, "Hotspot.preNormalise : " + tmp.toString());
        list.normalize(dimension);
        L.d(TAG, "Hotspot.postNormalise: " + tmp.toString());

        return list;
    }

    public HotspotMap() {
    }

    public synchronized void normalize(Bitmap b) {
        normalize(Dimension.fromBitmap(b));
    }

    public synchronized void normalize(Dimension d) {
        if (d != null && d.isSet()) {
            normalize(d.getWidth(), d.getHeight());
        }
    }

    public void normalize(double width, double height) {
        if (!mNormalised) {
            for (Hotspot h : mHotspots) {
                h.normalize(width, height);
            }
        }
        mNormalised = true;
    }

    @Override
    public JSONArray toJSON() {
        return null;
    }

    @NonNull
    public List<Hotspot> getHotspots(int[] visiblePages, int clickedPage, float x, float y) {
        ArrayList<Hotspot> list = new ArrayList<>();
        float length = (float) visiblePages.length;
        float xOnClickedPage = (x%(1f/length))*length;
        for (Hotspot h : mHotspots) {
            if (h.hasLocationAt(visiblePages, clickedPage, xOnClickedPage, y)) {
                list.add(h);
            }
        }
        return list;
    }

    @NonNull
    public List<Hotspot> getHotspots(int[] pages) {
        ArrayList<Hotspot> list = new ArrayList<>();
        for (Hotspot h : mHotspots) {
            if (isMatch(h.getPages(), pages)) {
                list.add(h);
            }
        }
        return list;
    }

    private boolean isMatch(int[] hotspotPages, int[] searchPages) {
        for (int hotspotPage : hotspotPages) {
            for (int page : searchPages) {
                if (hotspotPage == page) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mHotspots);
        dest.writeByte(this.mNormalised ? (byte) 1 : (byte) 0);
    }

    protected HotspotMap(Parcel in) {
        this.mHotspots = in.createTypedArrayList(Hotspot.CREATOR);
        this.mNormalised = in.readByte() != 0;
    }

    public static final Creator<HotspotMap> CREATOR = new Creator<HotspotMap>() {
        @Override
        public HotspotMap createFromParcel(Parcel source) {
            return new HotspotMap(source);
        }

        @Override
        public HotspotMap[] newArray(int size) {
            return new HotspotMap[size];
        }
    };

}
