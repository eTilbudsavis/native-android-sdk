/**
 * ****************************************************************************
 * Copyright 2014 eTilbudsavis
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package com.eTilbudsavis.etasdk.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.interfaces.IJson;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.utils.Json;
import com.eTilbudsavis.etasdk.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

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

    public static Session fromJSON(JSONObject session) {
        Session s = new Session();
        if (session == null) {
            return s;
        }

        s.setToken(Json.valueOf(session, JsonKey.TOKEN));
        String exp = Json.valueOf(session, JsonKey.EXPIRES);
        s.setExpires(Utils.stringToDate(exp));
        s.setClientId(Json.valueOf(session, JsonKey.CLIENT_ID));
        s.setReference(Json.valueOf(session, JsonKey.REFERENCE));

        JSONObject user = Json.getObject(session, JsonKey.USER);
        s.setUser(User.fromJSON(user));

        JSONObject perm = Json.getObject(session, JsonKey.PERMISSIONS);
        s.setPermission(Permission.fromJSON(perm));

        s.setProvider(Json.valueOf(session, JsonKey.PROVIDER));

        return s;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(JsonKey.TOKEN, Json.nullCheck(getToken()));
            o.put(JsonKey.EXPIRES, Json.nullCheck(Utils.dateToString(getExpire())));
            o.put(JsonKey.USER, getUser().getUserId() == User.NO_USER ? JSONObject.NULL : getUser().toJSON());
            o.put(JsonKey.PERMISSIONS, Json.toJson(getPermission()));
            o.put(JsonKey.PROVIDER, Json.nullCheck(getProvider()));
            o.put(JsonKey.CLIENT_ID, Json.nullCheck(mClientId));
            o.put(JsonKey.REFERENCE, Json.nullCheck(mReference));
        } catch (JSONException e) {
            EtaLog.e(TAG, "", e);
        }
        return o;
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

    public void setClientId(String clientId) {
        mClientId = clientId;
    }

    public String getReference() {
        return mReference;
    }

    public void setReference(String reference) {
        mReference = reference;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mClientId == null) ? 0 : mClientId.hashCode());
        result = prime * result
                + ((mExpires == null) ? 0 : mExpires.hashCode());
        result = prime * result
                + ((mPermission == null) ? 0 : mPermission.hashCode());
        result = prime * result
                + ((mProvider == null) ? 0 : mProvider.hashCode());
        result = prime * result
                + ((mReference == null) ? 0 : mReference.hashCode());
        result = prime * result + ((mToken == null) ? 0 : mToken.hashCode());
        result = prime * result + ((mUser == null) ? 0 : mUser.hashCode());
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
        Session other = (Session) obj;
        if (mClientId == null) {
            if (other.mClientId != null)
                return false;
        } else if (!mClientId.equals(other.mClientId))
            return false;
        if (mExpires == null) {
            if (other.mExpires != null)
                return false;
        } else if (!mExpires.equals(other.mExpires))
            return false;
        if (mPermission == null) {
            if (other.mPermission != null)
                return false;
        } else if (!mPermission.equals(other.mPermission))
            return false;
        if (mProvider == null) {
            if (other.mProvider != null)
                return false;
        } else if (!mProvider.equals(other.mProvider))
            return false;
        if (mReference == null) {
            if (other.mReference != null)
                return false;
        } else if (!mReference.equals(other.mReference))
            return false;
        if (mToken == null) {
            if (other.mToken != null)
                return false;
        } else if (!mToken.equals(other.mToken))
            return false;
        if (mUser == null) {
            if (other.mUser != null)
                return false;
        } else if (!mUser.equals(other.mUser))
            return false;
        return true;
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
