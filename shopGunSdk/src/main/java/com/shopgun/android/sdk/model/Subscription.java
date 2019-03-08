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
import androidx.annotation.Keep;

import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Keep
public class Subscription implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Subscription.class);
    public static final Parcelable.Creator<Subscription> CREATOR = new Parcelable.Creator<Subscription>() {
        public Subscription createFromParcel(Parcel source) {
            return new Subscription(source);
        }

        public Subscription[] newArray(int size) {
            return new Subscription[size];
        }
    };
    private Dealer mDealer;
    /**
     * Compare object, that uses {@link Dealer#getName() name} to compare two lists.
     */
    public static Comparator<Subscription> DEALER_NAME_COMPARATOR = new Comparator<Subscription>() {

        public int compare(Subscription item1, Subscription item2) {

            if (item1 == null || item2 == null) {
                return item1 == null ? (item2 == null ? 0 : 1) : -1;
            }

            Dealer d1 = item1.getDealer();
            Dealer d2 = item2.getDealer();
            if (d1 == null || d2 == null) {
                return d1 == null ? (d2 == null ? 0 : 1) : -1;
            } else {
                String t1 = d1.getName();
                String t2 = d2.getName();
                if (t1 == null || t2 == null) {
                    return t1 == null ? (t2 == null ? 0 : 1) : -1;
                }

                //ascending order
                return t1.compareToIgnoreCase(t2);
            }

        }

    };
    private String mDealerId;
    private boolean mSubscribed = false;

    private Subscription() {
        // empty
    }

    public Subscription(Subscription s) {
        setDealer(s.getDealer());
        setDealerId(s.getDealerId());
        setSubscribed(s.isSubscribed());
    }

    public Subscription(Dealer d) {
        setDealer(d);
    }

    public Subscription(String dealerId) {
        setDealerId(dealerId);
    }

    private Subscription(Parcel in) {
        this.mDealer = in.readParcelable(Dealer.class.getClassLoader());
        this.mDealerId = in.readString();
        this.mSubscribed = in.readByte() != 0;
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Subscription}
     * @return A {@link List} of POJO
     */
    public static List<Subscription> fromJSON(JSONArray array) {
        List<Subscription> list = new ArrayList<Subscription>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Subscription.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Subscription}
     * @return A {@link Subscription}, or {@code null} if {@code object} is {@code null}
     */
    public static Subscription fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Subscription s = new Subscription()
                .setDealerId(o.getDealerId())
                .setSubscribed(o.getSubscribed())
                .setDealer(o.getDealer());

        o.getStats().ignoreRejectedKeys(SgnJson.DEALER).log(TAG);

        return s;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setDealerId(getDealerId())
                .setSubscribed(isSubscribed())
                .putDealer(getDealer())
                .toJSON();
    }

    public Dealer getDealer() {
        return mDealer;
    }

    public Subscription setDealer(Dealer d) {
        mDealer = d;
        if (mDealer != null) {
            setDealerId(mDealer.getId());
        }
        return this;
    }

    public String getDealerId() {
        return mDealerId;
    }

    public Subscription setDealerId(String dealerId) {
        mDealerId = dealerId;
        if (mDealer != null && !mDealer.getId().equals(mDealerId)) {
            setDealer(null);
        }
        return this;
    }

    public boolean isSubscribed() {
        return mSubscribed;
    }

    public Subscription setSubscribed(boolean subscribed) {
        mSubscribed = subscribed;
        return this;
    }

    public void toggle() {
        mSubscribed = !mSubscribed;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDealer == null) ? 0 : mDealer.hashCode());
        result = prime * result
                + ((mDealerId == null) ? 0 : mDealerId.hashCode());
        result = prime * result + (mSubscribed ? 1231 : 1237);
        return result;
    };

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Subscription other = (Subscription) obj;
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
        if (mSubscribed != other.mSubscribed)
            return false;
        return true;
    }

    public String toString() {
        JSONObject o = toJSON();
        if (mDealer != null) {
            try {
                o.remove(SgnJson.DEALER);
                o.put(SgnJson.DEALER, mDealer.getName());
            } catch (JSONException e) {
                // ignore
            }
        }
        return o.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mDealer, flags);
        dest.writeString(this.mDealerId);
        dest.writeByte(mSubscribed ? (byte) 1 : (byte) 0);
    }

}
