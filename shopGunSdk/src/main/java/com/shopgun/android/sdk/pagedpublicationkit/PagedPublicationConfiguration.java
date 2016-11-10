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

    PagedPublicationHotspots getHotspots();
    boolean hasHotspots();

    boolean hasIntro();
    View getIntro(ViewGroup container, int page);

    boolean hasOutro();
    View getOutro(ViewGroup container, int page);

    void load(OnLoadComplete callback);
    boolean isLoading();
    void cancel();
    String getSource();

    interface OnLoadComplete {
        void onPublicationLoaded(PagedPublication publication);
        void onPagesLoaded(List<? extends PagedPublicationPage> pages);
        void onHotspotsLoaded(PagedPublicationHotspots hotspots);
        void onError(List<PublicationException> ex);
    }

}
