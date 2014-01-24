package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Share extends EtaObject implements Comparable<Share>, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String ACCESS_OWNER = "owner";
	public static final String ACCESS_READWRITE = "rw";
	public static final String ACCESS_READONLY = "r";
	
	public static final String TAG = "Share";

	/** States a shoppping list can be in */
	public interface State {
		int TO_SYNC	= 0;
		int SYNCING	= 1;
		int SYNCED	= 2;
		int DELETE	= 4;
		int ERROR	= 5;
	}
	
	private String mName;
	private String mEmail;
	private String mAccess;
	private String mShoppinglistId;
	private boolean mAccepted;
	private String mAcceptUrl;
	private int mState = State.SYNCED;
	
	public Share(String email, String access, String acceptUrl) {
		mEmail = email;
		mAccess = access;
		mAcceptUrl = acceptUrl;
		mState = State.TO_SYNC;
		mName = email;
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
			s.setEmail(jsonToString(o, ServerKey.EMAIL));
			s.setName(jsonToString(o, ServerKey.NAME));
			
			s.setAccess(jsonToString(share, ServerKey.ACCESS));
			s.setAccepted(jsonToBoolean(share, ServerKey.ACCEPTED, false));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
		return s;
	}
	
	public JSONObject toJSON() {
		return toJSON(this); 
	}

	public static JSONObject toJSON(Share s) {
		JSONObject o = new JSONObject();
		try {
			
			JSONObject user = new JSONObject();
			user.put(ServerKey.EMAIL, s.getEmail());
			user.put(ServerKey.NAME, s.getName());
			
			o.put(ServerKey.USER, user);
			o.put(ServerKey.ACCEPTED, s.getAccepted());
			o.put(ServerKey.ACCESS, s.getAccess());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
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
	
	public int getState() {
		return mState;
	}
	
	public Share setState(int state) {
		if (State.TO_SYNC <= state && state <= State.ERROR)
			mState = state;
		return this;
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
		result = prime * result + mState;
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
		if (mState != other.mState)
			return false;
		return true;
	}
	
	
	
}

