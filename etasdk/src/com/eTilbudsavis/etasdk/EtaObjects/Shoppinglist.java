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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.ListManager;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Utils;

/**
 * This class is a representation of the eTilbudsavis API v2 shopppinglist.
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class Shoppinglist extends EtaListObject< Shoppinglist> {

	public static final String TAG = "Shoppinglist";
	
	private static final long serialVersionUID = 5718447151312028262L;
	
	/**
	 * The default {@link Shoppinglist} type
	 */
	public static final String TYPE_SHOPPING_LIST = "";
	
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
	
	private String mName = "";
	private String mAccess = ACCESS_PRIVATE;
	private Date mModified;
	private String mPrevId;
	private String mType;
	private String mMeta;
	private Map<String, Share> mShares = new HashMap<String, Share>(1);
	private int mUserId = -1;
	
	private Shoppinglist() {
		setId(Utils.createUUID());
		mModified = Utils.roundTime(new Date());
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
	 * Convert a {@link JSONArray} into a {@link List}&lt;T&gt;.
	 * @param shoppinglists A {@link JSONArray} in the format of a valid API v2 shoppinglist response
	 * @return A {@link List} of POJO;
	 */
	public static ArrayList<Shoppinglist> fromJSON(JSONArray shoppinglists) {
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		
		try {
			for (int i = 0 ; i < shoppinglists.length() ; i++ ) {
				Shoppinglist s = Shoppinglist.fromJSON(shoppinglists.getJSONObject(i));
				list.add(s);
			}
		} catch (JSONException e) {
			EtaLog.e(TAG, e);
		}
		return list;
	}

	/**
	 * A factory method for converting {@link JSONObject} into a POJO.
	 * @param shoppinglist A {@link JSONObject} in the format of a valid API v
	 *                       shoppinglist response
	 * @return An {@link Shoppinglist} object
	 */
	public static Shoppinglist fromJSON(JSONObject shoppinglist) {
		return fromJSON(new Shoppinglist(), shoppinglist);
	}

	/**
	 * A factory method for converting {@link JSONObject} into POJO.
	 * <p>This method exposes a way, of updating/setting an objects properties</p>
	 * @param shoppinglist An object to set/update
	 * @param jShoppinglist A {@link JSONObject} in the format of a valid API v2 shoppinglist response
	 * @return A {@link List} of POJO
	 */
	public static Shoppinglist fromJSON(Shoppinglist shoppinglist, JSONObject jShoppinglist) {
		
		try {
			// We've don't use OWNER anymore, since we now get all share info.
			shoppinglist.setId(Json.valueOf(jShoppinglist, ServerKey.ID));
			shoppinglist.setErn(Json.valueOf(jShoppinglist, ServerKey.ERN));
			shoppinglist.setName(Json.valueOf(jShoppinglist, ServerKey.NAME));
			shoppinglist.setAccess(Json.valueOf(jShoppinglist, ServerKey.ACCESS));
			String modified = Json.valueOf(jShoppinglist, ServerKey.MODIFIED, Utils.DATE_EPOC);
			shoppinglist.setModified(Utils.parseDate(modified));
			shoppinglist.setPreviousId(Json.valueOf(jShoppinglist, ServerKey.PREVIOUS_ID));
			shoppinglist.setType(Json.valueOf(jShoppinglist, ServerKey.TYPE));
			
			// A whole lot of 'saving my ass from exceptions' for meta
			String metaString = Json.valueOf(jShoppinglist, ServerKey.META, "{}").trim();
			// If it looks like a JSONObject, try parsing it
			if (metaString.startsWith("{") && metaString.endsWith("}")) {
				
				try {
					// Try to parse the json string
					shoppinglist.setMeta(new JSONObject(metaString));
				} catch (JSONException e) {
					EtaLog.e(TAG, e);
					// Meta parsing failed, so we'll do a recovery
					shoppinglist.setMeta(new JSONObject());
					shoppinglist.setModified(new Date());
				}
				
			} else {
				// String doesn't look like json, so we'll do a recovery
				shoppinglist.setMeta(new JSONObject());
				shoppinglist.setModified(new Date());
			}
			
			shoppinglist.putShares(Share.fromJSON(jShoppinglist.getJSONArray(ServerKey.SHARES)));
		} catch (JSONException e) {
			EtaLog.e(TAG, e);
		}
		
		return shoppinglist;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = super.toJSON();
		try {
			o.put(ServerKey.NAME, Json.nullCheck(getName()));
			o.put(ServerKey.ACCESS, Json.nullCheck(getAccess()));
			o.put(ServerKey.MODIFIED, Json.nullCheck(Utils.parseDate(getModified())));
			o.put(ServerKey.PREVIOUS_ID, Json.nullCheck(getPreviousId()));
			o.put(ServerKey.TYPE, Json.nullCheck(getType()));
			o.put(ServerKey.META, Json.nullCheck(getMeta()));
			JSONArray shares = new JSONArray();
			for (Share share : getShares().values()) {
				shares.put(share.toJSON());
			}
			o.put(ServerKey.SHARES, shares);
		} catch (JSONException e) {
			EtaLog.e(TAG, e);
		}
		return o;
	}
	
	@Override
	public String getErnPrefix() {
		return ERN_SHOPPINGLIST;
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
	 * Set the access level for this {@link Shoppinglist}.
	 * <p>
	 * Current valid options are:
	 * <ul>
	 * <li> {@link #ACCESS_PRIVATE} </li>
	 * <li> {@link #ACCESS_SHARED} </li>
	 * <li> {@link #ACCESS_PUBLIC} </li>
	 * </ul>
	 * </p>
	 * @param access
	 * @return
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
		mModified = Utils.roundTime(time);
		return this;
	}
	
	/**
	 * Set the latest modified date for this {@link Shoppinglist}.
	 * 
	 * <p>If you are using the {@link ListManager} this will implicitly be
	 * handled for you.</p>
	 * @param time A date string for the latest edit to the list
	 * @return This object
	 */
	public Shoppinglist setModified(String time) {
		mModified = Utils.parseDate(time);
		return this;
	}
	
	/**
	 * Get the {@link Shoppinglist#getId() id} of the item that is 'above' or the
	 * previous element in the list of {@link Shoppinglist shoppinglists} for
	 * this user, when you display the lists to the user.
	 * 
	 * <p>If there are no {@link Shoppinglist} above this one the value will be
	 * {@link EtaListObject#FIRST_ITEM}</p>
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
	 * {@link EtaListObject#FIRST_ITEM}</p>
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
	 * Currently eTilbudsavis are using the two types below, but you are free to
	 * do other types too.
	 * <ul>
	 * <li> {@link Shoppinglist#TYPE_SHOPPING_LIST} </li>
	 * <li> {@link Shoppinglist#TYPE_WISH_LIST} </li>
	 * </ul>
	 * @return A type
	 */
	public String getType() {
		if (mType == null) {
			mType = TYPE_SHOPPING_LIST;
		}
		return mType;
	}
	

	/**
	 * Set the type of {@link Shoppinglist}
	 * 
	 * Currently eTilbudsavis are using the two types below, but you are free to
	 * do other types too.
	 * <ul>
	 * <li> {@link Shoppinglist#TYPE_SHOPPING_LIST} </li>
	 * <li> {@link Shoppinglist#TYPE_WISH_LIST} </li>
	 * </ul>
	 * @return This object
	 */
	public Shoppinglist setType(String type) {
		if (type == null) {
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
	public Map<String, Share> getShares() {
		return mShares;
	}
	
	/**
	 * Update the set of {@link Share shares} that may read/edit this {@link Shoppinglist}
	 * 
	 * <p>This method will <i>override</i> the current {@link Share shares} in
	 * the list. To append/edit the current set of shares use {@link #putShares(List)}</p>
	 * @param shares A new list of {@link Share shares}
	 * @return
	 */
	public Shoppinglist setShares(List<Share> shares) {
		mShares.clear();
		for (Share s : shares) {
			s.setShoppinglistId(getId());
			mShares.put(s.getEmail(), s);
		}
		return this;
	}
	
	/**
	 * Appends/edit the current list of {@link Share shares} that may read/write
	 * this {@link Shoppinglist}.
	 * @param shares A list of {@link Share shares} to append/edit
	 * @return This object
	 */
	public Shoppinglist putShares(List<Share> shares) {
		if (shares == null) return this;
		
		for (Share s : shares) {
			s.setShoppinglistId(getId());
			mShares.put(s.getEmail(), s);
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
		mShares.put(share.getEmail(), share);
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
		if (s == null) return this;
		mShares.remove(s.getEmail());
		return this;
	}
	
	/**
	 * Get the meta information for this {@link Shoppinglist}.
	 * 
	 * <p>If you edit the object, remember to use {@link #setMeta(JSONObject)}
	 * or your changes will not be saved to local DB.</p>
	 * 
	 * <p>Meta doesn't currently have any restrictions other that it must
	 * be a {@link JSONObject}, what goes inside is completely up to you.</p>
	 * 
	 * <p>What being said, please ensure not to accidentally override any keys
	 * that others may have set on the object. This can be done by name spacing
	 * your keys. Currently eTilbudsavis are using the <b>{@code eta_}</b> name space for
	 * our keys.</p>
	 * 
	 * @return A {@link JSONObject}
	 */
	public JSONObject getMeta() {
		if (mMeta == null) {
			return new JSONObject();
		} else {
			try {
				return new JSONObject(mMeta);
			} catch (JSONException e) {
				EtaLog.e(TAG, e);
			}
		}
		return new JSONObject();
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
	 * your keys. Currently eTilbudsavis are using the <b>{@code eta_}</b> name space for
	 * our keys.</p>
	 * 
	 * @param meta The new meta date to use
	 * @return This object
	 */
	public Shoppinglist setMeta(JSONObject meta) {
		mMeta = meta == null ? "{}" : meta.toString();
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
	public int getUserId() {
		return mUserId;
	}

	/**
	 * Set the id of the user, that this {@link Shoppinglist} is associated with.
	 * <p>This is mostly a use case when storing the item in a DB, where several
	 * users can have access to the same item (same {@link Shoppinglist#getId()}.</p>
	 * @param userId An id of a user
	 * @return This object
	 */
	public Shoppinglist setUserId(int userId) {
		mUserId = userId;
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

	/**
	 * Compare object, that uses {@link Shoppinglist#getName() name}
	 * to compare two lists.
	 */
	public static Comparator<Shoppinglist> NameComparator  = new Comparator<Shoppinglist>() {

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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mAccess == null) ? 0 : mAccess.hashCode());
		result = prime * result + ((mMeta == null) ? 0 : mMeta.hashCode());
		result = prime * result
				+ ((mModified == null) ? 0 : mModified.hashCode());
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		result = prime * result + ((mPrevId == null) ? 0 : mPrevId.hashCode());
		result = prime * result + ((mShares == null) ? 0 : mShares.hashCode());
		result = prime * result + ((mType == null) ? 0 : mType.hashCode());
		result = prime * result + mUserId;
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
		Shoppinglist other = (Shoppinglist) obj;
		if (mAccess == null) {
			if (other.mAccess != null)
				return false;
		} else if (!mAccess.equals(other.mAccess))
			return false;
		if (mMeta == null) {
			if (other.mMeta != null)
				return false;
		} else if (!mMeta.equals(other.mMeta))
			return false;
		if (mModified == null) {
			if (other.mModified != null)
				return false;
		} else if (!mModified.equals(other.mModified))
			return false;
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
		if (mType == null) {
			if (other.mType != null)
				return false;
		} else if (!mType.equals(other.mType))
			return false;
		if (mUserId != other.mUserId)
			return false;
		return true;
	}
	
}

