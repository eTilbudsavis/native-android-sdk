package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import Utils.Endpoint;
import Utils.Params;
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
	public static final String PARAM_IDS = Params.CATALOG_IDS;

	/** Parameter for posting a list of store id's to publish the catalog in */
	public static final String PARAM_STORE_IDS = Params.STORE_IDS;

	/** Parameter for posting a list of area id's to publish the catalog in */
	public static final String PARAM_AREA_IDS = Params.AREA_IDS;

	/** Parameter for the location of the PDF to post */
	public static final String PARAM_PDF = Params.PDF;

	/** Endpoint for catalog list resource */
	public static final String ENDPOINT_LIST = Endpoint.CATALOG_LIST;

	/** Endpoint for a single catalog resource */
	public static final String ENDPOINT_ID = Endpoint.CATALOG_ID;

	/** Endpoint for searching catalogs */
	public static final String ENDPOINT_SEARCH = Endpoint.CATALOG_SEARCH;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
	
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
	private Dimension mDimension;
	private Images mImages;
	private Pages mPages;
	
	// From seperate queries
	private Dealer mDealer;
	private Store mStore;
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
			mSelectStores = catalog.getBoolean("select_stores");
			setRunFrom(catalog.getString("run_from"));
			setRunTill(catalog.getString("run_till"));
			mPageCount = catalog.getInt("page_count");
			mOfferCount = catalog.getInt("offer_count");
			mBranding = new Branding(catalog.getJSONObject("branding"));
			mDealerId = catalog.getString("dealer_id");
			mDealerUrl = catalog.getString("dealer_url");
			mStoreId = catalog.getString("store_id");
			mStoreUrl = catalog.getString("store_url");
			mDimension = new Dimension(catalog.getJSONObject("dimensions"));
			mImages = new Images(catalog.getJSONObject("images"));
			mPages = new Pages(catalog.getJSONObject("pages"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Catalog setId(String id) {
		this.mId = id;
		return this;
	}

	public String getId() {
		return mId;
	}

	public Catalog setErn(String ern) {
		mErn = ern;
		return this;
	}

	public String getErn() {
		return mErn;
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

	public Catalog setSelectStores(Boolean selectStores) {
		this.mSelectStores = selectStores;
		return this;
	}

	public Boolean getSelectStores() {
		return mSelectStores;
	}

	public Catalog setRunFrom(Long time) {
		this.mRunFrom = time;
		return this;
	}

	public Catalog setRunFrom(String time) {
		try {
			mRunFrom = sdf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return this;
	}

	public Long getRunFrom() {
		return mRunFrom;
	}

	public String getRunFromString() {
		return sdf.format(new Date(mRunFrom));
	}

	public Catalog setRunTill(Long time) {
		this.mRunTill = time;
		return this;
	}

	public Catalog setRunTill(String time) {
		try {
			mRunTill = sdf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return this;
	}

	public Long getRunTill() {
		return mRunTill;
	}

	public String getRunTillString() {
		return sdf.format(new Date(mRunTill));
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
				mLabel.equals(c.getLabel()) &&
				mBackground.equals(c.getBackground()) &&
				mSelectStores == c.getSelectStores() &&
				mRunFrom == c.getRunFrom() &&
				mRunTill == c.getRunTill() &&
				mPageCount == c.getPageCount() &&
				mOfferCount == c.getOfferCount() &&
				mBranding == null ? c.getBranding() == null : mBranding.equals(c.getBranding()) &&
				mDealerId.equals(c.getDealerId()) &&
				mDealerUrl.equals(c.getDealerUrl()) &&
				mStoreId.equals(c.getStoreId()) &&
				mStoreUrl.equals(c.getStoreUrl()) &&
				mDimension == null ? c.getDimension() == null : mDimension.equals(c.getDimension()) &&
				mImages == null ? c.getImages() == null : mImages.equals(c.getImages()) &&
				mPages == null ? c.getPages() == null : mPages.equals(c.getPages()) &&
				mDealer == null ? c.getDealer() == null : (c.getDealer() == null ? false : mDealer.equals(c.getDealer())) &&
				mStore == null ? c.getStore() == null : (c.getStore() == null ? false : mStore.equals(c.getStore())) &&
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
