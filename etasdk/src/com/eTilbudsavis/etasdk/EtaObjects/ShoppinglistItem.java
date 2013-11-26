package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.EtaObjects.EtaObject.ServerKey;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ShoppinglistItem extends EtaErnObject implements Comparable<ShoppinglistItem>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "ShoppinglistItem";
	
	/** States a shopppingItem list can be in */
	public interface State {
		int TO_SYNC	= 0;
		int SYNCING	= 1;
		int SYNCED	= 2;
		int DELETE	= 4;
		int ERROR	= 5;
	}
	
	public final static String FIRST_ITEM = "00000000-0000-0000-0000-000000000000";
	
	private boolean mTick = false;
	private String mOfferId = null;
	private int mCount = 1;
	private String mDescription = null;
	private String mCreator;
	private Date mModified = new Date();
	private int mState = State.TO_SYNC;
	private Offer mOffer = null;
	private String mShoppinglistId;
	private String mPrevId;
	private String mMeta;
	private int mUserId = -1;

	public ShoppinglistItem() {
        String id = Utils.createUUID();
		setId(id);
        setErn("ern:shopping:item:" + id);
	}
	
	public ShoppinglistItem(Shoppinglist shoppinglist, String description) {
		this();
		setShoppinglistId(shoppinglist.getId());
		setDescription(description);
	}

	public ShoppinglistItem(Shoppinglist shoppinglist, Offer offer) {
		this();
		setShoppinglistId(shoppinglist.getId());
		setOffer(offer);
		setDescription(offer.getHeading());
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<ShoppinglistItem> fromJSON(JSONArray shoppinglistItems) {
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		
		try {
			
			String prevId = FIRST_ITEM;

			// Order from server is newest to oldest, so we'll have to reverse the list
			// And check if the previous_id is set, while doing it
			for (int i = shoppinglistItems.length()-1 ; i >= 0 ; i-- ) {
				
				ShoppinglistItem s = ShoppinglistItem.fromJSON((JSONObject)shoppinglistItems.get(i));
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
	public static ShoppinglistItem fromJSON(JSONObject shoppinglistItem) {
		return fromJSON(new ShoppinglistItem(), shoppinglistItem);
	}

	private static ShoppinglistItem fromJSON(ShoppinglistItem sli, JSONObject shoppinglistItem) {
		
		try {
			sli.setId(getJsonString(shoppinglistItem, S_ID));
			sli.setTick(shoppinglistItem.getBoolean(S_TICK));
			sli.setOfferId(getJsonString(shoppinglistItem, S_OFFER_ID));
			sli.setCount(shoppinglistItem.getInt(S_COUNT));
			sli.setDescription(getJsonString(shoppinglistItem, S_DESCRIPTION));
			sli.setShoppinglistId(getJsonString(shoppinglistItem, S_SHOPPINGLIST_ID));
			sli.setErn(getJsonString(shoppinglistItem, S_ERN));
			sli.setCreator(getJsonString(shoppinglistItem, S_CREATOR));
			sli.setModified(Utils.parseDate(shoppinglistItem.isNull(S_MODIFIED) ? "1970-01-01T00:00:00+0000" : shoppinglistItem.getString(S_MODIFIED)));
			sli.setPreviousId(getJsonString(shoppinglistItem, S_PREVIOUS_ID));
			
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return sli;
	}
	
	public String getTitle() {
		return mOffer == null ? mDescription : mOffer.getHeading();
	}

	public String getDescription() {
		return mDescription;
	}

	public ShoppinglistItem setDescription(String description) {
		mDescription = description;
		return this;
	}

	public int getCount() {
		return mCount;
	}

	public ShoppinglistItem setCount(int count) {
		mCount = count;
		return this;
	}

	public boolean isTicked() {
		return mTick;
	}

	public ShoppinglistItem setTick(Boolean tick) {
		mTick = tick;
		return this;
	}

	public Offer getOffer() {
		return mOffer;
	}

	public ShoppinglistItem setOffer(Offer offer) {
		mOffer = offer;
		if (mOffer != null) {
			setOfferId(offer.getId());
			setDescription(mOffer.getHeading());
		} else {
			setOfferId(null);
		}
		
		return this;
	}

	public String getCreator() {
		return mCreator;
	}

	public ShoppinglistItem setCreator(String creator) {
		mCreator = creator;
		return this;
	}

	public String getShoppinglistId() {
		return mShoppinglistId;
	}

	public ShoppinglistItem setShoppinglistId(String id) {
		mShoppinglistId = id;
		return this;
	}

	public String getPreviousId() {
		return mPrevId;
	}

	public ShoppinglistItem setPreviousId(String id) {
		mPrevId = id;
		return this;
	}

	public String getOfferId() {
		return mOfferId;
	}

	public ShoppinglistItem setOfferId(String offerId) {
		mOfferId = offerId;
		return this;
	}

	public Date getModified() {
		return mModified;
	}

	public ShoppinglistItem setModified(Date time) {
		time.setTime(1000 * (time.getTime()/ 1000));
		mModified = time;
		return this;
	}

	public int getState() {
		return mState;
	}
	
	public ShoppinglistItem setState(int state) {
		if (State.TO_SYNC <= state && state <= State.ERROR)
			mState = state;
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

	public ShoppinglistItem setMeta(JSONObject meta) {
		mMeta = meta == null ? null : meta.toString();
		return this;
	}

	public int getUserId() {
		return mUserId;
	}

	public ShoppinglistItem setUserId(int userId) {
		mUserId = userId;
		return this;
	}

	public Bundle getApiParams() {
		
		Bundle apiParams = new Bundle();
		apiParams.putString(S_DESCRIPTION, getDescription());
		apiParams.putInt(S_COUNT, getCount());
		apiParams.putBoolean(S_TICK, isTicked());
		apiParams.putString(S_OFFER_ID, getOfferId());
		apiParams.putString(S_MODIFIED, Utils.formatDate(getModified()));
		apiParams.putString(S_CREATOR, getCreator());
		apiParams.putString(S_SHOPPINGLIST_ID, getShoppinglistId());
		apiParams.putString(S_PREVIOUS_ID, getPreviousId());
		apiParams.putString(ServerKey.META, mMeta);
		return apiParams;
	}

	public int compareTo(ShoppinglistItem another) {

		return 0;
	}

	@Override
	public String toString() {
		return toString(false);
	}


	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[");
		sb.append("id=").append(mId);
		if (mDescription != null) {
			sb.append(", description=").append(mDescription);
		} 
		if (mOfferId != null) {
			sb.append(", offer_id=").append(mOfferId);
		}
		sb.append(", count=").append(mCount)
		.append(", ticked=").append(mTick)
		.append(", modified=").append(Utils.formatDate(mModified));
		if(everything) {
			sb.append(", creator=").append(mCreator)
			.append(", shoppinglist_id=").append(mShoppinglistId)
			.append(", state=").append(mState)
			.append(", userId=").append(mUserId)
			.append(", previous_id=").append(mPrevId)
			.append(", meta=").append(mMeta);
		}
		return sb.append("]").toString();
	}
	
	public static Comparator<ShoppinglistItem> TitleComparator  = new Comparator<ShoppinglistItem>() {

		public int compare(ShoppinglistItem item1, ShoppinglistItem item2) {

			//ascending order
			return item1.getTitle().compareToIgnoreCase(item2.getTitle());

			//descending order
//			return item2.getTitle().compareToIgnoreCase(item1.getTitle());
		}

	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mCount;
		result = prime * result + ((mCreator == null) ? 0 : mCreator.hashCode());
		result = prime * result + ((mDescription == null) ? 0 : mDescription.hashCode());
		result = prime * result + ((mMeta == null) ? 0 : mMeta.hashCode());
		result = prime * result + ((mModified == null) ? 0 : mModified.hashCode());
		result = prime * result + ((mOffer == null) ? 0 : mOffer.hashCode());
		result = prime * result + ((mOfferId == null) ? 0 : mOfferId.hashCode());
		result = prime * result + ((mPrevId == null) ? 0 : mPrevId.hashCode());
		result = prime * result + ((mShoppinglistId == null) ? 0 : mShoppinglistId.hashCode());
		result = prime * result + mState;
		result = prime * result + (mTick ? 1231 : 1237);
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
		ShoppinglistItem other = (ShoppinglistItem) obj;
		if (mCount != other.mCount)
			return false;
		if (mCreator == null) {
			if (other.mCreator != null)
				return false;
		} else if (!mCreator.equals(other.mCreator))
			return false;
		if (mDescription == null) {
			if (other.mDescription != null)
				return false;
		} else if (!mDescription.equals(other.mDescription))
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
		if (mOffer == null) {
			if (other.mOffer != null)
				return false;
		} else if (!mOffer.equals(other.mOffer))
			return false;
		if (mOfferId == null) {
			if (other.mOfferId != null)
				return false;
		} else if (!mOfferId.equals(other.mOfferId))
			return false;
		if (mPrevId == null) {
			if (other.mPrevId != null)
				return false;
		} else if (!mPrevId.equals(other.mPrevId))
			return false;
		if (mShoppinglistId == null) {
			if (other.mShoppinglistId != null)
				return false;
		} else if (!mShoppinglistId.equals(other.mShoppinglistId))
			return false;
		if (mState != other.mState)
			return false;
		if (mTick != other.mTick)
			return false;
		if (mUserId != other.mUserId)
			return false;
		return true;
	}
	
}
