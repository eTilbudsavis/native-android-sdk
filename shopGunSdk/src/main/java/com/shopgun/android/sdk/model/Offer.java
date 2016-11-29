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

import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.model.interfaces.ICatalog;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IErn;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.model.interfaces.IStore;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationOffer;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.utils.DateUtils;
import com.shopgun.android.utils.ParcelableUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>This class is a representation of an offer as the API v2 exposes it</p>
 *
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/offers.html">Offer Reference</a>
 * documentation, on the engineering blog.
 * </p>
 */
public class Offer implements IErn<Offer>, IJson<JSONObject>, ICatalog<Offer>, IDealer<Offer>, IStore<Offer>, PagedPublicationOffer, Parcelable {

    public static final String TAG = Constants.getTag(Offer.class);

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
    private Set<String> mCategoryIds = new HashSet<String>();
    private Branding mBranding;
    // Other stuff
    private Catalog mCatalog;
    private Dealer mDealer;
    private Store mStore;

    public Offer() {

    }

    public Offer(Offer offer) {

        // Ensure we don't reference objects
        Offer tmp = ParcelableUtils.copyParcelable(offer, Offer.CREATOR);

        this.mErn = tmp.mErn;
        this.mHeading = tmp.mHeading;
        this.mDescription = tmp.mDescription;
        this.mCatalogPage = tmp.mCatalogPage;
        this.mPricing = tmp.mPricing;
        this.mQuantity = tmp.mQuantity;
        this.mImages = tmp.mImages;
        this.mLinks = tmp.mLinks;
        this.mRunFrom = tmp.mRunFrom;
        this.mRunTill = tmp.mRunTill;
        this.mDealerUrl = tmp.mDealerUrl;
        this.mDealerId = tmp.mDealerId;
        this.mStoreUrl = tmp.mStoreUrl;
        this.mStoreId = tmp.mStoreId;
        this.mCatalogUrl = tmp.mCatalogUrl;
        this.mCatalogId = tmp.mCatalogId;
        this.mCategoryIds = tmp.mCategoryIds;
        this.mCatalog = tmp.mCatalog;
        this.mDealer = tmp.mDealer;
        this.mStore = tmp.mStore;
        this.mBranding = tmp.mBranding;

    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for an {@code Offer}
     * @return A {@link List} of POJO
     */
    public static List<Offer> fromJSON(JSONArray array) {
        List<Offer> list = new ArrayList<Offer>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Offer.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for an {@code Offer}
     * @return A {@link Offer}, or {@code null} if {@code object} is {@code null}
     */
    public static Offer fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        o.isErnTypeOrThrow(IErn.TYPE_OFFER, Offer.class);

        // A 'lite' offer - Stuff found in a hotspot
        Offer offer = new Offer()
                .setId(o.getId())
                .setErn(o.getErn())
                .setHeading(o.getHeading())
                .setPricing(o.getPricing())
                .setQuantity(o.getQuantity())
                .setRunFrom(o.getRunFrom())
                .setRunTill(o.getRunTill());

        // A full offer also contains these
        offer.setDescription(o.getDescription())
                .setCatalogPage(o.getCatalogPage())
                .setImages(o.getImages())
                .setLinks(o.getLinks())
                .setDealerUrl(o.getDealerUrl())
                .setDealerId(o.getDealerId())
                .setStoreUrl(o.getStoreUrl())
                .setStoreId(o.getStoreId())
                .setCatalogUrl(o.getCatalogUrl())
                .setCatalogId(o.getCatalogId())
                .setCategoryIds(o.getCategoryIds())
                .setBranding(o.getBranding());

        offer.mDealer = o.getDealer();
        offer.mStore = o.getStore();
        offer.mCatalog = o.getCatalog();

        if (!object.has(SgnJson.BRANDING) && !object.has(SgnJson.IMAGES) && !object.has(SgnJson.LINKS)) {
            o.getStats().ignoreRejectedKeys(
                    "catalog_page",
                    "description",
                    "images",
                    "links",
                    "dealer_url",
                    "dealer_id",
                    "store_url",
                    "store_id",
                    "catalog_url",
                    "catalog_id",
                    "category_ids",
                    "branding");
        }

        o.getStats().ignoreForgottenKeys("publish").ignoreRejectedKeys(SgnJson.DEALER, SgnJson.STORE, SgnJson.CATALOG).log(TAG);

        return offer;
    }

    /**
     * Check if this offer is from a {@link Hotspot} in a catalog.
     *
     * <p>This isn't perfect, basically just checks if images, and links is set.
     * So if a user sets those, this method will obviously not be correct.</p>
     * @return {@code true} if the offer is from a {@link Hotspot} else {@code false}
     */
    public boolean isHotspot() {
        return mImages == null || mLinks == null;
    }

    public JSONObject toJSON() {

        return new SgnJson()
                .setId(getId())
                .setErn(getErn())
                .setHeading(getHeading())
                .setDescription(getDescription())
                .setCatalogPage(getCatalogPage())
                .setPricing(getPricing())
                .setQuantity(getQuantity())
                .setImages(getImages())
                .setLinks(getLinks())
                .setRunFrom(getRunFrom())
                .setRunTill(getRunTill())
                .setDealerUrl(getDealerUrl())
                .setDealerId(getDealerId())
                .setStoreUrl(getStoreUrl())
                .setStoreId(getStoreId())
                .setCatalogUrl(getCatalogUrl())
                .setCatalogId(getCatalogId())
                .putDealer(getDealer())
                .putStore(getStore())
                .putCatalog(getCatalog())
                .setBranding(getBranding())
                .setCategoryIds(getCategoryIds())
                .toJSON();

    }

    public String getId() {
        if (mErn == null) {
            return null;
        }
        String[] parts = mErn.split(":");
        return parts[parts.length - 1];
    }

    public Offer setId(String id) {
        setErn((id == null) ? null : String.format("ern:%s:%s", getErnType(), id));
        return this;
    }

    public String getErn() {
        return mErn;
    }

    public Offer setErn(String ern) {
        if (ern == null || (ern.startsWith("ern:") && ern.split(":").length == 3 && ern.contains(getErnType()))) {
            mErn = ern;
        }
        return this;
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
     * therefore be {@code null}.
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
     * @return {@link Quantity} object, or {@code null}
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
     * @return {@link Links} object, or {@code null}
     */
    public Links getLinks() {
        return mLinks;
    }

    /**
     * Set {@link Links} relevant for this offer
     * @param links {@link Links} relevant for this offer
     * @return this object
     */
    public Offer setLinks(Links links) {
        mLinks = links;
        return this;
    }

    /**
     * Returns the {@link Date} this offer is be valid from.
     * @return A {@link Date}, or {@code null}
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
        mRunFrom = DateUtils.roundTime(date);
        return this;
    }

    /**
     * Returns the {@link Date} this offer is be valid till.
     * @return A {@link Date}, or {@code null}
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
        mRunTill = DateUtils.roundTime(date);
        return this;
    }

    /**
     * Get the URL that points directly to the {@link Dealer} resource of this
     * offer, this is for convenience only.
     * <p>e.g.: "https://api.etilbudsavis.dk/v2/dealers/9bc61"</p>
     * @return A {@link String}, or {@code null}
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
     * @return An id, or {@code null}
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
        } else if (mDealer != null && !mDealerId.equals(mDealer.getId())) {
            mDealer = null;
        }
        return this;
    }

    /**
     * Get the URL that points directly to the {@link Store} resource of this
     * offer, this is for convenience only.
     * <p>e.g.: "https://api.etilbudsavis.dk/v2/stores/6d36wXI"</p>
     * @return A {@link String}, or {@code null}
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
     * @return An id, or {@code null}
     */
    public String getStoreId() {
        return mStoreId;
    }

    /**
     * Set the id for a {@link Store} resource related to this offer.
     * <p>This is most likely to be set by the API, not the client</p>
     * @param storeId An id
     * @return This object
     */
    public Offer setStoreId(String storeId) {
        mStoreId = storeId;
        if (mStoreId == null) {
            mStore = null;
        } else if (mStore != null && !mStoreId.equals(mStore.getId())) {
            mStore = null;
        }
        return this;
    }

    /**
     * Get the URL that points directly to the {@link Catalog} resource of this
     * offer, this is for convenience only.
     * <p>e.g.: "https://api.etilbudsavis.dk/v2/catalogs/56e37cL"</p>
     * @return A {@link String}, or {@code null}
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
     * @return An id, or {@code null}
     */
    public String getCatalogId() {
        return mCatalogId;
    }

    /**
     * Set the id for a {@link Catalog} resource related to this offer.
     * <p>This is most likely to be set by the API, not the client</p>
     * @param catalogId An id
     * @return This object
     */
    public Offer setCatalogId(String catalogId) {
        mCatalogId = catalogId;
        if (mCatalogId == null) {
            mCatalog = null;
        } else if (mCatalog != null && !mCatalogId.equals(mCatalog.getId())) {
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
     * Set a {@link Catalog} on this offer, and updates the {@link Offer#getCatalogId() catalog id} to match the new {@link Catalog} object.
     * @param catalog A {@link Catalog} (preferably related to this offer)
     * @return This object
     */
    public Offer setCatalog(Catalog catalog) {
        mCatalog = catalog;
        mCatalogId = (mCatalog == null ? null : mCatalog.getId());
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
     * Set a {@link Dealer} on this offer, and updates the {@link Offer#getDealerId() dealer id} to match the new {@link Dealer} object.
     * @param dealer A {@link Dealer} (preferably related to this offer)
     * @return This object
     */
    public Offer setDealer(Dealer dealer) {
        mDealer = dealer;
        mDealerId = (mDealer == null ? null : mDealer.getId());
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
     * Set a {@link Store} on this offer, and updates the {@link Offer#getStoreId() store id} to match the new {@link Store} object.
     *
     * @param store A {@link Store} (preferably related to this offer)
     * @return This object
     */
    public Offer setStore(Store store) {
        mStore = store;
        mStoreId = (mStore == null ? null : mStore.getId());
        return this;
    }

    /**
     * Get the category id's for this object
     * @return A list of categories, or null
     */
    public Set<String> getCategoryIds() {
        return mCategoryIds;
    }

    /**
     * Set the list of categories for this object.
     * @param categoryIds A list of categories
     * @return This object
     */
    public Offer setCategoryIds(Set<String> categoryIds) {
        mCategoryIds = categoryIds;
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
     * Set the {@link Branding} to apply to this offer.
     * @param branding A branding object
     * @return This object
     */
    public Offer setBranding(Branding branding) {
        this.mBranding = branding;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Offer offer = (Offer) o;

        if (mCatalogPage != offer.mCatalogPage) return false;
        if (mErn != null ? !mErn.equals(offer.mErn) : offer.mErn != null) return false;
        if (mHeading != null ? !mHeading.equals(offer.mHeading) : offer.mHeading != null) return false;
        if (mDescription != null ? !mDescription.equals(offer.mDescription) : offer.mDescription != null) return false;
        if (mPricing != null ? !mPricing.equals(offer.mPricing) : offer.mPricing != null) return false;
        if (mQuantity != null ? !mQuantity.equals(offer.mQuantity) : offer.mQuantity != null) return false;
        if (mImages != null ? !mImages.equals(offer.mImages) : offer.mImages != null) return false;
        if (mLinks != null ? !mLinks.equals(offer.mLinks) : offer.mLinks != null) return false;
        if (mRunFrom != null ? !mRunFrom.equals(offer.mRunFrom) : offer.mRunFrom != null) return false;
        if (mRunTill != null ? !mRunTill.equals(offer.mRunTill) : offer.mRunTill != null) return false;
        if (mDealerUrl != null ? !mDealerUrl.equals(offer.mDealerUrl) : offer.mDealerUrl != null) return false;
        if (mDealerId != null ? !mDealerId.equals(offer.mDealerId) : offer.mDealerId != null) return false;
        if (mStoreUrl != null ? !mStoreUrl.equals(offer.mStoreUrl) : offer.mStoreUrl != null) return false;
        if (mStoreId != null ? !mStoreId.equals(offer.mStoreId) : offer.mStoreId != null) return false;
        if (mCatalogUrl != null ? !mCatalogUrl.equals(offer.mCatalogUrl) : offer.mCatalogUrl != null) return false;
        if (mCatalogId != null ? !mCatalogId.equals(offer.mCatalogId) : offer.mCatalogId != null) return false;
        if (mCategoryIds != null ? !mCategoryIds.equals(offer.mCategoryIds) : offer.mCategoryIds != null) return false;
        if (mBranding != null ? !mBranding.equals(offer.mBranding) : offer.mBranding != null) return false;
        if (mCatalog != null ? !mCatalog.equals(offer.mCatalog) : offer.mCatalog != null) return false;
        if (mDealer != null ? !mDealer.equals(offer.mDealer) : offer.mDealer != null) return false;
        return mStore != null ? mStore.equals(offer.mStore) : offer.mStore == null;

    }

    @Override
    public int hashCode() {
        int result = mErn != null ? mErn.hashCode() : 0;
        result = 31 * result + (mHeading != null ? mHeading.hashCode() : 0);
        result = 31 * result + (mDescription != null ? mDescription.hashCode() : 0);
        result = 31 * result + mCatalogPage;
        result = 31 * result + (mPricing != null ? mPricing.hashCode() : 0);
        result = 31 * result + (mQuantity != null ? mQuantity.hashCode() : 0);
        result = 31 * result + (mImages != null ? mImages.hashCode() : 0);
        result = 31 * result + (mLinks != null ? mLinks.hashCode() : 0);
        result = 31 * result + (mRunFrom != null ? mRunFrom.hashCode() : 0);
        result = 31 * result + (mRunTill != null ? mRunTill.hashCode() : 0);
        result = 31 * result + (mDealerUrl != null ? mDealerUrl.hashCode() : 0);
        result = 31 * result + (mDealerId != null ? mDealerId.hashCode() : 0);
        result = 31 * result + (mStoreUrl != null ? mStoreUrl.hashCode() : 0);
        result = 31 * result + (mStoreId != null ? mStoreId.hashCode() : 0);
        result = 31 * result + (mCatalogUrl != null ? mCatalogUrl.hashCode() : 0);
        result = 31 * result + (mCatalogId != null ? mCatalogId.hashCode() : 0);
        result = 31 * result + (mCategoryIds != null ? mCategoryIds.hashCode() : 0);
        result = 31 * result + (mBranding != null ? mBranding.hashCode() : 0);
        result = 31 * result + (mCatalog != null ? mCatalog.hashCode() : 0);
        result = 31 * result + (mDealer != null ? mDealer.hashCode() : 0);
        result = 31 * result + (mStore != null ? mStore.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mErn);
        dest.writeString(this.mHeading);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mCatalogPage);
        dest.writeParcelable(this.mPricing, 0);
        dest.writeParcelable(this.mQuantity, 0);
        dest.writeParcelable(this.mImages, 0);
        dest.writeParcelable(this.mLinks, 0);
        dest.writeLong(mRunFrom != null ? mRunFrom.getTime() : -1);
        dest.writeLong(mRunTill != null ? mRunTill.getTime() : -1);
        dest.writeString(this.mDealerUrl);
        dest.writeString(this.mDealerId);
        dest.writeString(this.mStoreUrl);
        dest.writeString(this.mStoreId);
        dest.writeString(this.mCatalogUrl);
        dest.writeString(this.mCatalogId);
        dest.writeStringList(new ArrayList<String>(mCategoryIds));
        dest.writeParcelable(this.mBranding, 0);
        dest.writeParcelable(this.mCatalog, 0);
        dest.writeParcelable(this.mDealer, 0);
        dest.writeParcelable(this.mStore, 0);
    }

    protected Offer(Parcel in) {
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
        ArrayList<String> catIds = new ArrayList<String>();
        in.readStringList(catIds);
        this.mCategoryIds = new HashSet<String>(catIds);
        this.mBranding = in.readParcelable(Branding.class.getClassLoader());
        this.mCatalog = in.readParcelable(Catalog.class.getClassLoader());
        this.mDealer = in.readParcelable(Dealer.class.getClassLoader());
        this.mStore = in.readParcelable(Store.class.getClassLoader());
    }

    public static final Parcelable.Creator<Offer> CREATOR = new Parcelable.Creator<Offer>() {
        public Offer createFromParcel(Parcel source) {
            return new Offer(source);
        }

        public Offer[] newArray(int size) {
            return new Offer[size];
        }
    };
}
