/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Permission;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;


/**
 * <p>This class is a representation of a user as the API v2 exposes it</p>
 * 
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/users.html">User Reference</a>
 * documentation, on the engineering blog.
 * </p>
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class User extends ErnObject<User> implements EtaObject<JSONObject>, Serializable {
	
	public static final String TAG = User.class.getSimpleName();
	
	private static final long serialVersionUID = 1L;

	private static final String ERN_CLASS = "user";
	
	public static final int NO_USER = -1;
	
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
	
	/**
	 * A factory method for converting {@link JSONObject} into a POJO.
	 * @param user A {@link JSONObject} in the format of a valid API v2 user response
	 * @return A User object
	 */
	public static User fromJSON(JSONObject jUser) {
		User user = new User();
		if (jUser == null) return user;
		
		try {
			user.setUserId(Json.valueOf(jUser, JsonKey.ID, User.NO_USER));
			user.setErn(Json.valueOf(jUser, JsonKey.ERN));
			user.setGender(Json.valueOf(jUser, JsonKey.GENDER));
			user.setBirthYear(Json.valueOf(jUser, JsonKey.BIRTH_YEAR, 0));
			user.setName(Json.valueOf(jUser, JsonKey.NAME));
			user.setEmail(Json.valueOf(jUser, JsonKey.EMAIL));
			user.setPermissions(Permission.fromJSON(jUser.getJSONObject(JsonKey.PERMISSIONS)));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return user;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.GENDER, Json.nullCheck(getGender()));
			o.put(JsonKey.BIRTH_YEAR, Json.nullCheck(getBirthYear()));
			o.put(JsonKey.NAME, Json.nullCheck(getName()));
			o.put(JsonKey.EMAIL, Json.nullCheck(getEmail()));
			o.put(JsonKey.PERMISSIONS, Json.toJson(getPermissions()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	@Override
	String getErnClass() {
		return ERN_CLASS;
	}
	
	/**
	 * Method for finding out if the user is logged in via the API. It is determined
	 * on the basis that the {@link #getEmail() email} != null and the
	 * {@link #getUserId() user id} > {@link #NO_USER -1}.
	 * 
	 * <p>It is not a requirement to be logged in, but it does offer some
	 * advantages, such as online lists</p> 
	 * @return
	 */
	public boolean isLoggedIn() {
		return mEmail != null && getUserId() > NO_USER;
	}
	
	/**
	 * Method returns the user id as a String.
	 * <p>This method is inherited from </p>
	 * @deprecated Use {@link #getUserId()} to get the id an an {@link Integer}
	 */
	@Override
	public String getId() {
		return String.valueOf(super.getId());
	}
	
	/**
     * This method is not supported and throws an UnsupportedOperationException when called.
     * <p>To set the user id, use {@link #setUserId(int)} instead</p>
     * @see #setUserId(int)
	 * @param id Ignored
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public User setId(String id) {
		throw new UnsupportedOperationException("Share does not support setId(String)");
	}
	
	/**
	 * Get the user id
	 * @return A user id
	 */
	public int getUserId() {
		return Integer.valueOf(super.getId());
	}
	
	/**
	 * Set the user id
	 * @param id A positive integer
	 * @return This object
	 */
	public User setUserId(int id) {
		super.setId(String.valueOf(id));
		return this;
	}
	
	/**
	 * Get the gender of this user. Gender can be either:
	 * 
	 * <table border=1>
	 * <tr>
	 * <td>male</td>
	 * 		<td>{@link String} "male"</td>
	 * </tr>
	 * <tr>
	 * 		<td>female</td>
	 * 		<td>{@link String} "female"</td>
	 * </tr>
	 * <tr>
	 * 		<td>unknown</td>
	 * 		<td><code>null</code></td>
	 * </tr>
	 * </table>
	 * 
	 * @return The gender, or <code>null</code>
	 */
	public String getGender() {
		return mGender;
	}

	/**
	 * Set the gender of the user. Gender can be set to:
	 * <table border=1>
	 * <tr>
	 * <td>male</td>
	 * 		<td>{@link String} "male"</td>
	 * </tr>
	 * <tr>
	 * 		<td>female</td>
	 * 		<td>{@link String} "female"</td>
	 * </tr>
	 * </table>
	 * 
	 * It is not allowed to 'reset' gender, to unknown by passing <code>null</code> as argument
	 * 
	 * @param gender of either male or female
	 * @return This object
	 */
	public User setGender(String gender) {
		if (gender != null) {
			gender = gender.toLowerCase();
			if (gender.equals("male") || gender.equals("female") ) {
				mGender = gender;
			}
		}		
		return this;
	}
	
	/**
	 * Get the birth year of the user
	 * @return Birth year as an {@link Integer}
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
	 * @return A name, or <code>null</code>
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * Set the name of a user.
	 * @param name A non-<code>null</code> {@link String} 
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
	 * @return An email, or <code>null</code>
	 */
	public String getEmail() {
		return mEmail;
	}
	
	/**
	 * Set the email of the user.
	 * @param email A non-<code>null</code> {@link String} 
	 * @return This object
	 */
	public User setEmail(String email) {
		mEmail = email;
		return this;
	}
	
	/**
	 * Get this users {@link Permission}. Permissions determine what access levelse the user has in the API.
	 * @return A set of permissions, or <code>null</code>
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
		int result = super.hashCode();
		result = prime * result + mBirthYear;
		result = prime * result + ((mEmail == null) ? 0 : mEmail.hashCode());
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
		if (!super.equals(obj))
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

}
