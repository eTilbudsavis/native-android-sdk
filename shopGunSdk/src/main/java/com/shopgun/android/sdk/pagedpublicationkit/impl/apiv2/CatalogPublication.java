package com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2;

import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Dimension;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublication;

public class CatalogPublication implements PagedPublication, Parcelable {

    String mId;
    int mBackgroundColor;
    int mPageCount;
    float mAspectRatio;
    String mOwnerId;

    public CatalogPublication(String catalogId) {
        mId = catalogId;
    }

    public CatalogPublication(Catalog catalog) {
        mId = catalog.getId();
        mBackgroundColor = catalog.getBranding().getColor();
        mPageCount = catalog.getPageCount();
        mAspectRatio = getAspectRatio(catalog);
        mOwnerId = catalog.getDealerId();
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    @Override
    public int getPageCount() {
        return mPageCount;
    }

    @Override
    public float getAspectRatio() {
        return mAspectRatio;
    }

    private float getAspectRatio(Catalog catalog) {
        Dimension d = catalog.getDimension();
        if (d != null) {
            float width = d.getWidth() == null ? 1f : d.getWidth().floatValue();
            float height = d.getHeight() == null ? 1f : d.getHeight().floatValue();
            return width / height;
        }
        return 1f;
    }

    @Override
    public String getOwnerId() {
        return mOwnerId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeInt(this.mBackgroundColor);
        dest.writeInt(this.mPageCount);
        dest.writeFloat(this.mAspectRatio);
        dest.writeString(this.mOwnerId);
    }

    protected CatalogPublication(Parcel in) {
        this.mId = in.readString();
        this.mBackgroundColor = in.readInt();
        this.mPageCount = in.readInt();
        this.mAspectRatio = in.readFloat();
        this.mOwnerId = in.readString();
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
