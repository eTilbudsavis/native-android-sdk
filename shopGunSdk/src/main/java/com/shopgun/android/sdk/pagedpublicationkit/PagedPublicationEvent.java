package com.shopgun.android.sdk.pagedpublicationkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.eventskit.Event;
import com.shopgun.android.sdk.eventskit.EventTracker;
import com.shopgun.android.utils.log.L;
import com.shopgun.android.verso.VersoTapInfo;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PagedPublicationEvent {

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

    private Event mEvent;

    PagedPublicationEvent(String type, JsonObject properties) {
        mEvent = new Event(type, properties);
    }

    public void track() {
        // avoid duplicates
        if (mEvent != null) {
            log();
            EventTracker.globalTracker().track(mEvent);
        }
        mEvent = null;
    }

    private void log() {
        StringBuilder sb = new StringBuilder();
        String type = mEvent.getType().substring(18);
        JsonObject p = mEvent.getProperties();
        if (p.has("pagedPublicationPage")) {
            JsonObject ppp = p.getAsJsonObject("pagedPublicationPage");
            sb.append("[").append(ppp.get("pageNumber").getAsString()).append("]");
        } else if (p.has("pagedPublicationPageSpread")) {
            JsonArray ppps = p.getAsJsonArray("pagedPublicationPageSpread");
            sb.append(ppps.toString());
        } else {
            sb.append(p.getAsJsonObject("pagedPublication").get("id").getAsString());
        }
        sb.append(" ").append(type);
        L.d(TAG, sb.toString());
    }

    private static JsonObject props(String publicationId, String ownedBy) {
        JsonObject pagedPublication = new JsonObject();
        pagedPublication.addProperty("id", publicationId);
        pagedPublication.addProperty("ownedBy", ownedBy);
        JsonObject properties = new JsonObject();
        properties.add("pagedPublication", pagedPublication);
        return properties;
    }

    private static PagedPublicationEvent props(String type, String publicationId, String ownedBy) {
        return new PagedPublicationEvent(type, props(publicationId, ownedBy));
    }

    private static JsonObject page(String publicationId, String ownedBy, int pageNumber) {
        JsonObject props = props(publicationId, ownedBy);
        JsonObject pagedPublicationPage = new JsonObject();
        pagedPublicationPage.addProperty("pageNumber", pageNumber);
        props.add("pagedPublicationPage", pagedPublicationPage);
        return props;
    }

    private static PagedPublicationEvent page(String type, String publicationId, String ownedBy, int pageNumber) {
        return new PagedPublicationEvent(type, page(publicationId, ownedBy, pageNumber+1));
    }

    private static JsonObject pageClick(String publicationId, String ownedBy, int pageNumber, float x, float y) {
        JsonObject props = props(publicationId, ownedBy);
        JsonObject pagedPublicationPage = new JsonObject();
        pagedPublicationPage.addProperty("pageNumber", pageNumber+1);
        pagedPublicationPage.addProperty("x", x);
        pagedPublicationPage.addProperty("y", y);
        props.add("pagedPublicationPage", pagedPublicationPage);
        return props;
    }

    private static PagedPublicationEvent pageClick(String type, String publicationId, String ownedBy, int pageNumber, float x, float y) {
        return new PagedPublicationEvent(type, pageClick(publicationId, ownedBy, pageNumber, x, y));
    }

    private static JsonObject pageSpread(String publicationId, String ownedBy, int[] pageNumbers) {
        JsonObject props = props(publicationId, ownedBy);
        JsonArray pagedPublicationPageSpread = new JsonArray();
        for (int page : pageNumbers) {
            pagedPublicationPageSpread.add(page+1);
        }
        props.add("pagedPublicationPageSpread", pagedPublicationPageSpread);
        return props;
    }

    private static PagedPublicationEvent pageSpread(String type, String publicationId, String ownedBy, int[] pageNumbers) {
        return new PagedPublicationEvent(type, pageSpread(publicationId, ownedBy, pageNumbers));
    }

    public static PagedPublicationEvent opened(PagedPublicationConfiguration config) {
        return opened(config.getPublication());
    }

    private static PagedPublicationEvent opened(PagedPublication publication) {
        return opened(publication.getId(), publication.getOwnerId());
    }

    private static PagedPublicationEvent opened(String publicationId, String ownedBy) {
        return props(OPENED, publicationId, ownedBy);
    }

    public static PagedPublicationEvent disappeared(PagedPublicationConfiguration config) {
        return disappeared(config.getPublication());
    }

    private static PagedPublicationEvent disappeared(PagedPublication publication) {
        return disappeared(publication.getId(), publication.getOwnerId());
    }

    private static PagedPublicationEvent disappeared(String publicationId, String ownedBy) {
        return props(DISAPPEARED, publicationId, ownedBy);
    }

    public static PagedPublicationEvent appeared(PagedPublicationConfiguration config) {
        return appeared(config.getPublication());
    }

    private static PagedPublicationEvent appeared(PagedPublication publication) {
        return appeared(publication.getId(), publication.getOwnerId());
    }

    private static PagedPublicationEvent appeared(String publicationId, String ownedBy) {
        return props(APPEARED, publicationId, ownedBy);
    }

    public static PagedPublicationEvent outlineAppeared(PagedPublicationConfiguration config) {
        return outlineAppeared(config.getPublication());
    }

    private static PagedPublicationEvent outlineAppeared(PagedPublication publication) {
        return outlineAppeared(publication.getId(), publication.getOwnerId());
    }

    private static PagedPublicationEvent outlineAppeared(String publicationId, String ownedBy) {
        return props(OUTLINE_APPEARED, publicationId, ownedBy);
    }

    public static PagedPublicationEvent pageAppeared(PagedPublicationConfiguration config, int pageNumber) {
        return pageAppeared(config.getPublication(), pageNumber);
    }

    private static PagedPublicationEvent pageAppeared(PagedPublication publication, int pageNumber) {
        return pageAppeared(publication.getId(), publication.getOwnerId(), pageNumber);
    }

    private static PagedPublicationEvent pageAppeared(String publicationId, String ownedBy, int pageNumber) {
        return page(PAGE_APPEARED, publicationId, ownedBy, pageNumber);
    }

    public static PagedPublicationEvent pageDisappeared(PagedPublicationConfiguration config, int pageNumber) {
        return pageDisappeared(config.getPublication(), pageNumber);
    }

    private static PagedPublicationEvent pageDisappeared(PagedPublication publication, int pageNumber) {
        return pageDisappeared(publication.getId(), publication.getOwnerId(), pageNumber);
    }

    private static PagedPublicationEvent pageDisappeared(String publicationId, String ownedBy, int pageNumber) {
        return page(PAGE_DISAPPEARED, publicationId, ownedBy, pageNumber);
    }

    public static PagedPublicationEvent pageLoaded(PagedPublicationConfiguration config, int pageNumber) {
        return pageLoaded(config.getPublication(), pageNumber);
    }

    private static PagedPublicationEvent pageLoaded(PagedPublication publication, int pageNumber) {
        return pageLoaded(publication.getId(), publication.getOwnerId(), pageNumber);
    }

    private static PagedPublicationEvent pageLoaded(String publicationId, String ownedBy, int pageNumber) {
        return page(PAGE_LOADED, publicationId, ownedBy, pageNumber);
    }

    public static PagedPublicationEvent pageClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return pageClicked(config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    private static PagedPublicationEvent pageClicked(String publicationId, String ownedBy, int pageNumber, float x, float y) {
        return pageClick(PAGE_CLICKED, publicationId, ownedBy, pageNumber, x, y);
    }

    public static PagedPublicationEvent pageDoubleClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return pageDoubleClicked(config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    private static PagedPublicationEvent pageDoubleClicked(String publicationId, String ownedBy, int pageNumber, float x, float y) {
        return pageClick(PAGE_DOUBLE_CLICKED, publicationId, ownedBy, pageNumber, x, y);
    }

    public static PagedPublicationEvent pageHotspotClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return pageHotspotClicked(config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    private static PagedPublicationEvent pageHotspotClicked(String publicationId, String ownedBy, int pageNumber, float x, float y) {
        return pageClick(PAGE_HOTSPOT_CLICKED, publicationId, ownedBy, pageNumber, x, y);
    }

    public static PagedPublicationEvent pageLongClicked(PagedPublicationConfiguration config, VersoTapInfo i) {
        return pageLongClicked(config.getPublication().getId(), config.getPublication().getOwnerId(), i.getPageTapped(), i.getPercentX(), i.getPercentY());
    }

    private static PagedPublicationEvent pageLongClicked(String publicationId, String ownedBy, int pageNumber, float x, float y) {
        return pageClick(PAGE_LONG_CLICKED, publicationId, ownedBy, pageNumber, x, y);
    }

    public static PagedPublicationEvent pageSpreadAppeared(PagedPublicationConfiguration config, int[] pageNumbers) {
        return pageSpreadAppeared(config.getPublication(), pageNumbers);
    }

    private static PagedPublicationEvent pageSpreadAppeared(PagedPublication publication, int[] pageNumbers) {
        return pageSpreadAppeared(publication.getId(), publication.getOwnerId(), pageNumbers);
    }

    private static PagedPublicationEvent pageSpreadAppeared(String publicationId, String ownedBy, int[] pageNumbers) {
        return pageSpread(PAGE_SPREAD_APPEARED, publicationId, ownedBy, pageNumbers);
    }

    public static PagedPublicationEvent pageSpreadDisappeared(PagedPublicationConfiguration config, int[] pageNumbers) {
        return pageSpreadDisappeared(config.getPublication(), pageNumbers);
    }

    private static PagedPublicationEvent pageSpreadDisappeared(PagedPublication publication, int[] pageNumbers) {
        return pageSpreadDisappeared(publication.getId(), publication.getOwnerId(), pageNumbers);
    }

    private static PagedPublicationEvent pageSpreadDisappeared(String publicationId, String ownedBy, int[] pageNumbers) {
        return pageSpread(PAGE_SPREAD_DISAPPEARED, publicationId, ownedBy, pageNumbers);
    }

    public static PagedPublicationEvent pageSpreadZoomedIn(PagedPublicationConfiguration config, int[] pageNumbers) {
        return pageSpreadZoomedIn(config.getPublication(), pageNumbers);
    }

    private static PagedPublicationEvent pageSpreadZoomedIn(PagedPublication publication, int[] pageNumbers) {
        return pageSpreadZoomedIn(publication.getId(), publication.getOwnerId(), pageNumbers);
    }

    private static PagedPublicationEvent pageSpreadZoomedIn(String publicationId, String ownedBy, int[] pageNumbers) {
        return pageSpread(PAGE_SPREAD_ZOOM_IN, publicationId, ownedBy, pageNumbers);
    }

    public static PagedPublicationEvent pageSpreadZoomedOut(PagedPublicationConfiguration config, int[] pageNumbers) {
        return pageSpreadZoomedOut(config.getPublication(), pageNumbers);
    }

    private static PagedPublicationEvent pageSpreadZoomedOut(PagedPublication publication, int[] pageNumbers) {
        return pageSpreadZoomedOut(publication.getId(), publication.getOwnerId(), pageNumbers);
    }

    private static PagedPublicationEvent pageSpreadZoomedOut(String publicationId, String ownedBy, int[] pageNumbers) {
        return pageSpread(PAGE_SPREAD_ZOOM_OUT, publicationId, ownedBy, pageNumbers);
    }

}
