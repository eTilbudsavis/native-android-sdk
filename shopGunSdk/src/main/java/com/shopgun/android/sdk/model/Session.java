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
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Json;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Session implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Session.class);
    public static Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {
        public Session createFromParcel(Parcel source) {
            return new Session(source);
        }

        public Session[] newArray(int size) {
            return new Session[size];
        }
    };
    private String mToken;
    private Date mExpires = new Date(1000);
    private User mUser = new User();
    private Permission mPermission;
    private String mProvider;
    private String mClientId;
    private String mReference;

    public Session() {

    }

    private Session(Parcel in) {
        this.mToken = in.readString();
        long tmpMExpires = in.readLong();
        this.mExpires = tmpMExpires == -1 ? null : new Date(tmpMExpires);
        this.mUser = in.readParcelable(User.class.getClassLoader());
        this.mPermission = in.readParcelable(Permission.class.getClassLoader());
        this.mProvider = in.readString();
        this.mClientId = in.readString();
        this.mReference = in.readString();
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Session}
     * @return A {@link List} of POJO
     */
    public static List<Session> fromJSON(JSONArray array) {
        List<Session> list = new ArrayList<Session>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = Json.getObject(array, i);
            if (o != null) {
                list.add(Session.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Session}
     * @return A {@link Session}, or {@code null} if {@code object} is {@code null}
     */
    public static Session fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Session s = new Session()
                .setToken(o.getToken())
                .setExpires(o.getExpires())
                .setClientId(o.getClientId())
                .setReference(o.getReference())
                .setUser(o.getUser())
                .setPermission(o.getPermissions())
                .setProvider(o.getProvider());

        o.getStats().log(TAG);

        return s;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setToken(getToken())
                .setExpires(getExpire())
                .setClientId(getClientId())
                .setReference(getReference())
                .setUser(getUser())
                .setPermissions(getPermission())
                .setProvider(getProvider())
                .toJSON();
    }

    /**
     * Get this Sessions token. Used for headers in API calls
     * @return token as String if session is active, otherwise null.
     */
    public String getToken() {
        return mToken;
    }

    public Session setToken(String token) {
        mToken = token;
        return this;
    }

    public String getClientId() {
        return mClientId;
    }

    public Session setClientId(String clientId) {
        mClientId = clientId;
        return this;
    }

    public String getReference() {
        return mReference;
    }

    public Session setReference(String reference) {
        mReference = reference;
        return this;
    }

    public User getUser() {
        return mUser;
    }

    public Session setUser(User user) {
        mUser = user == null ? new User() : user;
        return this;
    }

    public Permission getPermission() {
        return mPermission;
    }

    public Session setPermission(Permission permission) {
        mPermission = permission;
        return this;
    }

    public String getProvider() {
        return mProvider;
    }

    public Session setProvider(String provider) {
        mProvider = provider;
        return this;
    }

    public Session setExpires(Date time) {
        mExpires = time;
        return this;
    }

    public Date getExpire() {
        return mExpires;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        if (mToken != null ? !mToken.equals(session.mToken) : session.mToken != null) return false;
        if (mExpires != null ? !mExpires.equals(session.mExpires) : session.mExpires != null) return false;
        if (mUser != null ? !mUser.equals(session.mUser) : session.mUser != null) return false;
        if (mPermission != null ? !mPermission.equals(session.mPermission) : session.mPermission != null) return false;
        if (mProvider != null ? !mProvider.equals(session.mProvider) : session.mProvider != null) return false;
        if (mClientId != null ? !mClientId.equals(session.mClientId) : session.mClientId != null) return false;
        return mReference != null ? mReference.equals(session.mReference) : session.mReference == null;

    }

    @Override
    public int hashCode() {
        int result = mToken != null ? mToken.hashCode() : 0;
        result = 31 * result + (mExpires != null ? mExpires.hashCode() : 0);
        result = 31 * result + (mUser != null ? mUser.hashCode() : 0);
        result = 31 * result + (mPermission != null ? mPermission.hashCode() : 0);
        result = 31 * result + (mProvider != null ? mProvider.hashCode() : 0);
        result = 31 * result + (mClientId != null ? mClientId.hashCode() : 0);
        result = 31 * result + (mReference != null ? mReference.hashCode() : 0);
        return result;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mToken);
        dest.writeLong(mExpires != null ? mExpires.getTime() : -1);
        dest.writeParcelable(this.mUser, flags);
        dest.writeParcelable(this.mPermission, flags);
        dest.writeString(this.mProvider);
        dest.writeString(this.mClientId);
        dest.writeString(this.mReference);
    }

}
