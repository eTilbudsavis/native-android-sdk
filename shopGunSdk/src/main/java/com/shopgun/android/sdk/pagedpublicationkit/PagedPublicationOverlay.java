package com.shopgun.android.sdk.pagedpublicationkit;

import java.util.List;

public interface PagedPublicationOverlay {
    void showHotspots(List<PagedPublicationHotspot> hotspots);
    void hideHotspots();
}
