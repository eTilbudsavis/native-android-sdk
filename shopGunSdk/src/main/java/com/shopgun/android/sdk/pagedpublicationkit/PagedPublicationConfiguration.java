package com.shopgun.android.sdk.pagedpublicationkit;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.verso.VersoSpreadConfiguration;

import java.util.List;

public interface PagedPublicationConfiguration extends VersoSpreadConfiguration, Parcelable {

    PagedPublication getPublication();
    boolean hasPublication();

    List<? extends PagedPublicationPage> getPages();
    boolean hasPages();

    PagedPublicationHotspotCollection getHotspotCollection();
    boolean hasHotspotCollection();

    boolean hasIntro();
    View getIntroPageView(ViewGroup container, int page);

    boolean hasOutro();
    View getOutroPageView(ViewGroup container, int page);

    void load(OnLoadComplete callback);
    boolean isLoading();
    void cancel();
    String getSource();

    interface OnLoadComplete {
        void onPublicationLoaded(PagedPublication publication);
        void onPagesLoaded(List<? extends PagedPublicationPage> pages);
        void onHotspotsLoaded(PagedPublicationHotspotCollection hotspots);
        void onError(List<PublicationException> ex);
    }

}
