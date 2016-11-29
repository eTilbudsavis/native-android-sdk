package com.shopgun.android.sdk.pagedpublicationkit;

import android.graphics.RectF;
import android.os.Parcelable;

import com.shopgun.android.utils.PolygonF;

import java.util.List;

public interface PagedPublicationHotspot extends Parcelable {

    boolean hasPolygonAt(int[] visiblePages, int clickedPage, float x, float y);
    int[] getPages();
    String getType();
    PagedPublicationOffer getOffer();
    List<PolygonF> getPolygons();
    List<PolygonF> getPolygons(int[] pages);
    RectF getBoundsForPages(int[] pages);

}
