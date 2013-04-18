package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Catalog implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sort catalog list by popularity.
	 */
	public static final String SORT_POPULARITY = "popularity";
	
	/**
	 * Sort catalog list by distance.
	 */
	public static final String SORT_DISTANCE = "distance";
	
	/**
	 * Sort catalog list by name.
	 */
	public static final String SORT_NAME = "name";
	
	/**
	 * Sort catalog list by published.
	 */
	public static final String SORT_PUBLISHED = "published";

	/**
	 * Sort catalog list by expired.
	 */
	public static final String SORT_EXPIRED = "expired";

	/**
	 * Sort catalog list by expired.
	 * TODO: Remember to check this against the online API v2 docs to confirm.
	 */
	public static final String SORT_CREATED = "created";
	
	
	private int mPageCount;
	private Store mStore;
	private int mOfferCount;
	private int mWeeksTo;
	private int mWeeksFrom;
	private String mUrl;
	private Dealer mDealer;
	private String mId;
	private long mExpires;
	private long mRunFrom;
	private long mRunTill;
	private String mPromoted;
	private Branding mBranding;
	private String mImageView;
	private String mImageZoom;
	private String mImageThumb;
	private boolean mSelectStores;
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
			mPageCount = catalog.getInt("pageCount");
			mStore = new Store(catalog.getJSONObject("store"));
			mOfferCount = catalog.getInt("offerCount");
			mWeeksTo = catalog.getJSONObject("weeks").getInt("to");
			mWeeksFrom = catalog.getJSONObject("weeks").getInt("from");
			mUrl = catalog.getString("url");
			mDealer = new Dealer(catalog.getJSONObject("dealer"));
			mId = catalog.getString("id");
			mExpires = (catalog.getLong("expires")*1000);
			mRunFrom = (catalog.getLong("runFrom")*1000);
			mRunTill = (catalog.getLong("runTill")*1000);
			mPromoted = catalog.getString("promoted");
			mBranding = new Branding(catalog.getJSONObject("branding"));
			mImageView = catalog.getJSONObject("images").getString("view");
			mImageZoom = catalog.getJSONObject("images").getString("zoom");
			mImageThumb = catalog.getJSONObject("images").getString("thumb");
			mSelectStores = catalog.getBoolean("selectStores");
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

	public Catalog setStore(Store mStore) {
		this.mStore = mStore;
		return this;
	}

	public Store getStore() {
		return mStore;
	}

	public Catalog setOfferCount(Integer mOfferCount) {
		this.mOfferCount = mOfferCount;
		return this;
	}

	public int getOfferCount() {
		return mOfferCount;
	}

	public Catalog setWeeksTo(Integer mWeeksTo) {
		this.mWeeksTo = mWeeksTo;
		return this;
	}

	public int getWeeksTo() {
		return mWeeksTo;
	}

	public Catalog setWeeksFrom(Integer mWeeksFrom) {
		this.mWeeksFrom = mWeeksFrom;
		return this;
	}

	public int getWeeksFrom() {
		return mWeeksFrom;
	}

	public Catalog setUrl(String url) {
		this.mUrl = url;
		return this;
	}

	public String getUrl() {
		return mUrl;
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

	public Catalog setExpires(Long expires) {
		this.mExpires = expires;
		return this;
	}

	public Long getExpires() {
		return mExpires;
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
				mStore == null ? c.getStore() == null : mStore.equals(c.getStore()) &&
				mOfferCount == c.getOfferCount() &&
				mWeeksTo == c.getWeeksTo() &&
				mWeeksFrom == c.getWeeksFrom() &&
				mUrl.equals(c.getUrl()) &&
				mDealer.equals(c.getDealer()) &&
				mId.equals(c.getId()) &&
				mExpires == c.getExpires() &&
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
		.append("Dealer: ").append(mDealer.getName())
		.append(", Id: ").append(mId)
		.append(", City: ").append(mStore.getCity())
		.append(", Street: ").append(mStore.getStreet())
		.append(" }").toString();
	}

	
	
}
