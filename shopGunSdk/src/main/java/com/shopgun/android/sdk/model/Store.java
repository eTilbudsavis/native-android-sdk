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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IErn;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * <p>This class is a representation of a store as the API v2 exposes it</p>
 *
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/stores.html">Store Reference</a>
 * documentation, on the engineering blog.
 * </p>
 */
public class Store implements IErn<Store>, IJson<JSONObject>, IDealer<Store>, Parcelable {

    public static final String TAG = Constants.getTag(Store.class);

    private String mErn;
    private String mStreet;
    private String mCity;
    private String mZipcode;
    private Country mCountry;
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private String mDealerUrl;
    private String mDealerId;
    private Branding mBranding;
    private String mContact;
    private Dealer mDealer;
    private Set<String> mCategoryIds;

    public Store() {
    }

    public Store(Store store) {

        // Ensure we don't reference objects
        Store tmp = Utils.copyParcelable(store, Store.CREATOR);

        this.mErn = tmp.mErn;
        this.mStreet = tmp.mStreet;
        this.mCity = tmp.mCity;
        this.mZipcode = tmp.mZipcode;
        this.mCountry = tmp.mCountry;
        this.mLatitude = tmp.mLatitude;
        this.mLongitude = tmp.mLongitude;
        this.mDealerUrl = tmp.mDealerUrl;
        this.mDealerId = tmp.mDealerId;
        this.mBranding = tmp.mBranding;
        this.mContact = tmp.mContact;
        this.mDealer = tmp.mDealer;
        this.mCategoryIds = tmp.mCategoryIds;
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a store
     * @return A {@link List} of POJO
     */
    public static ArrayList<Store> fromJSON(JSONArray array) {
        ArrayList<Store> list = new ArrayList<Store>();
        try {
            for (int i = 0; i < array.length(); i++)
                list.add(Store.fromJSON((JSONObject) array.get(i)));

        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a store
     * @return A {@link Store}, or {@code null} if {@code object} is {@code null}
     */
    public static Store fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        o.isErnTypeOrThrow(IErn.TYPE_STORE, Store.class);
        Store s = new Store()
                .setId(o.getId())
                .setErn(o.getErn())
                .setStreet(o.getStreet())
                .setCity(o.getCity())
                .setZipcode(o.getZipCode())
                .setCountry(o.getCountry())
                .setLatitude(o.getLatitude())
                .setLongitude(o.getLongitude())
                .setDealerUrl(o.getDealerUrl())
                .setDealerId(o.getDealerId())
                .setBranding(o.getBranding())
                .setContact(o.getContact())
                .setCategoryIds(o.getCategoryIds());

        s.mDealer = o.getDealer();

        o.getStats()
                .ignoreForgottenKeys("youtube_user_id", "twitter_handle", "facebook_page_id")
                .ignoreRejectedKeys(SgnJson.DEALER)
                .log(TAG);

        return s;
    }

    public JSONObject toJSON() {

        return new SgnJson()
                .setId(getId())
                .setErn(getErn())
                .setStreet(getStreet())
                .setCity(getCity())
                .setZipCode(getZipcode())
                .setCountry(getCountry())
                .setLatitude(getLatitude())
                .setLongitude(getLongitude())
                .setDealerUrl(getDealerUrl())
                .setDealerId(getDealerId())
                .setBranding(getBranding())
                .setContact(getContact())
                .setCategoryIds(getCategoryIds())
                // Internal SDK variables
                .putDealer(getDealer())
                .toJSON();

    }

    public String getId() {
        if (mErn == null) {
            return null;
        }
        String[] parts = mErn.split(":");
        return parts[parts.length - 1];
    }

    public Store setId(String id) {
        setErn((id == null) ? null : String.format("ern:%s:%s", getErnType(), id));
        return this;
    }

    public String getErn() {
        return mErn;
    }

    public Store setErn(String ern) {
        if (ern == null || (ern.startsWith("ern:") && ern.split(":").length == 3 && ern.contains(getErnType()))) {
            mErn = ern;
        }
        return this;
    }

    public String getErnType() {
        return IErn.TYPE_STORE;
    }

    /**
     * Get the street for this {@link Store}
     * @return A street, or {@code null}
     */
    public String getStreet() {
        return mStreet;
    }

    /**
     * Set a street for this {@link Store}.
     * @param street A street
     * @return this object
     */
    public Store setStreet(String street) {
        mStreet = street;
        return this;
    }

    /**
     * Get the city for this {@link Store}
     * @return A city, or {@code null}
     */
    public String getCity() {
        return mCity;
    }

    /**
     * Set a city for this {@link Store}.
     * @param city A city
     * @return this object
     */
    public Store setCity(String city) {
        mCity = city;
        return this;
    }

    /**
     * Get the zip code for this {@link Store}
     * @return A zipcode, or {@code null}
     */
    public String getZipcode() {
        return mZipcode;
    }

    /**
     * Set a zipcode for this {@link Store}.
     * @param zipcode A zipcode
     * @return this object
     */
    public Store setZipcode(String zipcode) {
        mZipcode = zipcode;
        return this;
    }

    /**
     * Get the country for this object
     * @return A {@link Country}, or {@code null}
     */
    public Country getCountry() {
        return mCountry;
    }

    /**
     * Set the {@link Country} object for this {@link Store}
     * @param country A {@link Country}
     * @return this object
     */
    public Store setCountry(Country country) {
        mCountry = country;
        return this;
    }

    /**
     * Set the latitude for this {@link Store}
     * @return A latitude, or 0.0d if no latitude was provided for this {@link Store}
     */
    public Double getLatitude() {
        return mLatitude;
    }

    /**
     * Set the latitude for this {@link Store}
     * @param latitude A latitude for this {@link Store}
     * @return this object
     */
    public Store setLatitude(Double latitude) {
        mLatitude = latitude;
        return this;
    }

    /**
     * Set the longitude for this {@link Store}
     * @return A longitude, or 0.0d if no longitude was provided for this {@link Store}
     */
    public Double getLongitude() {
        return mLongitude;
    }

    /**
     * Set the longitude for this {@link Store}
     * @param longitude A longitude for this {@link Store}
     * @return this object
     */
    public Store setLongitude(Double longitude) {
        mLongitude = longitude;
        return this;
    }

    /**
     * Get the URL that points directly to the {@link Dealer} resource of this
     * {@link Store}, this is for convenience only.
     * <p>e.g.: "https://api.etilbudsavis.dk/v2/dealers/9bc61"</p>
     * @return A {@link String}, or {@code null}
     */
    public String getDealerUrl() {
        return mDealerUrl;
    }

    /**
     * Set an URL of the {@link Dealer} resource of this {@link Store}.
     * <p>This is most likely decided by the API</p>
     * @param url An URL to a dealer resource
     * @return This object
     */
    public Store setDealerUrl(String url) {
        mDealerUrl = url;
        return this;
    }

    /**
     * Get the id for a {@link Dealer} resource related to this {@link Store}.
     * @return An id, or {@code null}
     */
    public String getDealerId() {
        return mDealerId;
    }

    /**
     * Set the id for a {@link Dealer} resource related to this {@link Store}.
     * <p>This is most likely to be set by the API, not the client</p>
     * @param dealerId A secure id - as provided by the API
     * @return This object
     */
    public Store setDealerId(String dealerId) {
        mDealerId = dealerId;
        if (mDealerId == null) {
            mDealer = null;
        } else if (mDealer != null && !mDealerId.equals(mDealer.getId())) {
            mDealer = null;
        }
        return this;
    }

    /**
     * The {@link Branding} that is specific for this {@link Store}
     * @return A {@link Branding}, or {@code null}
     */
    public Branding getBranding() {
        return mBranding;
    }

    /**
     * Set a {@link Branding} that is specific for this store.
     * @param branding A {@link Branding}
     * @return this object
     */
    public Store setBranding(Branding branding) {
        mBranding = branding;
        return this;
    }

    /**
     * This is (for now) unused by the API. Please ignore.
     * @return A string, or {@code null}
     */
    public String getContact() {
        return mContact;
    }

    /**
     * This is (for now) unused by the API. Please ignore.
     * @param contact A contact string
     * @return This object
     */
    public Store setContact(String contact) {
        mContact = contact;
        return this;
    }

    /**
     * Get the {@link Dealer} which is (or rather should be, but this is not
     * guaranteed) related to this store.
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
     * Set a {@link Dealer} on this {@link Store}, and updates the {@link Store#getDealerId() dealer id} to match the new {@link Dealer} object.
     * @param dealer A {@link Dealer} (preferably related to this store)
     * @return This object
     */
    public Store setDealer(Dealer dealer) {
        mDealer = dealer;
        mDealerId = (mDealer == null ? null : mDealer.getId());
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
    public Store setCategoryIds(Set<String> categoryIds) {
        mCategoryIds = categoryIds;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Store store = (Store) o;

        if (Double.compare(store.mLatitude, mLatitude) != 0) return false;
        if (Double.compare(store.mLongitude, mLongitude) != 0) return false;
        if (mErn != null ? !mErn.equals(store.mErn) : store.mErn != null) return false;
        if (mStreet != null ? !mStreet.equals(store.mStreet) : store.mStreet != null) return false;
        if (mCity != null ? !mCity.equals(store.mCity) : store.mCity != null) return false;
        if (mZipcode != null ? !mZipcode.equals(store.mZipcode) : store.mZipcode != null) return false;
        if (mCountry != null ? !mCountry.equals(store.mCountry) : store.mCountry != null) return false;
        if (mDealerUrl != null ? !mDealerUrl.equals(store.mDealerUrl) : store.mDealerUrl != null) return false;
        if (mDealerId != null ? !mDealerId.equals(store.mDealerId) : store.mDealerId != null) return false;
        if (mBranding != null ? !mBranding.equals(store.mBranding) : store.mBranding != null) return false;
        if (mContact != null ? !mContact.equals(store.mContact) : store.mContact != null) return false;
        return mDealer != null ? mDealer.equals(store.mDealer) : store.mDealer == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = mErn != null ? mErn.hashCode() : 0;
        result = 31 * result + (mStreet != null ? mStreet.hashCode() : 0);
        result = 31 * result + (mCity != null ? mCity.hashCode() : 0);
        result = 31 * result + (mZipcode != null ? mZipcode.hashCode() : 0);
        result = 31 * result + (mCountry != null ? mCountry.hashCode() : 0);
        temp = Double.doubleToLongBits(mLatitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mLongitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (mDealerUrl != null ? mDealerUrl.hashCode() : 0);
        result = 31 * result + (mDealerId != null ? mDealerId.hashCode() : 0);
        result = 31 * result + (mBranding != null ? mBranding.hashCode() : 0);
        result = 31 * result + (mContact != null ? mContact.hashCode() : 0);
        result = 31 * result + (mDealer != null ? mDealer.hashCode() : 0);
        return result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mErn);
        dest.writeString(this.mStreet);
        dest.writeString(this.mCity);
        dest.writeString(this.mZipcode);
        dest.writeParcelable(this.mCountry, 0);
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
        dest.writeString(this.mDealerUrl);
        dest.writeString(this.mDealerId);
        dest.writeParcelable(this.mBranding, 0);
        dest.writeString(this.mContact);
        dest.writeParcelable(this.mDealer, 0);
    }

    protected Store(Parcel in) {
        this.mErn = in.readString();
        this.mStreet = in.readString();
        this.mCity = in.readString();
        this.mZipcode = in.readString();
        this.mCountry = in.readParcelable(Country.class.getClassLoader());
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
        this.mDealerUrl = in.readString();
        this.mDealerId = in.readString();
        this.mBranding = in.readParcelable(Branding.class.getClassLoader());
        this.mContact = in.readString();
        this.mDealer = in.readParcelable(Dealer.class.getClassLoader());
    }

    public static final Creator<Store> CREATOR = new Creator<Store>() {
        public Store createFromParcel(Parcel source) {
            return new Store(source);
        }

        public Store[] newArray(int size) {
            return new Store[size];
        }
    };
}
