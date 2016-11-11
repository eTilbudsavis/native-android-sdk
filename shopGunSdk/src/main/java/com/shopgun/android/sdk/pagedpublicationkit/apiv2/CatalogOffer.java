package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.model.Offer;
import com.shopgun.android.sdk.model.Pricing;
import com.shopgun.android.sdk.model.Quantity;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationOffer;

import java.util.Date;

public class CatalogOffer implements PagedPublicationOffer, Parcelable {

    Offer mOffer;

    public CatalogOffer(Offer offer) {
        mOffer = offer;
    }

    @Override
    public String getId() {
        return mOffer.getId();
    }

    @Override
    public String getErn() {
        return mOffer.getErn();
    }

    @Override
    public String getHeading() {
        return mOffer.getHeading();
    }

    @Override
    public Pricing getPricing() {
        return mOffer.getPricing();
    }

    @Override
    public Quantity getQuantity() {
        return mOffer.getQuantity();
    }

    @Override
    public Date getRunFrom() {
        return mOffer.getRunFrom();
    }

    @Override
    public Date getRunTill() {
        return mOffer.getRunTill();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mOffer, flags);
    }

    protected CatalogOffer(Parcel in) {
        this.mOffer = in.readParcelable(Offer.class.getClassLoader());
    }

    public static final Creator<CatalogOffer> CREATOR = new Creator<CatalogOffer>() {
        @Override
        public CatalogOffer createFromParcel(Parcel source) {
            return new CatalogOffer(source);
        }

        @Override
        public CatalogOffer[] newArray(int size) {
            return new CatalogOffer[size];
        }
    };

}
