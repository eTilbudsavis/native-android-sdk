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

public class ListManager {

	public static final String TAG = "ListManager";

	/** Supported sync speeds for shopping list manager */
	public interface SyncSpeed {
		int SLOW = 10000;
		int MEDIUM = 6000;
		int FAST = 3000;
	}
	
	/** The global eta object */
	private Eta mEta;
	
	/** Subscriber queue for shopping list changes */
	private List<OnChangeListener> mListSubscribers = new ArrayList<OnChangeListener>();
	
	/** Manager for doing asynchronous sync */
	private ListSyncManager mSyncManager;
	
	/** The notification service for ListManager, this allows for bundling
	 * list and item notifications, to avoid multiple updates for a single operation */
	private ListNotification mNotification = new ListNotification(false);
	
	public ListManager(Eta eta) {
		mEta = eta;
		mSyncManager = new ListSyncManager(eta);
	}
	
	/**
	 * Method for determining if the first sync cycle is done.
	 * This is dependent on, both the ShoppinglistSyncManager having a first sync, and whether or not a user
	 * is actually logged in. If no user is logged in, the method will return true (as no sync can occour).
	 * @return True if the first sync is complete, or there is no user to sync.
	 */
	public boolean hasFirstSync() {
		return !mEta.getUser().isLoggedIn() || mSyncManager.hasFirstSync();
	}
	
	/**
	 * Get a shoppinglist from it's ID.
	 * @param id of the shoppinglist to get
	 * @return A shopping list, or <code>null</code> if no shopping list exists
	 */
	public Shoppinglist getList(String id) {
		return DbHelper.getInstance().getList(id, user());
	}
	
	/**
	 * The complete set of shopping lists
	 * @return <li>All shopping lists
	 */
	public List<Shoppinglist> getLists() {
		return DbHelper.getInstance().getLists(user()); 
	}
	
	/**
	 * Get a shopping list from it's human readable name
	 * @param name of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public List<Shoppinglist> getListFromName(String name) {
		return DbHelper.getInstance().getListFromName(name,user());
	}

	/**
	 * Add a new shopping list.<br>
	 * If owner haven't been set, we will assume that it is the user who is currently logged in.
	 * if no user is logged inn, then we assume it is a offline list.<br>
	 * shopping list added to the database, and changes is synchronized to the server if possible.<br>
	 * 
	 * @param sl - the new shoppinglist to add to the database, and server
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
	 * Edit a shopping list already in the database.<br>
	 * shopping list is replaced in the database, and changes is synchronized to the server if possible.<br>
	 * @param sl - Shopping list to be replaced
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
	 * <p>The {@link Shoppinglist shoppinglist} is deleted from the local database, and
	 * changes are synchronized to the server, when and if possible. All
	 * {@link ShoppinglistItem shoppinglistitems} associated with the
	 * {@link Shoppinglist shoppinglist} are also deleted.</p>
	 * @param sl - Shopping list to delete
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
	 * Get a shopping list item by it's ID
	 * @param id of the shopping list item
	 * @return A shopping list item, or <code>null</code> if no item can be found.
	 */
	public ShoppinglistItem getItem(String id) {
		return DbHelper.getInstance().getItem(id, user());
	}

	/**
	 * Get a shopping list from it's human readable name
	 * (well actually an List, as we cannot guarantee duplicate names)
	 * @param description of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public List<ShoppinglistItem> getItemFromDescription(String description) {
		return DbHelper.getInstance().getItemFromDescription(description, user());
	}
	
	
	/**
	 * Get a shopping list item by it's ID
	 * @param sl of the shopping list item
	 * @return A list of shoppinglistitem.
	 */
	public List<ShoppinglistItem> getItems(Shoppinglist sl) {
		List<ShoppinglistItem> items = DbHelper.getInstance().getItems(sl, user());
		Utils.sortItems(items);
		return items;
	}
	
	/**
	 * Add a {@link ShoppinglistItem} to a {@link Shoppinglist}
	 * <p>{@link ShoppinglistItem}s are inserted into the local database, and
	 * changes are synchronized to the server when and if possible. If the 
	 * {@link Shoppinglist} does not exist in the database or the server, the SDK
	 * will try to create a new {@link Shoppinglist}</p>
	 * @param sli - shopping list item that should be added.
	 */
	public boolean addItem(ShoppinglistItem sli) {
		return addItem(sli, true, user());
	}
	
	/**
	 * Add an item to a shopping list.<br>
	 * shopping list items is inserted into the database, and changes is synchronized to the server if possible.
	 * If the shopping list does not exist in the database or the server, a new one is created and synchronized if possible
	 * @param sli - shoppinglist item to add
	 * @param incrementCountItemExists - 
	 * 			increment the count on the shoppinglistitem if an item like it exists, 
	 * 			instead of adding new item.
	 * @param listener for completion callback
	 */
	@SuppressLint("DefaultLocale") 
	public boolean addItem(ShoppinglistItem sli, boolean incrementCountItemExists, User user) {
		
		if (!canEdit(sli.getShoppinglistId(), user)) {
			EtaLog.d(TAG, "Current user only has read-rights to the list");
			return false;
		}

		if (sli.getOfferId() == null && sli.getDescription() == null) {
			EtaLog.d(TAG, "Shoppinglist item seems to be empty, please add stuff");
			return false;
		}
		
		DbHelper db = DbHelper.getInstance();
		
		Date now = new Date();
		sli.setModified(now);
		sli.setState(ShoppinglistItem.State.TO_SYNC);
		
		// If the item exists in DB, then just increase count and edit the item
		if (incrementCountItemExists) {
			
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
			Shoppinglist sl = getList(sli.getShoppinglistId());
			sl.setModified(now);
			db.editList(sl, user);
			mNotification.edit(sl);
			mNotification.add(sli);
		}
		notifySubscribers(mNotification);
		return success;
	}
	
