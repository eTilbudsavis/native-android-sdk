package com.shopgun.android.sdk.pagedpublicationkit;

import java.util.List;

public interface PagedPublicationLoader {

    void load(OnLoadComplete callback);

    boolean isLoading();

    void cancel();

    interface OnLoadComplete {
        void onPublicationLoaded(PagedPublication publication);
        void onPagesLoaded(List<PagedPublicationPage> pages);
        void onHotspotsLoaded(PagedPublicationHotspots hotspots);
    }

}
