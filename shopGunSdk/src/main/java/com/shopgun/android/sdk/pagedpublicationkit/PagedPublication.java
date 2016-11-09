package com.shopgun.android.sdk.pagedpublicationkit;

import android.support.annotation.ColorInt;

public interface PagedPublication {
    String getId();
    @ColorInt int getBackgroundColor();
    int getPageCount();
    float getAspectRatio();
    String getOwnerId();
}
