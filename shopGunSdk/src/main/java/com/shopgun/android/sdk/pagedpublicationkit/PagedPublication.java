package com.shopgun.android.sdk.pagedpublicationkit;

import androidx.annotation.ColorInt;

public interface PagedPublication {
    String getId();
    @ColorInt int getBackgroundColor();
    int getPageCount();
    float getAspectRatio();
    String getOwnerId();
}
