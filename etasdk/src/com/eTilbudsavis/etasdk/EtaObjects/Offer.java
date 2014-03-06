package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Utils;

/**
 * <p>This class is a representation of an offer as the API v2 exposes it</p>
 * 
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/offers.html">Offer Reference</a>
 * documentation, on the engineering blog.
 * </p>
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class Offer extends EtaErnObject<Offer> implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Offer";
	
	private String mHeading;
	private String mDescription;
	private int mCatalogPage = 0;
	private Pricing mPricing;
	private Quantity mQuantity;
	private Images mImages;
	private Links mLinks;
	private Date mRunFrom;
	private Date mRunTill;
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
	
	/**
	 * Default constructor
	 */
	public Offer() {
		
	}

	/**
	 * Convert a {@link JSONArray} into a {@link List} of Offer.
	 * @param list A {@link JSONArray} containing API v2 Offer objects
	 * @return A {@link List} of Offer
	 */
	public static ArrayList<Offer> fromJSON(JSONArray offers) {
		ArrayList<Offer> list = new ArrayList<Offer>();
		try {
			for (int i = 0 ; i < offers.length() ; i++ )
				list.add(Offer.fromJSON((JSONObject) offers.get(i)));
			
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}

	/**
	 * A factory method for converting JSON into POJO.
	 * @param offer A {@link JSONArray} containing API v2 offer objects
	 * @return A {@link List}
	 */
	public static Offer fromJSON(JSONObject offer) {
		return fromJSON(new Offer(), offer);
	}

	private static Offer fromJSON(Offer o, JSONObject offer) {
		if (o == null) o = new Offer();
		if (offer == null) return o;

		try {
			o.setId(Json.valueOf(offer, ServerKey.ID));
			o.setErn(Json.valueOf(offer, ServerKey.ERN));
			o.setHeading(Json.valueOf(offer, ServerKey.HEADING));
			o.setDescription(Json.valueOf(offer, ServerKey.DESCRIPTION));
			o.setCatalogPage(Json.valueOf(offer, ServerKey.CATALOG_PAGE, 0));
			o.setPricing(Pricing.fromJSON(offer.getJSONObject(ServerKey.PRICING)));
			o.setQuantity(Quantity.fromJSON(offer.getJSONObject(ServerKey.QUANTITY)));
			o.setImages(Images.fromJSON(offer.getJSONObject(ServerKey.IMAGES)));
			o.setLinks(Links.fromJSON(offer.getJSONObject(ServerKey.LINKS)));
			Date runFrom = Utils.parseDate(Json.valueOf(offer, ServerKey.RUN_FROM));
			o.setRunFrom(runFrom);
			Date runTill = Utils.parseDate(Json.valueOf(offer, ServerKey.RUN_TILL));
			o.setRunTill(runTill);
			o.setDealerUrl(Json.valueOf(offer, ServerKey.DEALER_URL));
			o.setDealerId(Json.valueOf(offer, ServerKey.DEALER_ID));
			o.setStoreUrl(Json.valueOf(offer, ServerKey.STORE_URL));
			o.setStoreId(Json.valueOf(offer, ServerKey.STORE_ID));
			o.setCatalogUrl(Json.valueOf(offer, ServerKey.CATALOG_URL));
			o.setCatalogId(Json.valueOf(offer, ServerKey.CATALOG_ID));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = super.toJSON();
		try {
			o.put(ServerKey.HEADING, Json.nullCheck(getHeading()));
			o.put(ServerKey.DESCRIPTION, Json.nullCheck(getDescription()));
			o.put(ServerKey.CATALOG_PAGE, getCatalogPage());
			o.put(ServerKey.PRICING, Json.toJson(getPricing()));
			o.put(ServerKey.QUANTITY, Json.toJson(getQuantity()));
			o.put(ServerKey.IMAGES, Json.toJson(getImages()));
			o.put(ServerKey.LINKS, Json.toJson(getLinks()));
			o.put(ServerKey.RUN_FROM, Json.nullCheck(Utils.parseDate(getRunFrom())));
			o.put(ServerKey.RUN_TILL, Json.nullCheck(Utils.parseDate(getRunTill())));
			o.put(ServerKey.DEALER_URL, Json.nullCheck(getDealerUrl()));
			o.put(ServerKey.DEALER_ID, Json.nullCheck(getDealerId()));
			o.put(ServerKey.STORE_URL, Json.nullCheck(getStoreUrl()));
			o.put(ServerKey.STORE_ID, Json.nullCheck(getStoreId()));
			o.put(ServerKey.CATALOG_URL, Json.nullCheck(getCatalogUrl()));
			o.put(ServerKey.CATALOG_ID, Json.nullCheck(getCatalogId()));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	@Override
	public String getErnPrefix() {
		return ERN_OFFER;
	}
	
	/**
	 * Get the offer heading
	 * @return A {@link String}, or null
	 */
	public String getHeading() {
		return mHeading;
	}
	
	/**
	 * Set the heading of this offer.
	 * @param heading A string describing the offer
	 * @return This object
	 */
	public Offer setHeading(String heading) {
		mHeading = heading;
		return this;
	}
	
	/**
	 * Get the description of this object. Description isn't required and may
	 * therefore be <code>null</code>.
	 * @return A {@link String}, or null
	 */
	public String getDescription() {
		return mDescription;
	}
	
	/**
	 * Set the description of this offer.
	 * @param description A string describing the offer
	 * @return This object
	 */
	public Offer setDescription(String description) {
		mDescription = description;
		return this;
	}
	
	/**
	 * Get the page number of the catalog where this offer was indexed from.
	 * @return A page number, or 0 if a page number is not set.
	 */
	public int getCatalogPage() {
		return mCatalogPage;
	}
	
	/**
	 * Set the page number where this offer was found in the catalog.
	 * @param catalogPage A integer representing the page
	 * @return This object
	 */
	public Offer setCatalogPage(int catalogPage) {
		mCatalogPage = catalogPage;
		return this;
	}
	
	/**
	 * Returns a {@link Pricing} object, where additional information of price,
	 * discount e.t.c. can be found.
	 * @return {@link Pricing} object, or null 
	 */
	public Pricing getPricing() {
		return mPricing;
	}
	
	/**
	 * Set {@link Pricing} for this offer.
	 * @param pricing {@link Pricing} for this offer
	 * @return This object
	 */
	public Offer setPricing(Pricing pricing) {
		mPricing = pricing;
		return this;
	}
	
	/**
	 * Get the {@link Quantity} for this offer, where additional information
	 * can be gathered, about weight, dimensions e.t.c.
	 * @return {@link Quantity} object, or <code>null</code>
	 */
	public Quantity getQuantity() {
		return mQuantity;
	}
	
	/**
	 * Set the {@link Quantity} model for this offer
	 * @param quantity {@link Quantity} containing relevant information about this offer.
	 * @return This object
	 */
	public Offer setQuantity(Quantity quantity) {
		mQuantity = quantity;
		return this;
	}
	
	/**
	 * Get the {@link Images} object for this offer, where graphics for the offer is found.
	 * @return {@link Images}, or null
	 */
	public Images getImages() {
		return mImages;
	}
	
	/**
	 * Set {@link Images} relevant for this offer.
	 * @param images {@link Images} object with relevant graphics for this offer
	 * @return This object
	 */
	public Offer setImages(Images images) {
		mImages = images;
		return this;
	}
	
	/**
	 * Get {@link Links} that this offer has, including webshop links e.t.c.
	 * @return {@link Links} object, or <code>null</code>
	 */
	public Links getLinks() {
		return mLinks;
	}
	
	/**
	 * Set {@link Links} relevant for this offer
	 * @param links {@link Links} relevant for this offer
	 * @return
	 */
	public Offer setLinks(Links links) {
		mLinks = links;
		return this;
	}
	
	/**
	 * Returns the {@link Date} this offer will be valid from.
	 * @return A {@link Date}, or <code>null</code>
	 */
	public Date getRunFrom() {
		return mRunFrom;
	}
	
	/**
	 * Set the {@link Date} this offer will be valid from.
	 * 
	 * <p>The {@link Date#getTime() time} of the date will be floored to the
	 * nearest second (i.e. milliseconds will be removed) as the server responds 
	 * in seconds and comparison of {@link Date}s will other wise be 
	 * unpredictable/impossible.</p>
	 * 
	 * @param date A {@link Date}
	 * @return This object
	 */
	public Offer setRunFrom(Date date) {
		mRunFrom = Utils.roundTime(date);
		return this;
	}
	
	/**
	 * Returns the {@link Date} this offer will be valid till.
	 * @return A {@link Date}, or <code>null</code>
	 */
	public Date getRunTill() {
		return mRunTill;
	}

	/**
	 * Set the {@link Date} this offer will be valid till.
	 * 
	 * <p>The {@link Date#getTime() time} of the date will be floored to the
	 * nearest second (i.e. milliseconds will be removed) as the server responds 
	 * in seconds and comparison of {@link Date}s will other wise be 
	 * unpredictable/impossible.</p>
	 * 
	 * @param date A {@link Date}
	 * @return This object
	 */
	public Offer setRunTill(Date date) {
		mRunTill = Utils.roundTime(date);
		return this;
	}
	
	/**
	 * Get the URL that points directly to the {@link Dealer} resource of this
	 * offer, this is for convenience only.
	 * <p>e.g.: "https://api.etilbudsavis.dk/v2/dealers/9bc61"</p>
	 * @return A {@link String}, or <code>null</code>
	 */
	public String getDealerUrl() {
		return mDealerUrl;
	}
	
	/**
	 * Set an URL of the {@link Dealer} resource of this offer.
	 * <p>This is most likely decided by the API</p>
	 * @param url An URL to a dealer resource
	 * @return This object
	 */
	public Offer setDealerUrl(String url) {
		mDealerUrl = url;
		return this;
	}
	
	/**
	 * Get the id for a {@link Dealer} resource related to this offer. 
	 * @return An id, or <code>null</code>
	 */
	public String getDealerId() {
		return mDealerId;
	}
	
	/**
	 * Set the id for a {@link Dealer} resource related to this offer.
	 * <p>This is most likely to be set by the API, not the client</p>
	 * @param dealerId An id
	 * @return This object
	 */
	public Offer setDealerId(String dealerId) {
		mDealerId = dealerId;
		return this;
	}

	/**
	 * Get the URL that points directly to the {@link Store} resource of this
	 * offer, this is for convenience only.
	 * <p>e.g.: "https://api.etilbudsavis.dk/v2/stores/6d36wXI"</p>
	 * @return A {@link String}, or <code>null</code>
	 */
	public String getStoreUrl() {
		return mStoreUrl;
	}

	/**
	 * Set an URL of the {@link Store} resource of this offer.
	 * <p>This is most likely to be set by the API, not the client</p>
	 * @param url An URL to a store resource
	 * @return This object
	 */
	public Offer setStoreUrl(String url) {
		mStoreUrl = url;
		return this;
	}

	/**
	 * Get the id for a {@link Store} resource related to this offer. 
	 * @return An id, or <code>null</code>
	 */
	public String getStoreId() {
		return mStoreId;
	}

	/**
	 * Set the id for a {@link Store} resource related to this offer.
	 * <p>This is most likely to be set by the API, not the client</p>
	 * @param dealerId An id
	 * @return This object
	 */
	public Offer setStoreId(String storeId) {
		mStoreId = storeId;
		return this;
	}
	
	/**
	 * Get the URL that points directly to the {@link Catalog} resource of this
	 * offer, this is for convenience only.
	 * <p>e.g.: "https://api.etilbudsavis.dk/v2/catalogs/56e37cL"</p>
	 * @return A {@link String}, or <code>null</code>
	 */
	public String getCatalogUrl() {
		return mCatalogUrl;
	}

	/**
	 * Set an URL of the {@link Catalog} resource of this offer.
	 * <p>This is most likely to be set by the API, not the client</p>
	 * @param url An URL to a store resource
	 * @return This object
	 */
	public Offer setCatalogUrl(String url) {
		mCatalogUrl = url;
		return this;
	}

	/**
	 * Get the id for a {@link Catalog} resource related to this offer. 
	 * @return An id, or <code>null</code>
	 */
	public String getCatalogId() {
		return mCatalogId;
	}

	/**
	 * Set the id for a {@link Catalog} resource related to this offer.
	 * <p>This is most likely to be set by the API, not the client</p>
	 * @param dealerId An id
	 * @return This object
	 */
	public Offer setCatalogId(String catalogId) {
		mCatalogId = catalogId;
		return this;
	}
	
	/**
	 * Get the {@link Catalog} which is (or rather should be, but this is not
	 * guaranteed) related to this offer.
	 * 
	 * <p>The catalog-object <b>is not</b> automatically set by the SDK.
	 * The developer, needs to get the catalog resource from {@link #getCatalogUrl()},
	 * and then add it with {@link #setCatalog(Catalog)}.</p>
	 * @return A {@link Catalog}, or null if developer have not set the resource
	 */
	public Catalog getCatalog() {
		return mCatalog;
	}
	
	/**
	 * Set a {@link Catalog} on this offer
	 * @param catalog A {@link Catalog} (preferably related to this offer)
	 * @return This object
	 */
	public Offer setCatalog(Catalog catalog) {
		mCatalog = catalog;
		return this;
	}

	/**
	 * Get the {@link Dealer} which is (or rather should be, but this is not
	 * guaranteed) related to this offer.
	 * 
	 * <p>The dealer-object <b>is not</b> automatically set by the SDK.
	 * The developer, needs to get the dealer resource from {@link #getDealerUrl()},
	 * and then add it with {@link #setDealer(Dealer)}.</p>
	 * @return A {@link Dealer}, or null if developer have not set the resource
	 */
	public Dealer getDealer() {
		return mDealer;
	}

	/**
	 * Set a {@link Dealer} on this offer
	 * @param dealer A {@link Dealer} (preferably related to this offer)
	 * @return This object
	 */
	public Offer setDealer(Dealer dealer) {
		mDealer = dealer;
		return this;
	}

	/**
	 * Get the {@link Store} which is (or rather should be, but this is not
	 * guaranteed) related to this offer.
	 * 
	 * <p>The store-object <b>is not</b> automatically set by the SDK.
	 * The developer, needs to get the store resource from {@link #getStoreUrl()},
	 * and then add it with {@link #setStore(Store)}.</p>
	 * @return A {@link Store}, or null if developer have not set the resource
	 */
	public Store getStore() {
		return mStore;
	}

	/**
	 * Set a {@link Store} on this offer
	 * @param store A {@link Store} (preferably related to this offer)
	 * @return This object
	 */
	public Offer setStore(Store store) {
		mStore = store;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mCatalogId == null) ? 0 : mCatalogId.hashCode());
		result = prime * result + mCatalogPage;
		result = prime * result
				+ ((mCatalogUrl == null) ? 0 : mCatalogUrl.hashCode());
		result = prime * result
				+ ((mDealerId == null) ? 0 : mDealerId.hashCode());
		result = prime * result
				+ ((mDealerUrl == null) ? 0 : mDealerUrl.hashCode());
		result = prime * result
				+ ((mDescription == null) ? 0 : mDescription.hashCode());
		result = prime * result
				+ ((mHeading == null) ? 0 : mHeading.hashCode());
		result = prime * result + ((mImages == null) ? 0 : mImages.hashCode());
		result = prime * result + ((mLinks == null) ? 0 : mLinks.hashCode());
		result = prime * result
				+ ((mPricing == null) ? 0 : mPricing.hashCode());
		result = prime * result
				+ ((mQuantity == null) ? 0 : mQuantity.hashCode());
		result = prime * result
				+ ((mRunFrom == null) ? 0 : mRunFrom.hashCode());
		result = prime * result
				+ ((mRunTill == null) ? 0 : mRunTill.hashCode());
		result = prime * result
				+ ((mStoreId == null) ? 0 : mStoreId.hashCode());
		result = prime * result
				+ ((mStoreUrl == null) ? 0 : mStoreUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Offer other = (Offer) obj;
		if (mCatalogId == null) {
			if (other.mCatalogId != null)
				return false;
		} else if (!mCatalogId.equals(other.mCatalogId))
			return false;
		if (mCatalogPage != other.mCatalogPage)
			return false;
		if (mCatalogUrl == null) {
			if (other.mCatalogUrl != null)
				return false;
		} else if (!mCatalogUrl.equals(other.mCatalogUrl))
			return false;
		if (mDealerId == null) {
			if (other.mDealerId != null)
				return false;
		} else if (!mDealerId.equals(other.mDealerId))
			return false;
		if (mDealerUrl == null) {
			if (other.mDealerUrl != null)
				return false;
		} else if (!mDealerUrl.equals(other.mDealerUrl))
			return false;
		if (mDescription == null) {
			if (other.mDescription != null)
				return false;
		} else if (!mDescription.equals(other.mDescription))
			return false;
		if (mHeading == null) {
			if (other.mHeading != null)
				return false;
		} else if (!mHeading.equals(other.mHeading))
			return false;
		if (mImages == null) {
			if (other.mImages != null)
				return false;
		} else if (!mImages.equals(other.mImages))
			return false;
		if (mLinks == null) {
			if (other.mLinks != null)
				return false;
		} else if (!mLinks.equals(other.mLinks))
			return false;
		if (mPricing == null) {
			if (other.mPricing != null)
				return false;
		} else if (!mPricing.equals(other.mPricing))
			return false;
		if (mQuantity == null) {
			if (other.mQuantity != null)
				return false;
		} else if (!mQuantity.equals(other.mQuantity))
			return false;
		if (mRunFrom == null) {
			if (other.mRunFrom != null)
				return false;
		} else if (!mRunFrom.equals(other.mRunFrom))
			return false;
		if (mRunTill == null) {
			if (other.mRunTill != null)
				return false;
		} else if (!mRunTill.equals(other.mRunTill))
			return false;
		if (mStoreId == null) {
			if (other.mStoreId != null)
				return false;
		} else if (!mStoreId.equals(other.mStoreId))
			return false;
		if (mStoreUrl == null) {
			if (other.mStoreUrl != null)
				return false;
		} else if (!mStoreUrl.equals(other.mStoreUrl))
			return false;
		return true;
	}
	
}
