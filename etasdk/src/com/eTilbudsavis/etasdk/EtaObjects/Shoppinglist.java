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

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Shoppinglist extends EtaListObject< Shoppinglist> {

	public static final String TAG = "Shoppinglist";
	
	private static final long serialVersionUID = 5718447151312028262L;
	
	public static final String TYPE_SHOPPING_LIST = null;
	public static final String TYPE_WISH_LIST = "wish_list";
	
	public static final String ACCESS_PRIVATE = "private";
	public static final String ACCESS_SHARED = "shared";
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

	public static Shoppinglist fromName(String name) {
		Shoppinglist sl = new Shoppinglist();
		sl.setName(name);
		return sl;
	}
	
	public void set(JSONObject shoppinglist) {
		fromJSON(this, shoppinglist);
	}
	
	public static ArrayList<Shoppinglist> fromJSON(JSONArray shoppinglists) {
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		
		try {
			for (int i = 0 ; i < shoppinglists.length() ; i++ ) {
				Shoppinglist s = Shoppinglist.fromJSON(shoppinglists.getJSONObject(i));
				list.add(s);
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}
	
	public static Shoppinglist fromJSON(JSONObject shoppinglist) {
		return fromJSON(new Shoppinglist(), shoppinglist);
	}
	
	private static Shoppinglist fromJSON(Shoppinglist sl, JSONObject jSl) {
		
		try {
			// We've don't use OWNER anymore, since we now get all share info.
			sl.setId(Json.valueOf(jSl, ServerKey.ID));
			sl.setErn(Json.valueOf(jSl, ServerKey.ERN));
			sl.setName(Json.valueOf(jSl, ServerKey.NAME));
			sl.setAccess(Json.valueOf(jSl, ServerKey.ACCESS));
			sl.setModified(Json.valueOf(jSl, ServerKey.MODIFIED));
			sl.setPreviousId(Json.valueOf(jSl, ServerKey.PREVIOUS_ID));
			sl.setType(Json.valueOf(jSl, ServerKey.TYPE));
			
			// A whole lot of 'saving my ass from exceptions' for meta
			String metaString = Json.valueOf(jSl, ServerKey.META, "{}").trim();
			// If it looks like a JSONObject, try parsing it
			if (metaString.startsWith("{") && metaString.endsWith("}")) {
				
				try {
					// Try to parse the json string
					sl.setMeta(new JSONObject(metaString));
				} catch (JSONException e) {
					EtaLog.d(TAG, e);
					// Meta parsing failed, so we'll do a recovery
					sl.setMeta(new JSONObject());
					sl.setModified(new Date());
				}
				
			} else {
				// String doesn't look like json, so we'll do a recovery
				sl.setMeta(new JSONObject());
				sl.setModified(new Date());
			}
			
			sl.putShares(Share.fromJSON(jSl.getJSONArray(ServerKey.SHARES)));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
		return sl;
	}

	@Override
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Shoppinglist s) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.ID, Json.nullCheck(s.getId()));
			o.put(ServerKey.ERN, Json.nullCheck(s.getErn()));
			o.put(ServerKey.NAME, Json.nullCheck(s.getName()));
			o.put(ServerKey.ACCESS, Json.nullCheck(s.getAccess()));
			o.put(ServerKey.MODIFIED, Json.nullCheck(Utils.parseDate(s.getModified())));
			o.put(ServerKey.PREVIOUS_ID, Json.nullCheck(s.getPreviousId()));
			o.put(ServerKey.TYPE, Json.nullCheck(s.getType()));
			o.put(ServerKey.META, Json.nullCheck(s.getMeta()));
			JSONArray shares = new JSONArray();
			for (Share share : s.getShares().values()) {
				shares.put(share.toJSON());
			}
			o.put(ServerKey.SHARES, shares);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}

	@Override
	public String getErnPrefix() {
		return ERN_SHOPPINGLIST;
	}
	
	public String getName() {
		return mName;
	}

	public Shoppinglist setName(String name) {
		mName = name;
		return this;
	}

	public String getAccess() {
		return mAccess;
	}

	public Shoppinglist setAccess(String access) {
		mAccess = access;
		return this;
	}

	public Date getModified() {
		return mModified;
	}

	public Shoppinglist setModified(Date time) {
		mModified = Utils.roundTime(time);
		return this;
	}
	
	public String getPreviousId() {
		return mPrevId;
	}

	public Shoppinglist setPreviousId(String id) {
		mPrevId = id;
		return this;
	}

	public String getType() {
		return mType;
	}

	public Shoppinglist setType(String type) {
		if (type == null || type.equals("")) {
			// it s a shoppinglist
			mType = TYPE_SHOPPING_LIST;
		} else {
			mType = type;
		}
		return this;
	}

	public Map<String, Share> getShares() {
		return mShares;
	}
	
	public Shoppinglist setShares(List<Share> shares) {
		mShares.clear();
		for (Share s : shares) {
			s.setShoppinglistId(getId());
			mShares.put(s.getEmail(), s);
		}
		return this;
	}
	
	public Shoppinglist putShares(List<Share> shares) {
		if (shares == null) return this;
		
		for (Share s : shares) {
			s.setShoppinglistId(getId());
			mShares.put(s.getEmail(), s);
		}
		return this;
	}
	
	public Shoppinglist putShare(Share s) {
		if (s == null) return this;
		s.setShoppinglistId(getId());
		mShares.put(s.getEmail(), s);
		return this;
	}
	
	public Shoppinglist removeShare(Share s) {
		if (s == null) return this;
		mShares.remove(s.getEmail());
		return this;
	}
	
	public JSONObject getMeta() {
		if (mMeta == null) {
			return new JSONObject();
		} else {
			try {
				return new JSONObject(mMeta);
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
		}
		return new JSONObject();
	}
	
	public Shoppinglist setMeta(JSONObject meta) {
		mMeta = meta == null ? "{}" : meta.toString();
		return this;
	}

	public Shoppinglist setModified(String time) {
		mModified = Utils.parseDate(time);
		return this;
	}
	
	public Share getOwner() {
		for (Share s : mShares.values()) {
			if (s.getAccess().equals(Share.ACCESS_OWNER))
				return s;
		}
		return null;
	}
	
	public int getUserId() {
		return mUserId;
	}

	public Shoppinglist setUserId(int userId) {
		mUserId = userId;
		return this;
	}

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

