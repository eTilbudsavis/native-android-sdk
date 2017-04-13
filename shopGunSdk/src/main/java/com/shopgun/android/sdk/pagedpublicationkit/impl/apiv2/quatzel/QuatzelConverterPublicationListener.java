package com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.eventskit.Event;
import com.shopgun.android.sdk.eventskit.EventListener;
import com.shopgun.android.sdk.eventskit.EzEvent;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.CatalogConfiguration;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.CatalogPublication;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel.impl.ClockFactory;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel.impl.PageflipStatsCollectorImpl;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel.impl.StatDeliveryImpl;

public class QuatzelConverterPublicationListener implements EventListener {

    private final CatalogPublication mPublication;
    private final CatalogConfiguration mConfig;
    private final StatDelivery mStatDelivery;
    private final String mViewSession;
    private final Clock mClock;
    PageflipStatsCollector mCollector;

    public QuatzelConverterPublicationListener(CatalogPublication publication, CatalogConfiguration config, String viewSession) {
        mPublication = publication;
        mViewSession = viewSession;
        mClock = ClockFactory.getClock();
        mConfig = config;
        mStatDelivery = new StatDeliveryImpl(ShopGun.getInstance());
    }

    private PageflipStatsCollector createCollector(int[] pages) {
        return new PageflipStatsCollectorImpl(mViewSession, mPublication.getId(),
                pages, mConfig.getOrientation(), mClock, mStatDelivery);
    }

    private PageflipStatsCollector getCollector(int[] pages) {
        if (mCollector == null || mCollector.isCollected()) {
            mCollector = createCollector(pages);
        }
        return mCollector;
    }

    @Override
    public void onEvent(Event event) {

        String type = event.getType();
        JsonObject p = event.getProperties();

        switch (type) {
            case EzEvent.PAGED_PUBLICATION_OPENED:
                break;
            case EzEvent.PAGED_PUBLICATION_APPEARED:
                mCollector = getCollector(new int[]{});
                break;
            case EzEvent.PAGED_PUBLICATION_PAGE_SPREAD_APPEARED:
                JsonArray pageNumbers = p.getAsJsonArray("pagedPublicationPageSpread");
                int[] pages = new int[pageNumbers.size()];
                for (int i = 0; i < pageNumbers.size(); i++) {
                    pages[i] = pageNumbers.get(i).getAsInt();
                }
                break;
            case EzEvent.PAGED_PUBLICATION_PAGE_SPREAD_DISAPPEARED:
                break;
            case EzEvent.PAGED_PUBLICATION_PAGE_SPREAD_ZOOM_IN:
                break;
            case EzEvent.PAGED_PUBLICATION_PAGE_SPREAD_ZOOM_OUT:
                break;
        }


    }

}
