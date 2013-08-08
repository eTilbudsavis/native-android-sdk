package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.EventLogTags.Description;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.ShoppinglistManager;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ShoppinglistItem extends EtaErnObject implements Comparable<ShoppinglistItem>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "ShoppinglistItem";
	
	private boolean mTick = false;
	private String mOfferId = null;
	private int mCount = 1;
	private String mDescription = null;
	private String mCreator = "";
	private Date mModified = new Date();
	private int mState = ShoppinglistManager.STATE_TO_SYNC;
	private Offer mOffer = null;
	private String mShoppinglistId = "";

	public ShoppinglistItem() {
		setId(Utils.createUUID());
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
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<ShoppinglistItem> fromJSON(JSONArray shoppinglistItems) {
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		try {
			for (int i = 0 ; i < shoppinglistItems.length() ; i++ ) {
				ShoppinglistItem sli = ShoppinglistItem.fromJSON((JSONObject)shoppinglistItems.get(i));
				list.add(sli);
			}
			
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static ShoppinglistItem fromJSON(JSONObject shoppinglistItem) {
		return fromJSON(new ShoppinglistItem(), shoppinglistItem);
	}

	private static ShoppinglistItem fromJSON(ShoppinglistItem sli, JSONObject shoppinglistItem) {
		try {
			sli.setId(shoppinglistItem.getString(S_ID));
			sli.setTick(shoppinglistItem.getBoolean(S_TICK));
			sli.setOfferId(shoppinglistItem.isNull(S_OFFER_ID) ? null : shoppinglistItem.getString(S_OFFER_ID));
			sli.setCount(shoppinglistItem.getInt(S_COUNT));
			sli.setDescription(shoppinglistItem.getString(S_DESCRIPTION));
			sli.setShoppinglistId(shoppinglistItem.getString(S_SHOPPINGLIST_ID));
			sli.setErn(shoppinglistItem.getString(S_ERN));
			sli.setCreator(shoppinglistItem.getString(S_CREATOR));
			sli.setModified(Utils.parseDate(shoppinglistItem.isNull(S_MODIFIED) ? "1970-01-01T00:00:00+0000" : shoppinglistItem.getString(S_MODIFIED)));
		} catch (JSONException e) {
			if (Eta.DEBUG) e.printStackTrace();
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

	public Boolean isTicked() {
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
		setOfferId(offer.getId());
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
		if (ShoppinglistManager.STATE_TO_SYNC <= state ||
			state <= ShoppinglistManager.STATE_ERROR)
			mState = state;
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
		
		return mId.equals(sli.getId()) &&
				mErn.equals(sli.getErn()) &&
				mDescription == null ? sli.getDescription() == null : mDescription.equals(sli.getDescription()) && 
				mCount == sli.getCount() &&
				mTick == sli.isTicked() &&
//				mOfferId == null ? sli.getOfferId() == null : mOfferId.equals(sli.getOfferId()) &&
				mModified.equals(sli.getModified()) &&
				mShoppinglistId.equals(sli.getShoppinglistId()) &&
				mCreator.equals(sli.getCreator());
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
		} else if (mOfferId != null) {
			sb.append(", offer_id=").append(mOfferId);
		}
		sb.append(", count=").append(mCount)
		.append(", ticked=").append(mTick)
		.append(", modified=").append(Utils.formatDate(mModified));
		if(everything) {
			sb.append(", creator=").append(mCreator)
			.append(", shoppinglist_id=").append(mShoppinglistId)
			.append(", state=").append(mState);
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
