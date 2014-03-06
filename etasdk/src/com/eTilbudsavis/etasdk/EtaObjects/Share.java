package com.eTilbudsavis.etasdk.EtaObjects;

import java.util.ArrayList;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Share extends EtaListObject<Share> {

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
			EtaLog.d(TAG, e);
		}
		return list;
	}
	
	public static Share fromJSON(JSONObject share) {
		Share s = new Share();
		return fromJSON(s, share);
	}
	
	public static Share fromJSON(Share s, JSONObject share) {
		
		try {
			
			JSONObject o = share.getJSONObject(ServerKey.USER);
			s.setEmail(Json.valueOf(o, ServerKey.EMAIL));
			s.setName(Json.valueOf(o, ServerKey.NAME));
			
			s.setAccess(Json.valueOf(share, ServerKey.ACCESS));
			s.setAccepted(Json.valueOf(share, ServerKey.ACCEPTED, false));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
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
			EtaLog.d(TAG, e);
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
	
	public String getEmail() {
		return mEmail;
	}
	
	public Share setEmail(String email) {
		mEmail = email;
		return this;
	}

	public String getName() {
		return mName;
	}
	
	public Share setName(String name) {
		mName = name;
		return this;
	}

	public String getAccess() {
		return mAccess;
	}

	/**
	 * 
	 * @param readwrite true for 'read and write' access, false for 'read only'
	 * @return this share
	 */
	public Share setAccess(String permission) {
		mAccess = permission;
		return Share.this;
	}

	public boolean getAccepted() {
		return mAccepted;
	}
	
	public Share setAccepted(boolean accepted) {
		mAccepted = accepted;
		return this;
	}
	
	public String getAcceptUrl() {
		return mAcceptUrl;
	}
	
	public String getShoppinglistId() {
		return mShoppinglistId;
	}
	
	public Share setShoppinglistId(String id) {
		mShoppinglistId = id;
		return this;
	}
	
	public int compareTo(Share another) {
		return 0;
	}
	
	public static Comparator<Share> EmailComparator  = new Comparator<Share>() {
		
		public int compare(Share item1, Share item2) {

			String itemName1 = item1.getEmail().toUpperCase();
			String itemName2 = item2.getEmail().toUpperCase();
			
			//ascending order
			return itemName1.compareTo(itemName2);
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

