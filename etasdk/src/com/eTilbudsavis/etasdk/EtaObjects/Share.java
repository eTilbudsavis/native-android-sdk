package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

public class Share extends EtaObject implements Comparable<Share>, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String ACCESS_OWNER = "owner";
	public static final String ACCESS_READWRITE = "rw";
	public static final String ACCESS_READONLY = "r";
	
	public static final String TAG = "Share";
	
	private String mName;
	private String mEmail;
	private String mAccess;
	private boolean mAccepted;

	public Share() { }

	@SuppressWarnings("unchecked")
	public static ArrayList<Share> fromJSON(JSONArray shares) {
		ArrayList<Share> list = new ArrayList<Share>();
		try {
			for (int i = 0 ; i < shares.length() ; i++ )
				list.add(Share.fromJSON((JSONObject)shares.get(i)));
			
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static Share fromJSON(JSONObject share) {
		return fromJSON(new Share(), share);
	}
	
	public static Share fromJSON(Share s, JSONObject share) {
		
		try {
			String email = getJsonString(share, S_USER);
			
			if (email == null)
				return s;
			
			if (email.startsWith("{") && email.endsWith("}")) {
				JSONObject o = new JSONObject(email);
				s.setEmail(getJsonString(o, S_EMAIL));
				s.setName(getJsonString(o, S_NAME));
			} else {
				s.setEmail(email);
			}
			s.setAccess(getJsonString(share, S_ACCESS));
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
	public Share(String email, String permission) {
		mEmail = email;
		mAccess = permission;
		mAccepted = false;
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
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Share))
			return false;

		Share share = (Share)o;
		return share.getAccepted() == mAccepted &&
				stringCompare(share.getAccess(), mAccess) &&
				stringCompare(share.getEmail(), mEmail) &&
				stringCompare(share.getName(), mName);
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("email=").append(mEmail)
		.append(", name=").append(mName)
		.append(", access=").append(mAccess)
		.append(", accepted=").append(mAccepted)
		.append("]").toString();
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

}

