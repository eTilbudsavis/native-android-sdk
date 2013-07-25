package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Helpers.Share;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Shoppinglist extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ACCESS_PRIVATE = "private";
	public static final String ACCESS_SHARED = "shared";
	public static final String ACCESS_PUBLIC = "public";
	
	public static final String EMPTY_ALL = "all";
	public static final String EMPTY_TICKED = "ticked";
	public static final String EMPTY_UNTICKED = "unticked";
	
	public static final int STATE_INIT = 0;
	public static final int STATE_SYNCHRONIZING = 1;
	public static final int STATE_SYNCHRONIZED = 2;
	public static final int STATE_OFFLINE = 2;
	public static final int STATE_ERROR = 3;
	public static final int STATE_DELETING = 4;
	public static final int STATE_DELETED = 5;
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.DATE_FORMAT);

	// server vars
	private String mId = "";
	private String mErn = "";
	private String mName = "";
	private String mAccess = ACCESS_PRIVATE;
	private long mModified = 0L;
	private Share mOwner = new Share();
	
	// local vars
	private int mState = STATE_INIT;
	
	private Shoppinglist() {
		setId(Utils.createUUID());
		setModified(System.currentTimeMillis());
	}

	public static Shoppinglist fromName(String name) {
		Shoppinglist sl = new Shoppinglist();
		sl.setName(name);
		return sl;
	}
	
	public Shoppinglist(JSONObject shoppinglist) {
		set(shoppinglist);
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
			sl.setOwner(Share.fromJSON(shoppinglist.getString(S_OWNER)));
			
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return sl;
	}
	
	public Bundle getApiParams() {
		Bundle apiParams = new Bundle();
		apiParams.putString(S_MODIFIED, getModifiedString());
		apiParams.putString(S_NAME, getName());
		apiParams.putString(S_ACCESS, getAccess());
		return apiParams;
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
	
	public int getState() {
		return mState;
	}
	
	public Shoppinglist setState(int state) {
		if (STATE_INIT <= state && state <= STATE_DELETED) {
			mState = state;
		}
		return this;
	}
	
	public boolean isStateSynchronized() {
		return mState == STATE_SYNCHRONIZED;
	}

	public boolean isStateSynchronizing() {
		return mState == STATE_SYNCHRONIZING;
	}

	public boolean isStateDeleted() {
		return mState == STATE_DELETED;
	}

	public boolean isStateInitial() {
		return mState == STATE_INIT;
	}

	public boolean isStateOffline() {
		return mState == STATE_OFFLINE;
	}

	public boolean isStateError() {
		return mState == STATE_ERROR;
	}

	public Shoppinglist setModifiedFromJSON(JSONObject time) {
		try {
			setModified(time.getString(S_MODIFIED));
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
				mModified == sl.getModified() &&
				mState == sl.getState() &&
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

