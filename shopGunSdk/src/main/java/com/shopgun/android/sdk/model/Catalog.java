/*******************************************************************************
 * Copyright 2015 ShopGun
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
 ******************************************************************************/

package com.shopgun.android.sdk.model;

import android.graphics.pdf.PdfDocument.Page;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Keep;

import com.shopgun.android.materialcolorcreator.MaterialColor;
import com.shopgun.android.materialcolorcreator.MaterialColorImpl;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IErn;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.model.interfaces.IStore;
import com.shopgun.android.sdk.utils.Api.Endpoint;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.utils.DateUtils;
import com.shopgun.android.utils.ParcelableUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * <p>This class is a representation of a catalog as the API v2 exposes it</p>
 *
 * <p>More documentation available on via our
 * <a href="https://developers.shopgun.com/docs/catalog">Catalog Reference</a>
 * documentation, on the engineering blog.
 * </p>
 */
@Keep
public class Catalog implements IErn<Catalog>, IJson<JSONObject>, IDealer<Catalog>, IStore<Catalog>, Parcelable {

    public static final String TAG = Constants.getTag(Catalog.class);

    public enum PublicationType {
        PAGED, INCITO;

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }

    // From JSON blob
    private String mErn;
    private String mLabel;
    private MaterialColor mBackground;
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
    private Set<String> mCategoryIds;
    private String mPdfUrl;
    private boolean mIsAvailableInAllStores;
    // From separate queries
    private List<Images> mPages;
    private Dealer mDealer;
    private Store mStore;
    private HotspotMap mHotspots;

    /// Type of catalog.
    /// If it contains `paged`, it can only be viewed with PagedPublicationFragment
    /// If it contains `incito`, the `incitoId` will always have a value, and it can be viewed as an Incito
    /// If it ONLY contains `incito`, this cannot be viewed with PagedPublicationFragment (see `isOnlyIncito`)
    private EnumSet<PublicationType> mPublicationTypes;
    private String mIncitoId;

    public Catalog() {

    }

