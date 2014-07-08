/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.ListManager;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ShoppinglistItem extends EtaListObject<ShoppinglistItem> implements Serializable {

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
	
	/**
	 * Default constructor, this will create a new UUID as this objects id, and
	 * update modified to the creation time of this object (now).
	 */
	public ShoppinglistItem() {
		setId(Utils.createUUID());
		mModified = Utils.roundTime(new Date());
	}
	
	/**
	 * Constructor for creating a new ShoppinglistItem from a plain text description
	 * and attach it to a {@link Shoppinglist}.
	 * @param shoppinglist A list to associate this item with
	 * @param description A plain text description
	 */
	public ShoppinglistItem(Shoppinglist shoppinglist, String description) {
		this();
		setShoppinglistId(shoppinglist.getId());
		setDescription(description);
	}
	
	/**
	 * Constructor for creating a new ShoppinglistItem from an offer, and attach
	 * it to a {@link Shoppinglist}.
	 * @param shoppinglist A list to assiciate this item with
	 * @param offer An offer to attach to this item
	 */
	public ShoppinglistItem(Shoppinglist shoppinglist, Offer offer) {
		this();
		setShoppinglistId(shoppinglist.getId());
		setOffer(offer);
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
			EtaLog.e(TAG, "", e);
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
		String date = Json.valueOf(jSli, ServerKey.MODIFIED, Utils.DATE_EPOC);
		sli.setModified( Utils.parseDate(date) );
		sli.setPreviousId(Json.valueOf(jSli, ServerKey.PREVIOUS_ID, null));
		
		// A whole lot of 'saving my ass from exceptions' for meta
		String metaString = Json.valueOf(jSli, ServerKey.META, "{}").trim();
		// If it looks like a JSONObject, try parsing it
		if (metaString.startsWith("{") && metaString.endsWith("}")) {
			
			try {
				sli.setMeta(new JSONObject(metaString));
			} catch (JSONException e) {
				EtaLog.e(TAG, "", e);
				sli.setMeta(new JSONObject());
				sli.setModified(new Date());
			}
			
		} else {
			sli.setMeta(new JSONObject());
			sli.setModified(new Date());
		}
		
		return sli;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = super.toJSON();
		try {
			o.put(ServerKey.TICK, Json.nullCheck(isTicked()));
			o.put(ServerKey.OFFER_ID, Json.nullCheck(getOfferId()));
			o.put(ServerKey.COUNT, getCount());
			o.put(ServerKey.DESCRIPTION, Json.nullCheck(getDescription()));
			o.put(ServerKey.SHOPPINGLIST_ID, Json.nullCheck(getShoppinglistId()));
			o.put(ServerKey.CREATOR, Json.nullCheck(getCreator()));
			o.put(ServerKey.MODIFIED, Json.nullCheck(Utils.parseDate(getModified())));
			o.put(ServerKey.PREVIOUS_ID, Json.nullCheck(getPreviousId()));
			o.put(ServerKey.META, Json.nullCheck(getMeta()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	@Override
	public String getErnPrefix() {
		return ERN_SHOPPINGLISTITEM;
	}
	
	/**
	 * Get the description for this object
	 * @return A description, or an empty String
	 */
	public String getDescription() {
		return mDescription == null ? "" : mDescription;
	}
	
	/**
	 * Set the description for this ShoppinglistItem
	 * @param description A description
	 * @return This object
	 */
	public ShoppinglistItem setDescription(String description) {
		mDescription = description;
		return this;
	}
	
	/**
	 * Get the count.
	 * <p>Count represents the physical amount or number of items to get of this
	 * ShoppinglistItem, (such as 6 eggs or 500g of flour)</p>
	 * @return
	 */
	public int getCount() {
		return mCount;
	}
	
	/**
	 * Set the count
	 * <p>Count represents the physical amount or number of items to get of this
	 * ShoppinglistItem, (such as 6 eggs or 500g of flour)</p>
	 * @param count A number representing some type of 'amount'
	 * @return This object
	 */
	public ShoppinglistItem setCount(int count) {
		mCount = count;
		return this;
	}
	
	/**
	 * Whether this item it ticked, or not
	 * <p>Tick represents a notation of whether the item have been purchased or 
	 * is in the shopping basket</p>
	 * @return true if item have been ticked, else false
	 */
	public boolean isTicked() {
		return mTick;
	}
	
	/**
	 * Set the tick state of this item.
	 * <p>Tick represents a notation of whether the item have been purchased or 
	 * is in the shopping basket</p>
	 * @param tick The ticked state
	 * @return This object
	 */
	public ShoppinglistItem setTick(boolean tick) {
		mTick = tick;
		return this;
	}
	
	/**
	 * Get the offer associated with this object.
	 * @return An offer, or null
	 */
	public Offer getOffer() {
		return mOffer;
	}
	
	/**
	 * Attach an offer with this ShoppinglistItem.
	 * <p>To keep the object 'sane', this will also trigger
	 * {@link ShoppinglistItem#setOfferId(String) setOfferId(String)}, and if
	 * necessary {@link ShoppinglistItem#setDescription(String) setDescription(String)}
	 * </p> <p>
	 * (updating description is considered necessary if it's current value is
	 * {@code null}, or {@link Offer#getId() offer.getId()} is not equal to
	 * {@link #getOfferId() getOfferId()})</p>
	 * @param offer An offer to attach to this item
	 * @return This object
	 */
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
	
	/**
	 * Get the creator of this list (an e-mail address).
	 * @return The creator, or null
	 */
	public String getCreator() {
		return mCreator;
	}
	
	/**
	 * Set the creator of this list.
	 * <p>Creator must be an e-mail address, and must be a valid eTilbudsavis user</p>
	 * @param creator The creator of this list
	 * @return This object
	 */
	public ShoppinglistItem setCreator(String creator) {
		mCreator = creator;
		return this;
	}
	
	/**
	 * Get the id of the {@link Shoppinglist} that this item is attached to.
	 * @return A shoppinglist id, or null
	 */
	public String getShoppinglistId() {
		return mShoppinglistId;
	}
	
	/**
	 * Set the id of the {@link Shoppinglist} that should have an association
	 * to this item.
	 * @param id The shoppinglist id
	 * @return This object
	 */
	public ShoppinglistItem setShoppinglistId(String id) {
		mShoppinglistId = id;
		return this;
	}
	
	/**
	 * Get the id of the previous {@link ShoppinglistItem}.
	 * <p>{@code previous_id} is used primarily for drawing the ShoppinglistItems
	 * in the correct order when presenting the items for the user.
	 * The first item to draw will have the {@code previous_id} 
	 * {@link EtaListObject#FIRST_ITEM FIRST_ITEM}, the next item should then
	 * point at this items {@link #getId() id}, and so on</p>
	 * @return The previous id, or {@code null}
	 */
	public String getPreviousId() {
		return mPrevId;
	}
	
	/**
	 * Set the previous id of this item.
	 * <p>When updating one {@code prevoius_id} you would probably have to update
	 * several other {@link ShoppinglistItem} {@code previous_id}, if you are
	 * using the SDK's {@link ListManager}, this should be handled automatically.</p>
	 * <p>{@code previous_id} is used primarily for drawing the ShoppinglistItems
	 * in the correct order when presenting the items for the user.
	 * The first item to draw will have the {@code previous_id} 
	 * {@link EtaListObject#FIRST_ITEM FIRST_ITEM}, the next item should then
	 * point at this items {@link #getId() id}, and so on</p>
	 * @param id
	 * @return
	 */
	public ShoppinglistItem setPreviousId(String id) {
		mPrevId = id;
		return this;
	}
	
	/**
	 * Get the id of the offer associated with this item.
	 * @return A offer id, or null if no offer is associated
	 */
	public String getOfferId() {
		return mOfferId;
	}

	/**
	 * Set the offer id associated with this item
	 * @return This object
	 */
	public ShoppinglistItem setOfferId(String offerId) {
		mOfferId = offerId;
		return this;
	}
	
	/**
	 * Get the last-modified date
	 * @return A last-modified date
	 */
	public Date getModified() {
		return mModified;
	}
	
	/**
	 * Set the {@link Date} for when this object was last modified.
	 * <p>When using the SDK's {@link ListManager} this value is automatically
	 * set when using it's methods for ShoppinglistItem operations</p>
	 * @param time A date
	 * @return This object
	 */
	public ShoppinglistItem setModified(Date time) {
		mModified = Utils.roundTime(time);
		return this;
	}
	
	/**
	 * Get any meta data associated with this item.
	 * <p>Meta can be used for any kind of information, that is needed to
	 * describe any kind of information regarding this item. It's kind of a
	 * 'anything goes' item.</p>
	 * @return A meta object
	 */
	public JSONObject getMeta() {
		if (mMeta == null) {
			return new JSONObject();
		} else {
			try {
				return new JSONObject(mMeta);
			} catch (JSONException e) {
				EtaLog.e(TAG, "", e);
			}
		}
		return new JSONObject();
	}
	
	/**
	 * Set any meta information needed for this object.
	 * <p>Please ensure that you <b>do not accidentally override</b> information written by
	 * other apps, by reusing {@link #getMeta() meta} if it's present.</p>
	 * <p>Meta can be used for any kind of information, that is needed to
	 * describe any kind of information regarding this item. It's kind of a
	 * 'anything goes' item.</p>
	 * @param meta
	 * @return
	 */
	public ShoppinglistItem setMeta(JSONObject meta) {
		mMeta = meta == null ? "{}" : meta.toString();
		return this;
	}
	
	/**
	 * Get the id, of the user that has this item.
	 * <p>This is mostly a use case when storing the item in a DB, where several
	 * users can have access to the same item (same {@link ShoppinglistItem#getId()}.</p>
	 * @return A user id
	 */
	public int getUserId() {
		return mUserId;
	}
	
	/**
	 * Set the id of the user, that this item is associated with.
	 * <p>This is mostly a use case when storing the item in a DB, where several
	 * users can have access to the same item (same {@link ShoppinglistItem#getId()}.</p>
	 * @param userId An id of a user
	 * @return This object
	 */
	public ShoppinglistItem setUserId(int userId) {
		mUserId = userId;
		return this;
	}
	
	/**
	 * Get the comment set on this shoppinglistitem.
	 * <p>The comment is part of the {@link #getMeta() meta}-blob, and therefore
	 * has very few restrictions</p>
	 * @return A comment, or {@code null}
	 */
	public String getComment() {
		String comment = Json.valueOf(getMeta(), EtaObject.MetaKey.COMMENT);
		comment = (comment != null && comment.length() > 0) ? comment : null;
		return comment;
	}
	
	/**
	 * Set a comment on the shoppinglistitem. Setting comment to {@code null}
	 * will delete the {@link MetaKey#COMMENT comment}-key altogether.
	 * <p>The comment is part of the {@link #getMeta() meta}-blob, and therefore
	 * has very few restrictions</p>
	 * @param comment The comment to set on the shoppinglistitem, {@code null} to delete
	 * @return This object
	 */
	public ShoppinglistItem setComment(String comment) {
		try {
			JSONObject meta = getMeta();
			meta.put(MetaKey.COMMENT, comment);
			setMeta(meta);
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return this;
	}
	
	/**
	 * Compare method, that uses the {@link ShoppinglistItem#getTitle() title}
	 * to compare two items.
	 */
	public int compareTo(ShoppinglistItem another) {
		return TitleAscending.compare(this, another);
	}

	/**
	 * Compare object, that uses the {@link ShoppinglistItem#getDescription() description}
	 * to compare two items.
	 */
	public static Comparator<ShoppinglistItem> TitleAscending  = new Comparator<ShoppinglistItem>() {

		public int compare(ShoppinglistItem item1, ShoppinglistItem item2) {
			
			if (item1 == null || item2 == null) {
				return item1 == null ? (item2 == null ? 0 : 1) : -1;
			} else {
				String t1 = item1.getDescription();
				String t2 = item2.getDescription();
				if (t1 == null || t2 == null) {
					return t1 == null ? (t2 == null ? 0 : 1) : -1;
				}
				
				//ascending order
				return t1.compareToIgnoreCase(t2);
			}
			
		}

	};

	/**
	 * Compare object, that uses {@link ShoppinglistItem#getModified() modified}
	 * to compare two items.
	 */
	public static Comparator<ShoppinglistItem> ModifiedDescending  = new Comparator<ShoppinglistItem>() {

		public int compare(ShoppinglistItem item1, ShoppinglistItem item2) {
			
			if (item1 == null || item2 == null) {
				return item1 == null ? (item2 == null ? 0 : 1) : -1;
			} else {
				Date d1 = item1.getModified();
				Date d2 = item2.getModified();
				if (d1 == null || d2 == null) {
					return d1 == null ? (d2 == null ? 0 : 1) : -1;
				}
				
				// Descending order
				return d2.compareTo(d1);
			}
			
		}

	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + mCount;
		result = prime * result
				+ ((mCreator == null) ? 0 : mCreator.hashCode());
		result = prime * result
				+ ((mDescription == null) ? 0 : mDescription.hashCode());
		result = prime * result + ((mMeta == null) ? 0 : mMeta.hashCode());
		result = prime * result
				+ ((mModified == null) ? 0 : mModified.hashCode());
		result = prime * result + ((mOffer == null) ? 0 : mOffer.hashCode());
		result = prime * result
				+ ((mOfferId == null) ? 0 : mOfferId.hashCode());
		result = prime * result + ((mPrevId == null) ? 0 : mPrevId.hashCode());
		result = prime * result
				+ ((mShoppinglistId == null) ? 0 : mShoppinglistId.hashCode());
		result = prime * result + (mTick ? 1231 : 1237);
		result = prime * result + mUserId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, false, false);
	}
	
	public boolean equals(Object obj, boolean skipModified, boolean skipSyncState) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
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
		
		if (!skipModified) {
			if (mModified == null) {
				if (other.mModified != null)
					return false;
			} else if (!mModified.equals(other.mModified))
				return false;
		}
		
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
