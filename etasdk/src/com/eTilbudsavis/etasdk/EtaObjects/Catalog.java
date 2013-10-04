package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Sort;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Catalog extends EtaErnObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Catalog";
	
	/** Sort a list by popularity in ascending order. (smallest to largest) */
	public static final String SORT_POPULARITY = Sort.POPULARITY;

	/** Sort a list by popularity in descending order. (largest to smallest)*/
	public static final String SORT_POPULARITY_DESC = Sort.POPULARITY_DESC;

	/** Sort a list by distance in ascending order. (smallest to largest) */
	public static final String SORT_DISTANCE = Sort.DISTANCE;

	/** Sort a list by distance in descending order. (largest to smallest)*/
	public static final String SORT_DISTANCE_DESC = Sort.DISTANCE_DESC;

	/** Sort a list by name in ascending order. (smallest to largest) */
	public static final String SORT_NAME = Sort.NAME;

	/** Sort a list by name in descending order. (largest to smallest)*/
	public static final String SORT_NAME_DESC = Sort.NAME_DESC;

	/** Sort a list by published in ascending order. (smallest to largest) */
	public static final String SORT_PUBLISHED = Sort.PUBLISHED;

	/** Sort a list by published in descending order. (largest to smallest)*/
	public static final String SORT_PUBLISHED_DESC = Sort.PUBLISHED_DESC;

	/** Sort a list by expired in ascending order. (smallest to largest) */
	public static final String SORT_EXPIRED = Sort.EXPIRED;

	/** Sort a list by expired in descending order. (largest to smallest)*/
	public static final String SORT_EXPIRED_DESC = Sort.EXPIRED_DESC;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED = Sort.CREATED;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED_DESC = Sort.CREATED_DESC;

	/** Parameter for getting a list of specific catalog id's */
	public static final String FILTER_CATALOG_IDS = Params.FILTER_CATALOG_IDS;

	/** Parameter for posting a list of store id's to publish the catalog in */
	public static final String FILTER_STORE_IDS = Params.FILTER_STORE_IDS;

	/** Parameter for posting a list of area id's to publish the catalog in */
	public static final String FILTER_AREA_IDS = Params.FILTER_AREA_IDS;

	/** Parameter for the location of the PDF to post */
	public static final String PARAM_PDF = Params.PDF;

	/** String identifying the offset parameter for all list calls to the API */
	public static final String PARAM_OFFSET = Params.OFFSET;

	/** String identifying the offset parameter for all list calls to the API */
	public static final String PARAM_LIMIT = Params.LIMIT;

	/** String identifying the query parameter */
	public static final String PARAM_QUERY = Params.QUERY;
	
	/** Endpoint for catalog list resource */
	public static final String ENDPOINT_LIST = Endpoint.CATALOG_LIST;

	/** Endpoint for a single catalog resource */
	public static final String ENDPOINT_ID = Endpoint.CATALOG_ID;

	/** Endpoint for searching catalogs */
	public static final String ENDPOINT_SEARCH = Endpoint.CATALOG_SEARCH;

	// From JSON blob
	private String mLabel;
	private String mBackground;
	private Date mRunFrom;
	private Date mRunTill;
	private int mPageCount = 0;
	private int mOfferCount = 0;
	private Branding mBranding;
	private String mDealerId;
	private String mDealerUrl;
	private String mStoreId;
	private String mStoreUrl;
	private Dimension mDimension;
	private Images mImages;
	private Pages mPages;
	
	// From seperate queries
	private Dealer mDealer;
	private Store mStore;
	private int mOfferOnPage;

	public Catalog() {
	}

	public Catalog(Catalog c) {
		set(c);
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Catalog> fromJSON(JSONArray catalogs) {
		ArrayList<Catalog> list = new ArrayList<Catalog>();
		try {
			for (int i = 0 ; i < catalogs.length() ; i++ )
				list.add(Catalog.fromJSON((JSONObject)catalogs.get(i)));
			
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static Catalog fromJSON(JSONObject catalog) {
		return fromJSON(new Catalog(), catalog);
	}

	private static Catalog fromJSON(Catalog c, JSONObject catalog) {
		if(c == null)
			c = new Catalog();

		if (catalog == null)
			return c;

		if (catalog.has(S_STORE_ID) && catalog.has(S_OFFER_COUNT)) {
			// if we have a full catalog
			try {
				c.setId(getJsonString(catalog, S_ID));
				c.setErn(getJsonString(catalog, S_ERN));
				c.setLabel(getJsonString(catalog, S_LABEL));
				c.setBackground(getJsonString(catalog, S_BACKGROUND));
				c.setRunFrom(getJsonString(catalog, S_RUN_FROM));
				c.setRunTill(getJsonString(catalog, S_RUN_TILL));
				c.setPageCount(catalog.getInt(S_PAGE_COUNT));
				c.setOfferCount(catalog.getInt(S_OFFER_COUNT));
				c.setBranding(Branding.fromJSON(catalog.getJSONObject(S_BRANDING)));
				c.setDealerId(getJsonString(catalog, S_DEALER_ID));
				c.setDealerUrl(getJsonString(catalog, S_DEALER_URL));
				c.setStoreId(getJsonString(catalog, S_STORE_ID));
				c.setStoreUrl(getJsonString(catalog, S_STORE_URL));
				c.setDimension(Dimension.fromJSON(catalog.getJSONObject(S_DIMENSIONS)));
				c.setImages(Images.fromJSON(catalog.getJSONObject(S_IMAGES)));
				c.setPages(Pages.fromJSON(catalog.getJSONObject(S_PAGES)));
			} catch (JSONException e) {
				Utils.logd(TAG, e);
			}
		} else if (catalog.has(S_ID) && catalog.has(P_PAGE)) {
			// If it is a partial catalog
			try {
				c.setId(getJsonString(catalog, S_ID));
				c.setOfferOnPage(catalog.getInt(P_PAGE));
			} catch (JSONException e) {
				Utils.logd(TAG, e);
			}
		}
		return c;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Catalog c) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_ID, c.getId());
			o.put(S_ERN, c.getErn());
			o.put(S_LABEL, c.getLabel());
			o.put(S_BACKGROUND, c.getBackground());
			o.put(S_RUN_FROM, Utils.formatDate(c.getRunFrom()));
			o.put(S_RUN_TILL, Utils.formatDate(c.getRunTill()));
			o.put(S_PAGE_COUNT, c.getPageCount());
			o.put(S_OFFER_COUNT, c.getOfferCount());
			o.put(S_BRANDING, c.getBranding().toJSON());
			o.put(S_DEALER_ID, c.getDealerId());
			o.put(S_DEALER_URL, c.getDealerUrl());
			o.put(S_STORE_ID, c.getStoreId());
			o.put(S_STORE_URL, c.getStoreUrl());
			o.put(S_DIMENSIONS, c.getDimension().toJSON());
			o.put(S_IMAGES, c.getImages().toJSON());
			o.put(S_PAGES, c.getPages().toJSON());
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		return o;
	}
	
	public void set(Catalog c) {
		mId = c.getId();
		mErn = c.getErn();
		mLabel = c.getLabel();
		mBackground = c.getBackground();
		mRunFrom = c.getRunFrom();
		mRunTill = c.getRunTill();
		mPageCount = c.getPageCount();
		mOfferCount = c.getOfferCount();
		mBranding = c.getBranding();
		mDealerId = c.getDealerId();
		mDealerUrl = c.getDealerUrl();
		mStoreId = c.getStoreId();
		mStoreUrl = c.getStoreUrl();
		mDimension = c.getDimension();
		mImages = c.getImages();
		mPages = c.getPages();
	}

	public String getLabel() {
		return mLabel;
	}

	public Catalog setLabel(String label) {
		mLabel = label;
		return this;
	}

	public String getBackground() {
		return mBackground;
	}

	public Catalog setBackground(String background) {
		mBackground = background;
		return this;
	}

	public Catalog setRunFrom(Date time) {
		this.mRunFrom = time;
		return this;
	}

	public Catalog setRunFrom(String time) {
		mRunFrom = Utils.parseDate(time);
		return this;
	}

	public Date getRunFrom() {
		return mRunFrom;
	}

	public Catalog setRunTill(Date time) {
		this.mRunTill = time;
		return this;
	}

	public Catalog setRunTill(String time) {
		mRunTill = Utils.parseDate(time);
		return this;
	}

	public Date getRunTill() {
		return mRunTill;
	}

	public Catalog setPageCount(Integer mPageCount) {
		this.mPageCount = mPageCount;
		return this;
	}

	public int getPageCount() {
		return mPageCount;
	}

	public Catalog setOfferCount(Integer mOfferCount) {
		this.mOfferCount = mOfferCount;
		return this;
	}

	public int getOfferCount() {
		return mOfferCount;
	}

	public Catalog setBranding(Branding branding) {
		this.mBranding = branding;
		return this;
	}

	public Branding getBranding() {
		return mBranding;
	}

	public Catalog setDealerId(String dealer) {
		this.mDealerId = dealer;
		return this;
	}

	public String getDealerId() {
		return mDealerId;
	}

	public String getDealerUrl() {
		return mDealerUrl;
	}

	public Catalog setDealerUrl(String url) {
		mDealerUrl = url;
		return this;
	}

	public Catalog setStoreId(String storeId) {
		mStoreId = storeId;
		return this;
	}

	public String getStoreId() {
		return mStoreId;
	}

	public String getStoreUrl() {
		return mStoreUrl;
	}

	public Catalog setStoreUrl(String url) {
		mStoreUrl = url;
		return this;
	}

	public Dimension getDimension() {
		return mDimension;
	}

	public Catalog setDimension(Dimension dimension) {
		mDimension = dimension;
		return this;
	}

	public Catalog setImages(Images images) {
		mImages = images;
		return this;
	}

	public Images getImages() {
		return mImages;
	}

	public Pages getPages() {
		return mPages;
	}

	public void setPages(Pages pages) {
		mPages = pages;
	}

	public Catalog setOfferOnPage(Integer offerOnPage) {
		this.mOfferOnPage = offerOnPage;
		return this;
	}

	public int getOfferOnPage() {
		return mOfferOnPage;
	}

	public Catalog setStore(Store store) {
		mStore = store;
		return this;
	}

	public Store getStore() {
		return mStore;
	}

	public Catalog setDealer(Dealer dealer) {
		this.mDealer = dealer;
		return this;
	}

	public Dealer getDealer() {
		return mDealer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Catalog))
			return false;

		Catalog c = (Catalog)o;
		return mId.equals(c.getId()) &&
				mErn.equals(c.getErn()) &&
				stringCompare(mLabel, c.getLabel()) &&
				stringCompare(mBackground, c.getBackground()) &&
				mRunFrom == c.getRunFrom() &&
				mRunTill == c.getRunTill() &&
				mPageCount == c.getPageCount() &&
				mOfferCount == c.getOfferCount() &&
				mBranding == null ? c.getBranding() == null : mBranding.equals(c.getBranding()) &&
				stringCompare(mDealerId, c.getDealerId()) &&
				stringCompare(mDealerUrl, c.getDealerUrl()) &&
				stringCompare(mStoreId, c.getStoreId()) &&
				stringCompare(mStoreUrl, c.getStoreUrl()) &&
				mDimension == null ? c.getDimension() == null : mDimension.equals(c.getDimension()) &&
				mImages == null ? c.getImages() == null : mImages.equals(c.getImages()) &&
				mPages == null ? c.getPages() == null : mPages.equals(c.getPages()) &&
				mDealer == null ? c.getDealer() == null : (c.getDealer() != null && mDealer.equals(c.getDealer())) &&
				mStore == null ? c.getStore() == null : (c.getStore() != null && mStore.equals(c.getStore())) &&
				mOfferOnPage == c.getOfferOnPage();
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append("branding=").append(mBranding == null ? null : mBranding.toString(everything))
		.append(", id=").append(mId)
		.append(", from=").append(Utils.formatDate(getRunFrom()))
		.append(", till=").append(Utils.formatDate(getRunTill()));
		if(everything) {
			sb.append(", ern=").append(mErn)
			.append(", background=").append(mBackground)
			.append(", pageCount=").append(mPageCount)
			.append(", offerCount=").append(mOfferCount)
			.append(", dealer=").append(mDealer == null ? mDealerId : mDealer.toString())
			.append(", store=").append(mStore == null ? mStoreId : mStore.toString())
			.append(", dimension=").append(mDimension == null ? null : mDimension.toString())
			.append(", images=").append(mImages == null ? null : mImages.toString())
			.append(", pages=").append(mPages == null ? null : mPages.toString());
		}
		return sb.append("]").toString();
	}
	
}
