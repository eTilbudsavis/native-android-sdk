package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.model.HotspotMap;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspots;

public class CatalogHotspots implements PagedPublicationHotspots, Parcelable {

    public static CatalogHotspots from(HotspotMap hotspots) {
        // FIXME: 07/11/16 Parse HotspotMap to PagedPublicationHotspots
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public CatalogHotspots() {
    }

    protected CatalogHotspots(Parcel in) {
    }

    public static final Creator<CatalogHotspots> CREATOR = new Creator<CatalogHotspots>() {
        @Override
        public CatalogHotspots createFromParcel(Parcel source) {
            return new CatalogHotspots(source);
        }

        @Override
        public CatalogHotspots[] newArray(int size) {
            return new CatalogHotspots[size];
        }
    };
}
