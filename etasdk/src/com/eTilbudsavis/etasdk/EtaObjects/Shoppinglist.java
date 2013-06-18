package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Tools.Utilities;

public class Shoppinglist implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PARAM_ID = "id";
	public static final String PARAM_ERN = "ern";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_ACCESS = "access";
	public static final String PARAM_MODIFIED = "modified";
	
	public static final String ACCESS_PRIVATE = "private";
	public static final String ACCESS_SHARED = "shared";
	public static final String ACCESS_PUBLIC = "public";

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.DATE_FORMAT);

	// server vars
	private String mId;
	private String mErn;
	private String mName;
	private String mAccess;
	private long mModified = 0L;
	
	// local vars
	private boolean mOffline = false;
	private boolean mSynced = false;
	private boolean mCurrent = false;
	
	private HashMap<String, Share> mShares = new HashMap<String, Share>();
	private HashMap<String, ShoppinglistItem> mItems = new HashMap<String, ShoppinglistItem>();

	public Shoppinglist() {
		setId(Utilities.createUUID());
		setModified(System.currentTimeMillis());
	}

	public static Shoppinglist fromName(String name) {
		Shoppinglist sl = new Shoppinglist();
		sl.setName(name);
		return sl;
	}
	
	public static ArrayList<Shoppinglist> fromJSONArray(String shoppinglists) {
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		try {
			list = fromJSONArray(new JSONArray(shoppinglists));
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		return list;
	}
	
	public static ArrayList<Shoppinglist> fromJSONArray(JSONArray shoppinglists) {
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		try {
			for (int i = 0 ; i < shoppinglists.length() ; i++ )
				list.add(Shoppinglist.fromJSON((JSONObject)shoppinglists.get(i)));
			
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		return list;
	}
	
	public static Shoppinglist fromJSON(String list) {
		Shoppinglist sl = new Shoppinglist();
		try {
			sl = fromJSON(sl, new JSONObject(list));
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		return sl;
	}

	public static Shoppinglist fromJSON(JSONObject list) {
		return fromJSON(new Shoppinglist(), list);
	}

	private static Shoppinglist fromJSON(Shoppinglist sl, JSONObject shoppinglist) {
		if (sl == null) sl = new Shoppinglist();
		if (shoppinglist == null ) return sl;
		
		try {
			sl.setId(shoppinglist.getString(PARAM_ID));
			sl.setErn(shoppinglist.getString(PARAM_ERN));
			sl.setName(shoppinglist.getString(PARAM_NAME));
			sl.setAccess(shoppinglist.getString(PARAM_ACCESS));
			sl.setModified(shoppinglist.getString(PARAM_MODIFIED));
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		
		return sl;
	}
	
	public Bundle getApiParams() {
		Bundle apiParams = new Bundle();
		apiParams.putString(PARAM_MODIFIED, getModifiedString());
		apiParams.putString(PARAM_NAME, getName());
		apiParams.putString(PARAM_ACCESS, getAccess());
		return apiParams;
	}
	
	public void set(JSONObject shoppinglist) {
		fromJSON(this, shoppinglist);
	}

	public String getId() {
		return mId;
	}

	public Shoppinglist setId(String id) {
		mId = id;
		return this;
	}

	public String getErn() {
		return mErn;
	}

	public Shoppinglist setErn(String ern) {
		mErn = ern;
		return this;
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

	public long getModified() {
		return mModified;
	}

	public String getModifiedString() {
		return sdf.format(new Date(mModified));
	}

	public Shoppinglist setModified(long time) {
		mModified = time;
		return this;
	}

	public boolean isSynced() {
		return mSynced;
	}

	public Shoppinglist setSynced(boolean synced) {
		mSynced = synced;
		return this;
	}

	public boolean isOffline() {
		return mOffline;
	}

	public Shoppinglist setOffline(boolean offline) {
		mOffline = offline;
		return this;
	}

	public boolean isCurrent() {
		return mCurrent;
	}

	public Shoppinglist setCurrent(boolean current) {
		mCurrent = current;
		return this;
	}

	public Shoppinglist setModified(String time) {
		try {
			mModified = sdf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return this;
	}

	public Shoppinglist setShare(Share share) {
		mShares.put(share.getUser(), share);
		return this;
	}

	public HashMap<String, Share> getShares() {
		return mShares;
	}

	public void putShare(Share share) {
		mShares.put(share.getUser(), share);
	}
	
	/**
	 * Add a new list of shares to this shoppinglist.
	 * Old list of shares will be removed.
	 * @param shares to put into shoppinglist
	 */
	public void putShares(HashMap<String, Share> shares) {
		mShares.clear();
		mShares.putAll(shares);
	}
	
	public void addItem(ShoppinglistItem shoppinglistItem) {
		mItems.put(shoppinglistItem.getId(), shoppinglistItem);
	}

	public Boolean removeItem(ShoppinglistItem shoppinglistItem) {
		return  mItems.remove(shoppinglistItem.getId()) == null ? false : true;
	}

	public Boolean removeShare(Share share) {
		return  mShares.remove(share.getUser()) == null ? false : true;
	}
	
	public HashMap<String, ShoppinglistItem> getShoppinglistItems() {
		return mItems;
	}

	public Boolean compareShares(HashMap<String, Share> shares) {
		if (!mShares.keySet().containsAll(shares.keySet()) && !shares.keySet().containsAll(mShares.keySet()))
			return false;
		
		for(String key : shares.keySet())
			if (!mShares.get(key).equals(shares.get(key)))
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

	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append("name=").append(mName)
		.append(", id=").append(mId)
		.append(", ern=").append(mErn)
		.append(", access=").append(mAccess)
		.append(", modified=").append(mModified)
		.append(", synced=").append(mSynced)
		.append(", offline=").append(mOffline);
		return sb.append("]").toString();
	}
	
}

