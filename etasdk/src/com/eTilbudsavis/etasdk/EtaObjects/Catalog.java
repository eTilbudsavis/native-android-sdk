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
package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.PageflipWebview;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
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
	
	// From separate queries
	private Pages mPages;
	private Dealer mDealer;
	private Store mStore;
	private int mOfferOnPage = 1;

	public Catalog() {
	}

	/**
	 * Convert a {@link JSONArray} into a {@link List}&lt;T&gt;.
	 * @param catalogs A {@link JSONArray} in the format of a valid API v2 catalog response
	 * @return A {@link List} of POJO
	 */
	public static List<Catalog> fromJSON(JSONArray catalogs) {
		List<Catalog> list = new ArrayList<Catalog>();
		try {
			for (int i = 0 ; i < catalogs.length() ; i++ ) {
				list.add(Catalog.fromJSON((JSONObject)catalogs.get(i)));
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}

	/**
	 * A factory method for converting {@link JSONObject} into a POJO.
	 * @param catalog A {@link JSONObject} in the format of a valid API v2 catalog response
	 * @return A Catalog object
	 */
	public static Catalog fromJSON(JSONObject catalog) {
		return fromJSON(new Catalog(), catalog);
	}

	/**
	 * A factory method for converting {@link JSONObject} into POJO.
	 * <p>This method exposes a way, of updating/setting an objects properties</p>
	 * @param catalog An object to set/update
	 * @param jCatalog A {@link JSONObject} in the format of a valid API v2 catalog response.
	 * 		But a special case exists where, {@link PageflipWebview} only exposes
	 * 		the keys "{@link ServerKey#ID id}", and "{@link ServerKey#PAGE page}", this is
	 * 		valid too, but not a complete/workable catalog object.
	 * @return A {@link List} of POJO
	 */
	public static Catalog fromJSON(Catalog catalog, JSONObject jCatalog) {
		if (catalog == null) catalog = new Catalog();
		if (jCatalog == null) return catalog;
		
		if (jCatalog.has(ServerKey.STORE_ID) && jCatalog.has(ServerKey.OFFER_COUNT)) {
			// if we have a full catalog
			try {
				catalog.setId(Json.valueOf(jCatalog, ServerKey.ID));
				catalog.setErn(Json.valueOf(jCatalog, ServerKey.ERN));
				catalog.setLabel(Json.valueOf(jCatalog, ServerKey.LABEL));
				catalog.setBackground(Json.valueOf(jCatalog, ServerKey.BACKGROUND));
				Date runFrom = Utils.parseDate(Json.valueOf(jCatalog, ServerKey.RUN_FROM));
				catalog.setRunFrom(runFrom);
				Date runTill = Utils.parseDate(Json.valueOf(jCatalog, ServerKey.RUN_TILL));
				catalog.setRunTill(runTill);
				catalog.setPageCount(Json.valueOf(jCatalog, ServerKey.PAGE_COUNT, 0));
				catalog.setOfferCount(Json.valueOf(jCatalog, ServerKey.OFFER_COUNT, 0));
				catalog.setBranding(Branding.fromJSON(jCatalog.getJSONObject(ServerKey.BRANDING)));
				catalog.setDealerId(Json.valueOf(jCatalog, ServerKey.DEALER_ID));
				catalog.setDealerUrl(Json.valueOf(jCatalog, ServerKey.DEALER_URL));
				catalog.setStoreId(Json.valueOf(jCatalog, ServerKey.STORE_ID));
				catalog.setStoreUrl(Json.valueOf(jCatalog, ServerKey.STORE_URL));
				catalog.setDimension(Dimension.fromJSON(jCatalog.getJSONObject(ServerKey.DIMENSIONS)));
				catalog.setImages(Images.fromJSON(jCatalog.getJSONObject(ServerKey.IMAGES)));
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
			
		} else if (jCatalog.has(ServerKey.ID) && jCatalog.has(ServerKey.PAGE)) {
			// If it is a partial catalog
			catalog.setId(Json.valueOf(jCatalog, ServerKey.ID));
			catalog.setOfferOnPage(Json.valueOf(jCatalog, ServerKey.PAGE, 1));
			
		}
		return catalog;
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
	
	/**
	 * TODO what is label?
	 * @return
	 */
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

	/**
	 * Set the {@link Date} this catalog is be valid from.
	 * 
	 * <p>The {@link Date#getTime() time} of the date will be floored to the
	 * nearest second (i.e. milliseconds will be removed) as the server responds 
	 * in seconds and comparison of {@link Date}s will other wise be 
	 * unpredictable/impossible.</p>
	 * 
	 * @param date A {@link Date}
	 * @return This object
	 */
	public Catalog setRunFrom(Date time) {
		mRunFrom = Utils.roundTime(time);
		return this;
	}

	/**
	 * Returns the {@link Date} this catalog is be valid from.
	 * @return A {@link Date}, or <code>null</code>
	 */
	public Date getRunFrom() {
		return mRunFrom;
	}

	/**
	 * Set the {@link Date} this catalog is be valid till.
	 * 
	 * <p>The {@link Date#getTime() time} of the date will be floored to the
	 * nearest second (i.e. milliseconds will be removed) as the server responds 
	 * in seconds and comparison of {@link Date}s will other wise be 
	 * unpredictable/impossible.</p>
	 * 
	 * @param date A {@link Date}
	 * @return This object
	 */
	public Catalog setRunTill(Date time) {
		mRunTill = Utils.roundTime(time);
		return this;
	}

	/**
	 * Returns the {@link Date} this catalog is be valid till.
	 * @return A {@link Date}, or <code>null</code>
	 */
	public Date getRunTill() {
		return mRunTill;
	}
	
	/**
	 * Set the number of pages this catalog has.
	 * @param pageCount Number of pages in this catalog
	 * @return This object
	 */
	public Catalog setPageCount(Integer pageCount) {
		mPageCount = pageCount;
		return this;
	}
	
	/**
	 * Get the number of pages in this catalog
	 * @return The number of pages
	 */
	public int getPageCount() {
		return mPageCount;
	}
	
	/**
	 * Set the number of {@link Offer} in this catalog.
	 * @param offerCount The number of offers
	 * @return This object
	 */
	public Catalog setOfferCount(Integer offerCount) {
		this.mOfferCount = offerCount;
		return this;
	}
	
	/**
	 * Get the number of {@link Offer}s in this catalog.
	 * @return The number of catalogs
	 */
	public int getOfferCount() {
		return mOfferCount;
	}
	
	/**
	 * Set the {@link Branding} to apply to this catalog.
	 * @param branding A branding object
	 * @return This object
	 */
	public Catalog setBranding(Branding branding) {
		this.mBranding = branding;
		return this;
	}
	
	/**
	 * Get the branding, that is applied to this catalog.
	 * @return A {@link Branding} object, or {@code null}
	 */
	public Branding getBranding() {
		return mBranding;
	}
	
	/**
	 * Set the dealer id associated with this catalog
	 * @param dealerId A dealer id to set
	 * @return This object
	 */
	public Catalog setDealerId(String dealerId) {
		this.mDealerId = dealerId;
		return this;
	}
	
	/**
	 * Get the dealer id associated with this object
	 * @return A dealer id, or {@code null}
	 */
	public String getDealerId() {
		return mDealerId;
	}
	
	/**
	 * Get the URL to the {@link Dealer} resource for this {@link Catalog}
	 * @return A {@link Dealer} resource URL, or {@code null}
	 */
	public String getDealerUrl() {
		return mDealerUrl;
	}
	
	/**
	 * Set the URL to the {@link Dealer} resource for this {@link Catalog}
	 * @param url An URL pointing to a {@link Dealer} resource for this catalog
	 * @return This object
	 */
	public Catalog setDealerUrl(String url) {
		mDealerUrl = url;
		return this;
	}

	/**
	 * Set the {@link Store#getId() store.id} associated with this catalog
	 * @param storeId A store id to set
	 * @return This object
	 */
	public Catalog setStoreId(String storeId) {
		mStoreId = storeId;
		return this;
	}

	/**
	 * Get the {@link Store#getId() store.id} id associated with this object
	 * @return A store id, or {@code null}
	 */
	public String getStoreId() {
		return mStoreId;
	}

	/**
	 * Get the URL to the {@link Store} resource for this {@link Catalog}
	 * @return A {@link Store} resource URL, or {@code null}
	 */
	public String getStoreUrl() {
		return mStoreUrl;
	}

	/**
	 * Set the URL to the {@link Store} resource for this {@link Catalog}
	 * @param url An URL pointing to a {@link Store} resource for this catalog
	 * @return This object
	 */
	public Catalog setStoreUrl(String url) {
		mStoreUrl = url;
		return this;
	}
	
	/**
	 * Get the dimensions for this catalog.
	 * @return A dimensions object, or {@code null}
	 */
	public Dimension getDimension() {
		return mDimension;
	}
	
	/**
	 * Set the dimensions for this catalog
	 * @param dimension A dimension
	 * @return This object
	 */
	public Catalog setDimension(Dimension dimension) {
		mDimension = dimension;
		return this;
	}
	
	/**
	 * Set the images associated with this object
	 * @param images An image object
	 * @return This object
	 */
	public Catalog setImages(Images images) {
		mImages = images;
		return this;
	}
	
	/**
	 * Get the images associated with this catalog
	 * @return An images object, or {@code null}
	 */
	public Images getImages() {
		return mImages;
	}
	
	/**
	 * Get the pages in this catalog.
	 * <p>Pages isn't bundled in the catalog object by default. But should be
	 * downloaded separately via the pages endpoint, and
	 * {@link Catalog#setPages(Pages) set} manually by the developer. </p>
	 * @return
	 */
	public Pages getPages() {
		return mPages;
	}
	
	/**
	 * Method for setting the {@link Pages} associated with this catalog
	 * @param pages A pages object
	 */
	public void setPages(Pages pages) {
		mPages = pages;
	}
	
	/**
	 * Set the page number of a specific {@link Offer}.
	 * <p>This is a use case for e.g. {@link PageflipWebview}</p>
	 * @param offerOnPage A page number of an offer
	 * @return This object
	 */
	public Catalog setOfferOnPage(Integer offerOnPage) {
		this.mOfferOnPage = offerOnPage;
		return this;
	}

	/**
	 * Get the page number of a specific {@link Offer}.
	 * <p>This is a use case for e.g. {@link PageflipWebview}</p>
	 * @return A page number of a specific {@link Offer} to display
	 */
	public int getOfferOnPage() {
		return mOfferOnPage;
	}

	/**
	 * Method for setting the {@link Store} associated with this catalog
	 * @param store A Store object
	 */
	public Catalog setStore(Store store) {
		mStore = store;
		return this;
	}

	/**
	 * Get the {@link Store} associated with this catalog.
	 * <p>Store isn't bundled in the catalog object by default. But should be
	 * downloaded separately via the store {@link Endpoint#storeId(String) endpoint},
	 * and  {@link Catalog#setStore(Store) set} manually by the developer. </p>
	 * @return A Store, or {@code null}
	 */
	public Store getStore() {
		return mStore;
	}

	/**
	 * Method for setting the {@link Dealer} associated with this catalog
	 * @param store A Dealer object
	 */
	public Catalog setDealer(Dealer dealer) {
		this.mDealer = dealer;
		return this;
	}

	/**
	 * Get the {@link Dealer} associated with this catalog.
	 * <p>Dealer isn't bundled in the catalog object by default. But should be
	 * downloaded separately via the dealer {@link Endpoint#dealerId(String) endpoint},
	 * and  {@link Catalog#setDealer(Dealer) set} manually by the developer. </p>
	 * @return A Dealer, or {@code null}
	 */
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