	/**
	 * Insert an updated shopping list item into the db.<br>
	 * shopping list items is replaced in the database, and changes is synchronized to the server if possible.
	 * @param sli shopping list item to edit
	 */
	public boolean editItem(ShoppinglistItem sli) {
		User u = user();
		if (!canEdit(sli.getShoppinglistId(), u)) {
			EtaLog.d(TAG, "Current user only has read-rights to the list");
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
	 * Delete all items from a shoppinglist where <code>isTicked() == true.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteItemsTicked(Shoppinglist sl) {
		deleteItems(sl, Shoppinglist.EMPTY_TICKED, user());
	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == false.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteItemsUnticked(Shoppinglist sl) {
		deleteItems(sl, Shoppinglist.EMPTY_UNTICKED, user());
	}

	/**
	 * Delete ALL items from a given shoppinglist.<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteItemsAll(Shoppinglist sl) {
		deleteItems(sl, Shoppinglist.EMPTY_ALL, user());
	}
	
	/**
	 * Generic method to delete all items that matches any clauses given in the <code>apiParams</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl to remove items from
	 * @param whatToDelete describes what needs to be deleted
	 */
	private boolean deleteItems(final Shoppinglist sl, String whatToDelete, User user) {
		
		if (!canEdit(sl, user))
			return false;
		
		DbHelper db = DbHelper.getInstance();
		
		Date now = new Date();
		
		// Ticked = true, unticked = false, all = null.. all in a nice ternary
		final Boolean state = whatToDelete.equals(Shoppinglist.EMPTY_ALL) ? null : whatToDelete.equals(Shoppinglist.EMPTY_TICKED) ? true : false;
		
        List<ShoppinglistItem> list = getItems(sl);
        int count = 0;

		String preGoodId = ShoppinglistItem.FIRST_ITEM;
		
		for (ShoppinglistItem sli : list) {
			if (state == null) {
				// Delete all items
				mNotification.del(sli);
			} else if (sli.isTicked() == state) {
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
			count = db.deleteItems(sl.getId(), state, user) ;
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
	 * Deletes a given shopping list item<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sli to delete from the db
	 */
	public boolean deleteItem(ShoppinglistItem sli) {
		User u = user();
		if (!canEdit(sli.getShoppinglistId(), u)){
			EtaLog.d(TAG, "Current user only has read-rights to the list");
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
	 * Get the userId of the current user logged in.
	 * @return the user id
	 */
	private User user() {
		return Eta.getInstance().getUser();
	}

	public boolean canEdit(String shoppinglistId, User user) {
		Shoppinglist sl = DbHelper.getInstance().getList(shoppinglistId, user());
		return sl == null ? false : canEdit(sl, user);
	}

	public boolean canEdit(Shoppinglist sl, User user) {
		if (sl == null) {
			return false;
		}
		Share s = sl.getShares().get(user.getEmail());
		return s == null ? false : canEdit(sl, s);
	}
	
	public boolean canEdit(Shoppinglist sl, Share s) {
		if (s==null||sl==null) {
			return false;
		}
		boolean isInList = s.getShoppinglistId().equals(sl.getId());
		return isInList && ( s.getAccess().equals(Share.ACCESS_OWNER) || s.getAccess().equals(Share.ACCESS_READWRITE) );
	}

	/**
	 * Set the synchronization intervals for the shoppinglists, and their items.<br>
	 * The synchronization of items will be the time specified, and the list
	 * synchronization will be a factor three of that time, as the lists themselves
	 * are less subjected to change. Also time must be 3000 milliseconds or more.
	 * @param time in milliseconds
	 */
	public void setSyncSpeed(int time) {
		if (time == SyncSpeed.SLOW || time == SyncSpeed.MEDIUM || time == SyncSpeed.FAST )
			mSyncManager.setSyncSpeed(time);
	}
	
	/**
	 * Deletes all rows in DB.
	 */
	public void clear() {
		DbHelper.getInstance().clear();
		mSyncManager.onPause();
	}

	/**
	 * Deletes all rows belonging to a logged in user
	 */
	public void clear(int userId) {
		DbHelper.getInstance().clear(userId);
	}
	
	public void onResume() {
		mSyncManager.onResume();
	}
	
	public void onPause() {
		mSyncManager.onPause();
	}
	
	public void forceSync() {
		mSyncManager.runSyncLoop();
	}
	
	public void setOnChangeListener(OnChangeListener l) {
		if (!mListSubscribers.contains(l)) {
			mListSubscribers.add(l);
		}
	}

	public void removeOnChangeListener(OnChangeListener l) {
		mListSubscribers.remove(l);
	}
	
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