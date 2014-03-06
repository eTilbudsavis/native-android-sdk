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

/**
 * <p>This class is a representation of a catalog as the API v2 exposes it</p>
 * 
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/catalogs.html">Catalog Reference</a>
 * documentation, on the engineering blog.
 * </p>
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class Catalog extends EtaErnObject<Catalog> implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Catalog";
	
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
	
	public static ArrayList<Catalog> fromJSON(JSONArray catalogs) {
		ArrayList<Catalog> list = new ArrayList<Catalog>();
		try {
			for (int i = 0 ; i < catalogs.length() ; i++ ) {
				list.add(Catalog.fromJSON((JSONObject)catalogs.get(i)));
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}
	
	public static Catalog fromJSON(JSONObject catalog) {
		return fromJSON(new Catalog(), catalog);
	}

	public static Catalog fromJSON(Catalog c, JSONObject catalog) {
		if(c == null)
			c = new Catalog();

		if (catalog == null)
			return c;

		if (catalog.has(ServerKey.STORE_ID) && catalog.has(ServerKey.OFFER_COUNT)) {
			// if we have a full catalog
			try {
				c.setId(Json.valueOf(catalog, ServerKey.ID));
				c.setErn(Json.valueOf(catalog, ServerKey.ERN));
				c.setLabel(Json.valueOf(catalog, ServerKey.LABEL));
				c.setBackground(Json.valueOf(catalog, ServerKey.BACKGROUND));
				Date runFrom = Utils.parseDate(Json.valueOf(catalog, ServerKey.RUN_FROM));
				c.setRunFrom(runFrom);
				Date runTill = Utils.parseDate(Json.valueOf(catalog, ServerKey.RUN_TILL));
				c.setRunTill(runTill);
				c.setPageCount(Json.valueOf(catalog, ServerKey.PAGE_COUNT, 0));
				c.setOfferCount(Json.valueOf(catalog, ServerKey.OFFER_COUNT, 0));
				c.setBranding(Branding.fromJSON(catalog.getJSONObject(ServerKey.BRANDING)));
				c.setDealerId(Json.valueOf(catalog, ServerKey.DEALER_ID));
				c.setDealerUrl(Json.valueOf(catalog, ServerKey.DEALER_URL));
				c.setStoreId(Json.valueOf(catalog, ServerKey.STORE_ID));
				c.setStoreUrl(Json.valueOf(catalog, ServerKey.STORE_URL));
				c.setDimension(Dimension.fromJSON(catalog.getJSONObject(ServerKey.DIMENSIONS)));
				c.setImages(Images.fromJSON(catalog.getJSONObject(ServerKey.IMAGES)));
				c.setPages(Pages.fromJSON(catalog.getJSONObject(ServerKey.PAGES)));
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
		} else if (catalog.has(ServerKey.ID) && catalog.has(ServerKey.PAGE)) {
			// If it is a partial catalog
			c.setId(Json.valueOf(catalog, ServerKey.ID));
			c.setOfferOnPage(Json.valueOf(catalog, ServerKey.PAGE, 1));
			
		}
		return c;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = super.toJSON();
		try {
			o.put(ServerKey.LABEL, Json.nullCheck(getLabel()));
			o.put(ServerKey.BACKGROUND, Json.nullCheck(getBackground()));
			o.put(ServerKey.RUN_FROM, Json.nullCheck(Utils.parseDate(getRunFrom())));
			o.put(ServerKey.RUN_TILL, Json.nullCheck(Utils.parseDate(getRunTill())));
			o.put(ServerKey.PAGE_COUNT, getPageCount());
			o.put(ServerKey.OFFER_COUNT, getOfferCount());
			o.put(ServerKey.BRANDING, Json.nullCheck(getBranding().toJSON()));
			o.put(ServerKey.DEALER_ID, Json.nullCheck(getDealerId()));
			o.put(ServerKey.DEALER_URL, Json.nullCheck(getDealerUrl()));
			o.put(ServerKey.STORE_ID, Json.nullCheck(getStoreId()));
			o.put(ServerKey.STORE_URL, Json.nullCheck(getStoreUrl()));
			o.put(ServerKey.DIMENSIONS, Json.nullCheck(getDimension().toJSON()));
			o.put(ServerKey.IMAGES, Json.nullCheck(getImages().toJSON()));
			o.put(ServerKey.PAGES, Json.nullCheck(getPages().toJSON()));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	@Override
	public String getErnPrefix() {
		return ERN_CATALOG;
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
		mRunFrom = Utils.roundTime(time);
		return this;
	}

	public Date getRunFrom() {
		return mRunFrom;
	}

	public Catalog setRunTill(Date time) {
		mRunTill = Utils.roundTime(time);
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mBackground == null) ? 0 : mBackground.hashCode());
		result = prime * result
				+ ((mBranding == null) ? 0 : mBranding.hashCode());
		result = prime * result
				+ ((mDealerId == null) ? 0 : mDealerId.hashCode());
		result = prime * result
				+ ((mDealerUrl == null) ? 0 : mDealerUrl.hashCode());
		result = prime * result
				+ ((mDimension == null) ? 0 : mDimension.hashCode());
		result = prime * result + ((mImages == null) ? 0 : mImages.hashCode());
		result = prime * result + ((mLabel == null) ? 0 : mLabel.hashCode());
		result = prime * result + mOfferCount;
		result = prime * result + mOfferOnPage;
		result = prime * result + mPageCount;
		result = prime * result + ((mPages == null) ? 0 : mPages.hashCode());
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
		Catalog other = (Catalog) obj;
		if (mBackground == null) {
			if (other.mBackground != null)
				return false;
		} else if (!mBackground.equals(other.mBackground))
			return false;
		if (mBranding == null) {
			if (other.mBranding != null)
				return false;
		} else if (!mBranding.equals(other.mBranding))
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
		if (mDimension == null) {
			if (other.mDimension != null)
				return false;
		} else if (!mDimension.equals(other.mDimension))
			return false;
		if (mImages == null) {
			if (other.mImages != null)
				return false;
		} else if (!mImages.equals(other.mImages))
			return false;
		if (mLabel == null) {
			if (other.mLabel != null)
				return false;
		} else if (!mLabel.equals(other.mLabel))
			return false;
		if (mOfferCount != other.mOfferCount)
			return false;
		if (mOfferOnPage != other.mOfferOnPage)
			return false;
		if (mPageCount != other.mPageCount)
			return false;
		if (mPages == null) {
			if (other.mPages != null)
				return false;
		} else if (!mPages.equals(other.mPages))
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
