package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Utils;

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
	private Date mRunFrom = null;
	private Date mRunTill = null;
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

	public Offer() { }
	
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
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Offer offer) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.ID, Json.nullCheck(offer.getId()));
			o.put(ServerKey.ERN, Json.nullCheck(offer.getErn()));
			o.put(ServerKey.HEADING, Json.nullCheck(offer.getHeading()));
			o.put(ServerKey.DESCRIPTION, Json.nullCheck(offer.getDescription()));
			o.put(ServerKey.CATALOG_PAGE, offer.getCatalogPage());
			o.put(ServerKey.PRICING, Json.toJson(offer.getPricing()));
			o.put(ServerKey.QUANTITY, Json.toJson(offer.getQuantity()));
			o.put(ServerKey.IMAGES, Json.toJson(offer.getImages()));
			o.put(ServerKey.LINKS, Json.toJson(offer.getLinks()));
			o.put(ServerKey.RUN_FROM, Json.nullCheck(Utils.parseDate(offer.getRunFrom())));
			o.put(ServerKey.RUN_TILL, Json.nullCheck(Utils.parseDate(offer.getRunTill())));
			o.put(ServerKey.DEALER_URL, Json.nullCheck(offer.getDealerUrl()));
			o.put(ServerKey.DEALER_ID, Json.nullCheck(offer.getDealerId()));
			o.put(ServerKey.STORE_URL, Json.nullCheck(offer.getStoreUrl()));
			o.put(ServerKey.STORE_ID, Json.nullCheck(offer.getStoreId()));
			o.put(ServerKey.CATALOG_URL, Json.nullCheck(offer.getCatalogUrl()));
			o.put(ServerKey.CATALOG_ID, Json.nullCheck(offer.getCatalogId()));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	@Override
	public String getErnPrefix() {
		return ERN_OFFER;
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

	public Date getRunFrom() {
		return mRunFrom;
	}

	public Offer setRunFrom(Date time) {
		time.setTime(1000 * (time.getTime()/ 1000));
		mRunFrom = time;
		return this;
	}

	public Date getRunTill() {
		return mRunTill;
	}

	public Offer setRunTill(Date time) {
		time.setTime(1000 * (time.getTime()/ 1000));
		mRunTill = time;
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
