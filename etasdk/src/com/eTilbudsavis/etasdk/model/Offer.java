/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.interfaces.ICatalog;
import com.eTilbudsavis.etasdk.model.interfaces.IDealer;
import com.eTilbudsavis.etasdk.model.interfaces.IErn;
import com.eTilbudsavis.etasdk.model.interfaces.IJson;
import com.eTilbudsavis.etasdk.model.interfaces.IStore;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.utils.Json;
import com.eTilbudsavis.etasdk.utils.Utils;

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
public class Offer implements IErn<Offer>, IJson<JSONObject>, ICatalog<Offer>, IDealer<Offer>, IStore<Offer>, Serializable, Parcelable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = Eta.TAG_PREFIX + Offer.class.getSimpleName();
	
	private String mErn;
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

	public static Parcelable.Creator<Offer> CREATOR = new Parcelable.Creator<Offer>(){
		public Offer createFromParcel(Parcel source) {
			return new Offer(source);
		}
		public Offer[] newArray(int size) {
			return new Offer[size];
		}
	};

	public Offer() {
		
	}
	
	/**
	 * Convert a {@link JSONArray} into a {@link List}&lt;T&gt;.
	 * @param offers A {@link JSONArray} in the format of a valid API v2 offer response
	 * @return A {@link List} of POJO;
	 */
	public static List<Offer> fromJSON(JSONArray offers) {
		List<Offer> list = new ArrayList<Offer>();
		try {
			for (int i = 0 ; i < offers.length() ; i++ ) {
				list.add(Offer.fromJSON((JSONObject) offers.get(i)));
			}
		} catch (JSONException e) {
			EtaLog.e(TAG, e.getMessage(), e);
		}
		return list;
	}
	
	/**
	 * A factory method for converting {@link JSONObject} into a POJO.
	 * @param offer A {@link JSONObject} in the format of a valid API v2 offer response
	 * @return An Offer object
	 */
	public static Offer fromJSON(JSONObject jOffer) {
		Offer offer = new Offer();
		if (jOffer == null) {
			return offer;
		}
		
		try {
			
			// Stuff found in the Hotspots parsed first
			offer.setId(Json.valueOf(jOffer, JsonKey.ID));
			offer.setErn(Json.valueOf(jOffer, JsonKey.ERN));
			offer.setHeading(Json.valueOf(jOffer, JsonKey.HEADING));
			offer.setPricing(Pricing.fromJSON(jOffer.getJSONObject(JsonKey.PRICING)));
			offer.setQuantity(Quantity.fromJSON(jOffer.getJSONObject(JsonKey.QUANTITY)));
			Date runFrom = Utils.stringToDate(Json.valueOf(jOffer, JsonKey.RUN_FROM));
			offer.setRunFrom(runFrom);
			Date runTill = Utils.stringToDate(Json.valueOf(jOffer, JsonKey.RUN_TILL));
			offer.setRunTill(runTill);
			
			// TODO: What about the publish key, found in hotspots?
			
			// The rest isn't in the hotspots and is discarded
			if ( jOffer.has(JsonKey.DESCRIPTION) && jOffer.has(JsonKey.CATALOG_ID) ) {
				offer.setDescription(Json.valueOf(jOffer, JsonKey.DESCRIPTION));
				offer.setCatalogPage(Json.valueOf(jOffer, JsonKey.CATALOG_PAGE, 0));
				offer.setImages(Images.fromJSON(jOffer.getJSONObject(JsonKey.IMAGES)));
				offer.setLinks(Links.fromJSON(jOffer.getJSONObject(JsonKey.LINKS)));
				offer.setDealerUrl(Json.valueOf(jOffer, JsonKey.DEALER_URL));
				offer.setDealerId(Json.valueOf(jOffer, JsonKey.DEALER_ID));
				offer.setStoreUrl(Json.valueOf(jOffer, JsonKey.STORE_URL));
				offer.setStoreId(Json.valueOf(jOffer, JsonKey.STORE_ID));
				offer.setCatalogUrl(Json.valueOf(jOffer, JsonKey.CATALOG_URL));
				offer.setCatalogId(Json.valueOf(jOffer, JsonKey.CATALOG_ID));
			}
			
			if (jOffer.has(JsonKey.SDK_DEALER)) {
				JSONObject jDealer = Json.getObject(jOffer, JsonKey.SDK_DEALER, null);
				offer.setDealer(Dealer.fromJSON(jDealer));
			}
			
			if (jOffer.has(JsonKey.SDK_STORE)) {
				JSONObject jStore = Json.getObject(jOffer, JsonKey.SDK_STORE, null);
				offer.setStore(Store.fromJSON(jStore));
			}
			
			if (jOffer.has(JsonKey.SDK_CATALOG)) {
				JSONObject jCatalog = Json.getObject(jOffer, JsonKey.SDK_CATALOG, null);
				offer.setCatalog(Catalog.fromJSON(jCatalog));
			}
			
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		
		return offer;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.ID, Json.nullCheck(getId()));
			o.put(JsonKey.ERN, Json.nullCheck(getErn()));
			o.put(JsonKey.HEADING, Json.nullCheck(getHeading()));
			o.put(JsonKey.DESCRIPTION, Json.nullCheck(getDescription()));
			o.put(JsonKey.CATALOG_PAGE, getCatalogPage());
			o.put(JsonKey.PRICING, Json.toJson(getPricing()));
			o.put(JsonKey.QUANTITY, Json.toJson(getQuantity()));
			o.put(JsonKey.IMAGES, Json.toJson(getImages()));
			o.put(JsonKey.LINKS, Json.toJson(getLinks()));
			o.put(JsonKey.RUN_FROM, Json.nullCheck(Utils.dateToString(getRunFrom())));
			o.put(JsonKey.RUN_TILL, Json.nullCheck(Utils.dateToString(getRunTill())));
			o.put(JsonKey.DEALER_URL, Json.nullCheck(getDealerUrl()));
			o.put(JsonKey.DEALER_ID, Json.nullCheck(getDealerId()));
			o.put(JsonKey.STORE_URL, Json.nullCheck(getStoreUrl()));
			o.put(JsonKey.STORE_ID, Json.nullCheck(getStoreId()));
			o.put(JsonKey.CATALOG_URL, Json.nullCheck(getCatalogUrl()));
			o.put(JsonKey.CATALOG_ID, Json.nullCheck(getCatalogId()));

			if (mCatalog!=null) {
				o.put(JsonKey.SDK_CATALOG, Json.toJson(mCatalog));
			}

			if (mDealer!=null) {
				o.put(JsonKey.SDK_DEALER, Json.toJson(mDealer));
			}
			
			if (mStore!=null) {
				o.put(JsonKey.SDK_STORE, Json.toJson(mStore));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	public Offer setId(String id) {
		setErn((id==null) ? null : String.format("ern:%s:%s", getErnType(), id));
		return this;
	}
	
	public String getId() {
		if (mErn==null) {
			return null;
		}
		String[] parts = mErn.split(":");
		return parts[parts.length-1];
	}
	
	public Offer setErn(String ern) {
		if (ern==null || ( ern.startsWith("ern:") && ern.split(":").length==3 && ern.contains(getErnType()) )) {
			mErn = ern;
		}
		return this;
	}
	
	public String getErn() {
		return mErn;
	}
	
	public String getErnType() {
		return IErn.TYPE_OFFER;
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
	 * Returns the {@link Date} this offer is be valid from.
	 * @return A {@link Date}, or <code>null</code>
	 */
	public Date getRunFrom() {
		return mRunFrom;
	}
	
	/**
	 * Set the {@link Date} this offer is be valid from.
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
	 * Returns the {@link Date} this offer is be valid till.
	 * @return A {@link Date}, or <code>null</code>
	 */
	public Date getRunTill() {
		return mRunTill;
	}

	/**
	 * Set the {@link Date} this offer is be valid till.
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
		if (mDealerId == null) {
			mDealer = null;
		} else if (mDealer != null && !mDealerId.equals(mDealer.getId()) ) {
			mDealer = null;
		}
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
		if (mStoreId == null) {
			mStore = null;
		} else if (mStore != null && !mStoreId.equals(mStore.getId()) ) {
			mStore = null;
		}
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
		if (mCatalogId == null) {
			mCatalog = null;
		} else if (mCatalog != null && !mCatalogId.equals(mCatalog.getId()) ) {
			mCatalog = null;
		}
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
		mCatalogId = (mCatalog==null ? null : mCatalog.getId());
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
		mDealerId = (mDealer==null ? null : mDealer.getId());
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
		mStoreId = (mStore==null ? null : mStore.getId());
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mCatalog == null) ? 0 : mCatalog.hashCode());
		result = prime * result
				+ ((mCatalogId == null) ? 0 : mCatalogId.hashCode());
		result = prime * result + mCatalogPage;
		result = prime * result
				+ ((mCatalogUrl == null) ? 0 : mCatalogUrl.hashCode());
		result = prime * result + ((mDealer == null) ? 0 : mDealer.hashCode());
		result = prime * result
				+ ((mDealerId == null) ? 0 : mDealerId.hashCode());
		result = prime * result
				+ ((mDealerUrl == null) ? 0 : mDealerUrl.hashCode());
		result = prime * result
				+ ((mDescription == null) ? 0 : mDescription.hashCode());
		result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
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
		result = prime * result + ((mStore == null) ? 0 : mStore.hashCode());
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Offer other = (Offer) obj;
		if (mCatalog == null) {
			if (other.mCatalog != null)
				return false;
		} else if (!mCatalog.equals(other.mCatalog))
			return false;
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
		if (mDealer == null) {
			if (other.mDealer != null)
				return false;
		} else if (!mDealer.equals(other.mDealer))
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
		if (mErn == null) {
			if (other.mErn != null)
				return false;
		} else if (!mErn.equals(other.mErn))
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
		if (mStore == null) {
			if (other.mStore != null)
				return false;
		} else if (!mStore.equals(other.mStore))
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

	private Offer(Parcel in) {
		this.mErn = in.readString();
		this.mHeading = in.readString();
		this.mDescription = in.readString();
		this.mCatalogPage = in.readInt();
		this.mPricing = in.readParcelable(Pricing.class.getClassLoader());
		this.mQuantity = in.readParcelable(Quantity.class.getClassLoader());
		this.mImages = in.readParcelable(Images.class.getClassLoader());
		this.mLinks = in.readParcelable(Links.class.getClassLoader());
		long tmpMRunFrom = in.readLong(); 
		this.mRunFrom = tmpMRunFrom == -1 ? null : new Date(tmpMRunFrom);
		long tmpMRunTill = in.readLong(); 
		this.mRunTill = tmpMRunTill == -1 ? null : new Date(tmpMRunTill);
		this.mDealerUrl = in.readString();
		this.mDealerId = in.readString();
		this.mStoreUrl = in.readString();
		this.mStoreId = in.readString();
		this.mCatalogUrl = in.readString();
		this.mCatalogId = in.readString();
		this.mCatalog = in.readParcelable(Catalog.class.getClassLoader());
		this.mDealer = in.readParcelable(Dealer.class.getClassLoader());
		this.mStore = in.readParcelable(Store.class.getClassLoader());
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mErn);
		dest.writeString(this.mHeading);
		dest.writeString(this.mDescription);
		dest.writeInt(this.mCatalogPage);
		dest.writeParcelable(this.mPricing, flags);
		dest.writeParcelable(this.mQuantity, flags);
		dest.writeParcelable(this.mImages, flags);
		dest.writeParcelable(this.mLinks, flags);
		dest.writeLong(mRunFrom != null ? mRunFrom.getTime() : -1);
		dest.writeLong(mRunTill != null ? mRunTill.getTime() : -1);
		dest.writeString(this.mDealerUrl);
		dest.writeString(this.mDealerId);
		dest.writeString(this.mStoreUrl);
		dest.writeString(this.mStoreId);
		dest.writeString(this.mCatalogUrl);
		dest.writeString(this.mCatalogId);
		dest.writeParcelable(this.mCatalog, flags);
		dest.writeParcelable(this.mDealer, flags);
		dest.writeParcelable(this.mStore, flags);
	}
    
}
