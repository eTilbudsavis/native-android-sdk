package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.ShoppinglistManager;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Shoppinglist extends EtaErnObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ACCESS_PRIVATE = "private";
	public static final String ACCESS_SHARED = "shared";
	public static final String ACCESS_PUBLIC = "public";
	
	public static final String EMPTY_ALL = "all";
	public static final String EMPTY_TICKED = "ticked";
	public static final String EMPTY_UNTICKED = "unticked";
	
	// server vars
	private String mName = "";
	private String mAccess = ACCESS_PRIVATE;
	private Date mModified = null;
	private Share mOwner = new Share();
	private int mState;
	
	private Shoppinglist() {
		setId(Utils.createUUID());
		setModified(new Date());
		setState(ShoppinglistManager.STATE_TO_SYNC);
	}

	public static Shoppinglist fromName(String name) {
		Shoppinglist sl = new Shoppinglist();
		sl.setName(name);
		return sl;
	}
	
	public void set(JSONObject shoppinglist) {
		fromJSON(this, shoppinglist);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Shoppinglist> fromJSON(JSONArray shoppinglists) {
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		try {
			for (int i = 0 ; i < shoppinglists.length() ; i++ )
				list.add(Shoppinglist.fromJSON(new Shoppinglist(), (JSONObject)shoppinglists.get(i)));
			
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static Shoppinglist fromJSON(JSONObject shoppinglist) {
		return fromJSON(new Shoppinglist(), shoppinglist);
	}
	
	private static Shoppinglist fromJSON(Shoppinglist sl, JSONObject shoppinglist) {
		
		try {
			sl.setId(shoppinglist.getString(S_ID));
			sl.setErn(shoppinglist.getString(S_ERN));
			sl.setName(shoppinglist.getString(S_NAME));
			sl.setAccess(shoppinglist.getString(S_ACCESS));
			sl.setModified(shoppinglist.getString(S_MODIFIED));
			sl.setOwner(Share.fromJSON(shoppinglist.getJSONObject(S_OWNER)));
			
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return sl;
	}
	
	public Bundle getApiParams() {
		Bundle apiParams = new Bundle();
		apiParams.putString(S_MODIFIED, Utils.formatDate(mModified));
		apiParams.putString(S_NAME, getName());
		apiParams.putString(S_ACCESS, getAccess());
		return apiParams;
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
		mModified = time;
		return this;
	}
	
	public int getState() {
		return mState;
	}
	
	public Shoppinglist setState(int state) {
		if (ShoppinglistManager.STATE_TO_SYNC <= state && state <= ShoppinglistManager.STATE_ERROR)
			mState = state;
		return this;
	}
	
	public Shoppinglist setModified(String time) {
		mModified = Utils.parseDate(time);
		return this;
	}
	
	public Share getOwner() {
		return mOwner;
	}
	
	public Shoppinglist setOwner(Share owner) {
		mOwner = owner;
		return this;
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
				mErn.equals(sl.getErn()) &&
				mAccess.equals(sl.getAccess()) &&
				mModified.equals(sl.getModified()) &&
				mOwner.equals(sl.getOwner()) &&
				mName.equals(sl.getName());
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
		.append(", state=").append(mState)
		.append(", owner=").append(mOwner.toString());
		return sb.append("]").toString();
	}
	
}

