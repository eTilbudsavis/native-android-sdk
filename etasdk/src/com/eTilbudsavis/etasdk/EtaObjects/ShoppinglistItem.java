package com.eTilbudsavis.etasdk.EtaObjects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ShoppinglistItem extends EtaListObject<ShoppinglistItem> {

	public static final String TAG = "ShoppinglistItem";
	
	private static final long serialVersionUID = -8186715532715467496L;

	private boolean mTick = false;
	private String mOfferId = null;
	private int mCount = 1;
	private String mDescription = null;
	private String mCreator;
	private Date mModified;
	private Offer mOffer = null;
	private String mShoppinglistId;
	private String mPrevId;
	private String mMeta;
	private int mUserId = -1;
	
	public ShoppinglistItem() {
		setId(Utils.createUUID());
		mModified = Utils.roundTime(new Date());
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

	/**
	 * Convert a {@link JSONArray} into a {@link List}&lt;T&gt;.
	 * @param shoppinglistItems A {@link JSONArray} in the format of a valid API v2 shoppinglistItem response
	 * @return A {@link List} of POJO;
	 */
	public static List<ShoppinglistItem> fromJSON(JSONArray shoppinglistItems) {
		List<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		
		try {
			for (int i = 0 ; i < shoppinglistItems.length() ; i++ ) {
				ShoppinglistItem s = ShoppinglistItem.fromJSON((JSONObject)shoppinglistItems.get(i));
				list.add(s);
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}

	/**
	 * A factory method for converting {@link JSONObject} into a POJO.
	 * @param shoppinglistItem A {@link JSONObject} in the format of a valid API v2 shoppinglistItem response
	 * @return An ShoppinglistItem object
	 */
	public static ShoppinglistItem fromJSON(JSONObject shoppinglistItem) {
		return fromJSON(new ShoppinglistItem(), shoppinglistItem);
	}

	/**
	 * A factory method for converting {@link JSONObject} into POJO.
	 * <p>This method exposes a way, of updating/setting an objects properties</p>
	 * @param sli An object to set/update
	 * @param jOffer A {@link JSONObject} in the format of a valid API v2 offer response
	 * @return A {@link List} of POJO
	 */
	public static ShoppinglistItem fromJSON(ShoppinglistItem sli, JSONObject jSli) {
		
		sli.setId(Json.valueOf(jSli, ServerKey.ID));
		sli.setTick(Json.valueOf(jSli, ServerKey.TICK, false));
		sli.setOfferId(Json.valueOf(jSli, ServerKey.OFFER_ID));
		sli.setCount(Json.valueOf(jSli, ServerKey.COUNT, 1));
		sli.setDescription(Json.valueOf(jSli, ServerKey.DESCRIPTION));
		sli.setShoppinglistId(Json.valueOf(jSli, ServerKey.SHOPPINGLIST_ID));
		sli.setErn(Json.valueOf(jSli, ServerKey.ERN));
		sli.setCreator(Json.valueOf(jSli, ServerKey.CREATOR));
		String date = Json.valueOf(jSli, ServerKey.MODIFIED, "1970-01-01T00:00:00+0000");
		if (sli.getTitle().contains("lagkage")) {
			EtaLog.d(TAG, sli.getTitle() + " " + date);
		}
		sli.setModified( Utils.parseDate(date) );
		sli.setPreviousId(Json.valueOf(jSli, ServerKey.PREVIOUS_ID, null));
		
		return sli;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.ID, Json.nullCheck(getId()));
			o.put(ServerKey.TICK, Json.nullCheck(isTicked()));
			o.put(ServerKey.OFFER_ID, Json.nullCheck(getOfferId()));
			o.put(ServerKey.COUNT, getCount());
			o.put(ServerKey.DESCRIPTION, Json.nullCheck(getDescription()));
			o.put(ServerKey.SHOPPINGLIST_ID, Json.nullCheck(getShoppinglistId()));
			o.put(ServerKey.ERN, Json.nullCheck(getErn()));
			o.put(ServerKey.CREATOR, Json.nullCheck(getCreator()));
			o.put(ServerKey.MODIFIED, Json.nullCheck(Utils.parseDate(getModified())));
			o.put(ServerKey.PREVIOUS_ID, Json.nullCheck(getPreviousId()));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	@Override
	public String getErnPrefix() {
		return ERN_SHOPPINGLISTITEM;
	}
	
	/**
	 * Returns a human readable title for this ShoppinglistItem.
	 * <p>The title is either the description, or heading of the related offer.
	 * If the description isn't <code>null</code>, this will be used, else if
	 * offer isn't <code>null</code>, the heading will be used, and finally,
	 * if non-exist it will be an empty string.</p>
	 * @return A human readable title
	 */
	public String getTitle() {
		return (mDescription == null || mDescription.length() == 0) ? (mOffer == null ? "" : mOffer.getHeading()) : mDescription;
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
			if (mDescription == null || !mOffer.getId().equals(mOfferId)) {
				setDescription(mOffer.getHeading());
			}
			setOfferId(offer.getId());
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
		mModified = Utils.roundTime(time);
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
	
	public int compareTo(ShoppinglistItem another) {
		if (another == null)
			return -1;
		
		String t1 = getTitle();
		String t2 = another.getTitle();
		if (t1 == null || t2 == null) {
			return t1 == null ? (t2 == null ? 0 : 1) : -1;
		}
		
		//ascending order
		return t1.compareToIgnoreCase(t2);
	}
	
	public static Comparator<ShoppinglistItem> TitleComparator  = new Comparator<ShoppinglistItem>() {

		public int compare(ShoppinglistItem item1, ShoppinglistItem item2) {

			if (item1 == null || item2 == null) {
				return item1 == null ? (item2 == null ? 0 : 1) : -1;
			} else {
				String t1 = item1.getTitle();
				String t2 = item2.getTitle();
				if (t1 == null || t2 == null) {
					return t1 == null ? (t2 == null ? 0 : 1) : -1;
				}
				
				//ascending order
				return t1.compareToIgnoreCase(t2);
			}
			
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
		if (mTick != other.mTick)
			return false;
		if (mUserId != other.mUserId)
			return false;
		return true;
	}

}
