package com.shopgun.android.sdk.pagedpublicationkit;

import com.google.gson.JsonObject;
import com.shopgun.android.sdk.eventskit.EzEvent;

/**
 * All events related to publications.
 */
public class PagedPublicationEvent extends EzEvent {

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

        // publication id is part of the view token
        event.setViewToken(config.getPublication().getId());

        JsonObject payload = new JsonObject();
        payload.addProperty("pp.id", config.getPublication().getId());
        event.setPayload(payload);

        return event;
    }

    /**
     * When a particular page presented to the user disappears
     * @param config configuration of the publication
     * @return paged publication page open event
     */
    public static PagedPublicationEvent pageDisappeared(PagedPublicationConfiguration config, int page) {
        PagedPublicationEvent event = new PagedPublicationEvent(PAGED_PUBLICATION_PAGE_DISAPPEARED);

        // publication id and page number are part of the view token
        event.setViewToken(config.getPublication().getId().concat(String.valueOf(page)));

        JsonObject payload = new JsonObject();
        payload.addProperty("pp.id", config.getPublication().getId());
        payload.addProperty("ppp.n", page);
        event.setPayload(payload);

        return event;
    }

}
