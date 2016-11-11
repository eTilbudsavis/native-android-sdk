package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import com.shopgun.android.sdk.model.Hotspot;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationOffer;

public class CatalogHotspot implements PagedPublicationHotspot {

    public static final String TAG = CatalogHotspot.class.getSimpleName();

    Hotspot mHotspot;
    CatalogOffer mOffer;

    public CatalogHotspot(Hotspot hotspot) {
        mHotspot = hotspot;
        mOffer = new CatalogOffer(mHotspot.getOffer());
    }

    @Override
    public boolean hasLocationAt(int[] visiblePages, int clickedPage, float x, float y) {
        return mHotspot.hasLocationAt(visiblePages, clickedPage, x, y);
    }

    @Override
    public int[] getPages() {
        return mHotspot.getPages();
    }

    @Override
    public String getType() {
        return mHotspot.getType();
    }

    @Override
    public PagedPublicationOffer getOffer() {
        return mOffer;
    }

    @Override
    public String toString() {
        return mHotspot.toString();
    }

}
