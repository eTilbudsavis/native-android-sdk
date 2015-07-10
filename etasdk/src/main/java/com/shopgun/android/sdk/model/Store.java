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
import com.shopgun.android.sdk.log.EtaLog;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IErn;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Api.JsonKey;
import com.shopgun.android.sdk.utils.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * <p>This class is a representation of a store as the API v2 exposes it</p>
 *
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/stores.html">Store Reference</a>
 * documentation, on the engineering blog.
 * </p>
 *
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class Store implements IErn<Store>, IJson<JSONObject>, IDealer<Store>, Parcelable {

    public static final String TAG = Constants.getTag(Store.class);
    public static Parcelable.Creator<Store> CREATOR = new Parcelable.Creator<Store>() {
        public Store createFromParcel(Parcel source) {
            return new Store(source);
        }

        public Store[] newArray(int size) {
            return new Store[size];
        }
    };
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

    public Store() {

    }

    private Store(Parcel in) {
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

    public static ArrayList<Store> fromJSON(JSONArray stores) {
        ArrayList<Store> list = new ArrayList<Store>();
        try {
            for (int i = 0; i < stores.length(); i++)
                list.add(Store.fromJSON((JSONObject) stores.get(i)));

        } catch (JSONException e) {
            EtaLog.e(TAG, "", e);
        }
        return list;
    }

    public static Store fromJSON(JSONObject store) {
        Store s = new Store();
        if (store == null) {
            return s;
        }

        try {
            s.setId(Json.valueOf(store, JsonKey.ID));
            s.setErn(Json.valueOf(store, JsonKey.ERN));
            s.setStreet(Json.valueOf(store, JsonKey.STREET));
            s.setCity(Json.valueOf(store, JsonKey.CITY));
            s.setZipcode(Json.valueOf(store, JsonKey.ZIP_CODE));
            s.setCountry(Country.fromJSON(store.getJSONObject(JsonKey.COUNTRY)));
            s.setLatitude(Json.valueOf(store, JsonKey.LATITUDE, 0.0d));
            s.setLongitude(Json.valueOf(store, JsonKey.LONGITUDE, 0.0d));
            s.setDealerUrl(Json.valueOf(store, JsonKey.DEALER_URL));
            s.setDealerId(Json.valueOf(store, JsonKey.DEALER_ID));
            s.setBranding(Branding.fromJSON(store.getJSONObject(JsonKey.BRANDING)));
            s.setContact(Json.valueOf(store, JsonKey.CONTACT));

            if (store.has(JsonKey.SDK_DEALER)) {
                JSONObject jDealer = Json.getObject(store, JsonKey.SDK_DEALER, null);
                s.setDealer(Dealer.fromJSON(jDealer));
            }

        } catch (JSONException e) {
            EtaLog.e(TAG, "", e);
        }
        return s;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(JsonKey.ID, Json.nullCheck(getId()));
            o.put(JsonKey.ERN, Json.nullCheck(getErn()));
            o.put(JsonKey.STREET, Json.nullCheck(getStreet()));
            o.put(JsonKey.CITY, Json.nullCheck(getCity()));
            o.put(JsonKey.ZIP_CODE, Json.nullCheck(getZipcode()));
            o.put(JsonKey.COUNTRY, Json.nullCheck(getCountry().toJSON()));
            o.put(JsonKey.LATITUDE, getLatitude());
            o.put(JsonKey.LONGITUDE, getLongitude());
            o.put(JsonKey.DEALER_URL, Json.nullCheck(getDealerUrl()));
            o.put(JsonKey.DEALER_ID, Json.nullCheck(getDealerId()));
            o.put(JsonKey.BRANDING, Json.nullCheck(getBranding().toJSON()));
            o.put(JsonKey.CONTACT, Json.nullCheck(getContact()));

            if (mDealer != null) {
                o.put(JsonKey.SDK_DEALER, Json.toJson(mDealer));
            }

        } catch (JSONException e) {
            EtaLog.e(TAG, "", e);
        }
        return o;
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
     * @return A street, or <code>null</code>
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
     * @return A city, or <code>null</code>
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
     * @return A zipcode, or <code>null</code>
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
     * @return A {@link Country}, or <code>null</code>
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
     * @param latitude
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
     * @param longitude
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
     * @return A {@link String}, or <code>null</code>
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
     * @return An id, or <code>null</code>
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
     * @return A {@link Branding}, or <code>null</code>
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
     * @return A string, or <code>null</code>
     */
    public String getContact() {
        return mContact;
    }

    /**
     * This is (for now) unused by the API. Please ignore.
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
    public Store setDealer(Dealer d) {
        mDealer = d;
        mDealerId = (mDealer == null ? null : mDealer.getId());
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mBranding == null) ? 0 : mBranding.hashCode());
        result = prime * result + ((mCity == null) ? 0 : mCity.hashCode());
        result = prime * result
                + ((mContact == null) ? 0 : mContact.hashCode());
        result = prime * result
                + ((mCountry == null) ? 0 : mCountry.hashCode());
        result = prime * result + ((mDealer == null) ? 0 : mDealer.hashCode());
        result = prime * result
                + ((mDealerId == null) ? 0 : mDealerId.hashCode());
        result = prime * result
                + ((mDealerUrl == null) ? 0 : mDealerUrl.hashCode());
        result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
        long temp;
        temp = Double.doubleToLongBits(mLatitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mLongitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((mStreet == null) ? 0 : mStreet.hashCode());
        result = prime * result
                + ((mZipcode == null) ? 0 : mZipcode.hashCode());
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
        Store other = (Store) obj;
        if (mBranding == null) {
            if (other.mBranding != null)
                return false;
        } else if (!mBranding.equals(other.mBranding))
            return false;
        if (mCity == null) {
            if (other.mCity != null)
                return false;
        } else if (!mCity.equals(other.mCity))
            return false;
        if (mContact == null) {
            if (other.mContact != null)
                return false;
        } else if (!mContact.equals(other.mContact))
            return false;
        if (mCountry == null) {
            if (other.mCountry != null)
                return false;
        } else if (!mCountry.equals(other.mCountry))
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
        if (mErn == null) {
            if (other.mErn != null)
                return false;
        } else if (!mErn.equals(other.mErn))
            return false;
        if (Double.doubleToLongBits(mLatitude) != Double
                .doubleToLongBits(other.mLatitude))
            return false;
        if (Double.doubleToLongBits(mLongitude) != Double
                .doubleToLongBits(other.mLongitude))
            return false;
        if (mStreet == null) {
            if (other.mStreet != null)
                return false;
        } else if (!mStreet.equals(other.mStreet))
            return false;
        if (mZipcode == null) {
            if (other.mZipcode != null)
                return false;
        } else if (!mZipcode.equals(other.mZipcode))
            return false;
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mErn);
        dest.writeString(this.mStreet);
        dest.writeString(this.mCity);
        dest.writeString(this.mZipcode);
        dest.writeParcelable(this.mCountry, flags);
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
        dest.writeString(this.mDealerUrl);
        dest.writeString(this.mDealerId);
        dest.writeParcelable(this.mBranding, flags);
        dest.writeString(this.mContact);
        dest.writeParcelable(this.mDealer, flags);
    }
}
