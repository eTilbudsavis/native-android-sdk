package com.shopgun.android.sdk.pagedpublicationkit;

import android.support.annotation.NonNull;

import java.util.List;

public interface PagedPublicationHotspotCollection {

    @NonNull
    List<PagedPublicationHotspot> getPagedPublicationHotspots(int[] visiblePages, int clickedPage, float x, float y);

    @NonNull
    List<PagedPublicationHotspot> getPagedPublicationHotspots(int[] visiblePages);

}
