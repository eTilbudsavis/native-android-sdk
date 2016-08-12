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

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class Pricing implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Pricing.class);

    private double mPrice = 1.0d;
    private Double mPrePrice;
    private Currency mCurrency;

    public Pricing() {

    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Pricing}
     * @return A {@link List} of POJO
     */
    public static List<Pricing> fromJSON(JSONArray array) {
        List<Pricing> list = new ArrayList<Pricing>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Pricing.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Pricing}
     * @return A {@link Pricing}, or {@code null} if {@code object} is {@code null}
     */
    public static Pricing fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Pricing p = new Pricing()
                .setPrice(o.getPrice())
                .setPrePrice(o.getPrePrice())
                .setCurrency(o.getCurrency());

        o.getStats().log(TAG);

        return p;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setPrice(getPrice())
                .setPrePrice(getPrePrice())
                .setCurrency(getCurrency().getCurrencyCode())
                .toJSON();
    }

    public double getPrice() {
        return mPrice;
    }

    public Pricing setPrice(double price) {
        mPrice = price;
        return this;
    }

    public Double getPrePrice() {
        return mPrePrice;
    }

    public Pricing setPrePrice(Double prePrice) {
        mPrePrice = prePrice;
        return this;
    }

    /**
     * Get the {@link Currency} for this {@link Pricing} object. If nothing else was set/specified
     * we'll default to "DKK", due to a bug in API v1.
     * @return A currency.
     */
    public Currency getCurrency() {
        if (mCurrency == null) {
            // API v1 had a null issue, we'll default to DKK
            return Currency.getInstance("DKK");
        }
        return mCurrency;
    }

    /**
     * A currency corresponding to an <a href="http://en.wikipedia.org/wiki/ISO_4217">ISO 4217</a>
     * currency code such as "EUR" or "USD". If <code>isoCurrencyCode</code> is {@code null}, we'll default to
     * "DKK", due to a bug i API v1.
     * @param isoCurrencyCode A currency string
     * @return this object
     */
    public Pricing setCurrency(String isoCurrencyCode) {
        mCurrency = Currency.getInstance(isoCurrencyCode==null ? "DKK" : isoCurrencyCode);
        return this;
    }

    /**
     * A currency corresponding to an <a href="http://en.wikipedia.org/wiki/ISO_4217">ISO 4217</a>
     * currency code such as "EUR" or "USD". If <code>currency</code> is {@code null}, we'll default to
     * "DKK", due to a bug i API v1.
     * @param currency A currency string
     * @return this object
     */
    public Pricing setCurrency(Currency currency) {
        // API v1 had a null issue, we'll default to DKK
        mCurrency = currency==null ? Currency.getInstance("DKK") : currency;
        return this;
    }

    public boolean hasPrePrice() {
        return mPrePrice != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pricing pricing = (Pricing) o;

        if (Double.compare(pricing.mPrice, mPrice) != 0) return false;
        if (mPrePrice != null ? !mPrePrice.equals(pricing.mPrePrice) : pricing.mPrePrice != null) return false;
        return !(mCurrency != null ? !mCurrency.equals(pricing.mCurrency) : pricing.mCurrency != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(mPrice);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (mPrePrice != null ? mPrePrice.hashCode() : 0);
        result = 31 * result + (mCurrency != null ? mCurrency.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mPrice);
        dest.writeValue(this.mPrePrice);
        dest.writeSerializable(this.mCurrency);
    }

    protected Pricing(Parcel in) {
        this.mPrice = in.readDouble();
        this.mPrePrice = (Double) in.readValue(Double.class.getClassLoader());
        this.mCurrency = (Currency) in.readSerializable();
    }

    public static final Creator<Pricing> CREATOR = new Creator<Pricing>() {
        public Pricing createFromParcel(Parcel source) {
            return new Pricing(source);
        }

        public Pricing[] newArray(int size) {
            return new Pricing[size];
        }
    };
}
