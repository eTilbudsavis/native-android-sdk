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
import java.util.ArrayList;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Share extends EtaListObject<Share> implements Serializable {
	
	public static final String TAG = "Share";
	
	private static final long serialVersionUID = -9184865445908448266L;
	
	public static final String ACCESS_OWNER = "owner";
	public static final String ACCESS_READWRITE = "rw";
	public static final String ACCESS_READONLY = "r";
	
	private String mName;
	private String mEmail;
	private String mAccess;
	private String mShoppinglistId;
	private boolean mAccepted;
	private String mAcceptUrl;
	
	public Share(String email, String access, String acceptUrl) {
		mName = email;
		mEmail = email;
		mAccess = access;
		mAcceptUrl = acceptUrl;
	}
	
	private Share() { }
	
	public static ArrayList<Share> fromJSON(JSONArray shares) {
		ArrayList<Share> list = new ArrayList<Share>();
		try {
			for (int i = 0 ; i < shares.length() ; i++ ) {
				Share s = Share.fromJSON(shares.getJSONObject(i));
				list.add(s);
			}
			
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return list;
	}
	
	public static Share fromJSON(JSONObject share) {
		return fromJSON(new Share(), share);
	}
	
	public static Share fromJSON(Share s, JSONObject share) {
		
		try {
			
			JSONObject o = share.getJSONObject(ServerKey.USER);
			s.setEmail(Json.valueOf(o, ServerKey.EMAIL));
			s.setName(Json.valueOf(o, ServerKey.NAME));
			
			s.setAccess(Json.valueOf(share, ServerKey.ACCESS));
			s.setAccepted(Json.valueOf(share, ServerKey.ACCEPTED, false));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		
		return s;
	}

	@Override
	public JSONObject toJSON() {
		
		/* Do not call super class to create JSON, as we server does not support
		 * id and ern keys.
		 */
		
		JSONObject o = new JSONObject();
		try {
			JSONObject user = new JSONObject();
			user.put(ServerKey.EMAIL, Json.nullCheck(getEmail()));
			user.put(ServerKey.NAME, Json.nullCheck(getName()));
			
			o.put(ServerKey.USER, Json.nullCheck(user));
			o.put(ServerKey.ACCEPTED, Json.nullCheck(getAccepted()));
			o.put(ServerKey.ACCESS, Json.nullCheck(getAccess()));
			if (getAcceptUrl() != null) {
				o.put(ServerKey.ACCEPT_URL, getAcceptUrl());
			}
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	@Override
	public String getErnPrefix() {
		return ERN_SHARE;
	}
	
	/**
     * This method is not supported and throws an UnsupportedOperationException when called.
	 * @param id Ignored
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public Share setId(String id) {
		throw new UnsupportedOperationException("Share does not support setId(String)");
	}

	/**
     * This method is not supported and throws an UnsupportedOperationException when called.
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public String getId() {
		throw new UnsupportedOperationException("Share does not support getId()");
	}

	/**
     * This method is not supported and throws an UnsupportedOperationException when called.
	 * @param id Ignored
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public Share setErn(String ern) {
		throw new UnsupportedOperationException("Share does not support setErn(String)");
	}

	/**
     * This method is not supported and throws an UnsupportedOperationException when called.
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public String getErn() {
		throw new UnsupportedOperationException("Share does not support getErn()");
	}
	
	/**
	 * Get the e-mail of this share
	 * @return An e-mail, or {@code null}
	 */
	public String getEmail() {
		return mEmail;
	}
	
	/**
	 * Set the e-mail of this share
	 * @param email An e-mail, not {@code null}
	 * @return This object
	 */
	public Share setEmail(String email) {
		if (email != null) {
			mEmail = email;
		}
		return this;
	}
	
	/**
	 * Get the name of this share
	 * @return A name, or {@code null}
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * Set the name of this share
	 * @param name A name, not {@code null}
	 * @return
	 */
	public Share setName(String name) {
		if (name != null) {
			mName = name;
		}
		return this;
	}
	
	/**
	 * Get the access parmission for this share
	 * <p>Current valid access permissions are {@link Share#ACCESS_OWNER},
	 * {@link Share#ACCESS_READONLY}, and {@link Share#ACCESS_READWRITE}.
	 * Other strings will be ignored by the API.</p>
	 * @return An access permission
	 */
	public String getAccess() {
		return mAccess;
	}

	/**
	 * Set the access level for this share.
	 * <p>Current valid access permissions are {@link Share#ACCESS_OWNER},
	 * {@link Share#ACCESS_READONLY}, and {@link Share#ACCESS_READWRITE}.
	 * Other strings will be ignored by the API.</p>
	 * @param permission An permission for this share
	 * @return This object
	 */
	public Share setAccess(String permission) {
		if (permission != null) {
			mAccess = permission;
		}
		return this;
	}
	
	/**
	 * Whether or not the share/user have accepted the invitation to the
	 * {@link Shoppinglist}, and are currently receiving updates about the
	 * {@link Shoppinglist}
	 * @return {@code true} if user have accepted, else {@code false}
	 */
	public boolean getAccepted() {
		return mAccepted;
	}
	
	/**
	 * Whether or not the share/user have accepted the invitation.
	 * <p>This shouldn't be handled by the app, or SDK, as this is something
	 * being determined by the API</p>
	 * @param accepted {@code true} if user have accepted the invitation, else {@code false}
	 * @return This object
	 */
	public Share setAccepted(boolean accepted) {
		mAccepted = accepted;
		return this;
	}
	
	/**
	 * Set the absolute URL to redirect the user to when he/she accepts to
	 * share the {@link Shoppinglist}.
	 * @param url The URL
	 * @return This object
	 */
	public Share setAcceptUrl(String url) {
		mAcceptUrl = url;
		return this;
	}
	
	/**
	 * Get the absolute URL to redirect the user to when he/she accepts to
	 * share the {@link Shoppinglist}.
	 * @return An URL
	 */
	public String getAcceptUrl() {
		return mAcceptUrl;
	}
	
	/**
	 * The {@link Shoppinglist#getId() id} of the shopping list associated with
	 * this share.
	 * @return The shopping list id, or {@code null}
	 */
	public String getShoppinglistId() {
		return mShoppinglistId;
	}
	
	/**
	 * Set the {@link Shoppinglist#getId() id} if the associated shoppinglist
	 * @param id An id of a {@link Shoppinglist}
	 * @return This object
	 */
	public Share setShoppinglistId(String id) {
		mShoppinglistId = id;
		return this;
	}
	
	public int compareTo(Share another) {
		return 0;
	}
	
	public static Comparator<Share> EmailAscending  = new Comparator<Share>() {
		
		@SuppressLint("DefaultLocale")
		public int compare(Share item1, Share item2) {
			
			String e1 = item1.getEmail().toLowerCase();
			String e2 = item2.getEmail().toLowerCase();
			
			//ascending order
			return e1.compareTo(e2);
			
		}
		
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mAcceptUrl == null) ? 0 : mAcceptUrl.hashCode());
		result = prime * result + (mAccepted ? 1231 : 1237);
		result = prime * result + ((mAccess == null) ? 0 : mAccess.hashCode());
		result = prime * result + ((mEmail == null) ? 0 : mEmail.hashCode());
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		result = prime * result
				+ ((mShoppinglistId == null) ? 0 : mShoppinglistId.hashCode());
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
		Share other = (Share) obj;
		if (mAcceptUrl == null) {
			if (other.mAcceptUrl != null)
				return false;
		} else if (!mAcceptUrl.equals(other.mAcceptUrl))
			return false;
		if (mAccepted != other.mAccepted)
			return false;
		if (mAccess == null) {
			if (other.mAccess != null)
				return false;
		} else if (!mAccess.equals(other.mAccess))
			return false;
		if (mEmail == null) {
			if (other.mEmail != null)
				return false;
		} else if (!mEmail.equals(other.mEmail))
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		if (mShoppinglistId == null) {
			if (other.mShoppinglistId != null)
				return false;
		} else if (!mShoppinglistId.equals(other.mShoppinglistId))
			return false;
		return true;
	}
	
}

