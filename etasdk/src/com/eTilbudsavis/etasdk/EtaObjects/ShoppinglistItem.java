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

import com.eTilbudsavis.etasdk.Eta;

import Utils.Utilities;
import android.annotation.SuppressLint;
import android.os.Bundle;

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

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.DATE_FORMAT);

	private String mId;
	private boolean mTick;
	private String mOfferId;
	private int mCount;
	private String mDescription;
	private String mShoppinglistId;
	private String mErn;
	private String mCreator;
	private long mModified;
	private Offer mOffer = null;

	public static ShoppinglistItem fromJSON(ShoppinglistItem sli, JSONObject shoppinglistItem) {
		if (sli == null) sli = new ShoppinglistItem();
		if (shoppinglistItem == null) return sli;
		
		try {
			sli.setId(shoppinglistItem.getString(S_ID));
			sli.setTick(shoppinglistItem.getBoolean(S_TICK));
			sli.setOfferId(shoppinglistItem.getString(S_OFFER_ID));
			sli.setCount(shoppinglistItem.getInt(S_COUNT));
			sli.setDescription(shoppinglistItem.getString(S_DESCRIPTION));
			sli.setShoppinglistId(shoppinglistItem.getString(S_SHOPPINGLIST_ID));
			sli.setErn(shoppinglistItem.getString(S_ERN));
			sli.setCreator(shoppinglistItem.getString(S_CREATOR));
			sli.setModified(shoppinglistItem.getString(S_MODIFIED));
		} catch (JSONException e) {
			if (Eta.mDebug) e.printStackTrace();
		}
		return sli;
	}
	
	public ShoppinglistItem() {
	}

	public static ArrayList<ShoppinglistItem> fromJSONArray(String shoppinglistItems) {
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		try {
			list = fromJSONArray(new JSONArray(shoppinglistItems));
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		return list;
	}
	
	public static ArrayList<ShoppinglistItem> fromJSONArray(JSONArray shoppinglistItems) {
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		try {
			for (int i = 0 ; i < shoppinglistItems.length() ; i++ )
				list.add(ShoppinglistItem.fromJSON((JSONObject)shoppinglistItems.get(i)));
			
		} catch (JSONException e) {
			if (Eta.mDebug)
				e.printStackTrace();
		}
		return list;
	}
	
	public static ShoppinglistItem fromJSON(String shoppinglistItem) {
		ShoppinglistItem sli = new ShoppinglistItem();
		try {
			sli = fromJSON(sli, new JSONObject(shoppinglistItem));
		} catch (JSONException e) {
			if (Eta.mDebug) e.printStackTrace();
		}
		return sli;
	}
	
	public static ShoppinglistItem fromJSON(JSONObject shoppinglistItem) {
		return fromJSON(new ShoppinglistItem(), shoppinglistItem);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public ShoppinglistItem(Shoppinglist shoppinglist, String description) {
		mDescription = description;
		mCount = 1;
		mTick = false;
		mOffer = null;
		mShoppinglistId = shoppinglist.getId();
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

	public ShoppinglistItem setShoppinglistId(String shoppinglist) {
		mShoppinglistId = shoppinglist;
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

	public Bundle getApiParamsAddItem() {

		Bundle apiParams = new Bundle();
		apiParams.putString("shoppinglist", mShoppinglistId);
		if (mOffer == null)
			apiParams.putString("description", mDescription);
		else
			apiParams.putString("offer", mOffer.getId());

		apiParams.putString("tick", String.valueOf(mTick));
		apiParams.putString("count", String.valueOf(getCount()));

		return apiParams;
	}

	public Bundle getApiParamsEditItem() {
		Bundle apiParams = getApiParamsAddItem();
		apiParams.putString("item", getId());
		if (getOffer() == null)
			apiParams.putString("removeOffer", String.valueOf(true));
		else
			apiParams.putString("removeOffer", String.valueOf(false));

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
				mShoppinglistId.equals(sli.getShoppinglistId());
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
