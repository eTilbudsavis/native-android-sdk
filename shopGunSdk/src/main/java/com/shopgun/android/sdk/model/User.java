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
import com.shopgun.android.sdk.model.interfaces.IErn;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Json;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>This class is a representation of a user as the API v2 exposes it</p>
 *
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/users.html">User Reference</a>
 * documentation, on the engineering blog.
 * </p>
 */
public class User implements IErn<User>, IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(User.class);

    public static final int NO_USER = -1;
    public static Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private String mErn;
    private String mGender;
    private int mBirthYear = 0;
    private String mName;
    private String mEmail;
    private Permission mPermissions;

    /**
     * Default constructor
     */
    public User() {
        setUserId(NO_USER);
    }

    private User(Parcel in) {
        this.mErn = in.readString();
        this.mGender = in.readString();
        this.mBirthYear = in.readInt();
        this.mName = in.readString();
        this.mEmail = in.readString();
        this.mPermissions = in.readParcelable(Permission.class.getClassLoader());
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code User}
     * @return A {@link List} of POJO
     */
    public static List<User> fromJSON(JSONArray array) {
        List<User> list = new ArrayList<User>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = Json.getObject(array, i);
            if (o != null) {
                list.add(User.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code User}
     * @return A {@link User}, or {@code null} if {@code object} is {@code null}
     */
    public static User fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        SgnJson o = new SgnJson(object);
        User user = new User()
                .setId(o.getId())
                .setErn(o.getErn())
                .setGender(o.getGender())
                .setBirthYear(o.getBirthYear())
                .setName(o.getName())
                .setEmail(o.getEmail())
                .setPermissions(o.getPermissions());

        o.getStats().log(TAG);

        return user;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setId(getId())
                .setErn(getErn())
                .setGender(getGender())
                .setBirthYear(getBirthYear())
                .setName(getName())
                .setEmail(getEmail())
                .setPermissions(getPermissions())
                .toJSON();
    }

    /**
     * Method for finding out if the user is logged in via the API. It is determined
     * on the basis that the {@link #getEmail() email}  {@code !=} {@code null} and the
     * {@link User#getUserId()} {@code >} {@link #NO_USER}.
     *
     * <p>It is not a requirement to be logged in, but it does offer some
     * advantages, such as online lists</p>
     * @return {@code true} if the {@link User} is logged in with a ShopGun account, else {@code false}
     */
    public boolean isLoggedIn() {
        return mEmail != null && getUserId() > NO_USER;
    }

    /**
     * Get the id of this user
     * @deprecated Use {@link User#getUserId()}} instead.
     */
    @Deprecated
    public String getId() {
        return String.valueOf(getUserId());
    }

    /**
     * @param id A string
     * @deprecated Use {@link User#setUserId(int)} instead.
     */
    @Deprecated
    public User setId(String id) {
        setUserId((id == null) ? NO_USER : Integer.valueOf(id));
        return this;
    }

    public String getErn() {
        return mErn;
    }

    public User setErn(String ern) {
        if (ern == null) {
            setUserId(NO_USER);
        } else if (ern.startsWith("ern:") && ern.split(":").length == 3 && ern.contains(getErnType())) {
            mErn = ern;
        }
        return this;
    }

    public String getErnType() {
        return IErn.TYPE_USER;
    }

    /**
     * Get the user id
     * @return A user id
     */
    public int getUserId() {
        if (mErn == null) {
            setUserId(NO_USER);
        }
        String[] parts = mErn.split(":");
        return Integer.valueOf(parts[parts.length - 1]);
    }

    /**
     * Set the user id
     * @param id A positive integer
     * @return This object
     */
    public User setUserId(int id) {
        setErn(String.format("ern:%s:%s", getErnType(), id));
        return this;
    }

    /**
     * Get the gender of this user. Gender can be either:
     *
     * <ul>
     *     <li>"male" for a male user</li>
     *     <li>"female" for a female user</li>
     *     <li>{@code null} if gender is unknown</li>
     * </ul>
     *
     * @return The gender, or {@code null}
     */
    public String getGender() {
        return mGender;
    }

    /**
     * Set the gender of the user. Gender can be set to:
     * <ul>
     *     <li>"male" for a male user</li>
     *     <li>"female" for a female user</li>
     * </ul>
     *
     * It is not allowed to 'reset' gender, to unknown by passing {@code null} as argument
     *
     * @param gender Either 'male' or 'female'
     * @return This object
     */
    public User setGender(String gender) {
        if (gender != null) {
            gender = gender.toLowerCase();
            if (gender.equals("male") || gender.equals("female")) {
                mGender = gender;
            }
        }
        return this;
    }

    /**
     * Get the birth year of the user
     * @return The birth year of the user
     */
    public int getBirthYear() {
        return mBirthYear;
    }

    /**
     * Set the birth year of the user.
     * <p>Not setting a birth year is preferred, over setting a fake birth year.</p>
     * @param birthYear An {@link Integer}
     * @return This object
     */
    public User setBirthYear(int birthYear) {
        mBirthYear = birthYear;
        return this;
    }

    /**
     * Get the name of the user.
     * @return A name, or {@code null}
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the name of a user.
     * @param name A non-{@code null} {@link String}
     * @return This object
     */
    public User setName(String name) {
        if (name != null) {
            mName = name;
        }
        return this;
    }

    /**
     * Get the e-mail address of the user.
     * @return An email, or {@code null}
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Set the email of the user.
     * @param email A non-{@code null} {@link String}
     * @return This object
     */
    public User setEmail(String email) {
        mEmail = email;
        return this;
    }

    /**
     * Get this users {@link Permission}. Permissions determine what access levelse the user has in the API.
     * @return A set of permissions, or {@code null}
     */
    public Permission getPermissions() {
        return mPermissions;
    }

    /**
     * Set {@link Permission}s for this user.
     *
     * <p>Note that, permissions isn't decided client-side, but should rather be handled by the API/SDK.</p>
     * @param permissions The new set of permissions
     * @return This object
     */
    public User setPermissions(Permission permissions) {
        mPermissions = permissions;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mBirthYear;
        result = prime * result + ((mEmail == null) ? 0 : mEmail.hashCode());
        result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
        result = prime * result + ((mGender == null) ? 0 : mGender.hashCode());
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        result = prime * result
                + ((mPermissions == null) ? 0 : mPermissions.hashCode());
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
        User other = (User) obj;
        if (mBirthYear != other.mBirthYear)
            return false;
        if (mEmail == null) {
            if (other.mEmail != null)
                return false;
        } else if (!mEmail.equals(other.mEmail))
            return false;
        if (mErn == null) {
            if (other.mErn != null)
                return false;
        } else if (!mErn.equals(other.mErn))
            return false;
        if (mGender == null) {
            if (other.mGender != null)
                return false;
        } else if (!mGender.equals(other.mGender))
            return false;
        if (mName == null) {
            if (other.mName != null)
                return false;
        } else if (!mName.equals(other.mName))
            return false;
        if (mPermissions == null) {
            if (other.mPermissions != null)
                return false;
        } else if (!mPermissions.equals(other.mPermissions))
            return false;
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mErn);
        dest.writeString(this.mGender);
        dest.writeInt(this.mBirthYear);
        dest.writeString(this.mName);
        dest.writeString(this.mEmail);
        dest.writeParcelable(this.mPermissions, flags);
    }

}
