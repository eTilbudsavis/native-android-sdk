package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.graphics.RectF;

import com.shopgun.android.sdk.model.Hotspot;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationOffer;
import com.shopgun.android.utils.PolygonF;

import java.util.List;

public class CatalogHotspot implements PagedPublicationHotspot {

    public static final String TAG = CatalogHotspot.class.getSimpleName();

    Hotspot mHotspot;
    CatalogOffer mOffer;

    public CatalogHotspot(Hotspot hotspot) {
        mHotspot = hotspot;
        mOffer = new CatalogOffer(mHotspot.getOffer());
    }

    @Override
    public boolean hasPolygonAt(int[] visiblePages, int clickedPage, float x, float y) {
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
    public List<PolygonF> getPolygons() {
        return mHotspot.getLocations();
    }

    @Override
    public List<PolygonF> getPolygons(int[] pages) {
        return mHotspot.getLocationsForPages(pages);
    }

    @Override
    public RectF getBoundsForPages(int[] pages) {
        return mHotspot.getBoundsForPages(pages);
    }

    @Override
    public String toString() {
        return mHotspot.toString();
    }

}
