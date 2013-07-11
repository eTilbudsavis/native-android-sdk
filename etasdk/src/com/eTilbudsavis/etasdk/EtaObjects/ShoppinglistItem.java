package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Tools.Utilities;

public class ShoppinglistItem implements Comparable<ShoppinglistItem>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "ShoppinglistItem";
	
	private static final String S_ID = "id";
	private static final String S_TICK = "tick";
	private static final String S_OFFER_ID = "offer_id";
	private static final String S_COUNT = "count";
	private static final String S_DESCRIPTION = "description";
	private static final String S_SHOPPINGLIST_ID = "shopping_list_id";
	private static final String S_ERN = "ern";
	private static final String S_CREATOR = "creator";
	private static final String S_MODIFIED = "modified";

	public static final int STATE_INIT = 0;
	public static final int STATE_SYNCHRONIZING = 1;
	public static final int STATE_SYNCHRONIZED = 2;
	public static final int STATE_ERROR = 3;
	public static final int STATE_DELETING = 4;
	public static final int STATE_DELETED = 5;
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.DATE_FORMAT);

	// Server vars
	private String mId = "";
	private boolean mTick = false;
	private String mOfferId = "";
	private int mCount = 1;
	private String mDescription = "";
	private String mShoppinglistIdDepricated = "";
	private String mErn = "";
	private String mCreator = "";
	private long mModified = 0L;
	private int mState = STATE_INIT;
	
	// local vars
	private Offer mOffer = null;
	private String mShoppinglistId = "";

	public ShoppinglistItem() {
		setId(Utilities.createUUID());
		setModified(System.currentTimeMillis());
	}

	public ShoppinglistItem(Shoppinglist shoppinglist, String description) {
		setId(Utilities.createUUID());
		setModified(System.currentTimeMillis());
		setDescription(description);
	}

	public static ArrayList<ShoppinglistItem> fromJSONArray(String shoppinglistItems, String shoppinglistId) {
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		try {
			list = fromJSONArray(new JSONArray(shoppinglistItems), shoppinglistId);
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return list;
	}
	
	public static ArrayList<ShoppinglistItem> fromJSONArray(JSONArray shoppinglistItems, String shoppinglistId) {
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		try {
			for (int i = 0 ; i < shoppinglistItems.length() ; i++ ) {
				ShoppinglistItem sli = ShoppinglistItem.fromJSON((JSONObject)shoppinglistItems.get(i), shoppinglistId);
				list.add(sli);
			}
			
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return list;
	}
	
	public static ShoppinglistItem fromJSON(String shoppinglistItem, String shoppinglistId) {
		ShoppinglistItem sli = new ShoppinglistItem();
		try {
			sli = fromJSON(sli, new JSONObject(shoppinglistItem), shoppinglistId);
		} catch (JSONException e) {
			if (Eta.DEBUG) e.printStackTrace();
		}
		return sli;
	}
	
	public static ShoppinglistItem fromJSON(JSONObject shoppinglistItem, String shoppinglistId) {
		return fromJSON(new ShoppinglistItem(), shoppinglistItem, shoppinglistId);
	}

	public static ShoppinglistItem fromJSON(ShoppinglistItem sli, JSONObject shoppinglistItem, String shoppinglistId) {
		if (sli == null) sli = new ShoppinglistItem();
		if (shoppinglistItem == null) return sli;
		
		try {
			sli.setId(shoppinglistItem.getString(S_ID));
			sli.setTick(shoppinglistItem.getBoolean(S_TICK));
			sli.setOfferId(shoppinglistItem.getString(S_OFFER_ID));
			sli.setCount(shoppinglistItem.getInt(S_COUNT));
			sli.setDescription(shoppinglistItem.getString(S_DESCRIPTION));
			sli.setShoppinglistIdDepricated(shoppinglistItem.getString(S_SHOPPINGLIST_ID));
			sli.setErn(shoppinglistItem.getString(S_ERN));
			sli.setCreator(shoppinglistItem.getString(S_CREATOR));
			sli.setModified(shoppinglistItem.getString(S_MODIFIED));
			sli.setShoppinglistId(shoppinglistId);
		} catch (JSONException e) {
			if (Eta.DEBUG) e.printStackTrace();
		}
		return sli;
	}
	
	public String getTitle() {
		return mOffer == null ? mDescription : mOffer.getHeading();
	}

	
	
	public String getId() {
		return mId;
	}

	public ShoppinglistItem setId(String id) {
		mId = id;
		return this;
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

	public String getShoppinglistIdDepricated() {
		return mShoppinglistIdDepricated;
	}

	public ShoppinglistItem setShoppinglistIdDepricated(String oldShoppinglistIdFormat) {
		mShoppinglistIdDepricated = oldShoppinglistIdFormat;
		return this;
	}

	public String getOfferId() {
		return mOfferId;
	}

	public ShoppinglistItem setOfferId(String offerId) {
		mOfferId = offerId;
		return this;
	}

	public String getErn() {
		return mErn;
	}

	public ShoppinglistItem setErn(String ern) {
		mErn = ern;
		return this;
	}

	public long getModified() {
		return mModified;
	}

	public String getModifiedString() {
		return sdf.format(new Date(mModified));
	}

	public ShoppinglistItem setModified(long time) {
		mModified = time;
		return this;
	}

	public ShoppinglistItem setModified(String time) {
		try {
			mModified = sdf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return this;
	}

	public int getState() {
		return mState;
	}
	
	public ShoppinglistItem setState(int state) {
		if (STATE_INIT <= state && state <= STATE_DELETED) {
			mState = state;
		}
		return this;
	}
	
	public Bundle getApiParams() {

		Bundle apiParams = new Bundle();
		apiParams.putString(S_DESCRIPTION, getDescription());
		apiParams.putInt(S_COUNT, getCount());
		apiParams.putBoolean(S_TICK, isTicked());
		apiParams.putString(S_OFFER_ID, getOfferId());
		apiParams.putString(S_MODIFIED, getModifiedString());

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
				( mDescription == null ? sli.getDescription() == null : mDescription.equals(sli.getDescription())) && 
				mCount == sli.getCount() &&
				mTick == sli.isTicked() &&
				( mOffer == null ? sli.getOffer() == null : mOffer.equals(sli.getOffer())) &&
				mCreator.equals(sli.getCreator()) &&
				mShoppinglistIdDepricated.equals(sli.getShoppinglistId());
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
