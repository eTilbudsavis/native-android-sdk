package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Api.JsonArrayListener;
import com.eTilbudsavis.etasdk.Api.JsonObjectListener;
import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaErnObject;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Share;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ShoppinglistManager {

	public static final String TAG = "ShoppinglistManager";
	
	public static final int SYNC_SLOW = 10000;
	public static final int SYNC_MEDIUM = 6000;
	public static final int SYNC_FAST = 3000;

	public static final int STATE_TO_SYNC	= 0;
	public static final int STATE_SYNCING	= 1;
	public static final int STATE_SYNCED	= 2;
	public static final int STATE_TO_DELET	= 3;
	public static final int STATE_DELETING	= 4;
	public static final int STATE_DELETED	= 5;
	public static final int STATE_ERROR		= 6;
	
	private Eta mEta;
	private DbHelper mDatabase;
	private int mSyncSpeed = SYNC_MEDIUM;
	private String mCurrentSlId = null;

	private List<QueueItem> mApiQueue = Collections.synchronizedList(new ArrayList<QueueItem>());
	
	private ArrayList<ShoppinglistManagerListener> mSubscribers = new ArrayList<ShoppinglistManager.ShoppinglistManagerListener>();
	
	private int syncCount = 0;
	private Runnable mSyncLoop = new Runnable() {
		
		public void run() {
			
			mEta.getHandler().postDelayed(mSyncLoop, mSyncSpeed);
			
			if (mApiQueue.size() > 0) {
				execApiQueue();
			} else {		
				if (syncCount%3 == 0) {
					syncLists();
				} else {
					syncItems();
				}
				syncCount++;
			}
			
		}
		
	};

	private void execApiQueue() {
		for (QueueItem q : mApiQueue) {
			if (q.retries > 0) {
				q.execute();
			} else {
				
			}
		}
	}
	
	private void addQueue(Api api) {
		mApiQueue.add(new QueueItem(api));
		execApiQueue();
	}
	
	class QueueItem {
		private int user;
		private int retries = 5;
		private boolean working = false;
		private Api api;
		private JsonObjectListener oldListener;
		private JsonObjectListener listener = new JsonObjectListener() {
			
			public void onComplete(int statusCode, JSONObject item, EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					
					mApiQueue.remove(QueueItem.this);
					if (user == mEta.getUser().getId()) {
						oldListener.onComplete(statusCode, item, error);
					}
					
				} else if (statusCode == -1) {
					
					mEta.getHandler().postDelayed(new Runnable() {
						
						public void run() {
							call();
						}
					}, 10000);
					
				} else {
					mApiQueue.remove(QueueItem.this);
					oldListener.onComplete(statusCode, item, error);
				}
				
			}
		};
		
		public QueueItem(Api api) {
			user = mEta.getUser().getId();
			this.api = api;
			oldListener = (JsonObjectListener)api.getListener();
			api.setListener(listener);
		}
		
		public void execute() {
			if (working)
				return;
			
			call();
		}
		
		private void call() {
			working = true;
			retries--;
			api.execute();
		}
		
	}
	
	public ShoppinglistManager(Eta eta) {
		mEta = eta;
		mDatabase = new DbHelper(mEta.getContext(), mEta);
		mDatabase.openDB();
		mCurrentSlId = mEta.getSettings().getShoppinglistManagerCurrent();
	}

	/**
	 * Get the shopping list that is currently in use, by the user.<br>
	 * The current list is synchronized more often, than other lists, since the list is likely
	 * to be visible to the user. This is useful when juggling multiple lists.
	 * @return The shopping list currently in use. or <code>null</code> if no shoppinglists exist.
	 */
	public Shoppinglist getCurrentList() {
		Shoppinglist sl = null;
		if (mCurrentSlId == null) {
			sl = setRandomCurrent();
		} else {
			sl = getList(mCurrentSlId);
			if (sl == null) {
				sl = setRandomCurrent();
			}
		}
		return sl;
	}
	
	private Shoppinglist setRandomCurrent() {
		Shoppinglist sl = null;
		Cursor c = mDatabase.getLists();
		if (c.moveToFirst()) {
			sl = DbHelper.curToSl(c);
			setCurrentList(sl);
		} 
		c.close();
		return sl;
	}
	
	/**
	 * Set the shopping list that is currently in use, by the user.<br>
	 * The current list is synchronized more often, than other lists, since the list is likely
	 * to be visible to the user. This is useful when juggling multiple lists.
	 * @param sl - shopping list to make the currently visible list
	 * @return This ShoppinglistManager
	 */
	public ShoppinglistManager setCurrentList(Shoppinglist sl) {
		mCurrentSlId = sl.getId();
		mEta.getSettings().setShoppinglistManagerCurrent(mCurrentSlId);
		return this;
	}
	
	/**
	 * Get a shoppinglist from it's ID.
	 * @param id of the shoppinglist to get
	 * @return A shopping list, or <code>null</code> if no shopping list exists
	 */
	public Shoppinglist getList(String id) {
		Cursor c = mDatabase.getList(id);
		return c.moveToFirst() == true ? DbHelper.curToSl(c) : null;
	}

	/**
	 * The complete set of shopping lists
	 * @return <li>All shopping lists
	 */
	public ArrayList<Shoppinglist> getLists() {
		Cursor c = mDatabase.getLists();
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.curToSl(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}
	
	/**
	 * Get a shopping list from it's human readable name
	 * @param id of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public ArrayList<Shoppinglist> getListFromName(String name) {
		Cursor c = mDatabase.getListFromName(name);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.curToSl(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}

	/**
	 * Add a new shopping list.<br>
	 * If owner haven't been set, we will assume that it is the user who is currently logged in.
	 * if no user is logged inn, then we assume it is a offline list.<br>
	 * shopping list added to the database, and changes is synchronized to the server if possible.<br>
	 * 
	 * @param sl - the new shoppinglist to add to the database, and server
	 */
	public void addList(final Shoppinglist sl, final JsonObjectListener listener) {
		
		// Make sure the UUID does not exist, before adding it
		while (mDatabase.existsList(sl.getId())) {
			sl.setId(Utils.createUUID());
		};
		
		if (mustSync()) {

			if (sl.getOwner().getUser() == null) {
				sl.getOwner().setUser(mEta.getUser().getEmail());
				sl.getOwner().setAccess(Share.ACCESS_OWNER);
				sl.getOwner().setAccepted(true);
			}
			
			sl.setState(STATE_SYNCING);
			
			// Sync online if possible
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
//					Utils.logd(TAG, "addList", statusCode, data, error);
					if (Utils.isSuccess(statusCode)) {
						Shoppinglist newSl = new Shoppinglist(data);
						newSl.setState(STATE_SYNCED);
						mDatabase.editList(newSl);
						notifyListEdit(newSl.getId());
					} else {
//						Utilities.logd(TAG, error.toString());
						sl.setState(STATE_ERROR);
						mDatabase.editList(sl);
						notifyListDelete(sl.getId());
					}
					listener.onComplete(statusCode, data, error);
				}
			};
			
			Api a = mEta.api().put(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), cb, sl.getApiParams());
			addQueue(a);
		}

		// Insert the new list to DB
		mDatabase.insertList(sl);
		
		notifyListAdd(sl.getId());
		
	}

	/**
	 * Edit a shopping list already in the database.<br>
	 * shopping list is replaced in the database, and changes is synchronized to the server if possible.<br>
	 * @param sl - Shopping list to be replaced
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public boolean editList(final Shoppinglist sl) {
		
		boolean resp = false;
		
		if (mustSync()) {

			sl.setState(STATE_SYNCING);
			resp = mDatabase.editList(sl) == -1 ? false : true;
			
			JsonObjectListener editItem = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
					Utils.logd(TAG, "editList", statusCode, data, error);
					Shoppinglist s = sl;
					if (Utils.isSuccess(statusCode)) {
						s = new Shoppinglist(data);
						s.setState(STATE_SYNCED);
					} else {
						Utils.logd(TAG, error.toString());
						s.setState(STATE_ERROR);
					}
					mDatabase.editList(s);
					notifyListEdit(s.getId());
				}
			};
			
			mEta.api().put(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), editItem, sl.getApiParams()).execute();
		} else {
			resp = mDatabase.editList(sl) == -1 ? false : true;
			if (resp)
				notifyListEdit(sl.getId());
		}
		return resp;
	}

	/**
	 * Delete a shopping list.<br>
	 * shopping list is deleted from the database, and changes is synchronized to the server if possible.<br>
	 * All shopping list items associated with the shopping list are also deleted.
	 * @param sl - Shopping list to delete
	 * @return the number of rows affected.
	 */
	public int deleteList(final Shoppinglist sl, final JsonObjectListener listener) {
		int count;
		if (mustSync()) {
			
			sl.setState(STATE_DELETING);
			mDatabase.editList(sl);
			
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
//					Utilities.logd(TAG, "deleteList", statusCode, data, error);
					if (Utils.isSuccess(statusCode)) {
						if (mDatabase.deleteList(sl.getId()) > 0) {
							mDatabase.deleteItems(sl.getId(), null);
							notifyListDelete(sl.getId());
						}
					} else {
						// TODO: What state are we in here? Server knows?
						Shoppinglist s = getList(sl.getId());
						s.setState(STATE_SYNCED);
						mDatabase.editList(s);
						notifyListEdit(sl.getId());
					}
					listener.onComplete(statusCode, data, error);
				}
			};
			
			mEta.api().delete(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), cb, sl.getApiParams()).execute();
			count = -1;
		} else {
			count = mDatabase.deleteList(sl.getId());
			if (count > 0) {
				mDatabase.deleteItems(sl.getId(), null);
				notifyListDelete(sl.getId());
			}
		}
		return count;
	}

	/**
	 * Get a shopping list item by it's ID
	 * @param id of the shopping list item
	 * @return A shopping list item, or <code>null</code> if no item can be found.
	 */
	public ShoppinglistItem getItem(String id) {
		Cursor c = mDatabase.getItem(id);
		boolean b = c.moveToFirst();
		c.close();
		return b ? DbHelper.curToSli(c) : null;
	}

	/**
	 * Get a shopping list item by it's ID
	 * @param id of the shopping list item
	 * @return A shopping list item, or <code>null</code> if no item can be found.
	 */
	public ArrayList<ShoppinglistItem> getItems(Shoppinglist sl) {

		Cursor c = mDatabase.getItems(sl);
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.curToSli(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
		
	}
	
	/**
	 * TODO: Check if shopping list exists, and do stuff accordingly
	 * Add an item to a shopping list.<br>
	 * shopping list items is inserted into the database, and changes is synchronized to the server if possible.
	 * If the shopping list does not exist in the database or the server, a new one is created and synchronized if possible
	 * @param sl - shopping list where item should be added to
	 * @param sli - shopping list item that should be added.
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long addItem(final Shoppinglist sl, final ShoppinglistItem sli, final JsonObjectListener listener) {
		
		if (mustSync()) {

			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
//					Utilities.logd(TAG, "addItem", statusCode, data, error);
					if (Utils.isSuccess(statusCode)) {
						ShoppinglistItem s = ShoppinglistItem.fromJSON(data, sl.getId());
						s.setState(STATE_SYNCED);
						mDatabase.editItem(s);
						notifyItemUpdate(s.getId());
					} else {
						Utils.logd(TAG, error.toString());
						sli.setState(STATE_ERROR);
						mDatabase.editItem(sli);
						notifyItemUpdate(sli.getId());
					}
					listener.onComplete(statusCode, data, error);
				}
			};
			
			mEta.api().put(Endpoint.getItemById(mEta.getUser().getId(), sl.getId(), sli.getId()), cb, sli.getApiParams()).execute();
		}
		
		return mDatabase.addItem(sli.setShoppinglistId(sl.getId()));
		
	}

	/**
	 * Insert an updated shopping list item into the db.<br>
	 * shopping list items is replaced in the database, and changes is synchronized to the server if possible.
	 * @param sli - shopping list item to edit
	 */
	public boolean editItem(final ShoppinglistItem sli, final JsonObjectListener listener) {
		boolean resp = true;
		if (mustSync()) {
			
			sli.setState(STATE_SYNCING);
			resp = mDatabase.editItem(sli) == -1 ? false : true;

			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
//					Utilities.logd(TAG, "editItem", statusCode, data, error);
					ShoppinglistItem s = sli;
					if (Utils.isSuccess(statusCode)) {
						s = ShoppinglistItem.fromJSON(data, sli.getId());
						s.setState(STATE_SYNCED);
						mDatabase.editItem(s);
						notifyItemUpdate(s.getShoppinglistId());
					} else {
						Utils.logd(TAG, error.toString());
						s.setState(STATE_SYNCED);
					}
					listener.onComplete(statusCode, data, error);
				}
			};
			
			mEta.api().put(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), cb, sli.getApiParams()).enableFlag(Api.FLAG_DEBUG).execute();
			
		} else {
			resp = mDatabase.editItem(sli) == -1 ? false : true;
			if (resp)
				notifyItemUpdate(sli.getShoppinglistId());
		}
		return resp;

	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == true.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 * @return number of affected rows
	 */
	public void deleteItemsTicked(Shoppinglist sl, final JsonObjectListener listener) {
		deleteItems(sl, Shoppinglist.EMPTY_TICKED, listener);
	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == false.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 * @return number of affected rows
	 */
	public void deleteItemsUnticked(Shoppinglist sl, final JsonObjectListener listener) {
		deleteItems(sl, Shoppinglist.EMPTY_UNTICKED, listener);
	}

	/**
	 * Delete ALL items from a given shoppinglist.<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 * @return number of affected rows
	 */
	public void deleteItemsAll(Shoppinglist sl, final JsonObjectListener listener) {
		deleteItems(sl, Shoppinglist.EMPTY_ALL, listener);
	}
	
	/**
	 * Generic method to delete all items that matches any clauses given in the <code>apiParams</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl to remove items from
	 * @param whatToDelete describes what needs to be deleted
	 * @return number of affected rows of any local call, if it synchronizes to server, it returns 0
	 */
	private int deleteItems(final Shoppinglist sl, final String whatToDelete, final JsonObjectListener listener) {
		
		Bundle b = new Bundle();
		b.putString(Params.FILTER_DELETE, whatToDelete);
		
		final Boolean state =  (whatToDelete == Shoppinglist.EMPTY_TICKED) ? true : (whatToDelete == Shoppinglist.EMPTY_UNTICKED) ? false : null;
		
		int count = 0;
		ArrayList<ShoppinglistItem> list = getItems(sl);
		for (ShoppinglistItem sli : list) {
			if (state == null) {
				sli.setState(STATE_DELETING);
				mDatabase.editItem(sli);
			} else {
				if (sli.isTicked() == state) {
					sli.setState(STATE_DELETING);
					mDatabase.editItem(sli);
				}
			}
		}
		
		if (mustSync()) {
			
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
					
					if (Utils.isSuccess(statusCode)) {
						mDatabase.deleteItems(sl.getId(), state);
					} else {
						ArrayList<ShoppinglistItem> list = getItems(sl);
						for (ShoppinglistItem sli : list) {
							if (sli.getState() == STATE_DELETING) {
								sli.setState(STATE_SYNCED);
								mDatabase.editItem(sli);
							} 
						}
					}
					listener.onComplete(statusCode, data, error);
				}
			};
			mEta.api().delete(Endpoint.getListEmpty(mEta.getUser().getId(), sl.getId()), cb, b).execute();
		} else {
			count = mDatabase.deleteItems(sl.getId(), state);
		}
		notifyItemUpdate(sl.getId());
		return count;
		
	}
	
	/**
	 * Deletes a given shopping list item<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sli to delete from the db
	 * @return the number of rows affected
	 */
	public int deleteItem(ShoppinglistItem sli, final JsonObjectListener listener) {
		// TODO: Shoppinglist - Delete item from server
		Utils.logd(TAG, "ShoppinglistManager.deleteItem() not implemented yet");
		return mDatabase.deleteItem(sli.getId());
	}

	public void addShare(Shoppinglist sl, Share share, final JsonObjectListener listener) {
		// TODO: Share - Add share implementation
		Utils.logd(TAG, "ShoppinglistManager.addShare() not implemented yet");
	}

	public void deleteShare(Shoppinglist sl, Share share) {
		// TODO: Share - Delete share implementation
		Utils.logd(TAG, "ShoppinglistManager.deleteShare() not implemented yet");
	}

	public void editShare(Shoppinglist sl, Share share) {
		// TODO: Share - Edit share implementation
		Utils.logd(TAG, "ShoppinglistManager.editShare() not implemented yet");
	}
	
	public List<Share> getShares(Shoppinglist sl) {
		Utils.logd(TAG, "ShoppinglistManager.getShares() not implemented yet");
		return new ArrayList<Share>();
		// TODO: Share - get shares implementation
	}
	
	/**
	 * Sync all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param shoppinglist update
	 */
	public void syncLists() {
		
		if (!mustSync()) return;

		ListListener<Shoppinglist> sll = new ListListener<Shoppinglist>() {
			
			public void onComplete(int statusCode, List<Shoppinglist> data, EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					mergeErnObjects(data, getLists());
				} else {
					Utils.logd(TAG, error.toString());
				}
			}
		};
		mEta.api().get(Endpoint.getListsByUserId(mEta.getUser().getId()), sll).execute();
	}
	
	
	private <T extends List<? extends EtaErnObject>> void mergeErnObjects(T newLists, T oldLists) {
		
		HashMap<String, EtaErnObject> oldset = new HashMap<String, EtaErnObject>();
		for (EtaErnObject sl : oldLists) {
			oldset.put(sl.getId(), sl);
		}

		HashMap<String, EtaErnObject> newset = new HashMap<String, EtaErnObject>();
		for (EtaErnObject eeo : newLists) {
			newset.put(eeo.getId(), eeo);
		}
		
		HashSet<String> union = new HashSet<String>();
		union.addAll(newset.keySet());
		union.addAll(oldset.keySet());

		List<String> added = new ArrayList<String>();
		List<String> deleted = new ArrayList<String>();
		List<String> edited = new ArrayList<String>();

		for (String key : union) {
			
			if (oldset.containsKey(key)) {
				if (newset.containsKey(key)) {
					if (!oldset.get(key).equals(newset.get(key))) {
						edited.add(key);
						if (newset.get(key) instanceof Shoppinglist) {
							mDatabase.editList((Shoppinglist)newset.get(key));
						} else if (newset.get(key) instanceof Shoppinglist) {
							mDatabase.editItem((ShoppinglistItem)newset.get(key));
						}
					}
				} else {
					deleted.add(key);
					mDatabase.deleteList(key);
				}
			} else {
				added.add(key);
				if (newset.get(key) instanceof Shoppinglist) {
					mDatabase.insertList((Shoppinglist)newset.get(key));
				} else if (newset.get(key) instanceof Shoppinglist) {
//					mDatabase.insertItem((ShoppinglistItem)newset.get(key));
				}
			}
		}
		
		// If no changes has been registeres, ship the rest
		if (!added.isEmpty() || !deleted.isEmpty() || !edited.isEmpty()) {
			syncItems();
		}
		
		notifyListUpdate(added.isEmpty() ? null : added, deleted.isEmpty() ? null : deleted, edited.isEmpty() ? null : edited);
		
	}

	/**
	 * Sync all shopping list items, in all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param shoppinglist update
	 */
	public void syncItems() {

		if (!mustSync()) return;

		for (final Shoppinglist sl : getLists()) {
			if (sl.getState() != STATE_SYNCING || sl.getState() != STATE_DELETING) {
				if (sl.getState() != STATE_SYNCED) {
					// If the list haven't synced yet, then don't ask, just do it
					// this is the case for both new lists and modified lists
					syncItems(sl);
				} else {
					// Else make a call to check when it's been modified
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(int statusCode, JSONObject data, EtaError error) {

//							Utilities.logd(TAG, "syncItems()", statusCode, data, error);
							
							if (Utils.isSuccess(statusCode)) {
								// If callback says the list has been modified, then sync items
								Date oldModified = sl.getModified();
								try {
									sl.setModified(data.getString(Shoppinglist.S_MODIFIED));
									if (oldModified.before(sl.getModified()))
										syncItems(sl);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							} else {
								Utils.logd(TAG, error.toString());
							}
						}
					};
					
					mEta.api().get(Endpoint.getListModifiedById(mEta.getUser().getId(), sl.getId()), cb).execute();
				}
			}
			
		}
		
	}
	
	/**
	 * Sync all shopping list items, associated with the given shopping list.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param shoppinglist update
	 */
	public void syncItems(final Shoppinglist sl) {

		Utils.logd(TAG, "syncItems(Shoppinglist)");
		
		sl.setState(STATE_SYNCING);
		mDatabase.editList(sl);
		
		JsonArrayListener cb = new JsonArrayListener() {
			
			public void onComplete(int statusCode, JSONArray data, EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					mDatabase.deleteItems(sl.getId(), null);
					mDatabase.addItems(ShoppinglistItem.fromJSON(data, sl.getId()));
					sl.setState(STATE_SYNCED);
				} else {
					Utils.logd(TAG, error.toString());
					sl.setState(STATE_ERROR);
				}
				mDatabase.editList(sl);
				notifyItemUpdate(sl.getId());
			}
		};
		
		mEta.api().get(Endpoint.getItemByListId(mEta.getUser().getId(), sl.getId()), cb).execute();
		
	}
	
	/**
	 * Start synchronization with server
	 * If Eta.onResume() is called, there should be no need to call this.
	 */
	public void startSync() {
		if (mustSync()) {
			mSyncLoop.run();
		}
	}
	
	/**
	 * Stop synchronization to server
	 * If Eta.onPause() is called, there should be no need to call this.
	 */
	public void stopSync() {
		mEta.getHandler().removeCallbacks(mSyncLoop);
	}
	
	public void onResume() {
		mDatabase.openDB();
		startSync();
	}
	
	public void onPause() {
		stopSync();
		mDatabase.closeDB();
	}
	
	/**
	 * Set the synchronization intervals for the shoppinglists, and their items.<br>
	 * The synchronization of items will be the time specified, and the list
	 * synchronization will be a factor three of that time, as the lists themselves
	 * are less subjected to change. Also time must be 3000 milliseconds or more.
	 * @param time in milliseconds
	 */
	public void setSyncSpeed(int time) {
		if (time == SYNC_SLOW || time == SYNC_MEDIUM || time == SYNC_FAST )
			mSyncSpeed = time;
	}
	
	/**
	 * Checks if a user is logged in, and are able to sync items.
	 * @return true if we can sync, else false.
	 */
	private boolean mustSync() {

		if (!mEta.getUser().isLoggedIn()) {
			Utils.logd(TAG, "No user loggedin, cannot sync shoppinglists");
			stopSync();
			return false;
		}
		return true;
	}
	
	public ShoppinglistManager subscribe(ShoppinglistManagerListener listener) {
		if (!mSubscribers.contains(listener)) {
			mSubscribers.add(listener);
		}
		return this;
	}

	public boolean unsubscribe(ShoppinglistManagerListener listener) {
		return mSubscribers.remove(listener);
	}

	/**
	 * Wrapper method to make notification when adding a new shoppinglist.<br>
	 * This will call
	 * @param id
	 * @return
	 */
	public ShoppinglistManager notifyListAdd(String id) {
		List<String> list = new ArrayList<String>();
		list.add(id);
		notifyListUpdate( list , new ArrayList<String>(), new ArrayList<String>());
		return this;
	}

	public ShoppinglistManager notifyListDelete(String id) {
		List<String> list = new ArrayList<String>();
		list.add(id);
		notifyListUpdate(new ArrayList<String>(), list, new ArrayList<String>());
		return this;
	}

	public ShoppinglistManager notifyListEdit(String id) {
		List<String> list = new ArrayList<String>();
		list.add(id);
		notifyListUpdate(new ArrayList<String>(), new ArrayList<String>(), list);
		return this;
	}
	
	public ShoppinglistManager notifyListUpdate(List<String> added, List<String> deleted, List<String> edited) {
//		Utilities.logd(TAG, "Sending LIST notifications - " + String.valueOf(mSubscribers.size()));
		for (ShoppinglistManagerListener s : mSubscribers) {
			try {
				s.onListUpdate(added, deleted, edited);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return this;
	}

	public ShoppinglistManager notifyItemUpdate(String shoppinglistId) {
//		Utilities.logd(TAG, "Sending ITEM notifications - " + String.valueOf(mSubscribers.size()));
		for (ShoppinglistManagerListener s : mSubscribers) {
			try {
				s.onItemUpdate(shoppinglistId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	
	public interface ShoppinglistManagerListener {
		public void onListUpdate(List<String> addedIds, List<String> deletedIds, List<String> editedIds);
		public void onItemUpdate(String shoppinglistId);
	}
	
}
