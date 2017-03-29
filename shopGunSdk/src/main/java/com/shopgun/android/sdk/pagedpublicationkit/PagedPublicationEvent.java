package com.shopgun.android.sdk.pagedpublicationkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.eventskit.EzEvent;
import com.shopgun.android.verso.VersoTapInfo;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PagedPublicationEvent extends EzEvent {

    public static final String TAG = PagedPublicationEvent.class.getSimpleName();

    public static final String OPENED = "paged-publication-opened";
    public static final String APPEARED = "paged-publication-appeared";
    public static final String DISAPPEARED = "paged-publication-disappeared";
    public static final String OUTLINE_APPEARED = "paged-publication-outline-appeared";
    public static final String PAGE_APPEARED = "paged-publication-page-appeared";
    public static final String PAGE_DISAPPEARED = "paged-publication-page-disappeared";
    public static final String PAGE_LOADED = "paged-publication-page-loaded";
    public static final String PAGE_CLICKED = "paged-publication-page-clicked";
    public static final String PAGE_DOUBLE_CLICKED = "paged-publication-page-double-clicked";
    public static final String PAGE_HOTSPOT_CLICKED = "paged-publication-page-hotspots-clicked";
    public static final String PAGE_LONG_CLICKED = "paged-publication-page-long-pressed";
    public static final String PAGE_SPREAD_APPEARED = "paged-publication-page-spread-appeared";
    public static final String PAGE_SPREAD_DISAPPEARED = "paged-publication-page-spread-disappeared";
    public static final String PAGE_SPREAD_ZOOM_IN = "paged-publication-page-spread-zoomed-in";
    public static final String PAGE_SPREAD_ZOOM_OUT = "paged-publication-page-spread-zoomed-out";
    public static final String OUTRO_APPEARED = "x-paged-publication-outro-appeared";

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
        return publication(OPENED, config.getPublication().getId(), config.getPublication().getOwnerId());
    }

    public static PagedPublicationEvent disappeared(PagedPublicationConfiguration config) {
        return publication(DISAPPEARED, config.getPublication().getId(), config.getPublication().getOwnerId());
    }

    public static PagedPublicationEvent appeared(PagedPublicationConfiguration config) {
        return publication(APPEARED, config.getPublication().getId(), config.getPublication().getOwnerId());
    }

    public static PagedPublicationEvent pageAppeared(PagedPublicationConfiguration config, int pageNumber) {
        return page(PAGE_APPEARED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumber);
    }

    public static PagedPublicationEvent pageDisappeared(PagedPublicationConfiguration config, int pageNumber) {
        return page(PAGE_DISAPPEARED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumber);
    }

    public static PagedPublicationEvent pageLoaded(PagedPublicationConfiguration config, int pageNumber) {
        return page(PAGE_LOADED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumber);
    }

    public static PagedPublicationEvent outroAppeared(PagedPublicationConfiguration config) {
        return publication(OUTRO_APPEARED, config.getPublication().getId(), config.getPublication().getOwnerId());
    }

    public static PagedPublicationEvent pageClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return getPageClickProperties(PAGE_CLICKED, config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    public static PagedPublicationEvent pageDoubleClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return getPageClickProperties(PAGE_DOUBLE_CLICKED, config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    public static PagedPublicationEvent pageHotspotClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return getPageClickProperties(PAGE_HOTSPOT_CLICKED, config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    public static PagedPublicationEvent pageLongClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return getPageClickProperties(PAGE_LONG_CLICKED, config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    public static PagedPublicationEvent pageSpreadAppeared(PagedPublicationConfiguration config, int[] pageNumbers) {
        return getPageSpreadProperties(PAGE_SPREAD_APPEARED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumbers);
    }

    public static PagedPublicationEvent pageSpreadDisappeared(PagedPublicationConfiguration config, int[] pageNumbers) {
        return getPageSpreadProperties(PAGE_SPREAD_DISAPPEARED, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumbers);
    }

    public static PagedPublicationEvent pageSpreadZoomedIn(PagedPublicationConfiguration config, int[] pageNumbers) {
        return getPageSpreadProperties(PAGE_SPREAD_ZOOM_IN, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumbers);
    }

    public static PagedPublicationEvent pageSpreadZoomedOut(PagedPublicationConfiguration config, int[] pageNumbers) {
        return getPageSpreadProperties(PAGE_SPREAD_ZOOM_OUT, config.getPublication().getId(), config.getPublication().getOwnerId(), pageNumbers);
    }

}
