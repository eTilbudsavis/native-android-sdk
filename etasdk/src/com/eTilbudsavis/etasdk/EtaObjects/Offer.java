package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

import android.annotation.SuppressLint;

import Utils.Endpoint;
import Utils.Params;
import Utils.Sort;
import Utils.Utilities;

public class Offer implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Offer";
	
	/** Sort a list by popularity in ascending order. (smallest to largest) */
	public static final String SORT_POPULARITY = Sort.POPULARITY;

	/** Sort a list by popularity in descending order. (largest to smallest)*/
	public static final String SORT_POPULARITY_DESC = Sort.POPULARITY_DESC;

	/** Sort a list by distance in ascending order. (smallest to largest) */
	public static final String SORT_DISTANCE = Sort.DISTANCE;

	/** Sort a list by distance in descending order. (largest to smallest)*/
	public static final String SORT_DISTANCE_DESC = Sort.DISTANCE_DESC;

	/** Sort a list by page (in catalog) in ascending order. (smallest to largest) */
	public static final String SORT_PAGE = Sort.PAGE;

	/** Sort a list by page (in catalog) in descending order. (largest to smallest)*/
	public static final String SORT_PAGE_DESC = Sort.PAGE_DESC;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED = Sort.CREATED;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED_DESC = Sort.CREATED_DESC;

	/** Parameter for getting a list of specific catalog id's */
	public static final String FILTER_CATALOG_IDS = Params.FILTER_CATALOG_IDS;

	/** Parameter for posting a list of store id's to publish the catalog in */
	public static final String FILTER_STORE_IDS = Params.FILTER_STORE_IDS;

	/** Parameter for posting a list of store id's to publish the catalog in */
	public static final String FILTER_DEALER_IDS = Params.FILTER_DEALER_IDS;

	/** String identifying the query parameter */
	public static final String PARAM_QUERY = Params.QUERY;
	
	/** Endpoint for offer list resource */
	public static final String ENDPOINT_LIST = Endpoint.OFFER_LIST;

	/** Endpoint for a single offer resource */
	public static final String ENDPOINT_ID = Endpoint.OFFER_ID;

	/** Endpoint for searching offers */
	public static final String ENDPOINT_SEARCH = Endpoint.OFFER_SEARCH;
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.DATE_FORMAT);
	
	// From JSON blob
	private String mId;
	private String mErn;
	private boolean mSelectStores;
	private String mHeading;
	private String mDescription;
	private int mCatalogPage;
	private Pricing mPricing;
	private Quantity mQuantity;
	private Images mImages;
	private Links mLinks;
	private long mRunFrom;
	private long mRunTill;
	private long mPublish;
	private String mDealerUrl;
	private String mDealerId;
	private String mStoreUrl;
	private String mStoreId;
	private String mCatalogUrl;
	private String mCatalogId;
	
	// Other stuff
	private Catalog mCatalog;
	private Dealer mDealer;
	private Store mStore;

	public Offer(JSONObject offer) {
		try {
			mId = offer.getString("id");
			mErn = offer.getString("ern");
			mSelectStores = offer.getBoolean("select_stores");
			mHeading = offer.getString("heading");
			mDescription = offer.getString("description");
			mCatalogPage = offer.getInt("catalog_page");
			mPricing = new Pricing(offer.getJSONObject("pricing"));
			mQuantity = new Quantity(offer.getJSONObject("quantity"));
			mImages = new Images(offer.getJSONObject("images"));
			mLinks = new Links(offer.getJSONObject("links"));
			setRunFrom(offer.getString("run_from"));
			setRunTill(offer.getString("run_till"));
			setPublish(offer.getString("publish"));
			mDealerUrl = offer.getString("dealer_url");
			mDealerId = offer.getString("dealer_id");
			mStoreUrl = offer.getString("store_url");
			mStoreId = offer.getString("store_id");
			mCatalogUrl = offer.getString("catalog_url");
			mCatalogId = offer.getString("catalog_id");

		} catch (JSONException e) {
			e.printStackTrace();
			
		}
	}

	public String getId() {
		return mId;
	}

	public Offer setId(String id) {
		mId = id;
		return this;
	}

	public String getErn() {
		return mErn;
	}

	public Offer setErn(String ern) {
		mErn = ern;
		return this;
	}

	public boolean isSelectStores() {
		return mSelectStores;
	}

	public Offer setSelectStores(boolean selectStores) {
		mSelectStores = selectStores;
		return this;
	}

	public String getHeading() {
		return mHeading;
	}

	public Offer setHeading(String heading) {
		mHeading = heading;
		return this;
	}

	public String getDescription() {
		return mDescription;
	}

	public Offer setDescription(String description) {
		mDescription = description;
		return this;
	}

	public int getCatalogPage() {
		return mCatalogPage;
	}

	public Offer setCatalogPage(int catalogPage) {
		mCatalogPage = catalogPage;
		return this;
	}

	public Pricing getPricing() {
		return mPricing;
	}

	public Offer setPricing(Pricing pricing) {
		mPricing = pricing;
		return this;
	}

	public Quantity getQuantity() {
		return mQuantity;
	}

	public Offer setQuantity(Quantity quantity) {
		mQuantity = quantity;
		return this;
	}

	public Images getImages() {
		return mImages;
	}

	public Offer setImages(Images images) {
		mImages = images;
		return this;
	}

	public Links getLinks() {
		return mLinks;
	}

	public Offer setLinks(Links links) {
		mLinks = links;
		return this;
	}

	public long getRunFrom() {
		return mRunFrom;
	}

	public String getRunFromString() {
		return sdf.format(new Date(mRunFrom));
	}

	public Offer setRunFrom(long time) {
		mRunFrom = time;
		return this;
	}

	public Offer setRunFrom(String time) {
		try {
			mRunFrom = sdf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return this;
	}

	public long getRunTill() {
		return mRunTill;
	}

	public String getRunTillString() {
		return sdf.format(new Date(mRunTill));
	}

	public Offer setRunTill(long time) {
		mRunTill = time;
		return this;
	}

	public Offer setRunTill(String time) {
		try {
			mRunTill = sdf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return this;
	}

	public long getPublish() {
		return mPublish;
	}

	public String getPublishString() {
		return sdf.format(new Date(mPublish));
	}

	public Offer setPublish(long time) {
		mPublish = time;
		return this;
	}

	public Offer setPublish(String time) {
		try {
			mPublish = sdf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return this;
	}

	public String getDealerUrl() {
		return mDealerUrl;
	}

	public Offer setDealerUrl(String url) {
		mDealerUrl = url;
		return this;
	}

	public String getDealerId() {
		return mDealerId;
	}

	public Offer setDealerId(String dealerId) {
		mDealerId = dealerId;
		return this;
	}

	public String getStoreUrl() {
		return mStoreUrl;
	}

	public Offer setStoreUrl(String url) {
		mStoreUrl = url;
		return this;
	}

	public String getStoreId() {
		return mStoreId;
	}

	public Offer setStoreId(String storeId) {
		mStoreId = storeId;
		return this;
	}

	public String getCatalogUrl() {
		return mCatalogUrl;
	}

	public Offer setCatalogUrl(String url) {
		mCatalogUrl = url;
		return this;
	}

	public String getCatalogId() {
		return mCatalogId;
	}

	public Offer setCatalogId(String catalogId) {
		mCatalogId = catalogId;
		return this;
	}

	public Catalog getCatalog() {
		return mCatalog;
	}

	public Offer setCatalog(Catalog catalog) {
		mCatalog = catalog;
		return this;
	}

	public Dealer getDealer() {
		return mDealer;
	}

	public Offer setDealer(Dealer dealer) {
		mDealer = dealer;
		return this;
	}

	public Store getStore() {
		return mStore;
	}

	public Offer setmStore(Store store) {
		mStore = store;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (!(obj instanceof Offer))
			return false;

		Offer o = (Offer)obj;
		return mId.equals(o.getId()) &&
				mErn == o.getErn() &&
				mSelectStores == o.isSelectStores() &&
				mHeading.equals(o.getHeading()) &&
				mDescription.equals(o.getDescription()) &&
				mCatalogPage == o.getCatalogPage() &&
				mPricing == null ? o.getPricing() == null : mPricing.equals(o.getPricing()) &&
				mQuantity == null ? o.getQuantity() == null : mQuantity.equals(o.getQuantity()) &&
				mImages == null ? o.getImages() == null : mImages.equals(o.getImages()) &&
				mLinks == null ? o.getLinks() == null : mLinks.equals(o.getLinks()) &&
				mRunFrom == o.getRunFrom() &&
				mRunTill == o.getRunTill() &&
				mPublish == o.getPublish() &&
				mDealerUrl.equals(o.getDealerUrl()) &&
				mDealerId.equals(o.getDealerId()) &&
				mStoreUrl.equals(o.getStoreUrl()) &&
				mStoreId.equals(o.getStoreId()) &&
				mCatalogUrl.equals(o.getCatalogUrl()) &&
				mCatalogId.equals(o.getCatalogId()) &&
				mCatalog == null ? o.getCatalog() == null : mCatalog.equals(o.getCatalog()) &&
				mDealer == null ? o.getDealer() == null : mDealer.equals(o.getDealer()) &&
				mStore == null ? o.getStore() == null : mStore.equals(o.getStore());
	}
	
	/**
	 * Returns a human readable string containing id, heading, dealer of the offer. <br>
	 * SimpleClassName[prop1=value, prop2=NestedObject[prop3=value]]
	 * @return <li> Offer digest as a string 
	 */
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append("heading=").append(mHeading)
		.append(", description=").append(mDescription)
		.append(", dealer=").append(mDealer == null ? mDealerId : mDealer.toString(everything))
		.append(", pricing=").append(mPricing.toString())
		.append(", runFrom=").append(getRunFromString())
		.append(", runTill=").append(getRunTillString());
		
		if (everything) {
			sb.append(", ern=").append(mErn)
			.append(", selectStores=").append(mSelectStores)
			.append(", catalogPage=").append(mCatalogPage)
			.append(", quantity=").append(mQuantity.toString())
			.append(", images=").append(mImages.toString())
			.append(", links=").append(mLinks.toString())
			.append(", publish=").append(mPublish)
			.append(", store=").append(mStore == null ? mStoreId : mStore.toString())
			.append(", catalog=").append(mCatalog == null ? mCatalogId : mCatalog.toString(everything));
		}
		return sb.append("]").toString();
	}
	
}
