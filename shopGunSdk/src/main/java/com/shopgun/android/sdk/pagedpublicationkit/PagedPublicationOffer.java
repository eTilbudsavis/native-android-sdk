package com.shopgun.android.sdk.pagedpublicationkit;

import android.os.Parcelable;

import com.shopgun.android.sdk.model.Pricing;
import com.shopgun.android.sdk.model.Quantity;

import java.util.Date;

public interface PagedPublicationOffer extends Parcelable {

    String getId();
    String getErn();
    String getHeading();
    Pricing getPricing();
    Quantity getQuantity();
    Date getRunFrom();
    Date getRunTill();

}
