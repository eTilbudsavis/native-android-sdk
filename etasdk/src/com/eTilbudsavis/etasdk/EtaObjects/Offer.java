package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import Utils.Endpoint;
import Utils.Sort;

public class Offer implements Serializable {
	
	private static final long serialVersionUID = 1L;

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

	/** Parameter for getting a list of specific offer id's */
	public static final String PARAM_IDS = "offer_ids";

	/** Endpoint for offer list resource */
	public static final String ENDPOINT_LIST = Endpoint.OFFER_LIST;

	/** Endpoint for a single offer resource */
	public static final String ENDPOINT_ID = Endpoint.OFFER_ID;

	/** Endpoint for getting multiple offer resources */
	public static final String ENDPOINT_IDS = Endpoint.OFFER_IDS;
	
	/** Endpoint for searching offers */
	public static final String ENDPOINT_SEARCH = Endpoint.OFFER_SEARCH;

	private String mId;
	private boolean mSelectStores;
	private boolean mPromoted;
	private String mHeading;
	private String mDescription;
	private double mPrice;
	private Double mPrePrice;
	private String mImageView;
	private String mImageZoom;
	private String mImageThumb;
	private String mUrl;
	private String mBuyUrl;
	private String mDealerId;
	private Dealer mDealer;
	private long mRunFrom;
	private long mRunTill;
	private Store mStore;
	private Catalog mCatalog;
	private String mCurrency;
	
	public Offer(JSONObject offer) {
		try {
			mId = offer.getString("id");
			mSelectStores = offer.getBoolean("selectStores");
			mPromoted = offer.getBoolean("promoted");
			mHeading = offer.getString("heading");
			mDescription = offer.getString("description");
			mPrice = offer.getDouble("price");
			mPrePrice = offer.getString("preprice").equals("null") == true ? null : offer.getDouble("preprice");
			mImageView = offer.getJSONObject("images").getString("view");
			mImageZoom = offer.getJSONObject("images").getString("zoom");
			mImageThumb = offer.getJSONObject("images").getString("thumbnail");
			mUrl = offer.getString("url");
			mBuyUrl = offer.getString("buy").equals("null") == true ? null : offer.getString("buy");
			mDealer = new Dealer(offer.getJSONObject("dealer"));
			mRunFrom = (offer.getLong("runFrom")*1000);
			mRunTill = (offer.getLong("runTill")*1000);
			mStore = new Store(offer.getJSONObject("store"));
			mCatalog = offer.getString("catalog").equals("null") == true ? null : new Catalog(offer.getJSONObject("catalog"));
			mCurrency = offer.getString("currency");
		} catch (JSONException e) {
			e.printStackTrace();
			
		}
	}
	
	public String getId() {
		return mId;
	}

	public boolean isSelectStores() {
		return mSelectStores;
	}

	public boolean isPromoted() {
		return mPromoted;
	}

	public String getHeading() {
		return mHeading;
	}

	public String getDescription() {
		return mDescription;
	}

	public Double getPrice() {
		return mPrice;
	}

	public Double getPrePrice() {
		return mPrePrice;
	}

	public String getImageView() {
		return mImageView;
	}

	public String getImageZoom() {
		return mImageZoom;
	}

	public String getImageThumb() {
		return mImageThumb;
	}

	public String getUrl() {
		return mUrl;
	}

	public String getBuyUrl() {
		return mBuyUrl;
	}

	public Dealer getDealer() {
		return mDealer;
	}

	public String getDealerId() {
		return mDealerId;
	}

	public Long getRunFrom() {
		return mRunFrom;
	}

	public Long getRunTill() {
		return mRunTill;
	}

	public Store getStore() {
		return mStore;
	}

	public Catalog getCatalog() {
		return mCatalog;
	}

	/**
	 * 
	 * @return
	 */
	public String getCurrency() {
		return mCurrency;
	}
	

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Offer))
			return false;

		Offer offer = (Offer)o;
		return mId.equals(offer.getId()) &&
				mSelectStores == offer.isSelectStores() &&
				mPromoted == offer.isPromoted() &&
				mHeading.equals(offer.getHeading()) &&
				mDescription.equals(offer.getDescription()) &&
				mPrice == offer.getPrice() &&
				( mPrePrice == null ? offer.getPrePrice() == null : mPrePrice.equals(offer.getPrePrice()) )&&
				mImageView.equals(offer.getImageView()) &&
				mImageZoom.equals(offer.getImageZoom()) &&
				mImageThumb.equals(offer.getImageThumb()) &&
				mUrl.equals(offer.getUrl()) &&
				( mBuyUrl == null ? offer.getBuyUrl() == null : mBuyUrl.equals(offer.getBuyUrl()) )&&
				mDealer.equals(offer.getDealer()) &&
				mRunFrom == offer.getRunFrom() &&
				mRunTill == offer.getRunTill() &&
				mStore.equals(offer.getStore()) &&
				mCatalog.equals(offer.getCatalog()) &&
				mCurrency.equals(offer.getCurrency());
	}
	
	/**
	 * Returns a human readable string containing id, heading, dealer of the offer. 
	 * E.g. <pre>Offer { Heading: Beer, Id: 1x2y3z, Dealer: SaveALot}</pre>
	 * @return <li> Offer digest as a string 
	 */
	@Override
	public String toString() {
		return new StringBuilder()
		.append("Offer: { ")
		.append("Heading: ").append(mHeading)
		.append(", Id: ").append(mId)
		.append(", DealerId: ").append(mDealerId)
		.append("}").toString();
		
	}
}
