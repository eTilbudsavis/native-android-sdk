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
	
	public static final String TAG = "Shoppinglist";

	/** States a shoppping list can be in */
	public interface State {
		int TO_SYNC	= 0;
		int SYNCING	= 1;
		int SYNCED	= 2;
		int DELETE	= 4;
		int ERROR	= 5;
	}
	
	public final static String FIRST_ITEM = "00000000-0000-0000-0000-000000000000";
	
	public static final String ACCESS_PRIVATE = "private";
	public static final String ACCESS_SHARED = "shared";
	public static final String ACCESS_PUBLIC = "public";
	
	public static final String EMPTY_ALL = "all";
	public static final String EMPTY_TICKED = "ticked";
	public static final String EMPTY_UNTICKED = "unticked";
	
	// server vars
	private String mName = "";
	private String mAccess = ACCESS_PRIVATE;
	private Date mModified = new Date();
	private Share mOwner = new Share();
	private int mState = State.TO_SYNC;
	private String mPrevId;
	
	private Shoppinglist() {
        String id = Utils.createUUID();
		setId(id);
        setErn("ern:shopping:list:" + id);
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
			Shoppinglist tmp = null;
			
			// Parse in opposite order, to get the right ordering from server until sorting is implemented
			for (int i = shoppinglists.length()-1 ; i >= 0 ; i-- ) {
				
				Shoppinglist s = Shoppinglist.fromJSON(shoppinglists.getJSONObject(i));
				if (s.getPreviousId() == null) {
					s.setPreviousId(tmp == null ? FIRST_ITEM : tmp.getId());
					tmp = s;
				}
				list.add(s);
			}

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
			sl.setId(getJsonString(shoppinglist, S_ID));
			sl.setErn(getJsonString(shoppinglist, S_ERN));
			sl.setName(getJsonString(shoppinglist, S_NAME));
			sl.setAccess(getJsonString(shoppinglist, S_ACCESS));
			sl.setModified(getJsonString(shoppinglist, S_MODIFIED));
			sl.setOwner(Share.fromJSON(shoppinglist.getJSONObject(S_OWNER)));
			sl.setPreviousId(getJsonString(shoppinglist, S_PREVIOUS_ID));
			
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
		apiParams.putString(S_PREVIOUS_ID, getPreviousId());
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
		if (State.TO_SYNC <= state && state <= State.ERROR)
			mState = state;
		return this;
	}

	public String getPreviousId() {
		return mPrevId;
	}

	public Shoppinglist setPreviousId(String id) {
		mPrevId = id;
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
		return stringCompare(mId, sl.getId()) &&
				stringCompare(mErn, sl.getErn()) &&
				mAccess.equals(sl.getAccess()) &&
				mModified.equals(sl.getModified()) &&
				mOwner.equals(sl.getOwner()) &&
				stringCompare(mName, sl.getName()) &&
				stringCompare(mPrevId, sl.getPreviousId());
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
		.append(", owner=").append(mOwner.toString())
		.append(", previous_id=").append(mPrevId);
		return sb.append("]").toString();
	}
	
}

