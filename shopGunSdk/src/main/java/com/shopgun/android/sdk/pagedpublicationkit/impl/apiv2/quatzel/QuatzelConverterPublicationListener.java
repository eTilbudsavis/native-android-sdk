package com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.eventskit.Event;
import com.shopgun.android.sdk.eventskit.EventListener;
import com.shopgun.android.sdk.eventskit.EzEvent;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationConfiguration;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.CatalogConfiguration;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel.impl.ClockFactory;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel.impl.PageflipStatsCollectorImpl;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel.impl.StatDeliveryImpl;
import com.shopgun.android.utils.enums.Orientation;

import java.util.ArrayList;
import java.util.Arrays;

public class QuatzelConverterPublicationListener implements EventListener {

    public static final String TAG = QuatzelConverterPublicationListener.class.getSimpleName();

    private String mViewSession;
    CatalogConfiguration mConfig;
    private String mCatalogId;
    private Orientation mOrientation;
    private StatDelivery mStatDelivery;
    private Clock mClock;
    private ArrayList<PageflipStatsCollector> mCollectors;
    private boolean[] mPagesLoaded;

    public QuatzelConverterPublicationListener() {
    }

    public void setConfig(PagedPublicationConfiguration config, String viewSession) {
        if (!(config instanceof CatalogConfiguration)) {
            return;
        }
        mConfig = (CatalogConfiguration) config;
        int pageCount = mConfig.getPublicationPageCount();
        mConfig.getPageCount();
        mCatalogId = mConfig.getPublication().getId();
        mOrientation = mConfig.getOrientation();
        mCollectors = new ArrayList<>(pageCount);
        for (int i = 0; i < pageCount; i++) {
            mCollectors.add(null);
        }
        mPagesLoaded = new boolean[pageCount];
        mViewSession = viewSession;
        mClock = ClockFactory.getClock();
        mStatDelivery = new StatDeliveryImpl(ShopGun.getInstance(), true);
    }

    private boolean isPagesLoadedAndAppeared(PropInfo info) {
        for (int page : info.pages) {
            if (!mPagesLoaded[page]) {
                return false;
            }
        }
        return true;
    }

    private void startView(PropInfo info) {
        if (isPagesLoadedAndAppeared(info)) {
            getCollector(info).startView();
        }
    }

    private void stopView(PropInfo info) {
        if (isPagesLoadedAndAppeared(info)) {
            getCollector(info).stopView();
        }
    }

    private void startZoom(PropInfo info) {
        if (isPagesLoadedAndAppeared(info)) {
            getCollector(info).startZoom();
        }
    }

    private void stopZoom(PropInfo info) {
        if (isPagesLoadedAndAppeared(info)) {
            getCollector(info).stopZoom();
        }
    }

    private PageflipStatsCollector getCollector(PropInfo info) {
        PageflipStatsCollector collector = mCollectors.get(info.spread);
        if (collector == null) {
            collector = new PageflipStatsCollectorImpl(mViewSession, mCatalogId, info.rawPages, mOrientation, mClock, mStatDelivery);
            mCollectors.add(info.spread, collector);
        }
        return collector;
    }

    @Override
    public void onEvent(Event event) {

        PropInfo info = new PropInfo(event);
//        L.d(TAG, event.getType() + ", " + info.toString());
        if (!info.isCollectable() || mViewSession == null) {
            return;
        }

        switch (event.getType()) {
            case EzEvent.PAGED_PUBLICATION_DISAPPEARED:
                for (int i = 0; i < mPagesLoaded.length; i++) {
                    mPagesLoaded[i] = false;
                }
            case EzEvent.PAGED_PUBLICATION_PAGE_SPREAD_APPEARED:
                startView(info);
                break;
            case EzEvent.PAGED_PUBLICATION_PAGE_SPREAD_DISAPPEARED:
                stopView(info);
                PageflipStatsCollector collector = getCollector(info);
                collector.collect();
                mCollectors.remove(collector);
                break;
            case EzEvent.PAGED_PUBLICATION_PAGE_LOADED:
                mPagesLoaded[info.page] = true;
                startView(info);
                break;
            case EzEvent.PAGED_PUBLICATION_PAGE_SPREAD_ZOOM_IN:
                startZoom(info);
                break;
            case EzEvent.PAGED_PUBLICATION_PAGE_SPREAD_ZOOM_OUT:
                stopZoom(info);
                break;
        }


    }

    private static final int[] EMPTY_PAGES = new int[]{};

    class PropInfo {

        int[] rawPages;
        int rawPage;
        int spread;
        int[] pages;
        int page;

        PropInfo(Event event) {
            this(event.getProperties());
        }

        PropInfo(JsonObject properties) {

            if (properties.has("pagedPublicationPageSpread")) {
                JsonArray pageNumbers = properties.getAsJsonArray("pagedPublicationPageSpread");
                rawPages = new int[pageNumbers.size()];
                for (int i = 0; i < pageNumbers.size(); i++) {
                    rawPages[i] = pageNumbers.get(i).getAsInt();
                }
                rawPage = rawPages[0];
            } else if (properties.has("pagedPublicationPage")) {
                rawPage = properties.getAsJsonObject("pagedPublicationPage").get("pageNumber").getAsInt();
            } else {
                return;
            }

            int fix = (mConfig.hasIntro() ? 2 : 1);
            page = rawPage - fix;
            spread = mConfig.getSpreadPositionFromPage(page);
            pages = mConfig.getPagesFromSpreadPosition(spread);
            if (rawPages == null) {
                rawPages = Arrays.copyOf(pages, pages.length);
                for (int i = 0; i < rawPages.length; i++) {
                    rawPages[i] = rawPages[i] + fix;
                }
            }
        }

        boolean isCollectable() {
            return rawPages != null && page >= 0 && pages[pages.length-1] < mConfig.getPublicationPageCount();
        }

        @Override
        public String toString() {
            return "PropInfo{" +
                    "rawPages=" + Arrays.toString(rawPages) +
                    ", rawPage=" + rawPage +
                    ", spread=" + spread +
                    ", pages=" + Arrays.toString(pages) +
                    ", page=" + page +
                    '}';
        }
    }

}
