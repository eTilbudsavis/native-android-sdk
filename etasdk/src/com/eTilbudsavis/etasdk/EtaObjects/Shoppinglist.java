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

import com.eTilbudsavis.etasdk.Eta;

public class Shoppinglist implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final String S_ID = "id";
	private static final String S_ERN = "ern";
	private static final String S_NAME = "name";
	private static final String S_ACCESS = "access";
	private static final String S_MODIFIED = "modified";

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.DATE_FORMAT);

	private String mId;
	private String mErn;
	private String mName;
	private String mAccess;
	private long mModified = 0L;
	
	private HashMap<String, Share> mShares = new HashMap<String, Share>();
	private HashMap<String, ShoppinglistItem> mItems = new HashMap<String, ShoppinglistItem>();

	public Shoppinglist() {
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
			sl.setId(shoppinglist.getString(S_ID));
			sl.setErn(shoppinglist.getString(S_ERN));
			sl.setName(shoppinglist.getString(S_NAME));
			sl.setAccess(shoppinglist.getString(S_ACCESS));
			sl.setModified(shoppinglist.getString(S_MODIFIED));
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		
		return sl;
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

	
}

