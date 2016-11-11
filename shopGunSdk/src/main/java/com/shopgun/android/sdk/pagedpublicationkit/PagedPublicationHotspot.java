package com.shopgun.android.sdk.pagedpublicationkit;

public interface PagedPublicationHotspot {

    boolean hasLocationAt(int[] visiblePages, int clickedPage, float x, float y);
    int[] getPages();
    String getType();
    PagedPublicationOffer getOffer();

}
