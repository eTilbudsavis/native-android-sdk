package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

public class Share extends EtaObject implements Comparable<Share>, Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String ACCESS_OWNER = "owner";
	public static final String ACCESS_READWRITE = "rw";
	public static final String ACCESS_READONLY = "r";
	
	public static final String TAG = "Share";
	
	private String mUser;
	private String mAccess;
	private boolean mAccepted;

	public Share() { }

	public static Share fromJSON(JSONObject share) {
		return fromJSON(new Share(), share);
	}
	
	public static Share fromJSON(Share s, JSONObject share) {
		try {
			s.setUser(share.getString(S_USER));
			s.setAccess(share.getString(S_ACCESS));
			s.setAccepted(share.getBoolean(S_ACCEPTED));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return s;
	}
	
	/**
	 * Create a new Share object
	 * @param user the e-mail address of the share
	 * @param readwrite true for 'read and write' access, false for 'read only'
	 */
	public Share(String user, String permission) {
		mUser = user;
		mAccess = permission;
		mAccepted = false;
	}

	public String getUser() {
		return mUser;
	}
	
	public Share setUser(String user) {
		mUser = user;
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
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Share))
			return false;

		Share share = (Share)o;
		return share.getAccepted() == mAccepted &&
				share.getAccess().equals(mAccess) &&
				share.getUser().equals(mUser);
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("user=").append(mUser)
		.append(", access=").append(mAccess)
		.append(", accepted=").append(mAccepted)
		.append("]").toString();
	}

	public int compareTo(Share another) {
		return 0;
	}

	public static Comparator<Share> UserComparator  = new Comparator<Share>() {

		public int compare(Share item1, Share item2) {

			String itemName1 = item1.getUser().toUpperCase();
			String itemName2 = item2.getUser().toUpperCase();

			//ascending order
			return itemName1.compareTo(itemName2);

			//descending order
			//return itemName2.compareTo(itemName1);
		}

	};

}

