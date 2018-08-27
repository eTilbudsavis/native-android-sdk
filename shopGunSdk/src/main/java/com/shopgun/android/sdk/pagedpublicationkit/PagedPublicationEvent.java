package com.shopgun.android.sdk.pagedpublicationkit;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.SgnPreferences;
import com.shopgun.android.sdk.eventskit.AnonymousEvent;
import com.shopgun.android.sdk.eventskit.EventUtils;

/**
 * All events related to publications.
 */
public class PagedPublicationEvent extends AnonymousEvent {

    public static final String TAG = PagedPublicationEvent.class.getSimpleName();

    private PagedPublicationEvent(int type) {
        super(type);
    }

    /**
     * A paged publication has been opened by the user.
     * @param config configuration of the publication
     * @return paged publication opened event
     */
    public static PagedPublicationEvent opened(PagedPublicationConfiguration config) {
        PagedPublicationEvent event = new PagedPublicationEvent(PAGED_PUBLICATION_OPENED);
        String ppId = config.getPublication().getId();

        EventUtils.addLocationInformation(ShopGun.getInstance().getContext(), event);

        event.addPublicationOpened(ppId)
                .addViewToken(EventUtils.generateViewToken(ppId.getBytes(), SgnPreferences.getInstance().getInstallationId()));

        return event;
    }

    /**
     * When a particular page presented to the user disappears
     * @param config configuration of the publication
     * @return paged publication page open event
     */
    public static PagedPublicationEvent pageDisappeared(PagedPublicationConfiguration config, int page) {
        PagedPublicationEvent event = new PagedPublicationEvent(PAGED_PUBLICATION_PAGE_DISAPPEARED);
        String ppId = config.getPublication().getId();

        EventUtils.addLocationInformation(ShopGun.getInstance().getContext(), event);

        event.addPageOpened(ppId, page)
                .addViewToken(EventUtils.generateViewToken(
                        EventUtils.getDataBytes(ppId, page), SgnPreferences.getInstance().getInstallationId()));
        // todo check if page ranges from 0 or 1
        return event;
    }

}
