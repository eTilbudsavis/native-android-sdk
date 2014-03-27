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
package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.eTilbudsavis.etasdk.EtaObjects.EtaListObject.State;
import com.eTilbudsavis.etasdk.EtaObjects.EtaObject.ServerKey;
import com.eTilbudsavis.etasdk.EtaObjects.Share;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

/**
 * This class provides methods, for easily handling of
 * {@link Shoppinglist Shoppinglists}, {@link ShoppinglistItem ShoppinglistItems},
 * and {@link Share Shares}, without having to worry about keeping a sane, and
 * synchronizing state with both the {@link DbHelper database} and, the
 * eTilbudsavis API.
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class ListManager {
	
	public static final String TAG = "ListManager";
	
	/** The global {@link Eta} object */
	private Eta mEta;
	
	/** Subscriber queue for shopping list changes */
	private List<OnChangeListener> mListSubscribers = new ArrayList<OnChangeListener>();
	
	/** The notification service for ListManager, this allows for bundling
	 * list and item notifications, to avoid multiple updates for a single operation */
	private ListNotification mNotification = new ListNotification(false);
	
	/**
	 * Default constructor for ListManager.
	 * @param eta The {@link Eta} instance to use
	 */
	public ListManager(Eta eta) {
		mEta = eta;
	}
	
	/**
	 * Get a {@link Shoppinglist} from it's ID.
	 * @param id A {@link Shoppinglist} id
	 * @return A shopping list, or {@code null}
	 */
	public Shoppinglist getList(String id) {
		return DbHelper.getInstance().getList(id, user());
	}
	
	/**
	 * The complete set of {@link Shoppinglist Shoppinglists}, that the current
	 * user has.
	 * @return A {@link List} of {@link Shoppinglist}, for current {@link User}
	 */
	public List<Shoppinglist> getLists() {
		return DbHelper.getInstance().getLists(user()); 
	}
	
	/**
	 * Add a new {@link Shoppinglist} to the current {@link User}
	 * 
	 * <p>If owner haven't been set, we will assume that it is the current
	 * {@link User} being used by the SDK, is the owner.</p>
	 * 
	 * <p>Changes are synchronized to the API when and if possible.<br>
	 * 
	 * @param sl A shoppinglist to add to the database
	 */
	public boolean addList(final Shoppinglist sl) {
		
		DbHelper db = DbHelper.getInstance();
		
		sl.setModified(new Date());
		
		final User user = user();
		
		Share owner = sl.getOwner();
		if (owner == null || owner.getEmail() == null) {
			JSONObject o = new JSONObject();
			try {
				JSONObject u = new JSONObject();
				u.put(ServerKey.EMAIL, mEta.getUser().getEmail());
				u.put(ServerKey.NAME, mEta.getUser().getName());
				o.put(ServerKey.USER, u);
				o.put(ServerKey.ACCEPTED, true);
				o.put(ServerKey.ACCESS, Share.ACCESS_OWNER);
				owner = Share.fromJSON(o);
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
			List<Share> shares = new ArrayList<Share>(1);
			shares.add(owner);
			sl.putShares(shares);

			owner.setShoppinglistId(sl.getId());
			db.insertShare(owner, user);
			
		}
		
		sl.setPreviousId(ShoppinglistItem.FIRST_ITEM);
		sl.setState(Shoppinglist.State.TO_SYNC);
		
		Shoppinglist first = db.getFirstList(user);
		if (first != null) {
			first.setPreviousId(sl.getId());
			first.setModified(new Date());
			first.setState(Shoppinglist.State.TO_SYNC);
			db.editList(first, user);
		}
		
		int count = db.insertList(sl, user);
		boolean success = count == 1;
		if (success) {
			mNotification.add(sl);
		}
		notifySubscribers(mNotification);
		return success;
	}
	
	/**
	 * Edit a shopping list already in the database.
	 * 
	 * <p>The {@link Shoppinglist} will replace data already in the
	 * {@link DbHelper database}, and changes will later be synchronized to the
	 * API if possible.</p>
	 * @param sl A shoppinglist that have been edited
	 */
	public boolean editList(Shoppinglist sl) {
		return editList(sl, user());
	}
	
	private boolean editList(Shoppinglist sl, User user) {
		
		DbHelper db = DbHelper.getInstance();
		
		Map<String, Share> dbShares = new HashMap<String, Share>();
		for (Share s : db.getShares(sl, user, false)) {
			dbShares.put(s.getEmail(), s);
		}
		
		Map<String, Share> slShares = sl.getShares();
		
		/* User have remove it self. Then only set the DELETE state on the share,
		 * ListSyncManager will delete from DB Once it's synced the changes to API
		 */
		if (!slShares.containsKey(user.getEmail())) {
			Share dbShare = dbShares.get(user.getEmail());
			dbShare.setState(Share.State.DELETE);
			db.editShare(dbShare, user);
			mNotification.del(sl);
			notifySubscribers(mNotification);
			return true;
		}
		
		if (!canEdit(sl, slShares.get(user.getEmail()))) {
			EtaLog.d(TAG, String.format("User [%s], doesn't have rights to edit this list", user.getEmail()));
			return false;
		}
		
		HashSet<String> union = new HashSet<String>();
		union.addAll(slShares.keySet());
		union.addAll(dbShares.keySet());
		
		/* Variable for owner. If it has been removed from the sl-shares-list
		 * then we need to re-add it from the DB
		 */
		Share owner = null;
		
		for (String shareId : union) {
			
			if (dbShares.containsKey(shareId)) {
				
				Share dbShare = dbShares.get(shareId);
				
				if (slShares.containsKey(shareId)) {
					Share slShare = slShares.get(shareId);
					
					if (!dbShare.equals(slShare)) {
						slShare.setState(Share.State.TO_SYNC);
						db.editShare(slShare, user);
						mNotification.edit(sl);
					}
					
				} else {
					if (dbShare.getAccess().equals(Share.ACCESS_OWNER)) {
						owner = dbShare;
						EtaLog.d(TAG, "Owner cannot be removed from lists, owner will be reattached");
					} else {
						if (user.isLoggedIn()) {
							dbShare.setState(Share.State.DELETE);
							db.editShare(dbShare, user);
						} else {
							db.deleteShare(dbShare, user);
						}
						mNotification.edit(sl);
					}
				}
				
			} else {
				Share slShare = slShares.get(shareId);
				db.insertShare(slShare, user);
				mNotification.edit(sl);
			}
			
		}
		
		// If owner was removed, then re-insert it.
		if (owner != null) {
			sl.putShare(owner);
		}

		Date now = new Date();
		
		sl.setModified(now);
		sl.setState(Shoppinglist.State.TO_SYNC);
		
		// Check for changes in previous item, and update surrounding
		Shoppinglist oldList = db.getList(sl.getId(), user);
		if (oldList == null) {
			EtaLog.d(TAG, "No such list exists in DB, considder addList() instead");
			return false;
		}
		
		if (oldList.getPreviousId() != null && !oldList.getPreviousId().equals(sl.getPreviousId())) {
			
			// If there is an item pointing at sl, it needs to point at the oldList.prev
			Shoppinglist slAfter = db.getListPrevious(sl.getId(), user);
			if (slAfter != null) {
				slAfter.setPreviousId(oldList.getPreviousId());
				slAfter.setModified(now);
				slAfter.setState(State.TO_SYNC);
				db.editList(slAfter, user);
				mNotification.edit(slAfter);
			}
			
			// If some another sl was pointing at the same item, it should be pointing at sl
			Shoppinglist slSamePointer = db.getListPrevious(sl.getPreviousId(), user);
			if (slSamePointer != null) {
				slSamePointer.setPreviousId(sl.getId());
				slSamePointer.setModified(now);
				slSamePointer.setState(State.TO_SYNC);
				db.editList(slSamePointer, user);
				mNotification.edit(slSamePointer);
			}
			
		}
		
		int count = db.editList(sl, user);
		boolean success = count == 1;
		if (success) {
			mNotification.edit(sl);
		}
		notifySubscribers(mNotification);
		return success;
	}
	
	/**
	 * Delete a shopping list
	 * <p>The {@link Shoppinglist shoppinglist} is deleted from the local database,
	 * and changes are later synchronized to the server, when and if possible.</p>
	 * <p>All {@link ShoppinglistItem shoppinglistitems} associated with the
	 * {@link Shoppinglist shoppinglist} are also deleted.</p>
	 * @param sl A shoppinglist to delete
	 */
	public void deleteList(Shoppinglist sl) {
		User u = user();
		if (canEdit(sl, u)) {
			deleteList(sl, u);
		}
	}
	
	private boolean deleteList(Shoppinglist sl, User user) {
		
		DbHelper db = DbHelper.getInstance();
		
		Date now = new Date();
		
		sl.setModified(now);
		
		// Update previous pointer, to preserve order
		Shoppinglist after = db.getListPrevious(sl.getId(), user);
		if (after != null) {
			after.setPreviousId(sl.getPreviousId());
			after.setModified(now);
			after.setState(State.TO_SYNC);
			db.editList(after, user);
			mNotification.edit(after);
		}
		
		int count = 0;
		
		List<ShoppinglistItem> items = getItems(sl);
		
		if (mEta.getUser().isLoggedIn()) {
			
			for (ShoppinglistItem sli : items) {
				sli.setState(ShoppinglistItem.State.DELETE);
				sli.setModified(now);
				db.editItem(sli, user);
				mNotification.del(sli);
			}
			 
			// Update local version of shoppinglist
			sl.setState(Shoppinglist.State.DELETE);
			count = db.editList(sl, user);
			
		} else {

			for (ShoppinglistItem sli : items) {
				sli.setState(ShoppinglistItem.State.DELETE);
				sli.setModified(now);
				mNotification.del(sli);
			}
			
			count = db.deleteList(sl, user);
			// Actually delete the items in the offline version
			db.deleteShares(sl, user);
			db.deleteItems(sl.getId(), null, user);
			
		}
		
		boolean success = count == 1;
		if (success) {
			mNotification.del(sl);
		}
		notifySubscribers(mNotification);
		return success;
	}

	/**
	 * Get a {@link ShoppinglistItem} item by it's ID
	 * @param id A {@link ShoppinglistItem} id
	 * @return A shopping list item, or {@code null}
	 */
	public ShoppinglistItem getItem(String id) {
		return DbHelper.getInstance().getItem(id, user());
	}
	
	/**
	 * Get all {@link ShoppinglistItem ShoppinglistItems} associated with a
	 * {@link Shoppinglist}.
	 * @param sl A {@link Shoppinglist} to get {@link ShoppinglistItem ShoppinglistItems} from
	 * @return A list of {@link ShoppinglistItem ShoppinglistItems}
	 */
	public List<ShoppinglistItem> getItems(Shoppinglist sl) {
		List<ShoppinglistItem> items = DbHelper.getInstance().getItems(sl, user());
		Utils.sortItems(items);
		return items;
	}
	
	/**
	 * Add a {@link ShoppinglistItem} to a {@link Shoppinglist}
	 * 
	 * <p>{@link ShoppinglistItem ShoppinglistItems} are inserted into the
	 * database, and changes are synchronized to the server when and if possible.</p>
	 * @param sli A {@link ShoppinglistItem} to add to a {@link Shoppinglist}
	 */
	public boolean addItem(ShoppinglistItem sli) {
		return addItem(sli, true, user());
	}
	
	/**
	 * Add a {@link ShoppinglistItem} to a {@link Shoppinglist}
	 * 
	 * <p>{@link ShoppinglistItem ShoppinglistItems} are inserted into the
	 * database, and changes are synchronized to the server when and if possible.</p>
	 * @param sli A {@link ShoppinglistItem} to add to a {@link Shoppinglist}
	 * @param incrementCount Increment the count on the {@link ShoppinglistItem}
	 * if an item like it exists, instead of adding new item.
	 * @param user A user that owns the {@link ShoppinglistItem}
	 */
	@SuppressLint("DefaultLocale") 
	public boolean addItem(ShoppinglistItem sli, boolean incrementCount, User user) {
		
		if (!canEdit(sli.getShoppinglistId(), user)) {
			EtaLog.d(TAG, "The user cannot edit the given ShoppinglistItem");
			return false;
		}
		
		if (sli.getOfferId() == null && sli.getDescription() == null) {
			EtaLog.d(TAG, "The ShoppinglistItem neither has offerId, or"
					+ "description, one or the other this is required by the API");
			return false;
		}
		
		DbHelper db = DbHelper.getInstance();
		
		Date now = new Date();
		sli.setModified(now);
		sli.setState(ShoppinglistItem.State.TO_SYNC);
		
		// If the item exists in DB, then just increase count and edit the item
		if (incrementCount) {
			
			List<ShoppinglistItem> items = db.getItems(sli.getShoppinglistId(), user, false);
			
			if (sli.getOfferId() != null) {
				for (ShoppinglistItem s : items) {
					if (sli.getOfferId().equals(s.getOfferId())) {
						s.setCount(s.getCount() + 1);
						return editItem(s);
					}
				}
			} else {
				for (ShoppinglistItem s : items) {
					String oldDesc = s.getDescription();
					String newDesc = sli.getDescription().toLowerCase();
					if (oldDesc != null && newDesc.equals(oldDesc.toLowerCase())) {
						s.setCount(s.getCount() + 1);
						return editItem(s);
					}
				}
			}
		}

		Shoppinglist sl = getList(sli.getShoppinglistId());

		if (sl == null) {
			EtaLog.d(TAG, "The shoppinglist id on the shoppinglist item, could"
					+ "not be found, please add a shoppinglist before adding items");
			return false;
		}
		
		// Set the creator of not done yet
		if (sli.getCreator() == null) {
			if (user.getName() != null && user.getName().length() > 0) {
				sli.setCreator(user.getName());
			} else {
				sli.setCreator(user.getEmail());
			}
		}
		
		sli.setPreviousId(ShoppinglistItem.FIRST_ITEM);
		ShoppinglistItem first = db.getFirstItem(sli.getShoppinglistId(), user);
		if (first != null) {
			first.setPreviousId(sli.getId());
			first.setModified(now);
			first.setState(ShoppinglistItem.State.TO_SYNC);
			db.editItem(first, user);
			mNotification.edit(first);
		}
		
		int count = db.insertItem(sli, user);
		boolean success = count == 1;
		if (success) {
			/* Update SL info, but not state. This will prevent sync, and API
			 * will auto update the modified tag, nice! */
			sl.setModified(now);
			db.editList(sl, user);
			mNotification.edit(sl);
			mNotification.add(sli);
		}
		notifySubscribers(mNotification);
		return success;
	}
	
	/**
	 * Insert an updated {@link ShoppinglistItem} into the database.
	 * <p>The {@link ShoppinglistItem} is replaced in the database, and changes
	 * is synchronized to the server when, and if possible.</p>
	 * @param sli An edited {@link ShoppinglistItem}
	 */
	public boolean editItem(ShoppinglistItem sli) {
		User u = user();
		if (!canEdit(sli.getShoppinglistId(), u)) {
			EtaLog.d(TAG, "The user cannot edit the given ShoppinglistItem");
			return false;
		}
		return editItem(sli, u);
	}
	
	private boolean editItem(final ShoppinglistItem sli, User user) {
		
		
		DbHelper db = DbHelper.getInstance();
		
		Date now = new Date();
		sli.setModified(now);
		sli.setState(ShoppinglistItem.State.TO_SYNC);
		
		// Check for changes in previous item, and update surrounding
		ShoppinglistItem oldItem = db.getItem(sli.getId(), user);
		if (oldItem == null) {
			EtaLog.d(TAG, "No such item exists, considder addItem() instead: " + sli.toString());
			return false;
		}
		
		if (!oldItem.getPreviousId().equals(sli.getPreviousId())) {
			
			String sl = sli.getShoppinglistId();
			
			// If there is an item pointing at sli, it needs to point at the oldSli.prev
			ShoppinglistItem sliAfter = db.getItemPrevious(sl, sli.getId(), user);
			if (sliAfter != null) {
				sliAfter.setPreviousId(oldItem.getPreviousId());
				sliAfter.setModified(now);
				sliAfter.setState(ShoppinglistItem.State.TO_SYNC);
				db.editItem(sliAfter, user);
				mNotification.edit(sliAfter);
			}
			
			// If some another sli was pointing at the same item, it should be pointing at sli
			ShoppinglistItem sliSamePointer = db.getItemPrevious(sl, sli.getPreviousId(), user);
			if (sliSamePointer != null) {
				sliSamePointer.setPreviousId(sli.getId());
				sliSamePointer.setModified(now);
				sliSamePointer.setState(ShoppinglistItem.State.TO_SYNC);
				db.editItem(sliSamePointer, user);
				mNotification.edit(sliSamePointer);
			}
			
		}
		boolean success = (db.editItem(sli, user) == 1);
		if (success) {
			/* Update SL info, but not state. This will prevent sync, and API
			 * will auto update the modified tag, nice!
			 */
			Shoppinglist sl = getList(sli.getShoppinglistId());
			sl.setModified(now);
			db.editList(sl, user);
			mNotification.edit(sl);
			mNotification.edit(sli);
		}
		notifySubscribers(mNotification);
		return success;
	}

	/**
	 * Delete all {@link ShoppinglistItem ShoppinglistItems} from a
	 * {@link Shoppinglist} where {@link ShoppinglistItem#isTicked() isTicked()}
	 * is {@code true}.
	 * <P>Changes are synchronized to the server when, and if possible.</p>
	 * @param sl A {@link Shoppinglist} to delete the
	 * 			{@link ShoppinglistItem ShoppinglistItems} from
	 */
	public void deleteItemsTicked(Shoppinglist sl) {
		deleteItems(sl, true, user());
	}

	/**
	 * Delete all {@link ShoppinglistItem ShoppinglistItems} from a
	 * {@link Shoppinglist} where {@link ShoppinglistItem#isTicked() isTicked()}
	 * is {@code false}.
	 * <P>Changes are synchronized to the server when, and if possible.</p>
	 * @param sl A {@link Shoppinglist} to delete the
	 * 			{@link ShoppinglistItem ShoppinglistItems} from
	 */
	public void deleteItemsUnticked(Shoppinglist sl) {
		deleteItems(sl, false, user());
	}

	/**
	 * Delete all {@link ShoppinglistItem ShoppinglistItems} from a {@link Shoppinglist}
	 * 
	 * <p>Changes are synchronized to the server when, and if possible.</p>
	 * @param sl A {@link Shoppinglist} to delete the
	 * 				{@link ShoppinglistItem ShoppinglistItems} from
	 */
	public void deleteItemsAll(Shoppinglist sl) {
		deleteItems(sl, null, user());
	}
	
	/**
	 * Method to delete all {@link ShoppinglistItem} that matches a given state.
	 * 
	 * <p>The possible states are:</p>
	 * <ul>
	 * 		<li>{@code true} - delete ticked items</li>
	 * 		<li>{@code false} - delete unticked items</li>
	 * 		<li>{@code null} - delete all items</li>
	 * </ul>
	 * 
	 * <p>Changes are synchronized to the server when, and if possible.</p>
	 * 
	 * @param sl A {@link Shoppinglist} to delete
	 * 				{@link ShoppinglistItem ShoppinglistItems} from
	 * @param stateToDelete A state that describes what to delete
	 * @param user the user that owns the {@link ShoppinglistItem ShoppinglistItems}
	 */
	private boolean deleteItems(final Shoppinglist sl, Boolean stateToDelete, User user) {
		
		if (!canEdit(sl, user))
			return false;
		
		DbHelper db = DbHelper.getInstance();
		
		Date now = new Date();
		
        List<ShoppinglistItem> list = getItems(sl);
        int count = 0;

		String preGoodId = ShoppinglistItem.FIRST_ITEM;
		
		for (ShoppinglistItem sli : list) {
			if (stateToDelete == null) {
				// Delete all items
				mNotification.del(sli);
			} else if (sli.isTicked() == stateToDelete) {
				// Delete if ticked matches the requested state
				mNotification.del(sli);
			} else {
				if (!sli.getPreviousId().equals(preGoodId)) {
					sli.setPreviousId(preGoodId);
					sli.setModified(now);
					sli.setState(ShoppinglistItem.State.TO_SYNC);
					db.editItem(sli, user);
				}
				preGoodId = sli.getId();
			}
		}
		
		if (mEta.getUser().isLoggedIn()) {
			for (ShoppinglistItem sli : mNotification.getDeletedItems()) {
				sli.setState(ShoppinglistItem.State.DELETE);
				sli.setModified(now);
				count += db.editItem(sli, user);
			}
		} else {
			count = db.deleteItems(sl.getId(), stateToDelete, user) ;
		}
		
		boolean success = count == mNotification.getDeletedItems().size();
		if (success) {
			/* Update SL info, but not state. This will prevent sync, and API
			 * will auto update the modified tag, nice!
			 */
			sl.setModified(now);
			db.editList(sl, user);
			mNotification.edit(sl);
		} else {
			// TODO handle shit? We might have some changes in items, but not all
			mNotification.clear();
		}
		notifySubscribers(mNotification);
		return success;
	}
	
	/**
	 * Deletes a {@link ShoppinglistItem}
	 * <p>The {@link ShoppinglistItem} is removed from the database, and later
	 * changes is synchronized to the server when and if possible</p>
	 * @param sli A {@link ShoppinglistItem} to delete
	 */
	public boolean deleteItem(ShoppinglistItem sli) {
		User u = user();
		if (!canEdit(sli.getShoppinglistId(), u)){
			EtaLog.d(TAG, "The user cannot edit the given ShoppinglistItem");
			return false;
		}
		return deleteItem(sli, u);
	}
	
	private boolean deleteItem(ShoppinglistItem sli, User user) {
		
		DbHelper db = DbHelper.getInstance();
		
		Date now = new Date();
		
		sli.setModified(now);

		// Update previous pointer
		ShoppinglistItem after = db.getItemPrevious(sli.getShoppinglistId(), sli.getId(), user);
		if (after != null) {
			after.setPreviousId(sli.getPreviousId());
			after.setModified(now);
			db.editItem(after, user);
			mNotification.edit(after);
		}
		
		int count = 0;
		if (user.getUserId() != User.NO_USER) {
			sli.setState(ShoppinglistItem.State.DELETE);
			count = db.editItem(sli, user);
		} else {
			count = db.deleteItem(sli, user);
		}
		boolean success = count == 1;
		if (success) {
			/* Update shoppinglist modified, but not state, so we have correct
			 * state but won't have to sync changes to API.
			 * API will change state based on the synced item.
			 */
			Shoppinglist sl = getList(sli.getShoppinglistId());
			sl.setModified(now);
			db.editList(sl, user);
			mNotification.edit(sl);
			
			mNotification.del(sli);
		}
		notifySubscribers(mNotification);
		return success;
	}
	
	/**
	 * Get the current user.
	 * <p>wrapper method for: Eta.getInstance().getUser()</p>
	 * @return A {@link User}
	 */
	private User user() {
		return Eta.getInstance().getUser();
	}
	
	/**
	 * Method that determines if a {@link User} can edit a {@link Shoppinglist}
	 * @param shoppinglistId A {@link Shoppinglist Shoppinglist id} to check edit rights on
	 * @param user The {@link User} that wants to edit the {@link Shoppinglist} 
	 * @return {@code true} if the {@link User} can edit the list, else {@code false}
	 */
	public boolean canEdit(String shoppinglistId, User user) {
		if (shoppinglistId == null || user == null) {
			return false;
		}
		Shoppinglist sl = DbHelper.getInstance().getList(shoppinglistId, user());
		return sl == null ? false : canEdit(sl, user);
	}
	
	/**
	 * Method that determines if a {@link User} can edit a {@link Shoppinglist}
	 * @param sl A {@link Shoppinglist} to check edit rights on
	 * @param user The {@link User} that wants to edit the {@link Shoppinglist} 
	 * @return {@code true} if the {@link User} can edit the list, else {@code false}
	 */
	public boolean canEdit(Shoppinglist sl, User user) {
		if (sl == null || user == null) {
			return false;
		}
		Share s = sl.getShares().get(user.getEmail());
		return s == null ? false : canEdit(sl, s);
	}

	/**
	 * Method that determines if a {@link Share} has sufficient rights to edit
	 * a {@link Shoppinglist}
	 * @param sl A {@link Shoppinglist} to check edit rights on
	 * @param share The {@link Share} that wants to edit the {@link Shoppinglist} 
	 * @return {@code true} if the {@link Share} can edit the list, else {@code false}
	 */
	public boolean canEdit(Shoppinglist sl, Share share) {
		if (share==null||sl==null) {
			return false;
		}
		boolean isInList = share.getShoppinglistId().equals(sl.getId());
		return isInList && ( share.getAccess().equals(Share.ACCESS_OWNER) || share.getAccess().equals(Share.ACCESS_READWRITE) );
	}
	
	/**
	 * Deletes all rows in the {@link DbHelper database}.
	 */
	public void clear() {
		DbHelper.getInstance().clear();
	}

	/**
	 * Deletes all rows in the {@link DbHelper database} associated with a
	 * given{@link User}.
	 */
	public void clear(int userId) {
		DbHelper.getInstance().clear(userId);
	}
	
	/**
	 * Method to call on all onResume events.
	 * <p>This is implicitly handled by the {@link Eta} instance</p>
	 */
	public void onResume() {
	}
	
	/**
	 * Method to call on all onPause events.
	 * <p>This is implicitly handled by the {@link Eta} instance</p>
	 */
	public void onPause() {
	}
	
	/**
	 * Method for subscribing to changes in {@link Shoppinglist Shoppinglists}
	 * and {@link ShoppinglistItem ShoppinglistItems}.
	 * @param l A {@link OnChangeListener} for receiving events
	 */
	public void setOnChangeListener(OnChangeListener l) {
		if (!mListSubscribers.contains(l)) {
			mListSubscribers.add(l);
		}
	}
	
	/**
	 * Unsubscribe from changes in {@link Shoppinglist} and {@link ShoppinglistItem}
	 * states.
	 * @param l The {@link OnChangeListener} to remove
	 */
	public void removeOnChangeListener(OnChangeListener l) {
		mListSubscribers.remove(l);
	}
	
	/**
	 * Method for notifying all {@link OnChangeListener subscribers} on a given
	 * set of events.
	 * @param n A set of changes in {@link Shoppinglist} and {@link ShoppinglistItem}
	 */
	public void notifySubscribers(final ListNotification n) {
		
		// Make sure that messages are passed on to the UI thread
		Eta.getInstance().getHandler().post(new Runnable() {
			
			public void run() {
				
				if (n.isFirstSync()) {
					
					for (OnChangeListener s : mListSubscribers) {
						s.onFirstSync();
					}
					
				}
				
				boolean list = n.hasListNotifications();
				boolean item = n.hasItemNotifications();
				
				if (list || item) {
					
					for (final OnChangeListener s : mListSubscribers) {
						if (list) {
							s.onListUpdate(n.isServer(), n.getAddedLists(), n.getDeletedLists(), n.getEditedLists());
						}
						if (item) {
							s.onItemUpdate(n.isServer(), n.getAddedItems(), n.getDeletedItems(), n.getEditedItems());
						}
					}
					n.clear();
					
				}

			}
		});
		
	}
	
	/**
	 * Interface for receiving notifications on list, and item changes.
	 * 
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public interface OnChangeListener {
		
        /**
         * Callback method for receiving updates on {@link Shoppinglist} updates.
         * 
         * @param isServer 
         * 			True if changes are from the API, else false
         * @param added 
         * 			A list of added {@link Shoppinglist}
         * @param deleted
         * 			A list of deleted {@link Shoppinglist}
         * @param edited 
         * 			A list of edited {@link Shoppinglist}. Edits might not be
         * significant (e.g. name or share changes), but can happen when an item
         * it has a reference to changes (so tick/untick on an item, also forces
         * modified on the containing {@link Shoppinglist} to update, hence an
         * edited list).
         */
		public void onListUpdate(boolean isServer, List<Shoppinglist> added, List<Shoppinglist> deleted, List<Shoppinglist> edited);
		
		/**
         * Callback method for receiving updates on {@link ShoppinglistItem} updates.
         * Updates to items, will also be reflected in the callback
         * {{@link #onListUpdate(boolean, List, List, List)} where the
         * {@link Shoppinglist}(s) containing any of the added/deleted/edited
         * {@link ShoppinglistItem} will also have been edited.
         * 
		 * @param isServer
		 * 			True if changes are from the API, else false
		 * @param added
         * 			A list of the added {@link ShoppinglistItem}
		 * @param deleted
         * 			A list of the deleted {@link ShoppinglistItem}
		 * @param edited
         * 			A list of the edited {@link ShoppinglistItem}
		 */
		public void onItemUpdate(boolean isServer, List<ShoppinglistItem> added, List<ShoppinglistItem> deleted, List<ShoppinglistItem> edited);
		
		/**
		 * Callback method notifying of the first successful synchronization
		 * iteration. And only if the {@link OnChangeListener} was subscribed
		 * prior to the first successful iteration.
		 */
		public void onFirstSync();
			
	}
	
}
