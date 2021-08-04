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

import com.shopgun.android.sdk.api.MetaKeys;
import com.shopgun.android.sdk.model.interfaces.IErn;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.model.interfaces.SyncState;
import com.shopgun.android.sdk.shoppinglists.ListManager;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shopgun.android.sdk.model.User.NO_USER;

/**
 * @deprecated No longer maintained
 *
 * This class is a representation of the ShopGun API v2 shopppinglist.
 */
@Deprecated
public class Shoppinglist implements Comparable<Shoppinglist>, SyncState<Shoppinglist>, IErn<Shoppinglist>, IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Shoppinglist.class);

    /**
     * The default {@link Shoppinglist} type
     */
    public static final String TYPE_SHOPPING_LIST = "shopping_list";

    /**
     * A type used for wishlists, this could be for christmas, birthdays e.t.c.
     */
    public static final String TYPE_WISH_LIST = "wish_list";

    /**
     * Access level that only permits the owner of the list to view it.
     */
    public static final String ACCESS_PRIVATE = "private";

    /**
     * Access level that allows {@link Share owner}, and {@link Share shares} of the
     * list to view/edit it.
     */
    public static final String ACCESS_SHARED = "shared";

    /**
     * Access level that makes it visible to everyone, but only editable
     * to anyone who has a {@link Share share} with read/write privileges.
     */
    public static final String ACCESS_PUBLIC = "public";
    public static final Parcelable.Creator<Shoppinglist> CREATOR = new Parcelable.Creator<Shoppinglist>() {
        public Shoppinglist createFromParcel(Parcel source) {
            return new Shoppinglist(source);
        }

        public Shoppinglist[] newArray(int size) {
            return new Shoppinglist[size];
        }
    };
    private String mErn;
    private String mName = "";
    /**
     * Compare object, that uses {@link Shoppinglist#getName() name}
     * to compare two lists.
     */
    public static Comparator<Shoppinglist> NAME_COMPARATOR = new Comparator<Shoppinglist>() {

        public int compare(Shoppinglist item1, Shoppinglist item2) {

            if (item1 == null || item2 == null) {
                return item1 == null ? (item2 == null ? 0 : 1) : -1;
            } else {
                String t1 = item1.getName();
                String t2 = item2.getName();
                if (t1 == null || t2 == null) {
                    return t1 == null ? (t2 == null ? 0 : 1) : -1;
                }

                //ascending order
                return t1.compareToIgnoreCase(t2);
            }

        }

    };
    private String mAccess = ACCESS_PRIVATE;
    private Date mModified;
    private String mPrevId;
    private String mType;
    private JSONObject mMeta;
    private HashMap<String, Share> mShares = new HashMap<String, Share>(1);
    private String mUserId = NO_USER;
    private int mSyncState = SyncState.TO_SYNC;
    private String mShareToken;

    private Shoppinglist() {
        setId(SgnUtils.createUUID());
        mModified = DateUtils.roundTime(new Date());
    }

    private Shoppinglist(Parcel in) {
        this.mErn = in.readString();
        this.mName = in.readString();
        this.mAccess = in.readString();
        long tmpMModified = in.readLong();
        this.mModified = tmpMModified == -1 ? null : new Date(tmpMModified);
        this.mPrevId = in.readString();
        this.mType = in.readString();
        try {
            String json = in.readString();
            this.mMeta = new JSONObject(json);
        } catch (JSONException e) {
            this.mMeta = new JSONObject();
        }
        ArrayList<Share> list = new ArrayList<Share>();
        in.readTypedList(list, Share.CREATOR);
        for (Share s : list) {
            putEmailInShareMap(s.getEmail(), s);
        }
        this.mUserId = in.readString();
        this.mSyncState = in.readInt();
        this.mShareToken = in.readString();
    }

    private void putEmailInShareMap(String email, Share share) {
        String e = email != null ? email.toLowerCase() : null;
        mShares.put(e, share);
    }

    /**
     * Factory method for creating a shoppinglist from a name.
     * @param name A name for the {@link Shoppinglist}
     * @return A {@link Shoppinglist}
     */
    public static Shoppinglist fromName(String name) {
        Shoppinglist sl = new Shoppinglist();
        sl.setName(name);
        return sl;
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Shoppinglist}
     * @return A {@link List} of POJO
     */
    public static ArrayList<Shoppinglist> fromJSON(JSONArray array) {
        ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Shoppinglist.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Shoppinglist}
     * @return A {@link Shoppinglist}, or {@code null} if {@code object} is {@code null}
     */
    public static Shoppinglist fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        Shoppinglist shoppinglist = new Shoppinglist()
                .setId(o.getId())
                .setErn(o.getErn())
                .setName(o.getName())
                .setAccess(o.getAccess())
                .setModified(o.getModified())
                .setPreviousId(o.getPreviousId())
                .setType(o.getType())
                .setMeta(o.getMeta())
                .putShares(o.getShares())
                .setShareToken(o.getShareToken());

        // Owner is part of the shares, we'll ignore it
        // previous_id haven't been implemented for shoppinglists on the API yet
        o.getStats().ignoreForgottenKeys(SgnJson.OWNER).ignoreRejectedKeys(SgnJson.PREVIOUS_ID).log(TAG);

        return shoppinglist;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setId(getId())
                .setErn(getErn())
                .setName(getName())
                .setAccess(getAccess())
                .setModified(getModified())
                .setPreviousId(getPreviousId())
                .setType(getType())
                .setMeta(getMeta())
                .setShares(getShares().values())
                .setShareToken(getShareToken())
                .toJSON();
    }

    public String getShareToken() {
        return mShareToken;
    }

    public Shoppinglist setShareToken(String shareToken) {
        mShareToken = shareToken;
        return this;
    }

    public String getId() {
        if (mErn == null) {
            return null;
        }
        String[] parts = mErn.split(":");
        return parts[parts.length - 1];
    }

    public Shoppinglist setId(String id) {
        setErn((id == null) ? null : String.format("ern:%s:%s", getErnType(), id));
        return this;
    }

    public String getErn() {
        return mErn;
    }

    public Shoppinglist setErn(String ern) {
        if (ern == null || (ern.startsWith("ern:") && ern.split(":").length == 4 && ern.contains(getErnType()))) {
            mErn = ern;
        }
        return this;
    }

    public String getErnType() {
        return IErn.TYPE_SHOPPINGLIST;
    }

    /**
     * Get the human readable name for this {@link Shoppinglist}
     * @return A name, or {@code null}
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the name for this {@link Shoppinglist}
     * @param name A name
     * @return This object
     */
    public Shoppinglist setName(String name) {
        mName = name;
        return this;
    }

    /**
     * Get the access level for this {@link Shoppinglist}, that other will have.
     * @return The access level
     */
    public String getAccess() {
        return mAccess;
    }

    /**
     * Set the access level for this {@link Shoppinglist}. Current valid options are:
     * <ul>
     *      <li> {@link #ACCESS_PRIVATE} </li>
     *      <li> {@link #ACCESS_SHARED} </li>
     *      <li> {@link #ACCESS_PUBLIC} </li>
     * </ul>
     *
     * @param access A access string
     * @return This object
     */
    public Shoppinglist setAccess(String access) {
        mAccess = access;
        return this;
    }

    /**
     * Get the latest modified date for the {@link Shoppinglist}
     * @return A date
     */
    public Date getModified() {
        return mModified;
    }

    /**
     * Set the latest modified date for this {@link Shoppinglist}.
     *
     * <p>If you are using the {@link ListManager} this will implicitly be
     * handled for you.</p>
     * @param time A date for latest edit to the list
     * @return This object
     */
    public Shoppinglist setModified(Date time) {
        mModified = DateUtils.roundTime(time);
        return this;
    }

    /**
     * Get the {@link Shoppinglist#getId() id} of the item that is 'above' or the
     * previous element in the list of {@link Shoppinglist shoppinglists} for
     * this user, when you display the lists to the user.
     *
     * <p>If there are no {@link Shoppinglist} above this one the value will be
     * {@link com.shopgun.android.sdk.utils.ListUtils#FIRST_ITEM}</p>
     *
     * @return An {@link Shoppinglist#getId() id}
     */
    public String getPreviousId() {
        return mPrevId;
    }

    /**
     *
     * Set the {@link Shoppinglist#getId() id} of the item that is 'above' or the
     * previous element in the list of {@link Shoppinglist shoppinglists} for
     * this user, when you display the lists to the user.
     *
     * <p>If there are no {@link Shoppinglist} above this one the value will be
     * {@link com.shopgun.android.sdk.utils.ListUtils#FIRST_ITEM}</p>
     *
     * <p><b>important</b> if you are using {@link ListManager} other
     * {@link Shoppinglist shoppinglists} will have their
     * {@link Shoppinglist#setPreviousId(String) previous_id},
     * {@link Shoppinglist#setModified(Date) modified} e.t.c. updated to reflect
     * any changes to this {@link Shoppinglist}, but if you do not use the
     * {@link ListManager} you <b>must</b> update all other {@link Shoppinglist}
     * that may be accefted by these changes</p>
     *
     * @param id A new previous_id
     * @return This object
     */
    public Shoppinglist setPreviousId(String id) {
        mPrevId = id;
        return this;
    }

    /**
     * Get the type of {@link Shoppinglist}
     *
     * Currently ShopGun are using the two types below, but you are free to
     * do other types too.
     * <ul>
     * <li> {@link Shoppinglist#TYPE_SHOPPING_LIST} </li>
     * <li> {@link Shoppinglist#TYPE_WISH_LIST} </li>
     * </ul>
     * @return A type
     */
    public String getType() {
        if (mType == null || mType.isEmpty()) {
            mType = TYPE_SHOPPING_LIST;
        }
        return mType;
    }

    /**
     * Set the type of {@link Shoppinglist}
     *
     * <p>Currently ShopGun are using the two types below, but you are free to
     * do other types too.</p>
     * <ul>
     *      <li> {@link Shoppinglist#TYPE_SHOPPING_LIST} </li>
     *      <li> {@link Shoppinglist#TYPE_WISH_LIST} </li>
     * </ul>
     * @param type A type string
     * @return This object
     */
    public Shoppinglist setType(String type) {
        if (type == null || type.isEmpty()) {
            // it's a Shoppinglist
            mType = TYPE_SHOPPING_LIST;
        } else {
            // It's a different type
            mType = type;
        }
        return this;
    }

    /**
     * Get a {@link Map} of all shares in this {@link Shoppinglist}
     * @return All shares
     */
    public HashMap<String, Share> getShares() {
        return mShares;
    }

    /**
     * Update the set of {@link Share shares} that may read/edit this {@link Shoppinglist}
     *
     * <p>This method will <i>override</i> the current {@link Share shares} in
     * the list. To append/edit the current set of shares use {@link #putShares(Collection)}</p>
     * @param shares A new list of {@link Share shares}
     * @return This object
     */
    public Shoppinglist setShares(List<Share> shares) {
        mShares.clear();
        for (Share s : shares) {
            s.setShoppinglistId(getId());
            putEmailInShareMap(s.getEmail(), s);
        }
        return this;
    }

    /**
     * Appends/edit the current list of {@link Share shares} that may read/write
     * this {@link Shoppinglist}.
     * @param shares A list of {@link Share shares} to append/edit
     * @return This object
     */
    public Shoppinglist putShares(Collection<Share> shares) {
        if (shares == null) return this;

        for (Share s : shares) {
            s.setShoppinglistId(getId());
            putEmailInShareMap(s.getEmail(), s);
        }
        return this;
    }

    /**
     * Add a share in the list of {@link Share shares} that may read/write to
     * this {@link Shoppinglist}. If the {@link Share#getEmail() email} already
     * exists, it will be overridden.
     * @param share A share to add/edit
     * @return This object
     */
    public Shoppinglist putShare(Share share) {
        if (share == null) return this;
        share.setShoppinglistId(getId());
        putEmailInShareMap(share.getEmail(), share);
        return this;
    }

    /**
     * Remove a {@link Share} from this {@link Shoppinglist}.
     *
     * <p>This will revoke/remove all the privileges the share previously had,
     * to read/write to the list.</p>
     * @param s The share to remove
     * @return This object
     */
    public Shoppinglist removeShare(Share s) {
        if (s == null || s.getEmail() == null) return this;
        mShares.remove(s.getEmail().toLowerCase());
        return this;
    }

    /**
     * Get the meta information for this {@link Shoppinglist}.
     *
     * <p>Meta doesn't currently have any restrictions other that it must
     * be a {@link JSONObject}, what goes inside is completely up to you.</p>
     *
     * <p>What being said, please ensure not to accidentally override any keys
     * that others may have set on the object. This can be done by name spacing
     * your keys. Currently ShopGun are using the <b>{@code eta_}</b> name space for
     * our keys.</p>
     *
     * @return A {@link JSONObject}
     */
    public JSONObject getMeta() {
        if (mMeta == null) {
            mMeta = new JSONObject();
            setModified(new Date());
        }
        return mMeta;
    }

    /**
     * Set any meta information you may wish on the {@link Shoppinglist}.
     *
     * <p><b>important:</b> to avoid deleting others keys by getting old meta
     * information from {@link Shoppinglist#getMeta()}</p>
     *
     * <p>Meta doesn't currently have any restrictions other that it must
     * be a {@link JSONObject}, what goes inside is completely up to you.</p>
     *
     * <p>What being said, please ensure not to accidentally override any keys
     * that others may have set on the object. This can be done by name spacing
     * your keys. Currently ShopGun are using the <b>{@code eta_}</b> name space for
     * our keys.</p>
     *
     * @param meta The new meta date to use
     * @return This object
     */
    public Shoppinglist setMeta(JSONObject meta) {
        mMeta = meta == null ? new JSONObject() : meta;
        return this;
    }

    /**
     * Get the shoppinglist theme id.
     * @return A theme id. If no id have been set it returns 'default'.
     */
    public String getTheme() {
        return getMeta().optString(MetaKeys.THEME, "default");
    }

    /**
     * Method for getting and setting a theme for a shoppinglist.
     * @param id A theme id
     * @return this object
     */
    public Shoppinglist setTheme(String id) {
        try {
            getMeta().put(MetaKeys.THEME, id);
        } catch (JSONException e) {
            // ignore
        }
        return this;
    }

    /**
     * Get the {@link Share owner} of the {@link Shoppinglist}
     * @return A {@link Share} that is the owner, or {@code null}
     */
    public Share getOwner() {
        for (Share s : mShares.values()) {
            if (s.getAccess().equals(Share.ACCESS_OWNER))
                return s;
        }
        return null;
    }

    /**
     * Get the id, of the user that has this {@link Shoppinglist}.
     * <p>This is mostly a use case when storing the item in a DB, where several
     * users can have access to the same item (same {@link Shoppinglist#getId()}.</p>
     * @return A user id
     */
    public String getUserId() {
        return mUserId;
    }

    /**
     * Set the id of the user, that this {@link Shoppinglist} is associated with.
     * <p>This is mostly a use case when storing the item in a DB, where several
     * users can have access to the same item (same {@link Shoppinglist#getId()}.</p>
     * @param userId An id of a user
     * @return This object
     */
    public Shoppinglist setUserId(String userId) {
        mUserId = userId;
        return this;
    }

    public int getState() {
        return mSyncState;
    }

    public Shoppinglist setState(int state) {
        mSyncState = state;
        return this;
    }

    /**
     * Compare method, that uses the {@link Shoppinglist#getName() name}
     * to compare two lists.
     */
    public int compareTo(Shoppinglist another) {
        if (another == null)
            return -1;

        String t1 = getName();
        String t2 = another.getName();
        if (t1 == null || t2 == null) {
            return t1 == null ? (t2 == null ? 0 : 1) : -1;
        }

        //ascending order
        return t1.compareToIgnoreCase(t2);
    }

    public boolean same(Object obj) {
        return compare(obj, false, false, false);
    }

    public boolean compare(Object obj, boolean modified, boolean syncState, boolean user) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Shoppinglist other = (Shoppinglist) obj;
        if (mAccess == null) {
            if (other.mAccess != null)
                return false;
        } else if (!mAccess.equals(other.mAccess))
            return false;
        if (mErn == null) {
            if (other.mErn != null)
                return false;
        } else if (!mErn.equals(other.mErn))
            return false;

        if (mMeta == null) {
            if (other.mMeta != null)
                return false;
        } else if (!SgnJson.jsonObjectEquals(mMeta, other.mMeta))
            return false;

        if (modified) {
            if (mModified == null) {
                if (other.mModified != null)
                    return false;
            } else if (!mModified.equals(other.mModified))
                return false;
        }

        if (mName == null) {
            if (other.mName != null)
                return false;
        } else if (!mName.equals(other.mName))
            return false;
        if (mPrevId == null) {
            if (other.mPrevId != null)
                return false;
        } else if (!mPrevId.equals(other.mPrevId))
            return false;
        if (mShares == null) {
            if (other.mShares != null)
                return false;
        } else if (!mShares.equals(other.mShares))
            return false;


        if (syncState) {
            if (mSyncState != other.mSyncState)
                return false;
        }


        if (mType == null) {
            if (other.mType != null)
                return false;
        } else if (!mType.equals(other.mType))
            return false;

        if (user) {
            if (mUserId == null) {
                if (other.mUserId != null)
                    return false;
            } else if (!mUserId.equals(other.mUserId))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mAccess == null) ? 0 : mAccess.hashCode());
        result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
        result = prime * result + SgnJson.jsonObjectHashCode(mMeta);
        result = prime * result
                + ((mModified == null) ? 0 : mModified.hashCode());
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        result = prime * result + ((mPrevId == null) ? 0 : mPrevId.hashCode());
        result = prime * result + ((mShares == null) ? 0 : mShares.hashCode());
        result = prime * result + mSyncState;
        result = prime * result + ((mType == null) ? 0 : mType.hashCode());
        result = prime * result + ((mUserId == null) ? 0 : mUserId.hashCode());
        result = prime * result + ((mShareToken == null) ? 0 : mShareToken.hashCode());
        return result;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return compare(obj, true, true, true);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mErn);
        dest.writeString(this.mName);
        dest.writeString(this.mAccess);
        dest.writeLong(mModified != null ? mModified.getTime() : -1);
        dest.writeString(this.mPrevId);
        dest.writeString(this.mType);
        dest.writeString(this.mMeta.toString());
        dest.writeTypedList(new ArrayList<Share>(mShares.values()));
        dest.writeString(this.mUserId);
        dest.writeInt(this.mSyncState);
        dest.writeString(this.mShareToken);
    }

}

