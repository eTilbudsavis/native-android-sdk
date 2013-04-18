package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

public class Share implements Comparable<Share>, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String mUser;
	private SharePermission mAccess;
	private Boolean mAccepted;

	public enum SharePermission {
		OWNER { public String toString() { return "owner"; }},
		R { public String toString() { return "r"; }},
		RW { public String toString() { return "rw"; }},
	}
	
	public Share(JSONObject share) {
		try {
			mUser = share.getString("user");
			mAccess = SharePermission.valueOf(share.getString("access").toUpperCase());
			mAccepted = share.getString("accepted").equals("true") ? true : false;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new Share object
	 * @param user the e-mail address of the share
	 * @param readwrite true for 'read and write' access, false for 'read only'
	 */
	public Share(String user, SharePermission permission) {
		mUser = user;
		mAccess = permission;
		mAccepted = false;
	}

	public String getUser() {
		return mUser;
	}

	public SharePermission getAccess() {
		return mAccess;
	}

	/**
	 * 
	 * @param readwrite true for 'read and write' access, false for 'read only'
	 * @return this share
	 */
	public Share setAccess(SharePermission permission) {
		mAccess = permission;
		return Share.this;
	}

	public Boolean getAccepted() {
		return mAccepted;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Share))
			return false;

		Share share = (Share)o;
		return share.getAccepted().equals(mAccepted) &&
				share.getAccess().equals(mAccess) &&
				share.getUser().equals(mUser);
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("Share { ")
		.append("User: ").append(mUser)
		.append(", Access: ").append(mAccess.toString())
		.append(", Accepted: ").append(mAccepted)
		.append(" }").toString();
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

