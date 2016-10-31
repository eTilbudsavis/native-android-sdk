package com.shopgun.android.sdk.pagedpublicationkit.impl;

import android.os.Parcel;

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Dimension;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublication;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationPage;

public class CatalogPublication implements PagedPublication {

    Catalog mCatalog;

    public CatalogPublication(String catalogId) {
        mCatalog = new Catalog();
        mCatalog.setId(catalogId);
    }

    public CatalogPublication(Catalog catalog) {
        mCatalog = catalog;
    }

    @Override
    public String getId() {
        return mCatalog.getId();
    }

    @Override
    public int getBackgroundColor() {
        return mCatalog.getBackground();
    }

    @Override
    public int getPageCount() {
        return mCatalog.getPageCount();
    }

    @Override
    public float getAspectRatio() {
        Dimension d = mCatalog.getDimension();
        if (d != null) {
            float width = d.getWidth() == null ? 1f : d.getWidth().floatValue();
            float height = d.getHeight() == null ? 1f : d.getHeight().floatValue();
            return width / height;
        }
        return 1f;
    }

    @Override
    public String getOwnerId() {
        return mCatalog.getDealerId();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCatalog, flags);
    }

    protected CatalogPublication(Parcel in) {
        this.mCatalog = in.readParcelable(Catalog.class.getClassLoader());
    }

    public static final Creator<CatalogPublication> CREATOR = new Creator<CatalogPublication>() {
        @Override
        public CatalogPublication createFromParcel(Parcel source) {
            return new CatalogPublication(source);
        }

        @Override
        public CatalogPublication[] newArray(int size) {
            return new CatalogPublication[size];
        }
    };
}
