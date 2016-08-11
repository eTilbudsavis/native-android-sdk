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

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HotspotMap implements IJson<JSONArray>,Parcelable {

    public static final String TAG = Constants.getTag(HotspotMap.class);

    private static final String TYPE_OFFER = "offer";
    private static final int[] mRectColors = { Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.MAGENTA };
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, List<Hotspot>> mMap = new HashMap<Integer, List<Hotspot>>();
    private boolean mIsNormalized = false;

    public HotspotMap() {
    }

    public void put(int page, List<Hotspot> list) {
        mMap.put(page, list);
    }

    public List<Hotspot> get(int page) {
        return mMap.get(page);
    }

    public static HotspotMap fromJSON(Dimension d, JSONArray hotspots) {

        HotspotMap map = new HotspotMap();
        if (hotspots == null) {
            return map;
        }

        for (int i = 0; i < hotspots.length(); i++) {
            JSONObject hotspot = hotspots.optJSONObject(i);
            String type = hotspot.optString(SgnJson.TYPE, null);
            // We all know that someone is going to introduce a new type at some point, so might as well check now
            if (TYPE_OFFER.equals(type)) {

                try {
                    JSONObject offer = hotspot.getJSONObject(SgnJson.OFFER);
                    Offer o = Offer.fromJSON(offer);

                    int color = mRectColors[i % mRectColors.length];

                    JSONObject rectangleList = hotspot.getJSONObject(SgnJson.LOCATIONS);

                    List<String> keys = SgnUtils.copyIterator(rectangleList.keys());

                    for (String key : keys) {

                        Integer page = Integer.valueOf(key);
                        JSONArray rect = rectangleList.getJSONArray(key);

                        if (!map.mMap.containsKey(page)) {
                            map.put(page, new ArrayList<Hotspot>());
                        }

                        Hotspot h = Hotspot.fromJSON(rect);

                        h.setPage(page);
                        h.setOffer(o);
                        h.setType(type);
                        h.setColor(color);
                        h.setDualPage(keys.size() > 1);
                        map.get(page).add(h);

                    }
                } catch (JSONException e) {
                    SgnLog.e(TAG, e.getMessage(), e);
                }

            }

        }

        map.normalize(d);

        return map;
    }

    public synchronized void normalize(Bitmap b) {
        if (!mIsNormalized) {
            normalize(Dimension.fromBitmap(b));
        }
    }

    public synchronized void normalize(Dimension d) {

        if (mIsNormalized || !d.isSet()) {
            return;
        }

        Set<Integer> keys = mMap.keySet();
        if (keys.isEmpty()) {
            return;
        }

        for (Integer i : keys) {

            List<Hotspot> hotspots = get(i);
            if (hotspots != null && !hotspots.isEmpty()) {
                for (Hotspot h : hotspots) {
                    h.normalize(d);
                }
            }

        }

        mIsNormalized = true;
    }

    public List<Hotspot> getHotspots(int page, double xPercent, double yPercent, int[] pages) {
        return getHotspots(page, xPercent, yPercent, Hotspot.SIGNIFICANT_AREA, pages);
    }

    public List<Hotspot> getHotspots(int page, double xPercent, double yPercent, double minArea, int[] pages) {
        List<Hotspot> list = new ArrayList<Hotspot>();
        List<Hotspot> lh = mMap.get(page);
        if (lh == null) {
            return list;
        }
        for (Hotspot h : lh) {
            if (h.inBounds(xPercent, yPercent, minArea, pages)) {
                list.add(h);
            }
        }
        return list;
    }

    public JSONArray toJSON() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HotspotMap that = (HotspotMap) o;

        return mIsNormalized == that.mIsNormalized &&
                !(mMap != null ? !mMap.equals(that.mMap) : that.mMap != null);

    }

    @Override
    public int hashCode() {
        int result = mMap != null ? mMap.hashCode() : 0;
        result = 31 * result + (mIsNormalized ? 1 : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMap.size());
        for (Map.Entry<Integer, List<Hotspot>> e : mMap.entrySet()) {
            dest.writeInt(e.getKey());
            dest.writeTypedList(e.getValue());
        }
        dest.writeByte(mIsNormalized ? (byte) 1 : (byte) 0);
    }

    protected HotspotMap(Parcel in) {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            Integer key = in.readInt();
            List<Hotspot> value = new ArrayList<Hotspot>();
            in.readTypedList(value, Hotspot.CREATOR);
            this.put(key, value);
        }
        this.mIsNormalized = in.readByte() != 0;
    }

    public static final Parcelable.Creator<HotspotMap> CREATOR = new Parcelable.Creator<HotspotMap>() {
        public HotspotMap createFromParcel(Parcel source) {
            return new HotspotMap(source);
        }

        public HotspotMap[] newArray(int size) {
            return new HotspotMap[size];
        }
    };
}
