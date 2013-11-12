package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Api.JsonArrayListener;
import com.eTilbudsavis.etasdk.Api.JsonObjectListener;
import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.Session.SessionListener;
import com.eTilbudsavis.etasdk.Database.DbQuery;
import com.eTilbudsavis.etasdk.Database.QueryDispatcher;
import com.eTilbudsavis.etasdk.Database.RunnableQuery;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Share;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ShoppinglistManager {

	public static final String TAG = "ShoppinglistManager";
	
	/** Supported sync speeds for shopping list manager */
	public interface SyncSpeed {
		int SLOW = 10000;
		int MEDIUM = 6000;
		int FAST = 3000;
	}
	
	private static final int CONNECTION_TIMEOUR_RETRY = 10000;
	
	/** The global eta object */
	private Eta mEta;
	
	/** The DbRequest queue */
	@SuppressWarnings("rawtypes")
	private final BlockingQueue<DbQuery> mQueue = new PriorityBlockingQueue<DbQuery>();
	private QueryDispatcher mQueryDispatcher;
	private AtomicInteger mSequence = new AtomicInteger();
	
	private int mSyncSpeed = SyncSpeed.MEDIUM;
	private boolean mHasFirstListSync = false;

	private HashSet<String> mApiQueueItems = new HashSet<String>();
	private List<QueueItem> mApiQueue = Collections.synchronizedList(new ArrayList<QueueItem>());

	/** Subscriber queue for shopping list item changes */
	private List<OnChangeListener<ShoppinglistItem>> mItemSubscribers = new ArrayList<OnChangeListener<ShoppinglistItem>>();

	/** Subscriber queue for shopping list changes */
	private List<OnChangeListener<Shoppinglist>> mListSubscribers = new ArrayList<OnChangeListener<Shoppinglist>>();
	
	/** Simple counter keeping track of sync loop */
	private int syncCount = 0;

	public ShoppinglistManager(Eta eta) {
		mEta = eta;
		mQueryDispatcher = new QueryDispatcher(mQueue);
		mQueryDispatcher.start();
	}

	private void addQueue(Runnable r) {
		RunnableQuery q = new RunnableQuery(r);
		int i = mSequence.incrementAndGet();
		q.setSequence(i);
		mQueue.add(q);
	}
	
	private Runnable mSyncLoop = new Runnable() {
		
		public void run() {
			
			if (mEta.getUser().isLoggedIn() && mEta.isResumed()) {
				
				int user = user();
				
				mEta.getHandler().postDelayed(mSyncLoop, mSyncSpeed);
				
				if (mApiQueue.size() == 0) {

	                if (syncCount%3 == 0) {
	                    syncLists(user);
	                } else {
	                    syncListsModified(user);
	                }
	                syncCount++;
				}

			}
			
		}
		
	};

	/**
	 * Sync all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 */
	public void syncLists(final int user) {
		
		if (!mustSync()) return;

		ListListener<Shoppinglist> sll = new ListListener<Shoppinglist>() {
			
			public void onComplete(boolean isCache, int statusCode, List<Shoppinglist> data, EtaError error) {
				
				if (mApiQueue.size() > 0)
					return;
				
				if (Utils.isSuccess(statusCode)) {
					mergeShoppinglists( data, getLists(), user);
				} 
			}
		};
		mEta.getApi().get(Endpoint.getListsByUserId(mEta.getUser().getId()), sll).execute();
		
	}

	private void mergeShoppinglists(List<Shoppinglist> newList, List<Shoppinglist> oldList, int userId) {

		if (newList.isEmpty() && oldList.isEmpty())
			return;

		HashMap<String, Shoppinglist> oldset = new HashMap<String, Shoppinglist>();
		for (Shoppinglist o : oldList) {
			oldset.put(o.getId(), o);
		}

		HashMap<String, Shoppinglist> newset = new HashMap<String, Shoppinglist>();
		for (Shoppinglist o : newList) {
			newset.put(o.getId(), o);
		}
		
		HashSet<String> union = new HashSet<String>();
		union.addAll(newset.keySet());
		union.addAll(oldset.keySet());

		List<Shoppinglist> added = new ArrayList<Shoppinglist>();
		List<Shoppinglist> deleted = new ArrayList<Shoppinglist>();
		List<Shoppinglist> edited = new ArrayList<Shoppinglist>();

		for (String key : union) {
			
			if (oldset.containsKey(key)) {
				
				if (newset.containsKey(key)) {
					
					Shoppinglist newSl = newset.get(key);
					newSl.setState(Shoppinglist.State.SYNCED);

					Shoppinglist oldSl = oldset.get(key);
					
					if (oldSl.getModified().before(newSl.getModified())) {
						edited.add(newSl);
						DbHelper.getInstance().editList(newSl, user());
					} else if (oldSl.getModified().after(newSl.getModified()) && oldSl.getState() != ShoppinglistItem.State.SYNCING) {
						editList(oldSl, null, newSl.getModified(), newSl.getUserId());
					}
					
					if (!oldSl.getModified().equals(newSl.getModified())) {
						DbHelper.getInstance().editList(newSl, user());
					}
					
				} else {
					deleted.add(oldset.get(key));
					DbHelper.getInstance().deleteList(oldset.get(key), user());
				}
			} else {
				added.add(newset.get(key));
				Shoppinglist sl = newset.get(key);
				sl.setState(Shoppinglist.State.TO_SYNC);
				DbHelper.getInstance().insertList(sl, user());
			}
		}

		// At this point, all lists (not their items) are sync'ed to the DB
		mHasFirstListSync = true;
		
		// If no changes has been registeres, ship the rest
		if (!added.isEmpty() || !deleted.isEmpty() || !edited.isEmpty()) {
			
			for (Shoppinglist sl : getLists()) {
				syncItems(sl, userId);
			}
			notifyListSubscribers(true, added, deleted, edited);
			
		}
		
	}
	
	
	/**
	 * Sync all shopping list items, in all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 */
	public void syncListsModified(final int userId) {
		
		addQueue(new Runnable() {
			
			public void run() {
				
				for (final Shoppinglist sl : getLists()) {
					
					// If they are in the state of processing, then skip
					if (sl.getState() == Shoppinglist.State.SYNCING || sl.getState() == Shoppinglist.State.DELETE) 
						return;
					
					// If it obviously needs syncing, then just do it
					if (sl.getState() == Shoppinglist.State.TO_SYNC) {
						// New shopping lists must always sync
						syncItems(sl, userId);
						return;
					} 
					
					// Run the check 
					sl.setState(Shoppinglist.State.SYNCING);
					DbHelper.getInstance().editList(sl, userId);
					
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, final EtaError error) {
							
							addQueue(new Runnable() {
								
								public void run() {
									
									if (mApiQueue.size() > 0)
										return;
									
									if (Utils.isSuccess(statusCode)) {
										// If callback says the list has been modified, then sync items
										try {
											Date newDate = Utils.parseDate(data.getString(Shoppinglist.S_MODIFIED));
											
											// If list has been modified, then sync
											if (sl.getModified().before(newDate)) {
												syncItems(sl, userId);
											}
											sl.setState(Shoppinglist.State.SYNCED);
											DbHelper.getInstance().editList(sl, userId);
											
										} catch (JSONException e) {
											e.printStackTrace();
										}
									} else {
										revertList(sl, null, userId);
									}
									
								}
							});
							
						}
					};
					
					mEta.getApi().get(Endpoint.getListModifiedById(mEta.getUser().getId(), sl.getId()), cb).execute();
					
				}
				
			}
		});
		
		
	}
	
	/**
	 * Sync all shopping list items, associated with the given shopping list.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param sl shoppinglist to update
	 */
	public void syncItems(final Shoppinglist sl, final int user) {
		
		final int userId = user();
		
		addQueue(new Runnable() {
			
			public void run() {
				
				sl.setState(Shoppinglist.State.SYNCING);
				DbHelper.getInstance().editList(sl, userId);
				
				JsonArrayListener cb = new JsonArrayListener() {
					
					public void onComplete(final boolean isCache, final int statusCode, final JSONArray data, final EtaError error) {
						
						addQueue(new Runnable() {
							
							public void run() {
								
								if (mApiQueue.size() > 0)
									return;
								
								if (Utils.isSuccess(statusCode)) {
									sl.setState(Shoppinglist.State.SYNCED);
									DbHelper.getInstance().editList(sl, userId);
									mergeShoppinglistItems( ShoppinglistItem.fromJSON(data), getItems(sl));
								} else {
									revertList(sl, null, userId);
								}
								
							}
						});
		                
					}
				};
				
				mEta.getApi().get(Endpoint.getItemByListId(mEta.getUser().getId(), sl.getId()), cb).execute();
				
			}
		});
		
	}

	private void mergeShoppinglistItems(List<ShoppinglistItem> newList, List<ShoppinglistItem> oldList) {
		
		int user = user();
		
		if (newList.isEmpty() && oldList.isEmpty())
			return;

		HashMap<String, ShoppinglistItem> oldset = new HashMap<String, ShoppinglistItem>();
		for (ShoppinglistItem sli : oldList) {
			oldset.put(sli.getId(), sli);
		}

		HashMap<String, ShoppinglistItem> newset = new HashMap<String, ShoppinglistItem>();
		for (ShoppinglistItem sli : newList) {
			sli.setState(ShoppinglistItem.State.SYNCED);
			newset.put(sli.getId(), sli);
		}
		
		HashSet<String> union = new HashSet<String>();
		union.addAll(newset.keySet());
		union.addAll(oldset.keySet());

		List<ShoppinglistItem> added = new ArrayList<ShoppinglistItem>();
		List<ShoppinglistItem> deleted = new ArrayList<ShoppinglistItem>();
		List<ShoppinglistItem> edited = new ArrayList<ShoppinglistItem>();
		
		for (String key : union) {

			ShoppinglistItem snew = newset.get(key);
			ShoppinglistItem sold = oldset.get(key);
			
			if (oldset.containsKey(key)) {

				if (newset.containsKey(key)) {
					
					if (sold.getModified().before(snew.getModified())) {
						edited.add(snew);
						DbHelper.getInstance().editItem(snew, user);
					} else if (sold.getModified().after(snew.getModified()) && 
							sold.getState() != ShoppinglistItem.State.SYNCING) {
						editItem(sold, null, sold.getModified(), user);
					} else if (!sold.getPreviousId().equals(snew.getPreviousId())) {
						
						// This is a special case, thats only relevant as long as the
						// server isn't sending previous_id's
						
						sold.setPreviousId(snew.getPreviousId());
						DbHelper.getInstance().editItem(sold, user);
					}
				} else {
					deleted.add(oldset.get(key));
					DbHelper.getInstance().deleteItem(oldset.get(key), user);
				}
			} else {
				added.add(snew);
				DbHelper.getInstance().insertItem(snew, user);
			}
		}
		
		// If no changes has been registeres, ship the rest
		if (!added.isEmpty() || !deleted.isEmpty() || !edited.isEmpty()) {
			notifyItemSubscribers(true, added, deleted, edited);

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
								api.execute();
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
			
			working = true;
			api.execute();
		}
		
	}

	public boolean hasFirstListSync() {
		return mHasFirstListSync;
	}
	
	/**
	 * Get the shopping list that is currently in use, by the user.<br>
	 * The current list is synchronized more often, than other lists, since the list is likely
	 * to be visible to the user. This is useful when juggling multiple lists.
	 * @return The shopping list currently in use. or <code>null</code> if no shoppinglists exist.
	 */
	public Shoppinglist getCurrentList() {
		Shoppinglist sl;
		boolean isLoggedIn = mEta.getUser().isLoggedIn();
		String c = mEta.getSettings().getShoppinglistManagerCurrent(isLoggedIn);
		sl = getList(c);
		if (sl == null) {
			sl = DbHelper.getInstance().getFirstList(user());
			if (sl != null) {
				setCurrentList(sl);
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
		String c = sl.getId();
		mEta.getSettings().setShoppinglistManagerCurrent(c, mEta.getUser().isLoggedIn());
		notifyListSubscribers(false, null, null, null);
		return this;
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
	public ArrayList<Shoppinglist> getLists() {
		ArrayList<Shoppinglist> list = DbHelper.getInstance().getLists(user()); 
		return list;
	}
	
	/**
	 * Get a shopping list from it's human readable name
	 * @param name of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public ArrayList<Shoppinglist> getListFromName(String name) {
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
	public void addList(final Shoppinglist sl, final OnCompletetionListener<Shoppinglist> l) {
		
		final int userId = user();
		
		addQueue(new Runnable() {
			
			public void run() {
				
				sl.setModified(new Date());
				sl.setPreviousId(ShoppinglistItem.FIRST_ITEM);
				int count = 0;
				
				Shoppinglist first = DbHelper.getInstance().getFirstList(userId);
				if (first != null) {
					first.setPreviousId(sl.getId());
					DbHelper.getInstance().editList(first, userId);
				}
				
				if (mustSync()) {

					if (sl.getOwner().getEmail() == null) {
						sl.getOwner().setEmail(mEta.getUser().getEmail());
						sl.getOwner().setAccess(Share.ACCESS_OWNER);
						sl.getOwner().setAccepted(true);
					}
					
					sl.setState(Shoppinglist.State.SYNCING);
					count = DbHelper.getInstance().insertList(sl, userId);
					
					// Sync online if possible
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, final EtaError error) {
							
							addQueue(new Runnable() {
								
								public void run() {
									
									Shoppinglist s = sl;
									if (Utils.isSuccess(statusCode)) {
										s = Shoppinglist.fromJSON(data);
										Shoppinglist dbList = DbHelper.getInstance().getList(s.getId(), userId);
										if (dbList != null && !s.getModified().before(dbList.getModified()) ) {
											s.setState(Shoppinglist.State.SYNCED);
											s.setPreviousId(s.getPreviousId() == null ? sl.getPreviousId() : s.getPreviousId());
						                    DbHelper.getInstance().editList(s, userId);
						                    syncItems(s, userId);
											notifyListListener(l, false, 200, sl, null);
											notifyListSubscribers(false, idToList(sl), null, null);
										}
									} else {
										revertList(s, l, userId);
									}
									
								}
							});
							
						}
					};
					
					Api a = mEta.getApi().put(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), cb, sl.getApiParams());
					addQueue(a, sl.getId());
				} else {
					sl.setState(Shoppinglist.State.SYNCED);
					count = DbHelper.getInstance().insertList(sl, userId);
				}
				
				if (count == 1) {
					notifyListListener(l, false, 200, sl, null);
					notifyListSubscribers(false, idToList(sl), null, null);
				} else {
					revertList(sl, l, userId);
				}
				
				
			}
		});
		
	}

	/**
	 * Edit a shopping list already in the database.<br>
	 * shopping list is replaced in the database, and changes is synchronized to the server if possible.<br>
	 * @param sl - Shopping list to be replaced
	 */
	public void editList(Shoppinglist sl, OnCompletetionListener<Shoppinglist> listener) {
		editList(sl, listener, new Date(), user());
	}
	
	private void editList(final Shoppinglist sl, final OnCompletetionListener<Shoppinglist> l, final Date date, final int userId) {
		
		addQueue(new Runnable() {
			
			public void run() {
				
				// Check for changes in previous item, and update surrounding
				Shoppinglist oldList = DbHelper.getInstance().getList(sl.getId(), userId);
				if (oldList == null) {
					EtaLog.d(TAG, "No such list exists, considder addList() instead: " + sl.toString());
					return;
				}
				
				if (!oldList.getPreviousId().equals(sl.getPreviousId())) {
					
					DbHelper d = DbHelper.getInstance();
					
					// If there is an item pointing at sl, it needs to point at the oldList.prev
					Shoppinglist sliAfter = d.getListPrevious(sl.getId(), userId);
					if (sliAfter != null) {
						sliAfter.setPreviousId(oldList.getPreviousId());
						d.editList(sliAfter, userId);
					}
					
					// If some another sl was pointing at the same item, it should be pointing at sl
					Shoppinglist sliSamePointer = d.getListPrevious(sl.getPreviousId(), userId);
					if (sliSamePointer != null) {
						sliSamePointer.setPreviousId(sl.getId());
						d.editList(sliSamePointer, userId);
					}
					
				}
				
				sl.setModified(date);
				
				if (mustSync()) {

					sl.setState(Shoppinglist.State.SYNCING);
					
					JsonObjectListener editItem = new JsonObjectListener() {
						
						public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
							
							Shoppinglist s = sl;
							if (Utils.isSuccess(statusCode)) {
								s = Shoppinglist.fromJSON(data);
								Shoppinglist dbList = DbHelper.getInstance().getList(s.getId(), userId);
								if (dbList != null && !s.getModified().before(dbList.getModified()) ) {
									s.setState(Shoppinglist.State.SYNCED);
									// If server havent delivered an prev_id, then use old id
									s.setPreviousId(s.getPreviousId() == null ? sl.getPreviousId() : s.getPreviousId());
									DbHelper.getInstance().editList(s, userId);
									notifyListListener(l, false, 200, sl, null);
									notifyListSubscribers(false, null, null, idToList(sl));
								}

							} else {
								revertList(sl, l, userId);
							}
							
						}
					};
					
					Api a = mEta.getApi().put(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), editItem, sl.getApiParams());
					addQueue(a, sl.getId());
				} else {
					sl.setState(Shoppinglist.State.SYNCED);
				}


				int row = DbHelper.getInstance().editList(sl, userId);
				// Do local callback stuff
				if (row == 1) {
					notifyListListener(l, false, 200, sl, null);
					notifyListSubscribers(false, null, null, idToList(sl));
				} else {
					revertList(sl, l, userId);
				}
				
				
				
			}
		});
		
	}

	/**
	 * Delete a shopping list.<br>
	 * shopping list is deleted from the database, and changes is synchronized to the server if possible.<br>
	 * All shopping list items associated with the shopping list are also deleted.
	 * @param sl - Shopping list to delete
	 */
	public void deleteList(final Shoppinglist sl, final OnCompletetionListener<Shoppinglist> listener) {
		deleteList(sl, listener, new Date(), user());
	}
	
	private void deleteList(final Shoppinglist sl, final OnCompletetionListener<Shoppinglist> l, final Date date, final int userId) {
		
		addQueue(new Runnable() {
			
			public void run() {
				
				int row = 0;
				sl.setModified(date);
				
				// Update previous pointer, to preserve order
				Shoppinglist after = DbHelper.getInstance().getListPrevious(sl.getId(), userId);
				if (after != null) {
					after.setPreviousId(sl.getPreviousId());
					DbHelper.getInstance().editList(after, userId);
				}
				
				// Mark all items in list to be deleted
				for (ShoppinglistItem sli : getItems(sl)) {
					sli.setState(Shoppinglist.State.DELETE);
					DbHelper.getInstance().editItem(sli, userId);
				}
				
				// Update local version of shoppinglist
				sl.setState(Shoppinglist.State.DELETE);
				DbHelper.getInstance().editList(sl, userId);
				
				if (mustSync()) {
					
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, final EtaError error) {
							
							addQueue(new Runnable() {
								
								public void run() {
									
									if (Utils.isSuccess(statusCode)) {
										DbHelper.getInstance().deleteList(sl, userId);
										DbHelper.getInstance().deleteItems(sl.getId(), null, userId);
										notifyListListener(l, true, statusCode, sl, null);
										notifyListSubscribers(true, null, idToList(sl), null);
									} else {
										revertList(sl, l, userId);
									}
									
								}
							});
							
						}
					};
					
					Api a = mEta.getApi().delete(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), cb, sl.getApiParams());
					addQueue(a, sl.getId());
					
				} else {
					row = DbHelper.getInstance().deleteList(sl, userId);
					if (row == 1) {
						DbHelper.getInstance().deleteItems(sl.getId(), null, userId);
					}
				}
				
				// Do local callback stuff
				if (row == 1) {
					notifyListListener(l, false, 200, sl, null);
					notifyListSubscribers(false, null, idToList(sl), null);
				} else {
					revertList(sl, l, userId);
				}
				
			}
		});
		
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
	 * @param description of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public ArrayList<ShoppinglistItem> getItemFromDescription(String description) {
		return DbHelper.getInstance().getItemFromDescription(description, user());
	}
	
	
	/**
	 * Get a shopping list item by it's ID
	 * @param sl of the shopping list item
	 * @return A list of shoppinglistitem.
	 */
	public ArrayList<ShoppinglistItem> getItems(Shoppinglist sl) {
		ArrayList<ShoppinglistItem> list = DbHelper.getInstance().getItems(sl, user());
		return list;
	}
	
	/**
	 * Add an item to a shopping list.<br>
	 * shopping list items is inserted into the database, and changes is synchronized to the server if possible.
	 * If the shopping list does not exist in the database or the server, a new one is created and synchronized if possible
	 * @param sli - shopping list item that should be added.
	 */
	public void addItem(final ShoppinglistItem sli, final OnCompletetionListener<ShoppinglistItem> l) {
		addItem(sli, true, l);
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
	public void addItem(final ShoppinglistItem sli, final boolean incrementCountItemExists, final OnCompletetionListener<ShoppinglistItem> l) {
		
		final int userId = user();
		
		addQueue(new Runnable() {
			
			public void run() {
				
				if (sli.getOfferId() == null && sli.getDescription() == null) {
					EtaLog.d(TAG, "Shoppinglist item seems to be empty, please add stuff");
					return;
				}
				
				// If the item exists in DB, then just increase count and edit the item
				if (incrementCountItemExists) {
					
					List<ShoppinglistItem> items = getItems(getList(sli.getShoppinglistId()));
					
					if (sli.getOfferId() != null) {
						for (ShoppinglistItem s : items) {
							if (sli.getOfferId().equals(s.getOfferId())) {
								s.setCount(s.getCount() + 1);
								editItem(s, l);
								return;
							}
						}
					} else {
						for (ShoppinglistItem s : items) {
							String sliOld = s.getDescription();
							String sliNew = sli.getDescription().toLowerCase();
							if (sliOld != null && sliNew.equals(sliOld.toLowerCase())) {
								s.setCount(s.getCount() + 1);
								editItem(s, l);
								return;
							}
						}
					}
				}

				// If not increment, then do all the hard work
				sli.setModified(new Date());
				sli.setPreviousId(ShoppinglistItem.FIRST_ITEM);
				
				ShoppinglistItem first = DbHelper.getInstance().getFirstItem(sli.getShoppinglistId(), userId);
				if (first != null) {
					first.setPreviousId(sli.getId());
					DbHelper.getInstance().editItem(first, userId);
				}
				
				int count = 0;
				
				if (mustSync()) {
					
					sli.setState(ShoppinglistItem.State.SYNCING);
					
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, final EtaError error) {
							
							addQueue(new Runnable() {
								
								public void run() {
									
									ShoppinglistItem s = sli;
									if (Utils.isSuccess(statusCode)) {
										s = ShoppinglistItem.fromJSON(data);
										ShoppinglistItem dbItem = DbHelper.getInstance().getItem(s.getId(), userId);
										if (dbItem != null && !s.getModified().before(dbItem.getModified()) ) {
											s.setState(ShoppinglistItem.State.SYNCED);
											// If server havent delivered an prev_id, then use old id
											s.setPreviousId(s.getPreviousId() == null ? sli.getPreviousId() : s.getPreviousId());
											DbHelper.getInstance().editItem(s, userId);
										}
									} else {
										revertItem(sli, l, userId);
									}
									
								}
							});
							
						}
					};
					
					Api a = mEta.getApi().put(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), cb, sli.getApiParams());
					addQueue(a, sli.getId());
					
				} else {
					sli.setState(ShoppinglistItem.State.SYNCED);
				}
				
				count = DbHelper.getInstance().insertItem(sli, userId);
				
				// Do local callback stuff
				if (count == 1) {
					notifyItemListener(l, false, 200, sli, null);
					notifyItemSubscribers(false, idToList(sli), null, null);
				} else {
					revertItem(sli, l, userId);
				}
					
				
			}
		});
		
	}
	
	/**
	 * Insert an updated shopping list item into the db.<br>
	 * shopping list items is replaced in the database, and changes is synchronized to the server if possible.
	 * @param sli shopping list item to edit
	 */
	public void editItem(ShoppinglistItem sli, OnCompletetionListener<ShoppinglistItem> listener) {
		editItem(sli, listener, new Date(), user());
	}

	private void editItem(final ShoppinglistItem sli, final OnCompletetionListener<ShoppinglistItem> l, final Date date, final int userId) {
		
		addQueue(new Runnable() {
			
			public void run() {
				
				// Check for changes in previous item, and update surrounding
				ShoppinglistItem oldItem = DbHelper.getInstance().getItem(sli.getId(), userId);
				if (oldItem == null) {
					EtaLog.d(TAG, "No such item exists, considder addItem() instead: " + sli.toString());
					return;
				}
				
				if (!oldItem.getPreviousId().equals(sli.getPreviousId())) {
					
					DbHelper d = DbHelper.getInstance();
					String sl = sli.getShoppinglistId();
					
					// If there is an item pointing at sli, it needs to point at the oldSli.prev
					ShoppinglistItem sliAfter = d.getItemPrevious(sl, sli.getId(), userId);
					if (sliAfter != null) {
						sliAfter.setPreviousId(oldItem.getPreviousId());
						d.editItem(sliAfter, userId);
					}
					
					// If some another sli was pointing at the same item, it should be pointing at sli
					ShoppinglistItem sliSamePointer = d.getItemPrevious(sl, sli.getPreviousId(), userId);
					if (sliSamePointer != null) {
						sliSamePointer.setPreviousId(sli.getId());
						d.editItem(sliSamePointer, userId);
					}
					
				}
				
				sli.setModified(date);
				long row;
				
				// Edit state according to login state, and push to DB
				if (mustSync()) {
					
					sli.setState(ShoppinglistItem.State.SYNCING);

					// Setup callback
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, EtaError error) {
							
							addQueue(new Runnable() {
								
								public void run() {
									
									ShoppinglistItem s = sli;
									if (Utils.isSuccess(statusCode)) {
										s = ShoppinglistItem.fromJSON(data);
										ShoppinglistItem dbItem = DbHelper.getInstance().getItem(s.getId(), userId);
										if (dbItem != null && !s.getModified().before(dbItem.getModified()) ) {
											s.setState(ShoppinglistItem.State.SYNCED);
											// If server havent delivered an prev_id, then use old id
											s.setPreviousId(s.getPreviousId() == null ? sli.getPreviousId() : s.getPreviousId());
											DbHelper.getInstance().editItem(s, userId);
								        	notifyItemListener(l, true, statusCode, s, null);
								        	notifyItemSubscribers(true, null, null, idToList(s));
										}
									} else {
										revertItem(s, l, userId);
									}
									
								}
							});
							
						}
					};
					
					// Push edit to server
					Api a = mEta.getApi().put(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), cb, sli.getApiParams());

					addQueue(a, sli.getId());
					
				} else {
					sli.setState(ShoppinglistItem.State.SYNCED);
				}

				row = DbHelper.getInstance().editItem(sli, userId);
				
		        if (row == 1) {
		        	notifyItemListener(l, false, 200, sli, null);
		        	notifyItemSubscribers(false, null, null, idToList(sli));
				} else {
					revertItem(sli, l, userId);
				}
		        
			}
		});
		
	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == true.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteItemsTicked(Shoppinglist sl, final OnCompletetionListener<Shoppinglist> listener) {
		sl.setModified(new Date());
		deleteItems(sl, Shoppinglist.EMPTY_TICKED, listener);
	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == false.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteItemsUnticked(Shoppinglist sl, final OnCompletetionListener<Shoppinglist> listener) {
		deleteItems(sl, Shoppinglist.EMPTY_UNTICKED, listener);
	}

	/**
	 * Delete ALL items from a given shoppinglist.<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteItemsAll(Shoppinglist sl, final OnCompletetionListener<Shoppinglist> listener) {
		deleteItems(sl, Shoppinglist.EMPTY_ALL, listener);
	}
	
	/**
	 * Generic method to delete all items that matches any clauses given in the <code>apiParams</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl to remove items from
	 * @param whatToDelete describes what needs to be deleted
	 */
	private void deleteItems(final Shoppinglist sl, final String whatToDelete, final OnCompletetionListener<Shoppinglist> l) {
		
		final int userId = user();
		
		addQueue(new Runnable() {
			
			public void run() {
				
				Date d = new Date();
				
				Bundle b = new Bundle();
				b.putString(Params.FILTER_DELETE, whatToDelete);
				b.putString(Params.MODIFIED, Utils.formatDate(d));
				
				// Ticked = true, unticked = false, all = null.. all in a nice ternary
				final Boolean state = whatToDelete.equals(Shoppinglist.EMPTY_ALL) ? null : whatToDelete.equals(Shoppinglist.EMPTY_TICKED) ? true : false;

		        
		        ArrayList<ShoppinglistItem> list = getItems(sl);
		        final List<ShoppinglistItem> deleted = new ArrayList<ShoppinglistItem>();
		        int count = list.size();

				String preGoodId = ShoppinglistItem.FIRST_ITEM;
				
				for (ShoppinglistItem sli : list) {
					if (state == null) {
						// Delete all items
						sli.setState(ShoppinglistItem.State.DELETE);
						sli.setModified(d);
						DbHelper.getInstance().editItem(sli, userId);
						deleted.add(sli);
					} else if (sli.isTicked() == state) {
						// Delete if ticked matches the requested state
						sli.setState(ShoppinglistItem.State.DELETE);
						sli.setModified(d);
						DbHelper.getInstance().editItem(sli, userId);
						deleted.add(sli);
					} else {
						if (!sli.getPreviousId().equals(preGoodId)) {
							sli.setPreviousId(preGoodId);
							DbHelper.getInstance().editItem(sli, userId);
						}
						preGoodId = sli.getId();
						count --;
					}
				}
				
				boolean sync = mustSync(); 
				
				if (sync) {
					
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, final EtaError error) {
							
							addQueue(new Runnable() {
								
								public void run() {
									
									if (Utils.isSuccess(statusCode)) {
										DbHelper.getInstance().deleteItems(sl.getId(), state, userId);
									} else {
										ArrayList<ShoppinglistItem> items = getItems(sl);
										for (ShoppinglistItem sli : items) {
											if (sli.getState() == ShoppinglistItem.State.DELETE) {
												revertItem(sli, null, userId);
											} 
										}
									}
									
								}
							});
							
						}
					};
					Api a = mEta.getApi().post(Endpoint.getListEmpty(mEta.getUser().getId(), sl.getId()), cb, b);
					addQueue(a, sl.getId());
					
				} else {
					count = DbHelper.getInstance().deleteItems(sl.getId(), state, userId) ;

				}

		        if (count == deleted.size()) {
		        	for (ShoppinglistItem sli : deleted) {
			        	notifyItemListener(l, false, 200, sli, null);
			        	notifyItemSubscribers(false, null, deleted, null);
		        	}
				} else {
		        	revertList(sl, l, userId);
				}
		        
			}
		});
		
		
	}
	
	/**
	 * Deletes a given shopping list item<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sli to delete from the db
	 */
	public void deleteItem(final ShoppinglistItem sli, final OnCompletetionListener<ShoppinglistItem> listener) {
		deleteItem(sli, listener, new Date(), user());
	}
	
	private void deleteItem(final ShoppinglistItem sli, final OnCompletetionListener<ShoppinglistItem> l, final Date date, final int userId) {
		
		addQueue(new Runnable() {
			
			public void run() {
				
				// Update previous pointer
				ShoppinglistItem after = DbHelper.getInstance().getItemPrevious(sli.getShoppinglistId(), sli.getId(), userId);
				if (after != null) {
					after.setPreviousId(sli.getPreviousId());
					DbHelper.getInstance().editItem(after, userId);
				}

				int count = 0;
				sli.setModified(date);
				
				if (mustSync()) {
					
					sli.setState(ShoppinglistItem.State.DELETE);
					count = DbHelper.getInstance().editItem(sli, userId);
					
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(final boolean isCache,final  int statusCode,final  JSONObject item,final  EtaError error) {
							
							new Runnable() {
								
								public void run() {
									if (Utils.isSuccess(statusCode)) {
										ShoppinglistItem s = ShoppinglistItem.fromJSON(item);
										DbHelper.getInstance().deleteItem(s, userId);
							        	notifyItemListener(l, true, statusCode, s, null);
							        	notifyItemSubscribers(true, null, idToList(s), null);
									} else {
										revertItem(sli, l, userId);
									}
								}
							};
							
						}
					};
					
					Api a = mEta.getApi().delete(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), cb, sli.getApiParams());
					addQueue(a, sli.getId());
					
					
				} else {
					count = DbHelper.getInstance().deleteItem(sli, userId);
				}
				
				// Do local callback stuff
				if (count == 1) {
					notifyItemListener(l, false, 200, sli, null);
					notifyItemSubscribers(false, null, idToList(sli), null);
				} else {
					revertItem(sli, l, userId);
				}
			}
		});
		
	}

	private void dbCleanup() {
		
		if (!mustSync()) return;
		
		List<Shoppinglist> sls = getLists();
		int count = 0;
		
		for (Shoppinglist sl : sls) {
			
			// First check if any items in the db need to be synced
			if (!mApiQueueItems.contains(sl.getId())) {
				
				switch (sl.getState()) {
					
					case Shoppinglist.State.DELETE:
						deleteList(sl, null, sl.getModified(), sl.getUserId());
						break;
						
					case Shoppinglist.State.SYNCING:
						editList(sl, null, sl.getModified(), sl.getUserId());
						break;
						
					case Shoppinglist.State.TO_SYNC:
						// Don't sync if not logged in
						if (mustSync())
							editList(sl, null, sl.getModified(), sl.getUserId());
						break;
	
					case Shoppinglist.State.ERROR:
						revertList(sl, null, sl.getUserId());
						break;
						
					default:
						count += 1;
						break;
				}

				cleanItems(sl);
			}
			
			
		}

		// If we didn't have anything local to commit, then sync everything 
		if (sls.size() == count)
			syncLists(user());

	}
	
	private void cleanItems(Shoppinglist sl) {

		List<ShoppinglistItem> slis = getItems(sl);
		int count = 0;
		
		for (ShoppinglistItem sli : slis) {

			// If, the items not currently in the process of being sync'ed
			// Then check state, and determine if further action is necessary
			if (!mApiQueueItems.contains(sli.getId())) {
				
				switch (sli.getState()) {
					case ShoppinglistItem.State.DELETE:
						deleteItem(sli, null, sli.getModified(), sli.getUserId());
						break;
						
					case ShoppinglistItem.State.SYNCING:
						editItem(sli, null, sli.getModified(), sli.getUserId());
						break;
						
					case ShoppinglistItem.State.TO_SYNC:
						// Don't sync if not logged in
						if (mustSync())
							editItem(sli, null, sli.getModified(), sli.getUserId());
						break;
						
					case ShoppinglistItem.State.ERROR:
						revertItem(sli, null, sli.getUserId());
						break;

					default:
						count += 1;
						break;
				}
				
			}
			
		}

		// If we didn't have anything local to commit, then sync everything 
		if (slis.size() == count) {
			syncItems(sl, sl.getUserId());
		}
		
	}
	
	private void revertList(final Shoppinglist sl, final OnCompletetionListener<Shoppinglist> l, final int userId) {
		
		addQueue(new Runnable() {
			
			public void run() {

				if (sl.getState() != Shoppinglist.State.ERROR) {
					sl.setState(Shoppinglist.State.ERROR);
					DbHelper.getInstance().editList(sl, userId);
				}
				
				JsonObjectListener listListener = new JsonObjectListener() {
					
					public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {

						Shoppinglist s = null;
						if (Utils.isSuccess(statusCode)) {
							s = Shoppinglist.fromJSON(item);
							s.setState(Shoppinglist.State.SYNCED);
							s.setPreviousId(s.getPreviousId() == null ? sl.getPreviousId() : s.getPreviousId());
							DbHelper.getInstance().editList(s, userId);
						} else {
							DbHelper.getInstance().deleteList(sl, userId);
						}
						notifyListSubscribers(true, null, null, idToList(s));
					}
				};
				mEta.getApi().get(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), listListener).execute();
				
			}
		});
		
	}
	
	private void revertItem(final ShoppinglistItem sli, final OnCompletetionListener<ShoppinglistItem> l, final int userId) {
		
		addQueue(new Runnable() {
			
			public void run() {
				
				if (sli.getState() != ShoppinglistItem.State.ERROR) {
					sli.setState(ShoppinglistItem.State.ERROR);
					DbHelper.getInstance().editItem(sli, userId);
				}
				
				JsonObjectListener itemListener = new JsonObjectListener() {
					
					public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {
						
						ShoppinglistItem s = null;
						if (Utils.isSuccess(statusCode)) {
							s = ShoppinglistItem.fromJSON(item);
							s.setState(ShoppinglistItem.State.SYNCED);
							s.setPreviousId(s.getPreviousId() == null ? sli.getPreviousId() : s.getPreviousId());
							DbHelper.getInstance().editItem(s, userId);
						} else {
							DbHelper.getInstance().deleteItem(sli, userId);
						}
						notifyItemListener(l, true, statusCode, s, error);
						notifyItemSubscribers(true, null, null, idToList(s));
					}
				};
				
				mEta.getApi().get(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), itemListener).execute();
				
				
			}
		});
		
	}
	
	SessionListener sessionListener = new SessionListener() {

		public void onUpdate() {
			// Remove this from UI thread, and into looper queue
			if (mEta.getUser().isLoggedIn()) {
				mSyncLoop.run();
			}
		}
		
	};
	
	public void onResume() {
		Eta.getInstance().getSession().subscribe(sessionListener);
		dbCleanup();
		mSyncLoop.run();
	}
	
	public void onPause() {
		Eta.getInstance().getSession().subscribe(sessionListener);
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
			mSyncSpeed = time;
	}
	
	private int user() {
		return Eta.getInstance().getUser().getId();
	}
	
	/**
	 * Deletes all rows in DB.
	 */
	public void clear() {
		DbHelper.getInstance().clear();
		if (Eta.getInstance().getUser().isLoggedIn())
			syncLists(user());
	}

	/**
	 * Deletes all rows belonging to a logged in user
	 */
	public void clear(int userId) {
		DbHelper.getInstance().clear(userId);
		syncLists(userId);
	}

	/**
	 * Checks if a user is logged in, and are able to sync items.
	 * @return true if we can sync, else false.
	 */
	private boolean mustSync() {
		boolean sync = mEta.getUser().isLoggedIn();
		if (!sync) {
			onPause();
		} 
		return sync;
	}

	public void setItemListener(OnChangeListener<ShoppinglistItem> l) {
		if (!mItemSubscribers.contains(l)) mItemSubscribers.add(l);
	}

	public void removeItemListener(OnChangeListener<ShoppinglistItem> l) {
		mItemSubscribers.remove(l);
	}

	public void setListListener(OnChangeListener<Shoppinglist> l) {
		if (!mListSubscribers.contains(l)) mListSubscribers.add(l);
	}

	public void removeListListener(OnChangeListener<Shoppinglist> l) {
		mListSubscribers.remove(l);
	}

	public <T> List<T> idToList(T object) {
		if (object == null)
			return null;
		
		List<T> list = new ArrayList<T>(1);
		list.add(object);
		return list;
	}
	

	@SuppressWarnings("rawtypes")
	private void notifyListListener(final OnCompletetionListener l, final boolean isServer, final int statusCode, final Shoppinglist o, final EtaError e) {
		if (l == null)
			return;
		
		Eta.getInstance().getHandler().post(new Runnable() {
			
			@SuppressWarnings("unchecked")
			public void run() {
				l.onComplete(isServer, statusCode, o, e);
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private void notifyItemListener(final OnCompletetionListener l, final boolean isServer, final int statusCode, final ShoppinglistItem o, final EtaError e) {
		if (l == null)
			return;
		
		Eta.getInstance().getHandler().post(new Runnable() {
			
			@SuppressWarnings("unchecked")
			public void run() {
				l.onComplete(isServer, statusCode, o, e);
			}
		});
	}
	
	private void notifyItemSubscribers(boolean isServer, List<ShoppinglistItem> added, List<ShoppinglistItem> deleted, List<ShoppinglistItem> edited) {

		if (added == null) added = new ArrayList<ShoppinglistItem>(0);
		
		if (deleted == null) deleted = new ArrayList<ShoppinglistItem>(0);
		
		if (edited == null) edited = new ArrayList<ShoppinglistItem>(0);
		
		postItemSubscribers(isServer, added, deleted, edited);
		
	}
	
	private void postItemSubscribers(final boolean isServer, final List<ShoppinglistItem> added, final List<ShoppinglistItem> deleted, final List<ShoppinglistItem> edited) {
		
		for (final OnChangeListener<ShoppinglistItem> s : mItemSubscribers) {
			try {
				mEta.getHandler().post(new Runnable() {
					
					public void run() {
						s.onUpdate(isServer, added, deleted, edited);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void notifyListSubscribers(boolean isServer, List<Shoppinglist> added, List<Shoppinglist> deleted, List<Shoppinglist> edited) {

		if (added == null) added = new ArrayList<Shoppinglist>(0);
		
		if (deleted == null) deleted = new ArrayList<Shoppinglist>(0);
		
		if (edited == null) edited = new ArrayList<Shoppinglist>(0);
		
		postListSubscribers(isServer, added, deleted, edited);
		
	}
	
    /**
     * Method for notifying all subscribers is changes to any shoppinglist/shoppinglistitems.
     *
     * @param isServer true if server response
     * @param added the id's thats been added
     * @param deleted the id's thats been deleted
     * @param edited the id's thats been edited
     */
	private void postListSubscribers(final boolean isServer, final List<Shoppinglist> added, final List<Shoppinglist> deleted, final List<Shoppinglist> edited) {

		for (final OnChangeListener<Shoppinglist> s : mListSubscribers) {
			mEta.getHandler().post(new Runnable() {
				
				public void run() {
					s.onUpdate(isServer, added, deleted, edited);
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
	}
	
	public interface OnCompletetionListener<T> {
        /**
         * The interface for recieving callbacks, whenever you are interacting with the shoppinglist manager
         * @param isServer true if server response
         * @param statusCode that the server has responede with
         * @param item the server reaponds with on a successfull request. Is <code>null</code> if server responded with an error
         * @param error if server isn't able to fulfill the request, an error is returned and item is <code>null</code>
         */
		public void onComplete(boolean isServer, int statusCode, T object, EtaError error);
	}
	
}