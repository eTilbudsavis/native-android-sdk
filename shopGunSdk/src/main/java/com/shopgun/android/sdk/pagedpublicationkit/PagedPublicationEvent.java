package com.shopgun.android.sdk.pagedpublicationkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.eventskit.EzEvent;
import com.shopgun.android.verso.VersoTapInfo;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PagedPublicationEvent extends EzEvent {

    public static final String TAG = PagedPublicationEvent.class.getSimpleName();

    PagedPublicationEvent(String type, JsonObject properties) {
        super(type, properties);
    }

    private static JsonObject publication(String publicationId, String ownedBy) {
        JsonObject pagedPublication = new JsonObject();
        pagedPublication.addProperty("id", publicationId);
        pagedPublication.addProperty("ownedBy", ownedBy);
        JsonObject properties = new JsonObject();
        properties.add("pagedPublication", pagedPublication);
        return properties;
    }

    private static PagedPublicationEvent publication(String type, String publicationId, String ownedBy) {
        return new PagedPublicationEvent(type, publication(publicationId, ownedBy));
    }

    private static JsonObject page(String publicationId, String ownedBy, int pageNumber) {
        JsonObject props = publication(publicationId, ownedBy);
        JsonObject pagedPublicationPage = new JsonObject();
        pagedPublicationPage.addProperty("pageNumber", pageNumber);
        props.add("pagedPublicationPage", pagedPublicationPage);
        return props;
    }

    private static PagedPublicationEvent page(String type, String publicationId, String ownedBy, int pageNumber) {
        return new PagedPublicationEvent(type, page(publicationId, ownedBy, pageNumber+1));
    }

    private static JsonObject getPageClickProperties(String publicationId, String ownedBy, int pageNumber, float x, float y) {
        JsonObject props = publication(publicationId, ownedBy);
        JsonObject pagedPublicationPage = new JsonObject();
        pagedPublicationPage.addProperty("pageNumber", pageNumber+1);
        pagedPublicationPage.addProperty("x", x);
        pagedPublicationPage.addProperty("y", y);
        props.add("pagedPublicationPage", pagedPublicationPage);
        return props;
    }

    private static PagedPublicationEvent getPageClickProperties(String type, String publicationId, String ownedBy, int pageNumber, float x, float y) {
        return new PagedPublicationEvent(type, getPageClickProperties(publicationId, ownedBy, pageNumber, x, y));
    }

    private static JsonObject getPageSpreadProperties(String publicationId, String ownedBy, int[] pageNumbers) {
        JsonObject props = publication(publicationId, ownedBy);
        JsonArray pagedPublicationPageSpread = new JsonArray();
        for (int page : pageNumbers) {
            pagedPublicationPageSpread.add(page+1);
        }
        props.add("pagedPublicationPageSpread", pagedPublicationPageSpread);
        return props;
    }

    private static PagedPublicationEvent getPageSpreadProperties(String type, String publicationId, String ownedBy, int[] pageNumbers) {
        return new PagedPublicationEvent(type, getPageSpreadProperties(publicationId, ownedBy, pageNumbers));
    }

    public static PagedPublicationEvent opened(PagedPublicationConfiguration config) {
        return publication(PAGED_PUBLICATION_OPENED, config.getPublication().getId(), config.getPublication().getOwnerId());
    }

    public static PagedPublicationEvent disappeared(PagedPublicationConfiguration config) {
        return publication(PAGED_PUBLICATION_DISAPPEARED, config.getPublication().getId(), config.getPublication().getOwnerId());
    }

    public static PagedPublicationEvent appeared(PagedPublicationConfiguration config) {
        return publication(PAGED_PUBLICATION_APPEARED, config.getPublication().getId(), config.getPublication().getOwnerId());
    }

    public static PagedPublicationEvent pageAppeared(PagedPublicationConfiguration config, int pageNumber) {
        return page(PAGED_PUBLICATION_PAGE_APPEARED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumber);
    }

    public static PagedPublicationEvent pageDisappeared(PagedPublicationConfiguration config, int pageNumber) {
        return page(PAGED_PUBLICATION_PAGE_DISAPPEARED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumber);
    }

    public static PagedPublicationEvent pageLoaded(PagedPublicationConfiguration config, int pageNumber) {
        return page(PAGED_PUBLICATION_PAGE_LOADED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumber);
    }

    public static PagedPublicationEvent outroAppeared(PagedPublicationConfiguration config) {
        return publication(PAGED_PUBLICATION_OUTRO_APPEARED, config.getPublication().getId(), config.getPublication().getOwnerId());
    }

    public static PagedPublicationEvent pageClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return getPageClickProperties(PAGED_PUBLICATION_PAGE_CLICKED, config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    public static PagedPublicationEvent pageDoubleClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return getPageClickProperties(PAGED_PUBLICATION_PAGE_DOUBLE_CLICKED, config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    public static PagedPublicationEvent pageHotspotClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return getPageClickProperties(PAGED_PUBLICATION_PAGE_HOTSPOT_CLICKED, config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    public static PagedPublicationEvent pageLongClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return getPageClickProperties(PAGED_PUBLICATION_PAGE_LONG_CLICKED, config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    public static PagedPublicationEvent pageSpreadAppeared(PagedPublicationConfiguration config, int[] pageNumbers) {
        return getPageSpreadProperties(PAGED_PUBLICATION_PAGE_SPREAD_APPEARED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumbers);
    }

    public static PagedPublicationEvent pageSpreadDisappeared(PagedPublicationConfiguration config, int[] pageNumbers) {
        return getPageSpreadProperties(PAGED_PUBLICATION_PAGE_SPREAD_DISAPPEARED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumbers);
    }

    public static PagedPublicationEvent pageSpreadZoomedIn(PagedPublicationConfiguration config, int[] pageNumbers) {
        return getPageSpreadProperties(PAGED_PUBLICATION_PAGE_SPREAD_ZOOM_IN, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumbers);
    }

    public static PagedPublicationEvent pageSpreadZoomedOut(PagedPublicationConfiguration config, int[] pageNumbers) {
        return getPageSpreadProperties(PAGED_PUBLICATION_PAGE_SPREAD_ZOOM_OUT, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumbers);
    }

}
