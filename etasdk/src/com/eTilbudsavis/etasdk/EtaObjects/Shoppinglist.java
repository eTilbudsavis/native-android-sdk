package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Shoppinglist implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String mId;
	private String mName;
	private String mAccess;
	private HashMap<String, Share> mHashShares = new HashMap<String, Share>();
	private double mModified = Double.valueOf(1);
	private HashMap<String, ShoppinglistItem> mHashItems = new HashMap<String, ShoppinglistItem>();
	private Boolean mWaitingForInitUpdate = true;

	public Shoppinglist(JSONObject shoppinglist) {
		try {
			mId = shoppinglist.getString("id");
			mName = shoppinglist.getString("name");
			mAccess = shoppinglist.getString("access");
			JSONArray jArray = shoppinglist.getJSONArray("shares");
			for (int i = 0; i < jArray.length(); i++) {
				Share s = new Share((JSONObject)jArray.get(i));
				mHashShares.put(s.getUser(), s);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void update(JSONObject shoppinglist) {
		try {
			mId = shoppinglist.getString("id");
			mName = shoppinglist.getString("name");
			mAccess = shoppinglist.getString("access");
			JSONArray jArray = shoppinglist.getJSONArray("shares");
			for (int i = 0; i < jArray.length(); i++) {
				Share s = new Share((JSONObject)jArray.get(i));
				mHashShares.put(s.getUser(), s);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public Shoppinglist setName(String name) {
		mName = name.trim();
		return this;
	}

	public String getAccess() {
		return mAccess;
	}

	public Shoppinglist setShare(Share share) {
		mHashShares.put(share.getUser(), share);
		return this;
	}

	public HashMap<String, Share> getShares() {
		return mHashShares;
	}

	public void putShare(Share share) {
		mHashShares.put(share.getUser(), share);
	}
	
	/**
	 * Add a new list of shares to this shoppinglist.
	 * Old list of shares will be removed.
	 * @param shares to put into shoppinglist
	 */
	public void putShares(HashMap<String, Share> shares) {
		mHashShares.clear();
		mHashShares.putAll(shares);
	}
	
	public Shoppinglist setModified(Double modified) {
		// Convert server time to milliseconds
		mModified = modified*1000;
		return this;
	}
	
	public Double getModified() {
		return mModified;
	}

	public void addItem(ShoppinglistItem shoppinglistItem) {
		mHashItems.put(shoppinglistItem.getId(), shoppinglistItem);
	}

	public Boolean removeItem(ShoppinglistItem shoppinglistItem) {
		return  mHashItems.remove(shoppinglistItem.getId()) == null ? false : true;
	}

	public Boolean removeShare(Share share) {
		return  mHashShares.remove(share.getUser()) == null ? false : true;
	}
	
	public HashMap<String, ShoppinglistItem> getShoppinglistItems() {
		return mHashItems;
	}

	public void waitingForInitUpdate(Boolean value) {
		mWaitingForInitUpdate = value;
	}

	public Boolean waitingForInitUpdate() {
		return mWaitingForInitUpdate;
	}
	
	public Boolean compareShares(HashMap<String, Share> shares) {
		if (!mHashShares.keySet().containsAll(shares.keySet()) && !shares.keySet().containsAll(mHashShares.keySet()))
			return false;
		
		for(String key : shares.keySet())
			if (!mHashShares.get(key).equals(shares.get(key)))
				return false;
		
		return true;
	}
	
	/**
	 * We are not comparing the modified field, as this field does not
	 * update the same as 
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Shoppinglist))
			return false;

		Shoppinglist sl = (Shoppinglist)o;
		return mId.equals(sl.getId()) &&
				mAccess.equals(sl.getAccess()) &&
				mName.equals(sl.getName()) &&
				this.compareShares(sl.getShares());
	}

	
}

