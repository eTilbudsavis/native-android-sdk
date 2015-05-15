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

import android.graphics.pdf.PdfDocument.Page;
import android.os.Parcel;
import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.interfaces.IDealer;
import com.eTilbudsavis.etasdk.model.interfaces.IErn;
import com.eTilbudsavis.etasdk.model.interfaces.IJson;
import com.eTilbudsavis.etasdk.model.interfaces.IStore;
import com.eTilbudsavis.etasdk.utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.utils.ColorUtils;
import com.eTilbudsavis.etasdk.utils.Json;
import com.eTilbudsavis.etasdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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
public class Catalog implements IErn<Catalog>, IJson<JSONObject>, IDealer<Catalog>, IStore<Catalog>, Serializable, Parcelable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = Constants.getTag(Catalog.class);
	
	// From JSON blob
	private String mErn;
	private String mLabel;
	private Integer mBackground;
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
	private HashSet<String> mCatrgoryIds;
	private String mPdfUrl;
	
	// From separate queries
	private List<Images> mPages;
	private Dealer mDealer;
	private Store mStore;
	private HotspotMap mHotspots;

	public static Parcelable.Creator<Catalog> CREATOR = new Parcelable.Creator<Catalog>(){
		public Catalog createFromParcel(Parcel source) {
			return new Catalog(source);
		}
		public Catalog[] newArray(int size) {
			return new Catalog[size];
		}
	};

	public Catalog() {
		
	}
	
	/**
	 * Convert a {@link JSONArray} into a {@link List};.
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
			EtaLog.e(TAG, "", e);
		}
		return list;
	}

	/**
	 * A factory method for converting {@link JSONObject} into a POJO.
	 * @param catalog A {@link JSONObject} in the format of a valid API v2 catalog response
	 * @return A Catalog object
	 */
	public static Catalog fromJSON(JSONObject jCatalog) {
		Catalog catalog = new Catalog();
		if (jCatalog == null) {
			return catalog;
		}
		
		try {
			catalog.setId(Json.valueOf(jCatalog, JsonKey.ID));
			catalog.setErn(Json.valueOf(jCatalog, JsonKey.ERN));
			catalog.setLabel(Json.valueOf(jCatalog, JsonKey.LABEL));
			catalog.setBackground(Json.colorValueOf(jCatalog, JsonKey.BACKGROUND));
			Date runFrom = Utils.stringToDate(Json.valueOf(jCatalog, JsonKey.RUN_FROM));
			catalog.setRunFrom(runFrom);
			Date runTill = Utils.stringToDate(Json.valueOf(jCatalog, JsonKey.RUN_TILL));
			catalog.setRunTill(runTill);
			catalog.setPageCount(Json.valueOf(jCatalog, JsonKey.PAGE_COUNT, 0));
			catalog.setOfferCount(Json.valueOf(jCatalog, JsonKey.OFFER_COUNT, 0));
			catalog.setBranding(Branding.fromJSON(jCatalog.getJSONObject(JsonKey.BRANDING)));
			catalog.setDealerId(Json.valueOf(jCatalog, JsonKey.DEALER_ID));
			catalog.setDealerUrl(Json.valueOf(jCatalog, JsonKey.DEALER_URL));
			catalog.setStoreId(Json.valueOf(jCatalog, JsonKey.STORE_ID));
			catalog.setStoreUrl(Json.valueOf(jCatalog, JsonKey.STORE_URL));
			catalog.setDimension(Dimension.fromJSON(jCatalog.getJSONObject(JsonKey.DIMENSIONS)));
			catalog.setImages(Images.fromJSON(jCatalog.getJSONObject(JsonKey.IMAGES)));
			
			if (jCatalog.has(JsonKey.CATEGORY_IDS)) {
				JSONArray jCats = jCatalog.getJSONArray(JsonKey.CATEGORY_IDS);
				HashSet<String> cat = new HashSet<String>(jCats.length());
				for (int i = 0 ; i < jCats.length() ; i++) {
					cat.add(jCats.getString(i));
				}
				catalog.setCatrgoryIds(cat);
			}
			
			catalog.setPdfUrl(Json.valueOf(jCatalog, JsonKey.PDF_URL));

			if (jCatalog.has(JsonKey.SDK_DEALER)) {
				JSONObject jDealer = Json.getObject(jCatalog, JsonKey.SDK_DEALER, null);
				catalog.setDealer(Dealer.fromJSON(jDealer));
			}

			if (jCatalog.has(JsonKey.SDK_STORE)) {
				JSONObject jStore = Json.getObject(jCatalog, JsonKey.SDK_STORE, null);
				catalog.setStore(Store.fromJSON(jStore));
			}
			
			if (jCatalog.has(JsonKey.SDK_PAGES)) {
				JSONArray jPages = Json.getArray(jCatalog, JsonKey.SDK_PAGES);
				List<Images> pages = new ArrayList<Images>(jPages.length());
				for (int i = 0; i < jPages.length(); i++) {
					pages.add(Images.fromJSON(jPages.getJSONObject(i)));
				}
				catalog.setPages(pages);
			}
			
			// TODO Fix HotspotsMap so it can be JSON'ed
			
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
			
		return catalog;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.ID, Json.nullCheck(getId()));
			o.put(JsonKey.ERN, Json.nullCheck(getErn()));
			o.put(JsonKey.LABEL, Json.nullCheck(getLabel()));
			o.put(JsonKey.BACKGROUND, Json.nullCheck(ColorUtils.toString(getBackground())));
			o.put(JsonKey.RUN_FROM, Json.nullCheck(Utils.dateToString(getRunFrom())));
			o.put(JsonKey.RUN_TILL, Json.nullCheck(Utils.dateToString(getRunTill())));
			o.put(JsonKey.PAGE_COUNT, getPageCount());
			o.put(JsonKey.OFFER_COUNT, getOfferCount());
			o.put(JsonKey.BRANDING, Json.nullCheck(getBranding().toJSON()));
			o.put(JsonKey.DEALER_ID, Json.nullCheck(getDealerId()));
			o.put(JsonKey.DEALER_URL, Json.nullCheck(getDealerUrl()));
			o.put(JsonKey.STORE_ID, Json.nullCheck(getStoreId()));
			o.put(JsonKey.STORE_URL, Json.nullCheck(getStoreUrl()));
			o.put(JsonKey.DIMENSIONS, Json.nullCheck(getDimension().toJSON()));
			o.put(JsonKey.IMAGES, Json.nullCheck(getImages().toJSON()));
			o.put(JsonKey.CATEGORY_IDS, new JSONArray(getCatrgoryIds()));
			o.put(JsonKey.PDF_URL, Json.nullCheck(getPdfUrl()));

			if (mDealer!=null) {
				o.put(JsonKey.SDK_DEALER, Json.toJson(mDealer));
			}

			if (mStore!=null) {
				o.put(JsonKey.SDK_STORE, Json.toJson(mStore));
			}

			if (mPages!=null) {
				JSONArray jPages = new JSONArray();
				for (Images i : mPages) {
					jPages.put(i.toJSON());
				}
				o.put(JsonKey.SDK_PAGES, Json.nullCheck(jPages));
			}
			
			// TODO Fix HotspotsMap so it can be JSON'ed
			
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}

	public Catalog setId(String id) {
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
	
	public Catalog setErn(String ern) {
		if (ern==null || ( ern.startsWith("ern:") && ern.split(":").length==3 && ern.contains(getErnType()) )) {
			mErn = ern;
		}
		return this;
	}
	
	public String getErn() {
		return mErn;
	}

	public String getErnType() {
		return IErn.TYPE_CATALOG;
	}
	
	public String getLabel() {
		return mLabel;
	}

	public Catalog setLabel(String label) {
		mLabel = label;
		return this;
	}
	
	/**
	 * Get the background color for this catalog.<br>
	 * For displaying the catalog, in a reader fashion, please use the color found in {@link Pageflip}.
	 * @return A color
	 */
	public Integer getBackground() {
		return mBackground;
	}
	
	public Catalog setBackground(Integer background) {
		mBackground = ColorUtils.stripAlpha(background);
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
		if (mDealerId == null) {
			mDealer = null;
		} else if (mDealer != null && !mDealerId.equals(mDealer.getId()) ) {
			mDealer = null;
		}
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
		if (mStoreId == null) {
			mStore = null;
		} else if (mStore != null && !mStoreId.equals(mStore.getId()) ) {
			mStore = null;
		}
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
	 * {@link Catalog#setPages(Page) set} manually by the developer. </p>
	 * @return
	 */
	public List<Images> getPages() {
		return mPages;
	}
	
	/**
	 * Method for setting the {@link Page} associated with this catalog
	 * @param pages A pages object
	 */
	public void setPages(List<Images> pages) {
		mPages = pages;
	}
	
	/**
	 * Method for setting the {@link Store} associated with this catalog, and updates the {@link Catalog#getStoreId() store id} to match the new {@link Store} object.
	 * @param store A Store object
	 */
	public Catalog setStore(Store store) {
		mStore = store;
		mStoreId = (mStore==null ? null : mStore.getId());
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
	 * Method for setting the {@link Dealer} associated with this catalog, and updates the {@link Catalog#getDealerId() dealer id} to match the new {@link Dealer} object.
	 * @param store A Dealer object
	 */
	public Catalog setDealer(Dealer dealer) {
		this.mDealer = dealer;
		mDealerId = (mDealer==null ? null : mDealer.getId());
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

	/**
	 * Method for setting the {@link HotspotMap} associated with this catalog
	 * @param hotspots A {@link HotspotMap} object
	 */
	public Catalog setHotspots(HotspotMap hotspots) {
		mHotspots = hotspots;
		return this;
	}

	/**
	 * Get the {@link HotspotMap} associated with this catalog.
	 * <p>Hotspots isn't bundled in the catalog object by default. But should be
	 * downloaded separately via the store {@link Endpoint#catalogHotspots(String) endpoint},
	 * and  {@link Catalog#setHotspots(HotspotMap) set} manually by the developer. </p>
	 * @return A {@link HotspotMap} object, or {@code null}
	 */
	public HotspotMap getHotspots() {
		return mHotspots;
	}
	
	/**
	 * Get the category id's for this catalog
	 * @return A list of categories, or null
	 */
	public HashSet<String> getCatrgoryIds() {
		return mCatrgoryIds;
	}
	
	/**
	 * Set the list of categories for this catalog.
	 * @param catrgoryIds A list of categories
	 */
	public void setCatrgoryIds(HashSet<String> catrgoryIds) {
		mCatrgoryIds = catrgoryIds;
	}
	
	/**
	 * Get the URL where the PDF can be downloaded.
	 * @return A url, or null
	 */
	public String getPdfUrl() {
		return mPdfUrl;
	}
	
	/**
	 * Set the URL where the PDF can be downloaded.
	 * @param pdfUrl A url
	 */
	public void setPdfUrl(String pdfUrl) {
		mPdfUrl = pdfUrl;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mBackground == null) ? 0 : mBackground.hashCode());
		result = prime * result
				+ ((mBranding == null) ? 0 : mBranding.hashCode());
		result = prime * result
				+ ((mCatrgoryIds == null) ? 0 : mCatrgoryIds.hashCode());
		result = prime * result + ((mDealer == null) ? 0 : mDealer.hashCode());
		result = prime * result
				+ ((mDealerId == null) ? 0 : mDealerId.hashCode());
		result = prime * result
				+ ((mDealerUrl == null) ? 0 : mDealerUrl.hashCode());
		result = prime * result
				+ ((mDimension == null) ? 0 : mDimension.hashCode());
		result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
		result = prime * result
				+ ((mHotspots == null) ? 0 : mHotspots.hashCode());
		result = prime * result + ((mImages == null) ? 0 : mImages.hashCode());
		result = prime * result + ((mLabel == null) ? 0 : mLabel.hashCode());
		result = prime * result + mOfferCount;
		result = prime * result + mPageCount;
		result = prime * result + ((mPages == null) ? 0 : mPages.hashCode());
		result = prime * result + ((mPdfUrl == null) ? 0 : mPdfUrl.hashCode());
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
		Catalog other = (Catalog) obj;
		if (mBackground == null) {
			if (other.mBackground != null) {
				return false;
			}
		} else if (!mBackground.equals(other.mBackground))
			return false;
		if (mBranding == null) {
			if (other.mBranding != null)
				return false;
		} else if (!mBranding.equals(other.mBranding))
			return false;
		if (mCatrgoryIds == null) {
			if (other.mCatrgoryIds != null)
				return false;
		} else if (!mCatrgoryIds.equals(other.mCatrgoryIds))
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
		if (mDimension == null) {
			if (other.mDimension != null)
				return false;
		} else if (!mDimension.equals(other.mDimension))
			return false;
		if (mErn == null) {
			if (other.mErn != null)
				return false;
		} else if (!mErn.equals(other.mErn))
			return false;
		if (mHotspots == null) {
			if (other.mHotspots != null)
				return false;
		} else if (!mHotspots.equals(other.mHotspots))
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
		if (mPageCount != other.mPageCount)
			return false;
		if (mPages == null) {
			if (other.mPages != null)
				return false;
		} else if (!mPages.equals(other.mPages))
			return false;
		if (mPdfUrl == null) {
			if (other.mPdfUrl != null)
				return false;
		} else if (!mPdfUrl.equals(other.mPdfUrl))
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

	private Catalog(Parcel in) {
		this.mErn = in.readString();
		this.mLabel = in.readString();
		this.mBackground = (Integer)in.readValue(Integer.class.getClassLoader());
		long tmpMRunFrom = in.readLong(); 
		this.mRunFrom = tmpMRunFrom == -1 ? null : new Date(tmpMRunFrom);
		long tmpMRunTill = in.readLong(); 
		this.mRunTill = tmpMRunTill == -1 ? null : new Date(tmpMRunTill);
		this.mPageCount = in.readInt();
		this.mOfferCount = in.readInt();
		this.mBranding = in.readParcelable(Branding.class.getClassLoader());
		this.mDealerId = in.readString();
		this.mDealerUrl = in.readString();
		this.mStoreId = in.readString();
		this.mStoreUrl = in.readString();
		this.mDimension = in.readParcelable(Dimension.class.getClassLoader());
		this.mImages = in.readParcelable(Images.class.getClassLoader());
		this.mCatrgoryIds = (HashSet<String>) in.readSerializable();
		this.mPdfUrl = in.readString();
		this.mPages = new ArrayList<Images>();
		in.readTypedList(mPages, Images.CREATOR);
		this.mDealer = in.readParcelable(Dealer.class.getClassLoader());
		this.mStore = in.readParcelable(Store.class.getClassLoader());
		this.mHotspots = in.readParcelable(HotspotMap.class.getClassLoader());
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mErn);
		dest.writeString(this.mLabel);
		dest.writeValue(this.mBackground);
		dest.writeLong(mRunFrom != null ? mRunFrom.getTime() : -1);
		dest.writeLong(mRunTill != null ? mRunTill.getTime() : -1);
		dest.writeInt(this.mPageCount);
		dest.writeInt(this.mOfferCount);
		dest.writeParcelable(this.mBranding, flags);
		dest.writeString(this.mDealerId);
		dest.writeString(this.mDealerUrl);
		dest.writeString(this.mStoreId);
		dest.writeString(this.mStoreUrl);
		dest.writeParcelable(this.mDimension, flags);
		dest.writeParcelable(this.mImages, flags);
		dest.writeSerializable(this.mCatrgoryIds);
		dest.writeString(this.mPdfUrl);
		dest.writeTypedList(mPages);
		dest.writeParcelable(this.mDealer, flags);
		dest.writeParcelable(this.mStore, flags);
		dest.writeParcelable(this.mHotspots, flags);
	}
	
}
