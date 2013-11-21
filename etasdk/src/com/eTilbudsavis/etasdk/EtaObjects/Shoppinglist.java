package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Shoppinglist extends EtaErnObject implements Serializable, Comparable<Shoppinglist> {
	
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

	public static final String TYPE_SHOPPING_LIST = null;
	public static final String TYPE_WISH_LIST = "wish_list";
	
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
	private String mType;
	private String mMeta;
	private int mUserId = -1;
	
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
			
			String prevId = FIRST_ITEM;
			
			// Order from server is newest to oldest, so we'll have to reverse the list
			// And check if the previous_id is set, while doing it
			for (int i = shoppinglists.length()-1 ; i >= 0 ; i-- ) {
				
				Shoppinglist s = Shoppinglist.fromJSON(shoppinglists.getJSONObject(i));
				if (s.getPreviousId() == null) {
					s.setPreviousId(prevId);
				}
				prevId = s.getId();
				list.add(s);
			}

		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static Shoppinglist fromJSON(JSONObject shoppinglist) {
		return fromJSON(new Shoppinglist(), shoppinglist);
	}
	
	private static Shoppinglist fromJSON(Shoppinglist sl, JSONObject shoppinglist) {
		
		try {
			sl.setId(getJsonString(shoppinglist, ServerKey.ID));
			sl.setErn(getJsonString(shoppinglist, ServerKey.ERN));
			sl.setName(getJsonString(shoppinglist, ServerKey.NAME));
			sl.setAccess(getJsonString(shoppinglist, ServerKey.ACCESS));
			sl.setModified(getJsonString(shoppinglist, ServerKey.MODIFIED));
			sl.setOwner(Share.fromJSON(shoppinglist.getJSONObject(ServerKey.OWNER)));
			sl.setPreviousId(getJsonString(shoppinglist, ServerKey.PREVIOUS_ID));
			sl.setType(getJsonString(shoppinglist, ServerKey.TYPE));
			String meta = getJsonString(shoppinglist, ServerKey.META);
			sl.setMeta(shoppinglist.isNull(ServerKey.META) ? null : (meta.equals("") ? null : new JSONObject(meta)));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
		return sl;
	}
	
	public Bundle getApiParams() {
		Bundle apiParams = new Bundle();
		apiParams.putString(ServerKey.MODIFIED, Utils.formatDate(mModified));
		apiParams.putString(ServerKey.NAME, getName());
		apiParams.putString(ServerKey.ACCESS, getAccess());
		apiParams.putString(ServerKey.META, mMeta == null ? null : mMeta.toString());
		apiParams.putString(ServerKey.TYPE, getType());
		apiParams.putString(ServerKey.PREVIOUS_ID, getPreviousId());
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
		time.setTime(1000 * (time.getTime()/ 1000));
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

	public String getType() {
		return mType;
	}

	public Shoppinglist setType(String type) {
		mType = type;
		return this;
	}
	
	public JSONObject getMeta() {
		JSONObject meta = null;
		try {
			meta = mMeta == null ? null : new JSONObject(mMeta);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return meta;
	}

	public Shoppinglist setMeta(JSONObject meta) {
		mMeta = meta == null ? null : meta.toString();
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

	public int getUserId() {
		return mUserId;
	}

	public Shoppinglist setUserId(int userId) {
		mUserId = userId;
		return this;
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
		.append(", user=").append(mUserId)
		.append(", previous_id=").append(mPrevId)
		.append(", type=").append(mType)
		.append(", meta=").append(getMeta() == null ? null : getMeta().toString());
		return sb.append("]").toString();
	}

	public static Comparator<Shoppinglist> NameComparator  = new Comparator<Shoppinglist>() {

		public int compare(Shoppinglist item1, Shoppinglist item2) {
			return item1.getName().toLowerCase().compareTo(item2.getName().toLowerCase());
		}

	};

	public int compareTo(Shoppinglist another) {
        return this.mName.toLowerCase().compareTo(another.getName().toLowerCase());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mAccess == null) ? 0 : mAccess.hashCode());
		result = prime * result + ((mMeta == null) ? 0 : mMeta.hashCode());
		result = prime * result + ((mModified == null) ? 0 : mModified.hashCode());
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		result = prime * result + ((mOwner == null) ? 0 : mOwner.hashCode());
		result = prime * result + ((mPrevId == null) ? 0 : mPrevId.hashCode());
		result = prime * result + mState;
		result = prime * result + ((mType == null) ? 0 : mType.hashCode());
		result = prime * result + mUserId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Shoppinglist other = (Shoppinglist) obj;
		if (mAccess == null) {
			if (other.mAccess != null)
				return false;
		} else if (!mAccess.equals(other.mAccess))
			return false;
		if (mMeta == null) {
			if (other.mMeta != null)
				return false;
		} else if (!mMeta.equals(other.mMeta))
			return false;
		if (mModified == null) {
			if (other.mModified != null)
				return false;
		} else if (!mModified.equals(other.mModified))
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		if (mOwner == null) {
			if (other.mOwner != null)
				return false;
		} else if (!mOwner.equals(other.mOwner))
			return false;
		if (mPrevId == null) {
			if (other.mPrevId != null)
				return false;
		} else if (!mPrevId.equals(other.mPrevId))
			return false;
		if (mState != other.mState)
			return false;
		if (mType == null) {
			if (other.mType != null)
				return false;
		} else if (!mType.equals(other.mType))
			return false;
		if (mUserId != other.mUserId)
			return false;
		return true;
	}
	
}