    public Catalog(Catalog catalog) {

        // Ensure we don't reference objects
        Catalog tmp = ParcelableUtils.copyParcelable(catalog, Catalog.CREATOR);

        this.mErn = tmp.mErn;
        this.mLabel = tmp.mLabel;
        this.mBackground = tmp.mBackground;
        this.mRunFrom = tmp.mRunFrom;
        this.mRunTill = tmp.mRunTill;
        this.mPageCount = tmp.mPageCount;
        this.mOfferCount = tmp.mOfferCount;
        this.mBranding = tmp.mBranding;
        this.mDealerId = tmp.mDealerId;
        this.mDealerUrl = tmp.mDealerUrl;
        this.mStoreId = tmp.mStoreId;
        this.mStoreUrl = tmp.mStoreUrl;
        this.mDimension = tmp.mDimension;
        this.mImages = tmp.mImages;
        this.mCategoryIds = tmp.mCategoryIds;
        this.mPdfUrl = tmp.mPdfUrl;
        this.mIsAvailableInAllStores = tmp.mIsAvailableInAllStores;
        this.mIncitoId = tmp.mIncitoId;
        this.mPublicationTypes = tmp.mPublicationTypes;

        this.mPages = tmp.mPages;
        this.mDealer = tmp.mDealer;
        this.mStore = tmp.mStore;
        this.mHotspots = tmp.mHotspots;
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Catalog}
     * @return A {@link List} of POJO
     */
    public static List<Catalog> fromJSON(JSONArray array) {
        List<Catalog> list = new ArrayList<Catalog>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Catalog.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Catalog}
     * @return A {@link Catalog}, or {@code null} if {@code object} is {@code null}
     */
    public static Catalog fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }
        SgnJson o = new SgnJson(object);
        o.isErnTypeOrThrow(IErn.TYPE_CATALOG, Catalog.class);
        Catalog c = new Catalog()
                .setId(o.getId())
                .setErn(o.getErn())
                .setLabel(o.getLabel())
                .setBackground(o.getBackground())
                .setRunFrom(o.getRunFrom())
                .setRunTill(o.getRunTill())
                .setPageCount(o.getPageCount())
                .setOfferCount(o.getOfferCount())
                .setBranding(o.getBranding())
                .setDealerId(o.getDealerId())
                .setDealerUrl(o.getDealerUrl())
                .setStoreId(o.getStoreId())
                .setStoreUrl(o.getStoreUrl())
                .setDimension(o.getDimensions())
                .setImages(o.getImages())
                .setCategoryIds(o.getCategoryIds())
                .setPdfUrl(o.getPdfUrl())
                .setAvailability(o.getAvailability())
                .setIncitoId(o.getIncitoId())
                .setPublicationTypes(o.getPublicationTypes())
                .setPages(o.getPages());

        // Internal SDK variables, avoid getter and setter to circumvent safety features... o_O
        c.mDealer = o.getDealer();
        c.mStore = o.getStore();

        o.getStats().ignoreRejectedKeys(SgnJson.DEALER, SgnJson.STORE).log(TAG);

        return c;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setId(getId())
                .setErn(getErn())
                .setLabel(getLabel())
                .setBackground(getBackgroundMaterialColor())
                .setRunFrom(getRunFrom())
                .setRunTill(getRunTill())
                .setPagecount(getPageCount())
                .setOfferCount(getOfferCount())
                .setBranding(getBranding())
                .setDealerId(getDealerId())
                .setDealerUrl(getDealerUrl())
                .setStoreId(getStoreId())
                .setStoreUrl(getStoreUrl())
                .setDimensions(getDimension())
                .setImages(getImages())
                .setCategoryIds(getCategoryIds())
                .setPdfUrl(getPdfUrl())
                .setAvailability(getIsAvailableInAllStores())
                .setIncitoId(getIncitoId())
                .setPublicationTypes(getPublicationTypes())
                // Internal SDK variables
                .putDealer(getDealer())
                .putStore(getStore())
                .putPages(getPages())
                .toJSON();
    }

    public String getId() {
        if (mErn == null) {
            return null;
        }
        String[] parts = mErn.split(":");
        return parts[parts.length - 1];
    }

    public Catalog setId(String id) {
        setErn((id == null) ? null : String.format("ern:%s:%s", getErnType(), id));
        return this;
    }

    public String getErn() {
        return mErn;
    }

    public Catalog setErn(String ern) {
        if (ern == null || (ern.startsWith("ern:") && ern.split(":").length == 3 && ern.contains(getErnType()))) {
            mErn = ern;
        }
        return this;
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
    public int getBackground() {
        return getBackgroundMaterialColor().getValue();
    }

    /**
     * Get the background color for this catalog.<br>
     * For displaying the catalog, in a reader fashion, please use the color found in {@link Pageflip}.
     * @return A color
     */
    public MaterialColor getBackgroundMaterialColor() {
        if (mBackground == null) {
            mBackground = new MaterialColorImpl();
        }
        return mBackground;
    }

    public Catalog setBackground(int background) {
        setBackground(new MaterialColorImpl(background));
        return this;
    }

    public Catalog setBackground(MaterialColor background) {
        mBackground = background;
        return this;
    }

    /**
     * Returns the {@link Date} this catalog is be valid from.
     * @return A {@link Date}, or {@code null}
     */
    public Date getRunFrom() {
        return mRunFrom;
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
    public Catalog setRunFrom(Date date) {
        mRunFrom = DateUtils.roundTime(date);
        return this;
    }

    /**
     * Returns the {@link Date} this catalog is be valid till.
     * @return A {@link Date}, or {@code null}
     */
    public Date getRunTill() {
        return mRunTill;
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
    public Catalog setRunTill(Date date) {
        mRunTill = DateUtils.roundTime(date);
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
     * Set the number of pages this catalog has.
     * @param pageCount Number of pages in this catalog
     * @return This object
     */
    public Catalog setPageCount(int pageCount) {
        mPageCount = pageCount;
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
     * Set the number of {@link Offer} in this catalog.
     * @param offerCount The number of offers
     * @return This object
     */
    public Catalog setOfferCount(int offerCount) {
        this.mOfferCount = offerCount;
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
     * Set the {@link Branding} to apply to this catalog.
     * @param branding A branding object
     * @return This object
     */
    public Catalog setBranding(Branding branding) {
        this.mBranding = branding;
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
     * Set the dealer id associated with this catalog
     * @param dealerId A dealer id to set
     * @return This object
     */
    public Catalog setDealerId(String dealerId) {
        this.mDealerId = dealerId;
        if (mDealerId == null) {
            mDealer = null;
        } else if (mDealer != null && !mDealerId.equals(mDealer.getId())) {
            mDealer = null;
        }
        return this;
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
     * Get the {@link Store#getId() store.id} id associated with this object
     * @return A store id, or {@code null}
     */
    public String getStoreId() {
        return mStoreId;
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
        } else if (mStore != null && !mStoreId.equals(mStore.getId())) {
            mStore = null;
        }
        return this;
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
     * Get the images associated with this catalog
     * @return An images object, or {@code null}
     */
    public Images getImages() {
        return mImages;
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
     * Get the pages in this catalog.
     * <p>Pages isn't bundled in the catalog object by default. But should be
     * downloaded separately via the pages endpoint, and
     * {@link Catalog#setPages(List) set}  manually by the developer. </p>
     * @return A list of {@link Images}
     */
    public List<Images> getPages() {
        return mPages;
    }

    /**
     * Method for setting the {@link Page} associated with this catalog
     * @param pages A pages object
     * @return This object
     */
    public Catalog setPages(List<Images> pages) {
        mPages = pages;
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
     * Method for setting the {@link Store} associated with this catalog, and updates the {@link Catalog#getStoreId() store id} to match the new {@link Store} object.
     * @param store A Store object
     */
    public Catalog setStore(Store store) {
        mStore = store;
        mStoreId = (mStore == null ? null : mStore.getId());
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
     * Method for setting the {@link Dealer} associated with this catalog, and updates the {@link Catalog#getDealerId() dealer id} to match the new {@link Dealer} object.
     * @param dealer A Dealer object
     */
    public Catalog setDealer(Dealer dealer) {
        this.mDealer = dealer;
        mDealerId = (mDealer == null ? null : mDealer.getId());
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
     * Method for setting the {@link HotspotMap} associated with this catalog
     * @param hotspots A {@link HotspotMap} object
     * @return this object
     */
    public Catalog setHotspots(HotspotMap hotspots) {
        mHotspots = hotspots;
        return this;
    }

    /**
     * Get the category id's for this catalog
     * @return A list of categories, or null
     */
    public Set<String> getCategoryIds() {
        return mCategoryIds;
    }

    /**
     * Set the list of categories for this catalog.
     * @param categoryIds A list of categories
     * @return This object
     */
    public Catalog setCategoryIds(Set<String> categoryIds) {
        mCategoryIds = categoryIds;
        return this;
    }

    /** @deprecated typo in method name */
    @Deprecated
    public Set<String> getCatrgoryIds() {
        return getCategoryIds();
    }

    /** @deprecated typo in method name */
    @Deprecated
    public Catalog setCatrgoryIds(Set<String> categoryIds) {
        return setCategoryIds(categoryIds);
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
     * @return This object
     */
    public Catalog setPdfUrl(String pdfUrl) {
        mPdfUrl = pdfUrl;
        return this;
    }

    /**
     * Get the availability of the catalog
     * @return true if the catalog is available in every store, false if it's available only in selected stores
     */
    public boolean getIsAvailableInAllStores() {
        return mIsAvailableInAllStores;
    }

    /**
     * Set the flag about the store availability of the catalog
     * @param isAvailableInAllStores true if the catalog is available in every store, false if it's available in selected stores
     * @return this object
     */
    public Catalog setAvailability(boolean isAvailableInAllStores) {
        mIsAvailableInAllStores = isAvailableInAllStores;
        return this;
    }

    /**
     * Check if this publication is Incito only compatible
     * @return true if is Incito only
     */
    public boolean isOnlyIncito() {
        return mPublicationTypes != null && mPublicationTypes.size() == 1 && mPublicationTypes.contains(PublicationType.INCITO);
    }

    /**
     * Id of the node in the graph (long based64 encoded id)
     * @return the incito id or {@code null}
     */
    public String getIncitoId() {
        return mIncitoId;
    }

    public Catalog setIncitoId(String incitoId) {
        mIncitoId = incitoId;
        return this;
    }

    /**
     * Array of publication that can be represented. Can be {@code paged}, {@code incito} or both.
     * If contains {@code incito}, then {@link Catalog#getIncitoId()} won't be {@code null}
     * @return {@link EnumSet} of {@link PublicationType}
     */
    public EnumSet<PublicationType> getPublicationTypes() {
        return mPublicationTypes;
    }

    public Catalog setPublicationTypes(EnumSet<PublicationType> publicationTypes) {
        mPublicationTypes = publicationTypes;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Catalog catalog = (Catalog) o;

        if (mPageCount != catalog.mPageCount) return false;
        if (mOfferCount != catalog.mOfferCount) return false;
        if (mErn != null ? !mErn.equals(catalog.mErn) : catalog.mErn != null) return false;
        if (mLabel != null ? !mLabel.equals(catalog.mLabel) : catalog.mLabel != null) return false;
        if (mBackground != null ? !mBackground.equals(catalog.mBackground) : catalog.mBackground != null) return false;
        if (mRunFrom != null ? !mRunFrom.equals(catalog.mRunFrom) : catalog.mRunFrom != null) return false;
        if (mRunTill != null ? !mRunTill.equals(catalog.mRunTill) : catalog.mRunTill != null) return false;
        if (mBranding != null ? !mBranding.equals(catalog.mBranding) : catalog.mBranding != null) return false;
        if (mDealerId != null ? !mDealerId.equals(catalog.mDealerId) : catalog.mDealerId != null) return false;
        if (mDealerUrl != null ? !mDealerUrl.equals(catalog.mDealerUrl) : catalog.mDealerUrl != null) return false;
        if (mStoreId != null ? !mStoreId.equals(catalog.mStoreId) : catalog.mStoreId != null) return false;
        if (mStoreUrl != null ? !mStoreUrl.equals(catalog.mStoreUrl) : catalog.mStoreUrl != null) return false;
        if (mDimension != null ? !mDimension.equals(catalog.mDimension) : catalog.mDimension != null) return false;
        if (mImages != null ? !mImages.equals(catalog.mImages) : catalog.mImages != null) return false;
        if (mCategoryIds != null ? !mCategoryIds.equals(catalog.mCategoryIds) : catalog.mCategoryIds != null)
            return false;
        if (mPdfUrl != null ? !mPdfUrl.equals(catalog.mPdfUrl) : catalog.mPdfUrl != null) return false;
        if (mIsAvailableInAllStores != catalog.mIsAvailableInAllStores) return false;
        if (mIncitoId != null ? !mIncitoId.equals(catalog.mIncitoId) : catalog.mIncitoId != null) return false;
        if (mPublicationTypes != null ? !mPublicationTypes.equals(catalog.mPublicationTypes) : catalog.mPublicationTypes != null) return false;
        if (mPages != null ? !mPages.equals(catalog.mPages) : catalog.mPages != null) return false;
        if (mDealer != null ? !mDealer.equals(catalog.mDealer) : catalog.mDealer != null) return false;
        if (mStore != null ? !mStore.equals(catalog.mStore) : catalog.mStore != null) return false;
        return mHotspots != null ? mHotspots.equals(catalog.mHotspots) : catalog.mHotspots == null;

    }

    @Override
    public int hashCode() {
        int result = mErn != null ? mErn.hashCode() : 0;
        result = 31 * result + (mLabel != null ? mLabel.hashCode() : 0);
        result = 31 * result + (mBackground != null ? mBackground.hashCode() : 0);
        result = 31 * result + (mRunFrom != null ? mRunFrom.hashCode() : 0);
        result = 31 * result + (mRunTill != null ? mRunTill.hashCode() : 0);
        result = 31 * result + mPageCount;
        result = 31 * result + mOfferCount;
        result = 31 * result + (mBranding != null ? mBranding.hashCode() : 0);
        result = 31 * result + (mDealerId != null ? mDealerId.hashCode() : 0);
        result = 31 * result + (mDealerUrl != null ? mDealerUrl.hashCode() : 0);
        result = 31 * result + (mStoreId != null ? mStoreId.hashCode() : 0);
        result = 31 * result + (mStoreUrl != null ? mStoreUrl.hashCode() : 0);
        result = 31 * result + (mDimension != null ? mDimension.hashCode() : 0);
        result = 31 * result + (mImages != null ? mImages.hashCode() : 0);
        result = 31 * result + (mCategoryIds != null ? mCategoryIds.hashCode() : 0);
        result = 31 * result + (mPdfUrl != null ? mPdfUrl.hashCode() : 0);
        result = 31 * result + Boolean.valueOf(mIsAvailableInAllStores).hashCode();
        result = 31 * result + (mIncitoId != null ? mIncitoId.hashCode() : 0);
        result = 31 * result + (mPublicationTypes != null ? mPublicationTypes.hashCode() : 0);
        result = 31 * result + (mPages != null ? mPages.hashCode() : 0);
        result = 31 * result + (mDealer != null ? mDealer.hashCode() : 0);
        result = 31 * result + (mStore != null ? mStore.hashCode() : 0);
        result = 31 * result + (mHotspots != null ? mHotspots.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mErn);
        dest.writeString(this.mLabel);
        dest.writeParcelable(this.mBackground, 0);
        dest.writeLong(mRunFrom != null ? mRunFrom.getTime() : -1);
        dest.writeLong(mRunTill != null ? mRunTill.getTime() : -1);
        dest.writeInt(this.mPageCount);
        dest.writeInt(this.mOfferCount);
        dest.writeParcelable(this.mBranding, 0);
        dest.writeString(this.mDealerId);
        dest.writeString(this.mDealerUrl);
        dest.writeString(this.mStoreId);
        dest.writeString(this.mStoreUrl);
        dest.writeParcelable(this.mDimension, 0);
        dest.writeParcelable(this.mImages, 0);
        dest.writeStringList(mCategoryIds != null ? new ArrayList<String>(mCategoryIds) : new ArrayList<String>());
        dest.writeString(this.mPdfUrl);
        dest.writeByte((byte) (mIsAvailableInAllStores ? 1 : 0));
        dest.writeString(this.mIncitoId);
        dest.writeSerializable(this.mPublicationTypes);
        dest.writeTypedList(mPages);
        dest.writeParcelable(this.mDealer, 0);
        dest.writeParcelable(this.mStore, 0);
        dest.writeParcelable(this.mHotspots, 0);
    }

    protected Catalog(Parcel in) {
        this.mErn = in.readString();
        this.mLabel = in.readString();
        this.mBackground = in.readParcelable(MaterialColor.class.getClassLoader());
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
        ArrayList<String> catIds = new ArrayList<String>();
        in.readStringList(catIds);
        this.mCategoryIds = new HashSet<String>(catIds);
        this.mPdfUrl = in.readString();
        this.mIsAvailableInAllStores = in.readByte() != 0;
        this.mIncitoId = in.readString();
        this.mPublicationTypes = (EnumSet<PublicationType>) in.readSerializable();
        this.mPages = in.createTypedArrayList(Images.CREATOR);
        this.mDealer = in.readParcelable(Dealer.class.getClassLoader());
        this.mStore = in.readParcelable(Store.class.getClassLoader());
        this.mHotspots = in.readParcelable(HotspotMap.class.getClassLoader());
    }

    public static final Creator<Catalog> CREATOR = new Creator<Catalog>() {
        public Catalog createFromParcel(Parcel source) {
            return new Catalog(source);
        }

        public Catalog[] newArray(int size) {
            return new Catalog[size];
        }
    };
}
