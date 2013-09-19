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

import android.annotation.SuppressLint;
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
	public static final int STATE_DELETE	= 4;
	public static final int STATE_ERROR		= 5;
	
	private static final int CONNECTION_TIMEOUR_RETRY = 10000;
	
	private Eta mEta;
	private int mSyncSpeed = SYNC_MEDIUM;
	private String mCurrentSlId = null;

	private HashSet<String> mApiQueueItems = new HashSet<String>();
	private List<QueueItem> mApiQueue = Collections.synchronizedList(new ArrayList<QueueItem>());
	
	private ArrayList<ShoppinglistManagerListener> mSubscribers = new ArrayList<ShoppinglistManager.ShoppinglistManagerListener>();
	
	private int syncCount = 0;
	private Runnable mSyncLoop = new Runnable() {
		
		public void run() {
			
			mEta.getHandler().postDelayed(mSyncLoop, mSyncSpeed);
			
			if (mApiQueue.size() == 0) {

                if (syncCount%3 == 0) {
                    syncLists();
                } else {
                    syncListsModified();
                }
                syncCount++;
			}
			
		}
		
	};

	public ShoppinglistManager(Eta eta) {
		mEta = eta;
		mCurrentSlId = mEta.getSettings().getShoppinglistManagerCurrent();
	}

	/**
	 * Sync all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 */
	public void syncLists() {
		
		if (!mustSync()) return;

		ListListener<Shoppinglist> sll = new ListListener<Shoppinglist>() {
			
			public void onComplete(boolean isCache, int statusCode, List<Shoppinglist> data, EtaError error) {
				
				if (mApiQueue.size() > 0)
					return;
				
				if (Utils.isSuccess(statusCode)) {
					mergeErnObjects( data, getLists());
				} 
			}
		};
		mEta.getApi().get(Endpoint.getListsByUserId(mEta.getUser().getId()), sll).execute();
		
	}
	
	
	/**
	 * Sync all shopping list items, in all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 */
	public void syncListsModified() {

		for (final Shoppinglist sl : getLists()) {
			if (sl.getState() != STATE_SYNCING || sl.getState() != STATE_DELETE) {
				if (sl.getState() == STATE_TO_SYNC) {
					// New shopping lists must always sync
					syncItems(sl);
				} else {
					
					sl.setState(STATE_SYNCING);
					DbHelper.getInstance().editList(sl);
					
					// Else make a call to check when it's been modified
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {

							if (mApiQueue.size() > 0)
								return;
							
							if (Utils.isSuccess(statusCode)) {
								// If callback says the list has been modified, then sync items
								try {
									Date newDate = Utils.parseDate(data.getString(Shoppinglist.S_MODIFIED));
									
									// If list has been modified, then sync
									if (sl.getModified().before(newDate)) {
										// Make sure to set a variable, so we can sync on resume if necessary
										syncItems(sl);
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							} else {
								sl.setState(STATE_ERROR);
								DbHelper.getInstance().editList(sl);
							}
						}
					};
					
					mEta.getApi().get(Endpoint.getListModifiedById(mEta.getUser().getId(), sl.getId()), cb).execute();
				}
			}
			
		}
		
	}
	
	/**
	 * Sync all shopping list items, associated with the given shopping list.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param sl shoppinglist to update
	 */
	public void syncItems(final Shoppinglist sl) {
		
		sl.setState(STATE_SYNCING);
		DbHelper.getInstance().editList(sl);
		
		JsonArrayListener cb = new JsonArrayListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONArray data, EtaError error) {

                if (mApiQueue.size() > 0)
					return;
				
				if (Utils.isSuccess(statusCode)) {
					sl.setState(STATE_SYNCED);
					DbHelper.getInstance().editList(sl);
					mergeErnObjects( ShoppinglistItem.fromJSON(data), getItems(sl));
				} else {
					sl.setState(STATE_ERROR);
					DbHelper.getInstance().editList(sl);
					notifySubscribers(false, true, null, null, idToList(sl.getId()));
				}
			}
		};
		
		mEta.getApi().get(Endpoint.getItemByListId(mEta.getUser().getId(), sl.getId()), cb).execute();
		
	}

	private <T extends List<? extends EtaErnObject>> void mergeErnObjects(T newLists, T oldLists) {
		
		boolean isList;
		if (0 < newLists.size()) {
			isList = (newLists.get(0) instanceof Shoppinglist);
		} else if (0 < oldLists.size()) {
			isList = (oldLists.get(0) instanceof Shoppinglist);
		} else {
			Utils.logd(TAG, "Nothing to merge");
			return;
		}

		HashMap<String, EtaErnObject> oldset = new HashMap<String, EtaErnObject>();
		for (EtaErnObject o : oldLists) {
			oldset.put(o.getId(), o);
		}

		HashMap<String, EtaErnObject> newset = new HashMap<String, EtaErnObject>();
		for (EtaErnObject o : newLists) {
			newset.put(o.getId(), o);
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
					
					Object o = setState(isList, newset.get(key), STATE_SYNCED);
					if (!oldset.get(key).equals(o)) {
						edited.add(key);
						if (isList) {
							DbHelper.getInstance().editList((Shoppinglist)o);
						} else {
							DbHelper.getInstance().editItem((ShoppinglistItem)o);
						}
					}
				} else {
					deleted.add(key);
					if (isList) {
						DbHelper.getInstance().deleteList(key);
					} else {
						DbHelper.getInstance().deleteItem(key);
					}
				}
			} else {
				added.add(key);

				Object o = setState(isList, newset.get(key), STATE_TO_SYNC);
				
				if (isList) {
					DbHelper.getInstance().insertList((Shoppinglist)o);
					syncItems((Shoppinglist)o);
				} else {
					DbHelper.getInstance().insertItem((ShoppinglistItem)newset.get(key));
				}
			}
		}
		
		// If no changes has been registeres, ship the rest
		if (!added.isEmpty() || !deleted.isEmpty() || !edited.isEmpty()) {
			if (isList) {
				for (Shoppinglist sl : getLists()) {
					syncItems(sl);
				}
			}
			notifySubscribers(false, isList, added, deleted, edited);

		}
		
	}

	private Object setState(boolean isList, Object o, int state) {
		if (isList) {
			return ((Shoppinglist)o).setState(state);
		} else {
			return ((ShoppinglistItem)o).setState(state);
		}
		
	}
	
	private void addQueue(Api api, String id) {
		mApiQueue.add(new QueueItem(api, id));
		mApiQueueItems.add(id);
        for (QueueItem q : mApiQueue) {
            q.execute();
        }
	}
	
	class QueueItem {
		public String id;
		private int user;
		private boolean working = false;
		private Api api;
		private JsonObjectListener oldListener;
		private JsonObjectListener listener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {
				
				// If connection timeout
				if (statusCode == -1) {

					mEta.getHandler().postDelayed(new Runnable() {
						
						public void run() {
							if (mEta.isResumed())
								call();
						}
					}, CONNECTION_TIMEOUR_RETRY);
					
				} else {
					
					mApiQueue.remove(QueueItem.this);
					mApiQueueItems.remove(id);
					
					if (user == mEta.getUser().getId()) {
						oldListener.onComplete(false, statusCode, item, error);
					}
					
				}
				
			}
		};
		
		public QueueItem(Api api, String id) {
			user = mEta.getUser().getId();
			this.id = id;
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
			api.execute();
		}
		
	}
	
	/**
	 * Get the shopping list that is currently in use, by the user.<br>
	 * The current list is synchronized more often, than other lists, since the list is likely
	 * to be visible to the user. This is useful when juggling multiple lists.
	 * @return The shopping list currently in use. or <code>null</code> if no shoppinglists exist.
	 */
	public Shoppinglist getCurrentList() {
		Shoppinglist sl;
		sl = getList(mCurrentSlId);
		if (sl == null) {
			List<Shoppinglist> list =  DbHelper.getInstance().getLists();
			if (list.size()>0) {
				sl = list.get(0);
				setCurrentList(sl);
			} else {
				//There is no lists..
				// TODO: Either create a list, or check if we have already updated
			}
		}
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
		notifySubscribers(true, false, new ArrayList<String>(0), new ArrayList<String>(0), new ArrayList<String>(0));
		return this;
	}
	
	/**
	 * Get a shoppinglist from it's ID.
	 * @param id of the shoppinglist to get
	 * @return A shopping list, or <code>null</code> if no shopping list exists
	 */
	public Shoppinglist getList(String id) {
		if (id == null)
			return null;
		return DbHelper.getInstance().getList(id);
	}

	/**
	 * The complete set of shopping lists
	 * @return <li>All shopping lists
	 */
	public ArrayList<Shoppinglist> getLists() {
		return DbHelper.getInstance().getLists();
	}
	
	/**
	 * Get a shopping list from it's human readable name
	 * @param name of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public ArrayList<Shoppinglist> getListFromName(String name) {
		return DbHelper.getInstance().getListFromName(name);
	}

	/**
	 * Add a new shopping list.<br>
	 * If owner haven't been set, we will assume that it is the user who is currently logged in.
	 * if no user is logged inn, then we assume it is a offline list.<br>
	 * shopping list added to the database, and changes is synchronized to the server if possible.<br>
	 * 
	 * @param sl - the new shoppinglist to add to the database, and server
	 */
	public void addList(final Shoppinglist sl, final OnCompletetionListener listener) {
		
		sl.setModified(new Date());
		
		if (mustSync()) {

			if (sl.getOwner().getEmail() == null) {
				sl.getOwner().setEmail(mEta.getUser().getEmail());
				sl.getOwner().setAccess(Share.ACCESS_OWNER);
				sl.getOwner().setAccepted(true);
			}
			
			sl.setState(STATE_SYNCING);
			
			// Sync online if possible
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {

					Shoppinglist s = sl;
					if (Utils.isSuccess(statusCode)) {
						s = Shoppinglist.fromJSON(data);
						s.setState(STATE_SYNCED);
					} else {
						s.setState(STATE_ERROR);
						revertList(sl);
					}

                    DbHelper.getInstance().editList(s);
                    syncItems(s);
					listener.onComplete(false, statusCode, data, error);
					notifySubscribers(false, true, idToList(s.getId()), null, null);
				}
			};
			
			Api a = mEta.getApi().put(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), cb, sl.getApiParams());
			addQueue(a, sl.getId());
		} else {
			sl.setState(STATE_SYNCED);
		}
		
		int row = DbHelper.getInstance().insertList(sl);
		if (row == 1) {
			listener.onComplete(true, 200, null, null);
			notifySubscribers(true, false, idToList(sl.getId()), null, null);
		} else {
			listener.onComplete(true, 400, null, null);
		}
		
	}

	/**
	 * Edit a shopping list already in the database.<br>
	 * shopping list is replaced in the database, and changes is synchronized to the server if possible.<br>
	 * @param sl - Shopping list to be replaced
	 */
	public void editList(final Shoppinglist sl, final OnCompletetionListener listener) {
		editList(sl, listener, new Date());
	}
	
	private void editList(final Shoppinglist sl, final OnCompletetionListener listener, Date date) {
		
		sl.setModified(date);
		
		if (mustSync()) {

			sl.setState(STATE_SYNCING);
			
			JsonObjectListener editItem = new JsonObjectListener() {
				
				public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
					
					Shoppinglist s = sl;
					if (Utils.isSuccess(statusCode)) {
						s = Shoppinglist.fromJSON(data);
						s.setState(STATE_SYNCED);
					} else {
						s.setState(STATE_ERROR);
						revertList(sl);
					}
					DbHelper.getInstance().editList(s);
					listener.onComplete(false, statusCode, data, error);
					notifySubscribers(false, true, null, null, idToList(s.getId()));
					
				}
			};
			
			Api a = mEta.getApi().put(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), editItem, sl.getApiParams());
			addQueue(a, sl.getId());
		} else {
			sl.setState(STATE_SYNCED);
		}


		int row = DbHelper.getInstance().editList(sl);
		// Do local callback stuff
		if (row == 1) {
			listener.onComplete(true, 200, null, null);
			notifySubscribers(true, false, null, null, idToList(sl.getId()));
		} else {
			listener.onComplete(true, 400, null, null);
		}
		
	}

	/**
	 * Delete a shopping list.<br>
	 * shopping list is deleted from the database, and changes is synchronized to the server if possible.<br>
	 * All shopping list items associated with the shopping list are also deleted.
	 * @param sl - Shopping list to delete
	 */
	public void deleteList(final Shoppinglist sl, final OnCompletetionListener listener) {
		deleteList(sl, listener, new Date());
	}
	
	private void deleteList(final Shoppinglist sl, final OnCompletetionListener listener, Date date) {
		
		int row = 0;
		sl.setModified(date);
		
		for (ShoppinglistItem sli : getItems(sl)) {
			sli.setState(STATE_DELETE);
			DbHelper.getInstance().editItem(sli);
		}

		sl.setState(STATE_DELETE);
		DbHelper.getInstance().editList(sl);
		
		if (mustSync()) {
			
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
					
					if (Utils.isSuccess(statusCode)) {
						if (DbHelper.getInstance().deleteList(sl.getId()) > 0) {
							DbHelper.getInstance().deleteItems(sl.getId(), null);
						}
					} else {
						Shoppinglist s = getList(sl.getId());
						if (s != null) {
							s.setState(STATE_ERROR);
							DbHelper.getInstance().editList(s);
							revertList(sl);
						}
					}
					listener.onComplete(true, statusCode, data, error);
					notifySubscribers(false, true, null, idToList(sl.getId()), null);
				}
			};
			
			Api a = mEta.getApi().delete(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), cb, sl.getApiParams());
			addQueue(a, sl.getId());
			
		} else {
			row = DbHelper.getInstance().deleteList(sl.getId());
			if (row == 1) {
				DbHelper.getInstance().deleteItems(sl.getId(), null);
			}
		}
		
		// Do local callback stuff
		if (row == 1) {
			listener.onComplete(true, 200, null, null);
			notifySubscribers(true, false, null, idToList(sl.getId()), null);
		} else {
			listener.onComplete(true, 400, null, null);
		}
		
	}

	/**
	 * Get a shopping list item by it's ID
	 * @param id of the shopping list item
	 * @return A shopping list item, or <code>null</code> if no item can be found.
	 */
	public ShoppinglistItem getItem(String id) {
		return DbHelper.getInstance().getItem(id);
	}

	/**
	 * Get a shopping list from it's human readable name
	 * @param description of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public ArrayList<ShoppinglistItem> getItemFromDescription(String description) {
		return DbHelper.getInstance().getItemFromDescription(description);
	}

	/**
	 * Get a shopping list item by it's ID
	 * @param sl of the shopping list item
	 * @return A list of shoppinglistitem.
	 */
	public ArrayList<ShoppinglistItem> getItems(Shoppinglist sl) {
		return DbHelper.getInstance().getItems(sl);
		
	}

	/**
	 * Add an item to a shopping list.<br>
	 * shopping list items is inserted into the database, and changes is synchronized to the server if possible.
	 * If the shopping list does not exist in the database or the server, a new one is created and synchronized if possible
	 * @param sli - shopping list item that should be added.
	 */
	public void addItem(final ShoppinglistItem sli, final OnCompletetionListener listener) {
		addItem(sli, true, listener);
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
	public void addItem(final ShoppinglistItem sli, boolean incrementCountItemExists, final OnCompletetionListener listener) {
		
		if (sli.getOfferId() == null && sli.getDescription() == null) {
			Utils.logd(TAG, "Shoppinglist item seems to be empty, please add stuff");
			return;
		}
		
		if (incrementCountItemExists) {
			
			List<ShoppinglistItem> items = getItems(getList(sli.getShoppinglistId()));
			
			if (sli.getOfferId() != null) {
				for (ShoppinglistItem s : items) {
					if (sli.getOfferId().equals(s.getOfferId())) {
						s.setCount(s.getCount() + 1);
						editItem(s, listener);
						return;
					}
				}
			} else {
				for (ShoppinglistItem s : items) {
					String sliOld = s.getDescription();
					String sliNew = sli.getDescription().toLowerCase();
					if (sliOld != null && sliNew.equals(sliOld.toLowerCase())) {
						s.setCount(s.getCount() + 1);
						editItem(s, listener);
						return;
					}
				}
			}
		}
		
		sli.setModified(new Date());
		long row;
		
		if (mustSync()) {
			
			sli.setState(STATE_SYNCING);
			
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
					
					ShoppinglistItem s = sli;
					if (Utils.isSuccess(statusCode)) {
						s = ShoppinglistItem.fromJSON(data);
						s.setState(STATE_SYNCED);
					} else {
						s.setState(STATE_ERROR);
						revertItem(sli);
					}
					DbHelper.getInstance().editItem(s);
					listener.onComplete(false, statusCode, data, error);
					notifySubscribers(false, false, idToList(s.getId()), null, null);
				}
			};
			
			Api a = mEta.getApi().put(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), cb, sli.getApiParams());
			addQueue(a, sli.getId());
			
		} else {
			sli.setState(STATE_SYNCED);
		}
		
		row = DbHelper.getInstance().insertItem(sli);

		// Do local callback stuff
		if (row == 1) {
			listener.onComplete(true, 200, null, null);
			notifySubscribers(true, false, idToList(sli.getId()), null, null);
		} else {
			listener.onComplete(true, 400, null, null);
		}
			
	}
	
	/**
	 * Insert an updated shopping list item into the db.<br>
	 * shopping list items is replaced in the database, and changes is synchronized to the server if possible.
	 * @param sli shopping list item to edit
	 */
	public void editItem(final ShoppinglistItem sli, final OnCompletetionListener listener) {
		editItem(sli, listener, new Date());
	}

	private void editItem(final ShoppinglistItem sli, final OnCompletetionListener listener, Date date) {

		sli.setModified(date);
		long row;
		
		// Edit state according to login state, and push to DB
		if (mustSync()) {
			
			sli.setState(STATE_SYNCING);

			// Setup callback
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
					
					ShoppinglistItem s = sli;
					if (Utils.isSuccess(statusCode)) {
						s = ShoppinglistItem.fromJSON(data);
						s.setState(STATE_SYNCED);
					} else {
						s.setState(STATE_ERROR);
						revertItem(sli);
					}
					DbHelper.getInstance().editItem(s);
					listener.onComplete(false, statusCode, data, error);
					notifySubscribers(false, false, null, null, idToList(s.getId()));
					
				}
			};
			
			// Push edit to server
			Api a = mEta.getApi().put(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), cb, sli.getApiParams());

			addQueue(a, sli.getId());
			
		} else {
			sli.setState(STATE_SYNCED);
		}

		row = DbHelper.getInstance().editItem(sli);
		// Do local callback stuff
		if (row == 1) {
			listener.onComplete(true, 200, null, null);
			notifySubscribers(true, false, null, null, idToList(sli.getId()));
		} else {
			listener.onComplete(true, 400, null, null);
		}		
	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == true.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteItemsTicked(Shoppinglist sl, final OnCompletetionListener listener) {
		sl.setModified(new Date());
		deleteItems(sl, Shoppinglist.EMPTY_TICKED, listener);
	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == false.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteItemsUnticked(Shoppinglist sl, final OnCompletetionListener listener) {
		deleteItems(sl, Shoppinglist.EMPTY_UNTICKED, listener);
	}

	/**
	 * Delete ALL items from a given shoppinglist.<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteItemsAll(Shoppinglist sl, final OnCompletetionListener listener) {
		deleteItems(sl, Shoppinglist.EMPTY_ALL, listener);
	}
	
	/**
	 * Generic method to delete all items that matches any clauses given in the <code>apiParams</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl to remove items from
	 * @param whatToDelete describes what needs to be deleted
	 */
	private void deleteItems(final Shoppinglist sl, final String whatToDelete, final OnCompletetionListener listener) {
		
		Date d = new Date();
		
		Bundle b = new Bundle();
		b.putString(Params.FILTER_DELETE, whatToDelete);
		b.putString(Params.MODIFIED, Utils.formatDate(d));
		
		final List<String> deleted = new ArrayList<String>();
		
		// Ticked = true, unticked = false, all = null.. all in a nice ternary
		final Boolean state = whatToDelete == null ? null : whatToDelete.equals(Shoppinglist.EMPTY_TICKED) ? true : false;

        long row = 0;

        ArrayList<ShoppinglistItem> list = getItems(sl);
		for (ShoppinglistItem sli : list) {
			if (state == null) {
				// Delete all items
				sli.setState(STATE_DELETE);
				sli.setModified(d);
				DbHelper.getInstance().editItem(sli);
				deleted.add(sli.getId());
                row++;
			} else if (sli.isTicked() == state) {
				// Delete if ticked matches the requested state
				sli.setState(STATE_DELETE);
				sli.setModified(d);
				DbHelper.getInstance().editItem(sli);
				deleted.add(sli.getId());
                row++;
			}
		}
		
		boolean sync = mustSync(); 
		
		if (sync) {
			
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
					
					if (Utils.isSuccess(statusCode)) {
						DbHelper.getInstance().deleteItems(sl.getId(), state);
					} else {
						ArrayList<ShoppinglistItem> list = getItems(sl);
						for (ShoppinglistItem sli : list) {
							if (sli.getState() == STATE_DELETE) {
								sli.setState(STATE_ERROR);
								DbHelper.getInstance().editItem(sli);
								revertItem(sli);
							} 
						}
					}
					listener.onComplete(false, statusCode, data, error);
					notifySubscribers(false, false, null, deleted, null);
				}
			};
			Api a = mEta.getApi().post(Endpoint.getListEmpty(mEta.getUser().getId(), sl.getId()), cb, b);
			addQueue(a, sl.getId());
			
		} else {
			row = DbHelper.getInstance().deleteItems(sl.getId(), state) ;

		}

        if (!sync && row == deleted.size()) {
			listener.onComplete(true, 200, null, null);
			notifySubscribers(true, false, null, deleted, null);
		} else if (sync) {
			listener.onComplete(true, 200, null, null);
			notifySubscribers(true, false, null, deleted, null);
		} else {
			listener.onComplete(true, 400, null, null);
		}
		
	}
	
	/**
	 * Deletes a given shopping list item<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sli to delete from the db
	 */
	public void deleteItem(final ShoppinglistItem sli, final OnCompletetionListener listener) {
		deleteItem(sli, listener, new Date());
	}
	
	private void deleteItem(final ShoppinglistItem sli, final OnCompletetionListener listener, Date date) {

		long row = 0;
		sli.setModified(date);
		
		if (mustSync()) {
			
			sli.setState(STATE_DELETE);
			row = DbHelper.getInstance().editItem(sli);
			
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {
					
					if (Utils.isSuccess(statusCode)) {
						DbHelper.getInstance().deleteItems(sli.getId(), null);
					} else {
						sli.setState(STATE_ERROR);
						DbHelper.getInstance().editItem(sli);
						revertItem(sli);
					}
					listener.onComplete(false, statusCode, item, error);
					notifySubscribers(false, false, null, idToList(sli.getId()), null);
				}
			};
			
			Api a = mEta.getApi().delete(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), cb, sli.getApiParams());
			addQueue(a, sli.getId());
			
			
		} else {
			row = DbHelper.getInstance().deleteItem(sli.getId());
		}
		
		// Do local callback stuff
		if (row == 1) {
			listener.onComplete(true, 200, null, null);
			notifySubscribers(true, false, null, idToList(sli.getId()), null);
		} else {
			listener.onComplete(true, 400, null, null);
		}
		
	}

	OnCompletetionListener cleanupListener = new OnCompletetionListener() {
		
		public void onComplete(boolean isLocalCallback, int statusCode,
				JSONObject item, EtaError error) {
		}
	};
	
	private void cleanupDB() {
		
		List<Shoppinglist> sls = getLists();
		int count = 0;
		
		for (Shoppinglist sl : sls) {
			
			// First check if any items in the db need to be synced
			if (!mApiQueueItems.contains(sl.getId())) {
				
				switch (sl.getState()) {
					
					case STATE_DELETE:
						deleteList(sl, cleanupListener, sl.getModified());
						break;
						
					case STATE_SYNCING:
					case STATE_TO_SYNC:
						editList(sl, cleanupListener, sl.getModified());
						break;
	
					case STATE_ERROR:
						revertList(sl);
						break;
						
					default:
						count += 1;
						break;
				}
				
			}
			
			cleanItems(sl);
			
		}
		
		// If we didn't have anything local to commit, then sync everything 
		if (sls.size() == count)
			syncLists();
		
	}
	
	private void cleanItems(Shoppinglist sl) {
		
		List<ShoppinglistItem> slis = getItems(sl);
		int count = 0;

		int inQueue = 0;
		int notInQueue = 0;
		
		for (ShoppinglistItem sli : slis) {
			
			// If, the items not currently in the process of being sync'ed
			// Then check state, and determine if further action is necessary
			if (!mApiQueueItems.contains(sli.getId())) {
				
				notInQueue += 1;
				
				switch (sli.getState()) {
					case STATE_DELETE:
						deleteItem(sli, cleanupListener, sli.getModified());
						break;
						
					case STATE_SYNCING:
					case STATE_TO_SYNC:
						editItem(sli, cleanupListener, sli.getModified());
						break;
						
					case STATE_ERROR:
						revertItem(sli);
						break;

					default:
						count += 1;
						break;
				}
				
			} else {
				inQueue += 1;
			}
			
			
		}
		
		// If we didn't have anything local to commit, then sync everything 
		if (slis.size() == count)
			syncItems(sl);
		
	}
	
	private void revertList(final Shoppinglist sl) {

		JsonObjectListener listListener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item,
					EtaError error) {

				
				if (Utils.isSuccess(statusCode)) {
					Shoppinglist s = Shoppinglist.fromJSON(item);
					s.setState(STATE_SYNCED);
					DbHelper.getInstance().editList(s);
				} else {
					DbHelper.getInstance().deleteList(sl.getId());
				}
				
			}
		};
		
		mEta.getApi().get(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), listListener).execute();
		
	}
	
	private void revertItem(final ShoppinglistItem sli) {

		JsonObjectListener itemListener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item,
					EtaError error) {

				if (Utils.isSuccess(statusCode)) {
					ShoppinglistItem s = ShoppinglistItem.fromJSON(item);
					s.setState(STATE_SYNCED);
					DbHelper.getInstance().editItem(s);
				} else {
					DbHelper.getInstance().deleteItem(sli.getId());
				}
				
			}
		};
		
		mEta.getApi().get(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), itemListener).execute();
		
	}
	
	public void onResume() {
		DbHelper.getInstance().openDB();
		if (mEta.getUser().isLoggedIn()) {
			cleanupDB();
			mSyncLoop.run();
		}
	}
	
	public void onPause() {
		if (mEta.getUser().isLoggedIn()) {
			mEta.getHandler().removeCallbacks(mSyncLoop);
		}
		DbHelper.getInstance().closeDB();
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
	 * Deletes all rows in DB.
	 */
	public void clearDB() {
		DbHelper.getInstance().clear();
	}

	/**
	 * Deletes all rows belonging to a logged in user
	 */
	public void clearUserDB() {
		DbHelper.getInstance().clearUserDB();
	}

	/**
	 * Deletes all rows not belonging to a user (offline usage)
	 */
	public void clearNonUserDB() {
		DbHelper.getInstance().clearNonUserDB();
	}
	
	/**
	 * Checks if a user is logged in, and are able to sync items.
	 * @return true if we can sync, else false.
	 */
	private boolean mustSync() {
		boolean sync = mEta.getUser().isLoggedIn();
		if (!sync) {
			Utils.logd(TAG, "Not able to sync - user loggedin");
			onPause();
		} 
		return sync;
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

	public List<String> idToList(String id) {
		List<String> list = new ArrayList<String>();
		list.add(id);
		return list;
	}

    /**
     * Nethod for notifying all subscribers is changes to any shoppinglist/shoppinglistitems.
     *
     * @param isLocalChange true if the change occoured in the local database, else it's a callback from server
     * @param isList true if it's changes to a shoppinglist, else it's changes to a shoppinglist item
     * @param added the id's thats been added
     * @param deleted the id's thats been deleted
     * @param edited the id's thats been edited
     */
	public ShoppinglistManager notifySubscribers(boolean isLocalChange, boolean isList, List<String> added, List<String> deleted, List<String> edited) {
		
		if (added == null)
			added = new ArrayList<String>(0);
		
		if (deleted == null)
			deleted = new ArrayList<String>(0);
		
		if (edited == null)
			edited = new ArrayList<String>(0);
		
		for (ShoppinglistManagerListener s : mSubscribers) {
			try {
				s.onUpdate(isLocalChange, isList, added, deleted, edited);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return this;
	}

	public interface ShoppinglistManagerListener {
        /**
         * The interface for recieving updates from the shoppinglist manager, given that you have subscribed to updates.
         *
         * @param isLocalChange true if the change occoured in the local database, else it's a callback from server
         * @param isList true if it's changes to a shoppinglist, else it's changes to a shoppinglist item
         * @param addedIds the id's thats been added
         * @param deletedIds the id's thats been deleted
         * @param editedIds the id's thats been edited
         */
		public void onUpdate(boolean isLocalChange, boolean isList, List<String> addedIds, List<String> deletedIds, List<String> editedIds);
	}
	
	public interface OnCompletetionListener {
        /**
         * The interface for recieving callbacks, whenever you are interacting with the shoppinglist manager
         * @param isLocalChange true if the change occoured in the local database, else it's a callback from server
         * @param statusCode that the server has responede with
         * @param item the server reaponds with on a successfull request. Is <code>null</code> if server responded with an error
         * @param error if server isn't able to fulfill the request, an error is returned and item is <code>null</code>
         */
		public void onComplete(boolean isLocalChange, int statusCode, JSONObject item, EtaError error);
	}
	
}
