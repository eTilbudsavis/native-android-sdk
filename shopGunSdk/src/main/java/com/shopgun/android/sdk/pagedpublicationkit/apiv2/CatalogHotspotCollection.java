package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shopgun.android.sdk.model.Hotspot;
import com.shopgun.android.sdk.model.HotspotMap;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspotCollection;

import java.util.ArrayList;
import java.util.List;

public class CatalogHotspotCollection implements PagedPublicationHotspotCollection, Parcelable {

    HotspotMap mHotspotMap;

    public CatalogHotspotCollection(HotspotMap hotspotMap) {
        mHotspotMap = hotspotMap;
    }

    @NonNull
    @Override
    public List<PagedPublicationHotspot> getHotspots(int[] visiblePages, int clickedPage, float x, float y) {
        List<Hotspot> list = mHotspotMap.getHotspots(visiblePages, clickedPage, x, y);
        List<PagedPublicationHotspot> hotspots = new ArrayList<>(list.size());
        for (Hotspot h : list) {
            hotspots.add(new CatalogHotspot(h));
        }
        return hotspots;
    }

    @NonNull
    @Override
    public List<PagedPublicationHotspot> getHotspots(int[] visiblePages) {
        List<Hotspot> list = mHotspotMap.getHotspots(visiblePages);
        List<PagedPublicationHotspot> hotspots = new ArrayList<>(list.size());
        for (Hotspot h : list) {
            hotspots.add(new CatalogHotspot(h));
        }
        return hotspots;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    protected CatalogHotspotCollection(Parcel in) {
    }

    public static final Creator<CatalogHotspotCollection> CREATOR = new Creator<CatalogHotspotCollection>() {
        @Override
        public CatalogHotspotCollection createFromParcel(Parcel source) {
            return new CatalogHotspotCollection(source);
        }

        @Override
        public CatalogHotspotCollection[] newArray(int size) {
            return new CatalogHotspotCollection[size];
        }
    };

}
