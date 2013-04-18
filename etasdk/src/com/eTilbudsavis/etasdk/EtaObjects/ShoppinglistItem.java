package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

public class ShoppinglistItem implements Comparable<ShoppinglistItem>, Serializable {

	private static final long serialVersionUID = 1L;

	private String mId;
	private String mDescription;
	private int mCount;
	private Boolean mTick;
	private Offer mOffer = null;
	private String mCreator;
	private String mShoppinglist;

	/**
	 * Creates a new ShoppinglistItem.<br><br>
	 * 
	 * The constructor takes a JSONObject that is returned from the server
	 * and creates a new Object from it.
	 * 
	 * @param shoppinglistItem
	 */
	public ShoppinglistItem(JSONObject shoppinglistItem) {
		try {
			mId = shoppinglistItem.getString("id");
			mDescription = shoppinglistItem.getString("description");

			String offer = shoppinglistItem.getString("offer");
			if (!offer.equals("null"))
				mOffer = new Offer(new JSONObject(offer));

			setCount(shoppinglistItem.getInt("count"));
			mTick = shoppinglistItem.getBoolean("tick");
			mCreator = shoppinglistItem.getString("creator");
			mShoppinglist = shoppinglistItem.getString("shoppinglist");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new ShoppinglistItem.<br><br>
	 * 
	 * <b>Notice:</b> this constructor sets description as ID
	 * until a correct ID can be returned from server. <br>
	 * You must change the ID of this object before sending any requests <br>
	 * back to the server, or this ID will not match the server ID.
	 * 
	 * @param shoppinglist to add shoppinglistItem to.
	 * @param description for the shoppinglistItem (where description is initial ID)
	 */
	public ShoppinglistItem(Shoppinglist shoppinglist, String description) {
		mDescription = description;
		mCount = 1;
		mTick = false;
		mOffer = null;
		mShoppinglist = shoppinglist.getId();
	}

	/**
	 * Creates a new ShoppinglistItem.<br><br>
	 * 
	 * <b>Notice:</b> this constructor sets description as ID
	 * until a correct ID can be returned from server. <br>
	 * You must change the ID of this object before sending any requests <br>
	 * back to the server, or this ID will not match the server ID.
	 * @param shoppinglist to add shoppinglistItem to.
	 * @param offer for the shoppinglistItem (where offer.getID() is initial ID)
	 */
	public ShoppinglistItem(Shoppinglist shoppinglist, Offer offer) {
		mDescription = null;
		mCount = 1;
		mTick = false;
		mOffer = offer;
		mShoppinglist = shoppinglist.getId();
	}

	public void setId(String id) {
		mId = id;
	}

	public String getId() {
		return mId;
	}

	public String getDescription() {
		return mDescription;
	}

	public int getCount() {
		return mCount;
	}

	public String getTitle() {
		return mOffer == null ? mDescription : mOffer.getHeading();
	}

	public void setCount(int count) {
		mCount = count;
	}

	public Boolean isTicked() {
		return mTick;
	}

	public void setTick(Boolean tick) {
		mTick = tick;
	}

	public Offer getOffer() {
		return mOffer;
	}

	public void setOffer(Offer offer) {
		mOffer = offer;
	}

	public String getCreator() {
		return mCreator;
	}

	public String getShoppinglist() {
		return mShoppinglist;
	}

	public Bundle getApiParamsAddItem() {

		Bundle apiParams = new Bundle();
		apiParams.putString("shoppinglist", mShoppinglist);
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
				mTick.equals(sli.isTicked()) &&
				( mOffer == null ? sli.getOffer() == null : mOffer.equals(sli.getOffer())) &&
				mCreator.equals(sli.getCreator()) &&
				mShoppinglist.equals(sli.getShoppinglist());
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
