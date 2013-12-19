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
	
	/** Subscriber queue for shopping list item changes */
	private List<OnChangeListener<ShoppinglistItem>> mItemSubscribers = new ArrayList<OnChangeListener<ShoppinglistItem>>();

	/** Subscriber queue for shopping list changes */
	private List<OnChangeListener<Shoppinglist>> mListSubscribers = new ArrayList<OnChangeListener<Shoppinglist>>();
	
	/** Manager for doing asynchronous sync */
	private ListSyncManager mSyncManager;
	
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
		if (success) notifyListSubscribers(false, idToList(sl), null, null);
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
		
		// User have remove it self.
		if (!slShares.containsKey(user.getEmail())) {
			Share dbShare = dbShares.get(user.getEmail());
			dbShare.setState(Share.State.DELETE);
			db.editShare(dbShare, user);
			notifyListSubscribers(false, null, idToList(sl), null);
			return true;
		}
		
		if (!canEdit(sl, slShares.get(user.getEmail()))) {
			EtaLog.d(TAG, "User, doesn't have rights to edit this list");
			return false;
		}
		
		HashSet<String> union = new HashSet<String>();
		union.addAll(slShares.keySet());
		union.addAll(dbShares.keySet());
		
		// Variable for owner. If it has been removed from the sl-shares-list then we need to re-add it from the DB
		Share owner = null;
		
		for (String unionS : union) {
			
			if (dbShares.containsKey(unionS)) {

				Share dbShare = dbShares.get(unionS);
				
				if (slShares.containsKey(unionS)) {
					Share slShare = slShares.get(unionS);
					
					if (!dbShare.equals(slShare)) {
						slShare.setState(Share.State.TO_SYNC);
						db.editShare(slShare, user);
						EtaLog.d(TAG, "Edited share: " + slShare.getEmail());
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
						EtaLog.d(TAG, "Deleted share: " + dbShare.getEmail());
					}
				}
				
			} else {
				Share slShare = slShares.get(unionS);
				db.insertShare(slShare, user);
				EtaLog.d(TAG, "Added share: " + slShare.getEmail());
			}
			
		}
		
		// If owner was removed, then re insert it.
		if (owner != null)
			sl.putShare(owner);
		
		sl.setModified(new Date());
		sl.setState(Shoppinglist.State.TO_SYNC);
		
		// Check for changes in previous item, and update surrounding
		Shoppinglist oldList = db.getList(sl.getId(), user);
		if (oldList == null) {
			EtaLog.d(TAG, "No such list exists, considder addList() instead: " + sl.toString());
			return false;
		}
		
		if (!oldList.getPreviousId().equals(sl.getPreviousId())) {
			
			// If there is an item pointing at sl, it needs to point at the oldList.prev
			Shoppinglist sliAfter = db.getListPrevious(sl.getId(), user);
			if (sliAfter != null) {
				sliAfter.setPreviousId(oldList.getPreviousId());
				db.editList(sliAfter, user);
			}
			
			// If some another sl was pointing at the same item, it should be pointing at sl
			Shoppinglist sliSamePointer = db.getListPrevious(sl.getPreviousId(), user);
			if (sliSamePointer != null) {
				sliSamePointer.setPreviousId(sl.getId());
				db.editList(sliSamePointer, user);
			}
			
		}
		
		int count = db.editList(sl, user);
		boolean success = count == 1;
		if (success) notifyListSubscribers(false, null, null, idToList(sl));
		return success;
	}

	/**
	 * Delete a shopping list.<br>
	 * shopping list is deleted from the database, and changes is synchronized to the server if possible.<br>
	 * All shopping list items associated with the shopping list are also deleted.
	 * @param sl - Shopping list to delete
	 */
	public void deleteList(Shoppinglist sl) {
		User u = user();
		if (canEdit(sl, u)) 
			deleteList(sl, u);
	}
	
	private boolean deleteList(Shoppinglist sl, User user) {
		
		DbHelper db = DbHelper.getInstance();
		
		sl.setModified(new Date());
		
		// Update previous pointer, to preserve order
		Shoppinglist after = db.getListPrevious(sl.getId(), user);
		if (after != null) {
			after.setPreviousId(sl.getPreviousId());
			db.editList(after, user);
		}
		
		int count = 0;
		if (mEta.getUser().isLoggedIn()) {
			
			EtaLog.d(TAG, "logged in");
			
			// Update local version of shoppinglist
			sl.setState(Shoppinglist.State.DELETE);
			count = db.editList(sl, user);
			
			// Mark all items in list to be deleted
			for (ShoppinglistItem sli : getItems(sl)) {
				sli.setState(ShoppinglistItem.State.DELETE);
				db.editItem(sli, user);
			}
			
		} else {
			
			EtaLog.d(TAG, "not logged in");
			count = db.deleteList(sl, user);
			db.deleteShares(sl, user);
			db.deleteItems(sl.getId(), null, user);
			
		}
		
		EtaLog.d(TAG, count + " " + sl.toJSON().toString());
		
		boolean success = count == 1;
		if (success) notifyListSubscribers(false, null, idToList(sl), null);
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
	 * Add an item to a shopping list.<br>
	 * shopping list items is inserted into the database, and changes is synchronized to the server if possible.
	 * If the shopping list does not exist in the database or the server, a new one is created and synchronized if possible
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
		
		sli.setModified(new Date());
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
			first.setModified(new Date());
			first.setState(ShoppinglistItem.State.TO_SYNC);
			db.editItem(first, user);
		}
		
		int count = db.insertItem(sli, user);
		boolean success = count == 1;
		if (success) notifyItemSubscribers(false, idToList(sli), null, null);
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
		
		sli.setModified(new Date());
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
				db.editItem(sliAfter, user);
			}
			
			// If some another sli was pointing at the same item, it should be pointing at sli
			ShoppinglistItem sliSamePointer = db.getItemPrevious(sl, sli.getPreviousId(), user);
			if (sliSamePointer != null) {
				sliSamePointer.setPreviousId(sli.getId());
				db.editItem(sliSamePointer, user);
			}
			
		}
		int count = DbHelper.getInstance().editItem(sli, user);
		boolean success = count == 1;
		if (success) notifyItemSubscribers(false, null, null, idToList(sli));
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
		
		Date d = new Date();
		
		// Ticked = true, unticked = false, all = null.. all in a nice ternary
		final Boolean state = whatToDelete.equals(Shoppinglist.EMPTY_ALL) ? null : whatToDelete.equals(Shoppinglist.EMPTY_TICKED) ? true : false;
		
        List<ShoppinglistItem> list = getItems(sl);
        final List<ShoppinglistItem> deleted = new ArrayList<ShoppinglistItem>();
        int count = 0;

		String preGoodId = ShoppinglistItem.FIRST_ITEM;
		
		for (ShoppinglistItem sli : list) {
			if (state == null) {
				// Delete all items
				deleted.add(sli);
			} else if (sli.isTicked() == state) {
				// Delete if ticked matches the requested state
				deleted.add(sli);
			} else {
				if (!sli.getPreviousId().equals(preGoodId)) {
					sli.setPreviousId(preGoodId);
					db.editItem(sli, user);
				}
				preGoodId = sli.getId();
			}
		}
		
		if (mEta.getUser().isLoggedIn()) {
			for (ShoppinglistItem sli : deleted) {
				sli.setState(ShoppinglistItem.State.DELETE);
				sli.setModified(d);
				count += db.editItem(sli, user);
			}
		} else {
			count = db.deleteItems(sl.getId(), state, user) ;
		}
		boolean success = count == deleted.size();
		if (success) notifyItemSubscribers(false, null, deleted, null);
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
		
		sli.setModified(new Date());

		// Update previous pointer
		ShoppinglistItem after = db.getItemPrevious(sli.getShoppinglistId(), sli.getId(), user);
		if (after != null) {
			after.setPreviousId(sli.getPreviousId());
			db.editItem(after, user);
		}
		
		int count = 0;
		if (user.getId() != User.NO_USER) {
			sli.setState(ShoppinglistItem.State.DELETE);
			count = db.editItem(sli, user);
		} else {
			count = db.deleteItem(sli, user);
		}
		boolean success = count == 1;
		if (success) notifyItemSubscribers(false, null, idToList(sli), null);
		return success;
	}
	
	/**
	 * Helper method, adding the Object<T> into a new List<T>.
	 * @param object to add
	 * @return List<T> containing only the object 
	 */
	private <T> List<T> idToList(T object) {
		if (object == null)
			return null;
		
		List<T> list = new ArrayList<T>(1);
		list.add(object);
		return list;
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
		return canEdit(sl, user);
	}

	public boolean canEdit(Shoppinglist sl, User user) {
		Share s = sl.getShares().get(user.getEmail());
		return canEdit(sl, s);
	}

	public boolean canEdit(Shoppinglist sl, Share s) {
		return s != null && ( s.getAccess().equals(Share.ACCESS_OWNER) || s.getAccess().equals(Share.ACCESS_READWRITE) );
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
	
	public void setOnItemChangeListener(OnChangeListener<ShoppinglistItem> l) {
		if (!mItemSubscribers.contains(l)) mItemSubscribers.add(l);
	}

	public void removeOnItemChangeListener(OnChangeListener<ShoppinglistItem> l) {
		mItemSubscribers.remove(l);
	}

	public void setOnListChangeListener(OnChangeListener<Shoppinglist> l) {
		if (!mListSubscribers.contains(l)) mListSubscribers.add(l);
	}

	public void removeOnListChangeListener(OnChangeListener<Shoppinglist> l) {
		mListSubscribers.remove(l);
	}
	
	public void notifyItemSubscribers(final boolean isServer, List<ShoppinglistItem> added, List<ShoppinglistItem> deleted, List<ShoppinglistItem> edited) {

		final List<ShoppinglistItem> mAdded = new ArrayList<ShoppinglistItem>(0);
		final List<ShoppinglistItem> mDeleted = new ArrayList<ShoppinglistItem>(0);
		final List<ShoppinglistItem> mEdited = new ArrayList<ShoppinglistItem>(0);
		
		if (added != null) mAdded.addAll(added);
		if (deleted != null) mDeleted.addAll(deleted);
		if (edited != null) mEdited.addAll(edited);
		
		for (final OnChangeListener<ShoppinglistItem> s : mItemSubscribers) {
			try {
				mEta.getHandler().post(new Runnable() {
					
					public void run() {
						s.onUpdate(isServer, mAdded, mDeleted, mEdited);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void notifyListSubscribers(final boolean isServer, List<Shoppinglist> added, List<Shoppinglist> deleted, List<Shoppinglist> edited) {
		
		final List<Shoppinglist> mAdded = new ArrayList<Shoppinglist>(0);
		final List<Shoppinglist> mDeleted = new ArrayList<Shoppinglist>(0);
		final List<Shoppinglist> mEdited = new ArrayList<Shoppinglist>(0);
		
		if (added != null) mAdded.addAll(added);
		if (deleted != null) mDeleted.addAll(deleted);
		if (edited != null) mEdited.addAll(edited);
		
		for (final OnChangeListener<Shoppinglist> s : mListSubscribers) {
			mEta.getHandler().post(new Runnable() {
				
				public void run() {
					s.onUpdate(isServer, mAdded, mDeleted, mEdited);
				}
			});
		}
	}
	
	public void notifyFirstSync() {
		
		for (final OnChangeListener<Shoppinglist> sub : mListSubscribers) {
			mEta.getHandler().post(new Runnable() {
				
				public void run() {
					sub.onFirstSync();
				}
			});
		}

		for (final OnChangeListener<ShoppinglistItem> sub : mItemSubscribers) {
			mEta.getHandler().post(new Runnable() {
				
				public void run() {
					sub.onFirstSync();
				}
			});
		}
		
	}
	
	public interface OnChangeListener<T> {
        /**
         * The interface for recieving updates from the shoppinglist manager, given that you have subscribed to updates.
         *
         * @param isServer true if server response
         * @param addedIds the id's thats been added
         * @param deletedIds the id's thats been deleted
         * @param editedIds the id's thats been edited
         */
		public void onUpdate(boolean isServer, List<T> addedIds, List<T> deletedIds, List<T> editedIds);
		
		public void onFirstSync();
			
	}
	
}