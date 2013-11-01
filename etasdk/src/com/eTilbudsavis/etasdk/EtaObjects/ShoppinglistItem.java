package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

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
	private int mUserId = -1;

	public ShoppinglistItem() {
        String id =Utils.createUUID();
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
			ShoppinglistItem tmp = null;
			// Parse in opposite order, to get the right ordering from server until sorting is implemented
			for (int i = shoppinglistItems.length()-1 ; i >= 0 ; i-- ) {
				
				ShoppinglistItem s = ShoppinglistItem.fromJSON((JSONObject)shoppinglistItems.get(i));
				if (s.getPreviousId() == null) {
					s.setPreviousId(tmp == null ? FIRST_ITEM : tmp.getId());
					tmp = s;
				}
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
		return apiParams;
	}

	public int compareTo(ShoppinglistItem another) {

		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (!(o instanceof ShoppinglistItem))
			return false;

		ShoppinglistItem sli = (ShoppinglistItem)o;
		
		return stringCompare(mId, sli.getId()) &&
				stringCompare(mErn, sli.getErn()) &&
				stringCompare(mDescription, sli.getDescription()) && 
				mCount == sli.getCount() &&
				mTick == sli.isTicked() &&
				stringCompare(mOfferId, sli.getOfferId()) &&
				mModified.equals(sli.getModified()) &&
				stringCompare(mShoppinglistId, sli.getShoppinglistId()) &&
				stringCompare(mCreator, sli.getCreator()) &&
				stringCompare(mPrevId, sli.getPreviousId()) &&
				mUserId == sli.getUserId();
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
			.append(", previous_id=").append(mPrevId);
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


}
