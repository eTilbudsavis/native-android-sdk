package com.shopgun.android.sdk.pagedpublicationkit;

import android.os.Parcelable;
import android.support.annotation.ColorInt;

public interface PagedPublication extends Parcelable {

    String getId();
    @ColorInt int getBackgroundColor();
    int getPageCount();
    float getAspectRatio();

}
