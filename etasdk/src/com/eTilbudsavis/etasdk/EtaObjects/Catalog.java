package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import Utils.Endpoint;
import Utils.Sort;

public class Catalog implements Serializable {
	
	private static final long serialVersionUID = 1L;

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
	public static final String CATALOG_IDS = "catalog_ids";

	/** Endpoint for catalog list resource */
	public static final String ENDPOINT_LIST = Endpoint.CATALOG_LIST;

	/** Endpoint for a single catalog resource */
	public static final String ENDPOINT_ID = Endpoint.CATALOG_ID;

	/** Endpoint for getting multiple catalog resources */
	public static final String ENDPOINT_IDS = Endpoint.CATALOG_IDS;
	
	/** Endpoint for searching catalogs */
	public static final String ENDPOINT_SEARCH = Endpoint.CATALOG_SEARCH;
	
	// From JSON blob
	private String mId;
	private String mErn;
	private String mLabel;
	private String mBackground;
	private boolean mSelectStores;
	private long mRunFrom;
	private long mRunTill;
	private int mPageCount;
	private int mOfferCount;
	private Branding mBranding;
	private String mDealerId;
	private String mDealerUrl;
	private String mStoreId;
	private String mStoreUrl;
	private double mDimenWidth;
	private double mDimenHeight;
	private String mImageView;
	private String mImageZoom;
	private String mImageThumb;
	private Pages mPages;
	
	// From seperate queries
	private Dealer mDealer;
	private Store mStore;
	private String mPromoted;
	private int mOfferOnPage;

	public Catalog(JSONObject catalog) {
		// if we have a full catalog
		if (catalog.has("store") && catalog.has("offerCount")) {
			setCatalog(catalog);
		}
		// If it is a partial catalog
		else if (catalog.has("id") && catalog.has("page")) {
			try {
				mId = catalog.getString("id");
				mOfferOnPage = catalog.getInt("page");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void setCatalog(JSONObject catalog) {
		try {
			mId = catalog.getString("id");
			mErn = catalog.getString("ern");
			mLabel = catalog.getString("label");
			mBackground = catalog.getString("background");
			mSelectStores = catalog.getBoolean("selectStores");
			mRunFrom = (catalog.getLong("runFrom")*1000);
			mRunTill = (catalog.getLong("runTill")*1000);
			mPageCount = catalog.getInt("pageCount");
			mOfferCount = catalog.getInt("offerCount");
			mBranding = new Branding(catalog.getJSONObject("branding"));
			mDealerId = catalog.getString("dealer_id");
			mDealerUrl = catalog.getString("dealer_url");
			mStoreId = catalog.getString("store_id");
			mStoreUrl = catalog.getString("store_url");
			mDimenWidth = catalog.getJSONObject("dimensions").getDouble("width");
			mDimenHeight = catalog.getJSONObject("dimensions").getDouble("height");
			mImageView = catalog.getJSONObject("images").getString("view");
			mImageZoom = catalog.getJSONObject("images").getString("zoom");
			mImageThumb = catalog.getJSONObject("images").getString("thumb");
			mPages = new Pages(catalog.getJSONObject("pages"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public Catalog setPageCount(Integer mPageCount) {
		this.mPageCount = mPageCount;
		return this;
	}

	public int getPageCount() {
		return mPageCount;
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

	public Catalog setStoreId(String storeId) {
		mStoreId = storeId;
		return this;
	}

	public String getStoreId() {
		return mStoreId;
	}

	public Catalog setOfferCount(Integer mOfferCount) {
		this.mOfferCount = mOfferCount;
		return this;
	}

	public int getOfferCount() {
		return mOfferCount;
	}

	public Catalog setDealerId(String dealer) {
		this.mDealerId = dealer;
		return this;
	}

	public String getDealerId() {
		return mDealerId;
	}

	public Catalog setDealer(Dealer dealer) {
		this.mDealer = dealer;
		return this;
	}

	public Dealer getDealer() {
		return mDealer;
	}

	public Catalog setId(String id) {
		this.mId = id;
		return this;
	}

	public String getId() {
		return mId;
	}

	/**
	 * Set the start time of this catalog.
	 * @param runFrom Time in milliseconds
	 * @return <li> This catalog
	 */
	public Catalog setRunFrom(Long runFrom) {
		this.mRunFrom = runFrom;
		return this;
	}

	/**
	 * The start time of this catalog. Note, that the server time is in seconds, 
	 * so this time has to be converted in order to use it to update server time.
	 * @return <li> Time in milliseconds
	 */
	public Long getRunFrom() {
		return mRunFrom;
	}

	public Catalog setRunTill(Long runTill) {
		this.mRunTill = runTill;
		return this;
	}

	public Long getRunTill() {
		return mRunTill;
	}

	public Catalog setPromoted(String promoted) {
		this.mPromoted = promoted;
		return this;
	}

	public String isPromoted() {
		return mPromoted;
	}

	public Catalog setBranding(Branding branding) {
		this.mBranding = branding;
		return this;
	}

	public Branding getBranding() {
		return mBranding;
	}

	public Catalog setImageView(String imageView) {
		this.mImageView = imageView;
		return this;
	}

	public String getImageView() {
		return mImageView;
	}

	public Catalog setImageZoom(String imageZoom) {
		this.mImageZoom = imageZoom;
		return this;
	}

	public String getImageZoom() {
		return mImageZoom;
	}

	public Catalog setImageThumb(String imageThumb) {
		this.mImageThumb = imageThumb;
		return this;
	}

	public String getImageThumb() {
		return mImageThumb;
	}

	public Catalog setSelectStores(Boolean selectStores) {
		this.mSelectStores = selectStores;
		return this;
	}

	public Boolean getSelectStores() {
		return mSelectStores;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Catalog))
			return false;

		Catalog c = (Catalog)o;
		return mPageCount == c.getPageCount() &&
				mStoreId.equals(c.getStoreId()) &&
				mStore == null ? c.getStore() == null : (c.getStore() == null ? false : mStore.equals(c.getStore())) &&
				mOfferCount == c.getOfferCount() &&
				mDealerId.equals(c.getDealerId()) &&
				mDealer == null ? c.getDealer() == null : (c.getDealer() == null ? false : mDealer.equals(c.getDealer())) &&
				mId.equals(c.getId()) &&
				mRunFrom == c.getRunFrom() &&
				mRunTill == c.getRunTill() &&
				mPromoted.equals(c.isPromoted()) &&
				mBranding == null ? c.getBranding() == null : mBranding.equals(c.getBranding()) &&
				mImageView.equals(c.getImageView()) &&
				mImageZoom.equals(c.getImageZoom()) &&
				mImageThumb.equals(c.getImageThumb()) &&
				mSelectStores == c.getSelectStores() &&
				mOfferOnPage == c.getOfferOnPage();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append("Catalog { ")
		.append("DealerId: ").append(mDealerId)
		.append(", Id: ").append(mId)
		.append(", StoreId: ").append(mStoreId)
		.append(" }").toString();
	}

	
	
}
