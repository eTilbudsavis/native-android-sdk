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

import android.annotation.SuppressLint;

import com.eTilbudsavis.etasdk.bus.ShoppinglistEvent;
import com.eTilbudsavis.etasdk.database.DatabaseWrapper;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Share;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.User;
import com.eTilbudsavis.etasdk.model.interfaces.SyncState;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.utils.ListUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * This class provides methods, for easily handling of
 * {@link Shoppinglist Shoppinglists}, {@link ShoppinglistItem ShoppinglistItems},
 * and {@link Share Shares}, without having to worry about keeping a sane, and
 * synchronizing state with both the {@link DatabaseWrapper database} and, the
 * eTilbudsavis API.
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class ListManager {
	
	public static final String TAG = Constants.getTag(ListManager.class);
	
	/** The global {@link Eta} object */
	private Eta mEta;
	private DatabaseWrapper mDatabase;

	/** The notification service for ListManager, this allows for bundling
	 * list and item notifications, to avoid multiple updates for a single operation */
	private ShoppinglistEvent.Builder mBuilder = new ShoppinglistEvent.Builder(false);
	
	/**
	 * Default constructor for ListManager.
	 * @param eta The {@link Eta} instance to use
	 */
	public ListManager(Eta eta, DatabaseWrapper db) {
		mEta = eta;
		mDatabase = db;
	}
	
	/**
	 * Get a {@link Shoppinglist} from it's ID.
	 * @param id A {@link Shoppinglist} id
	 * @return A shopping list, or {@code null}
	 */
	public Shoppinglist getList(String id) {
		return mDatabase.getList(id, user());
	}
	
	/**
	 * The complete set of {@link Shoppinglist Shoppinglists}, that the current
	 * user has.
	 * @return A {@link List} of {@link Shoppinglist}, for current {@link User}
	 */
	public List<Shoppinglist> getLists() {
		return mDatabase.getLists(user());
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

        List<Shoppinglist> lists = new ArrayList<Shoppinglist>();
        lists.add(sl);

		sl.setModified(new Date());
		
		final User user = user();
		
		Share owner = sl.getOwner();
		if (owner == null || owner.getEmail() == null) {
            owner = new Share(user.getEmail(), Share.ACCESS_OWNER, null);
            owner.setName(user.getName());
            owner.setAccepted(true);
            owner.setShoppinglistId(sl.getId());
            sl.putShare(owner);
        }

        sl.setPreviousId(ListUtils.FIRST_ITEM);
		sl.setState(SyncState.TO_SYNC);

		Shoppinglist first = mDatabase.getFirstList(user);
		if (first != null) {
			first.setPreviousId(sl.getId());
			first.setModified(new Date());
			first.setState(SyncState.TO_SYNC);
            lists.add(first);
		}

		boolean success = mDatabase.insertLists(lists, user);
		if (success) {
			mBuilder.add(sl);
		}
		postShoppinglistEvent();
		return success;
	}
	
	/**
	 * Edit a shopping list already in the database.
	 * 
	 * <p>The {@link Shoppinglist} will replace data already in the
	 * {@link DatabaseWrapper database}, and changes will later be synchronized to the
	 * API if possible.</p>
	 * @param sl A shoppinglist that have been edited
	 */
	public boolean editList(Shoppinglist sl) {
		return editList(sl, user());
	}
	
	private boolean editList(Shoppinglist sl, User user) {

        Shoppinglist oldList = mDatabase.getList(sl.getId(), user);
        // Check for changes in previous item, and update surrounding
        if (oldList == null) {
            EtaLog.i(TAG, "No such list exists in the database. To add new items, use addList().");
            return false;
        }

        Map<String, Share> dbShares = oldList.getShares();
		Map<String, Share> slShares = sl.getShares();
		
		/* User have remove it self. Then only set the DELETE state on the share,
		 * SyncManager will delete from DB Once it's synced the changes to API
		 */
		if (!slShares.containsKey(user.getEmail())) {
			Share dbShare = dbShares.get(user.getEmail());
			if (dbShare != null) {
				dbShare.setState(SyncState.DELETE);
				mDatabase.editShare(dbShare, user);
				mBuilder.del(sl);
				postShoppinglistEvent();
				return true;
			} else {
				// The user isn't in either the Shoppinglist shares,
				// or our list of shares in DB
				return false;
			}
		}

		mDatabase.allowEditOrThrow(sl, user);

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
						slShare.setState(SyncState.TO_SYNC);
						mDatabase.editShare(slShare, user);
						mBuilder.edit(sl);
					}
					
				} else {
					if (dbShare.getAccess().equals(Share.ACCESS_OWNER)) {
						owner = dbShare;
						EtaLog.i(TAG, "Owner cannot be removed from lists, owner will be reattached");
					} else {
						if (user.isLoggedIn()) {
							dbShare.setState(SyncState.DELETE);
							mDatabase.editShare(dbShare, user);
						} else {
							mDatabase.deleteShare(dbShare, user);
						}
						mBuilder.edit(sl);
					}
				}
				
			} else {
				Share slShare = slShares.get(shareId);
				mDatabase.insertShare(slShare, user);
				mBuilder.edit(sl);
			}
			
		}
		
		// If owner was removed, then re-insert it.
		if (owner != null) {
			sl.putShare(owner);
		}

		Date now = new Date();
		
		sl.setModified(now);
		sl.setState(SyncState.TO_SYNC);

        List<Shoppinglist> lists = new ArrayList<Shoppinglist>();
        // TODO add lists to list and bulk edit, rather than separate queries

		if (oldList.getPreviousId() != null && !oldList.getPreviousId().equals(sl.getPreviousId())) {
			
			// If there is an item pointing at sl, it needs to point at the oldList.prev
			Shoppinglist slAfter = mDatabase.getListPrevious(sl.getId(), user);
			if (slAfter != null) {
				slAfter.setPreviousId(oldList.getPreviousId());
				slAfter.setModified(now);
				slAfter.setState(SyncState.TO_SYNC);
				mDatabase.editList(slAfter, user);
				mBuilder.edit(slAfter);
			}
			
			// If some another sl was pointing at the same item, it should be pointing at sl
			Shoppinglist slSamePointer = mDatabase.getListPrevious(sl.getPreviousId(), user);
			if (slSamePointer != null) {
				slSamePointer.setPreviousId(sl.getId());
				slSamePointer.setModified(now);
				slSamePointer.setState(SyncState.TO_SYNC);
				mDatabase.editList(slSamePointer, user);
				mBuilder.edit(slSamePointer);
			}
			
		}

		boolean success = mDatabase.editList(sl, user);
		if (success) {
			mBuilder.edit(sl);
		}
		postShoppinglistEvent();
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
		mDatabase.allowEditOrThrow(sl, u);
		deleteList(sl, u);
	}
	
	private boolean deleteList(Shoppinglist sl, User user) {
		
		Date now = new Date();
		
		sl.setModified(now);
		
		// Update previous pointer, to preserve order
		Shoppinglist after = mDatabase.getListPrevious(sl.getId(), user);
		if (after != null) {
			after.setPreviousId(sl.getPreviousId());
			after.setModified(now);
			after.setState(SyncState.TO_SYNC);
			mDatabase.editList(after, user);
			mBuilder.edit(after);
		}

		boolean success = false;
		
		List<ShoppinglistItem> items = getItems(sl);
		
		if (user.isLoggedIn()) {
			
			for (ShoppinglistItem sli : items) {
				sli.setState(SyncState.DELETE);
				sli.setModified(now);
				mDatabase.editItem(sli, user);
				mBuilder.del(sli);
			}
			 
			// Update local version of shoppinglist
			sl.setState(SyncState.DELETE);
			success = mDatabase.editList(sl, user);

		} else {

			for (ShoppinglistItem sli : items) {
				sli.setState(SyncState.DELETE);
				sli.setModified(now);
				mBuilder.del(sli);
			}

			success = mDatabase.deleteList(sl, user);

			// Actually delete the items in the offline version
			mDatabase.deleteShares(sl, user);
			mDatabase.deleteItems(sl.getId(), null, user);
			
		}

		if (success) {
			mBuilder.del(sl);
		}
		postShoppinglistEvent();
		return success;
	}

	/**
	 * Get a {@link ShoppinglistItem} item by it's ID
	 * @param id A {@link ShoppinglistItem} id
	 * @return A shopping list item, or {@code null}
	 */
	public ShoppinglistItem getItem(String id) {
		return mDatabase.getItem(id, user());
	}
	
	/**
	 * Get all {@link ShoppinglistItem ShoppinglistItems} associated with a
	 * {@link Shoppinglist}.
	 * @param sl A {@link Shoppinglist} to get {@link ShoppinglistItem ShoppinglistItems} from
	 * @return A list of {@link ShoppinglistItem ShoppinglistItems}
	 */
	public List<ShoppinglistItem> getItems(Shoppinglist sl) {
		List<ShoppinglistItem> items = mDatabase.getItems(sl, user());
		ListUtils.sortItems(items);
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

		mDatabase.allowEditOrThrow(sli.getShoppinglistId(), user);

		if (sli.getOfferId() == null && sli.getDescription() == null) {
			EtaLog.i(TAG, "The ShoppinglistItem neither has offerId, or"
					+ "description, one or the other this is required by the API");
			return false;
		}
		
		Date now = new Date();
		sli.setModified(now);
		sli.setState(SyncState.TO_SYNC);
		
		// If the item exists in DB, then just increase count and edit the item
		if (incrementCount) {
			
			List<ShoppinglistItem> items = mDatabase.getItems(sli.getShoppinglistId(), user, false);
			
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
			EtaLog.i(TAG, "The shoppinglist id on the shoppinglist item, could"
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
		
		sli.setPreviousId(ListUtils.FIRST_ITEM);
		ShoppinglistItem first = mDatabase.getFirstItem(sli.getShoppinglistId(), user);
		if (first != null) {
			first.setPreviousId(sli.getId());
			first.setModified(now);
			first.setState(SyncState.TO_SYNC);
			mDatabase.editItem(first, user);
			mBuilder.edit(first);
		}

		boolean success = mDatabase.insertItem(sli, user);
		if (success) {
			/* Update SL info, but not state. This will prevent sync, and API
			 * will auto update the modified tag, nice! */
			sl.setModified(now);
			mDatabase.editList(sl, user);
			mBuilder.edit(sl);
			mBuilder.add(sli);
		}
		postShoppinglistEvent();
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
		boolean result = editItemImpl(u, sli);
		postShoppinglistEvent();
		return result;
	}
	
	/**
	 * Replace an item, in the database
	 * @param items A list of ShoppinglistItem to edit
	 * @return number of affected rows
	 */
	public int editItems(List<ShoppinglistItem> items) {
		User u = user();
		mDatabase.allowEditItemsOrThrow(items, u);
		int count = mDatabase.editItem(items, u);
		postShoppinglistEvent();
		return count;
	}
	
	private boolean editItemImpl(User u, ShoppinglistItem sli) {
		mDatabase.allowEditOrThrow(sli.getShoppinglistId(), u);
		return editItem(sli, u);
	}
	
	private boolean editItem(final ShoppinglistItem sli, User user) {
		
		Date now = new Date();
		sli.setModified(now);
		sli.setState(SyncState.TO_SYNC);
		
		// Check for changes in previous item, and update surrounding
		ShoppinglistItem oldItem = mDatabase.getItem(sli.getId(), user);
		if (oldItem == null) {
			EtaLog.i(TAG, "No such item exists, considder addItem() instead: " + sli.toString());
			return false;
		}
		
		boolean success = mDatabase.editItem(sli, user);
		if (success) {
			/* Update SL info, but not state. This will prevent sync, and API
			 * will auto update the modified tag, nice!
			 */
			Shoppinglist sl = getList(sli.getShoppinglistId());
			if (sl != null) {
				sl.setModified(now);
				mDatabase.editList(sl, user);
				mBuilder.edit(sl);
			}
			mBuilder.edit(sli);
		}
		
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

		mDatabase.allowEditOrThrow(sl.getId(), user);

		Date now = new Date();
		
        List<ShoppinglistItem> list = getItems(sl);
        int count = 0;

		String preGoodId = ListUtils.FIRST_ITEM;
		
		for (ShoppinglistItem sli : list) {
			if (stateToDelete == null) {
				// Delete all items
				mBuilder.del(sli);
			} else if (sli.isTicked() == stateToDelete) {
				// Delete if ticked matches the requested state
				mBuilder.del(sli);
			} else {
				if (!sli.getPreviousId().equals(preGoodId)) {
					sli.setPreviousId(preGoodId);
					sli.setModified(now);
					sli.setState(SyncState.TO_SYNC);
					mDatabase.editItem(sli, user);
				}
				preGoodId = sli.getId();
			}
		}
		
		if (user.isLoggedIn()) {
			for (ShoppinglistItem sli : mBuilder.getDeletedItems()) {
				sli.setState(SyncState.DELETE);
				sli.setModified(now);
				if (mDatabase.editItem(sli, user)) {
					count++;
				}
			}
		} else {
			count = mDatabase.deleteItems(sl.getId(), stateToDelete, user) ;
		}
		
		boolean success = count == mBuilder.getDeletedItems().size();
		if (success) {
			/* Update SL info, but not state. This will prevent sync, and API
			 * will auto update the modified tag, nice!
			 */
			sl.setModified(now);
			mDatabase.editList(sl, user);
			mBuilder.edit(sl);
		}
		
		postShoppinglistEvent();
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
		mDatabase.allowEditOrThrow(sli.getShoppinglistId(), u);
		return deleteItem(sli, u);
	}
	
	private boolean deleteItem(ShoppinglistItem sli, User user) {
		
		Date now = new Date();
		
		sli.setModified(now);

		// Update previous pointer
		ShoppinglistItem after = mDatabase.getItemPrevious(sli.getShoppinglistId(), sli.getId(), user);
		if (after != null) {
			after.setPreviousId(sli.getPreviousId());
			after.setModified(now);
			mDatabase.editItem(after, user);
			mBuilder.edit(after);
		}

		boolean success = false;
		if (user.getUserId() != User.NO_USER) {
			sli.setState(SyncState.DELETE);
			success = mDatabase.editItem(sli, user);
		} else {
			success = mDatabase.deleteItem(sli, user);
		}

		if (success) {
			/* Update shoppinglist modified, but not state, so we have correct
			 * state but won't have to sync changes to API.
			 * API will change state based on the synced item.
			 */
			Shoppinglist sl = getList(sli.getShoppinglistId());
			sl.setModified(now);
			mDatabase.editList(sl, user);
			mBuilder.edit(sl);
			
			mBuilder.del(sli);
		}
		postShoppinglistEvent();
		return success;
	}
	
	/**
	 * Get the current user.
	 * <p>wrapper method for: Eta.getInstance().getUser()</p>
	 * @return A {@link User}
	 */
	private User user() {
		return mEta.getUser();
	}

	/**
	 * Deletes all rows in the {@link DatabaseWrapper database}.
	 */
	public void clear() {
		mDatabase.clear();
	}

	/**
	 * Deletes all rows in the {@link DatabaseWrapper database} associated with a
	 * given{@link User}.
	 */
	public void clear(int userId) {
		mDatabase.clear(userId);
	}
	
	/**
	 * Method to call on all onResume events.
	 * <p>This is implicitly handled by the {@link Eta} instance</p>
	 */
	public void onStart() {
		mDatabase.open();
	}
	
	/**
	 * Method to call on all onPause events.
	 * <p>This is implicitly handled by the {@link Eta} instance</p>
	 */
	public void onStop() {
		mDatabase.close();
	}

	private void postShoppinglistEvent() {
		if (!mEta.getSyncManager().isPaused() && mBuilder.hasChanges()) {
            EventBus.getDefault().post(mBuilder.build());
			mBuilder = new ShoppinglistEvent.Builder(false);
		}
	}

}
