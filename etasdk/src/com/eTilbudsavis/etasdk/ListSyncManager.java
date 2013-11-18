package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.eTilbudsavis.etasdk.Api.JsonArrayListener;
import com.eTilbudsavis.etasdk.Api.JsonObjectListener;
import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.Session.SessionListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ListSyncManager {

	public static final String TAG = "ShoppinglistSyncManager";

	private static final String THREAD_NAME = "ShoppinglistSyncManager";
	
	private int mSyncSpeed = 6000;
	
	/** Simple counter keeping track of sync loop */
	private int mSyncCount = 0;
	
	/** Counter for checking if notifications should be pushed.
	 * It's basically a count of how many shopping lists are missing their item sync. */
//	private Integer mSyncWorkCount = 0;
	
	private Stack<Api> mCurrentRequests = new Stack<Api>();
	
	/** Reference to the main Eta object, for Api calls, and Shoppinglistmanager */
	private Eta mEta;
	
	/** The Handler instantiated on the sync Thread */
	private Handler mHandler;

	/** Listening for session changes, starting and stopping sync as needed */
	private SessionListener sessionListener = new SessionListener() {

		public void onUpdate() {
			runSyncLoop(mEta.getUser().isLoggedIn());
		}
	};

	/** The actual sync loop running every x seconds*/
	private Runnable mSyncLoop = new Runnable() {
		
		public void run() {
			
			if (!mEta.getUser().isLoggedIn() || !mEta.isResumed())
				return;
				
			mHandler.postDelayed(mSyncLoop, mSyncSpeed);
			
			// Only do an update, if there are no pending transactions, and we are online
			if (!mCurrentRequests.isEmpty() || !mEta.isOnline()) 
				return;
			
			// If there are local changes to a list, then syncLocalListChanges will handle it: return
			List<Shoppinglist> lists = DbHelper.getInstance().getLists(mEta.getUser().getId(), true);
			boolean changes = syncLocalListChanges(lists);
			if (changes) return;
			
			// If there are changes to any items, then syncLocalItemChanges will handle it: return
			for (Shoppinglist sl : lists) {
				changes = syncLocalItemChanges(sl) || changes;
			}
			if (changes) return;
			
			// Now finally we can query the server for any remote changes
			int user = mEta.getUser().getId();
			
            if (mSyncCount%3 == 0) {
                syncLists(user);
            } else {
                syncListsModified(user);
            }
            mSyncCount++;
            
		}
		
	};
	
	public ListSyncManager(Eta eta) {
		mEta = eta;
		HandlerThread mThread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_BACKGROUND);
		mThread.start();
		mHandler = new Handler(mThread.getLooper());
	}
	
	public boolean hasFirstSync() {
		return mSyncCount > 0;
	}
	
	/**
	 * Method for starting and stopping the sync manager
	 * @param run, true if manager should sync.
	 */
	public void runSyncLoop(boolean run) {
		// First make sure, that we do not leak memory by posting the runnable multiple times
		mHandler.removeCallbacks(mSyncLoop);
		// The optionally add it again
		if (run) mHandler.post(mSyncLoop);
	}
	
	/** Forces the ShoppinglistSyncManager to do a sync of all lists and items */
	public void forceSync() {
		mSyncCount = 0;
		runSyncLoop(true);
	}

	public void onResume() {
		mEta.getSession().subscribe(sessionListener);
		runSyncLoop(true);
	}
	
	public void onPause() {
		runSyncLoop(false);
		mEta.getSession().unSubscribe(sessionListener);
	}
	
	/**
	 * Set the speed, at which the SyncManager should do updates.
	 * NOTE: minimum sync time is 3 seconds (3000ms), to spare both the phone connection and the server
	 * @param time in milliseconds between sync loops
	 */
	public void setSyncSpeed(int time) {
		mSyncSpeed = time < 3000 ? 3000 : time;
	}

	private void addRequest(Api a) {
		synchronized (mCurrentRequests) {
			mCurrentRequests.add(a);
		}
	}

	private void popRequest() {
		synchronized (mCurrentRequests) {
			try {
				mCurrentRequests.pop();
			} catch (Exception e) {
				EtaLog.d(TAG, e);
			}
		}
	}
	
	/**
	 * Sync all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 */
	public void syncLists(final int user) {
		
		ListListener<Shoppinglist> listListener = new ListListener<Shoppinglist>() {
			
			public void onComplete(boolean isCache, int statusCode, List<Shoppinglist> serverList, EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					DbHelper db = DbHelper.getInstance();
					List<Shoppinglist> localList = db.getLists(user);
					mergeShoppinglists(serverList, localList, user);
					pushNotifications();
				} else {
					popRequest();
				}
			}
		};
		addRequest(api().get(Endpoint.getListsByUserId(mEta.getUser().getId()), listListener).execute());
		
	}

	private void mergeShoppinglists(List<Shoppinglist> serverList, List<Shoppinglist> localList, int userId) {
		
		if (serverList.isEmpty() && localList.isEmpty())
			return;
		
		DbHelper db = DbHelper.getInstance();
		
		HashMap<String, Shoppinglist> localset = new HashMap<String, Shoppinglist>();
		HashMap<String, Shoppinglist> serverset = new HashMap<String, Shoppinglist>();
		HashSet<String> union = new HashSet<String>();
		
		for (Shoppinglist o : localList) {
			localset.put(o.getId(), o);
		}

		for (Shoppinglist o : serverList) {
			serverset.put(o.getId(), o);
		}
		
		union.addAll(serverset.keySet());
		union.addAll(localset.keySet());

		List<Shoppinglist> added = new ArrayList<Shoppinglist>();
		List<Shoppinglist> deleted = new ArrayList<Shoppinglist>();
		List<Shoppinglist> edited = new ArrayList<Shoppinglist>();

		for (String key : union) {
			
			if (localset.containsKey(key)) {
				
				if (serverset.containsKey(key)) {
					
					Shoppinglist serverSl = serverset.get(key);
					serverSl.setState(Shoppinglist.State.SYNCED);

					Shoppinglist localSl = localset.get(key);
					
					if (localSl.getModified().before(serverSl.getModified())) {
						edited.add(serverSl);
						db.editList(serverSl, userId);
					}
					
				} else {
					deleted.add(localset.get(key));
					db.deleteList(localset.get(key), userId);
				}
			} else {
				Shoppinglist sl = serverset.get(key);
				sl.setState(Shoppinglist.State.TO_SYNC);
				added.add(sl);
				db.insertList(sl, userId);
			}
			
		}
		
		// If no changes has been registeres, ship the rest
		if (!added.isEmpty() || !deleted.isEmpty() || !edited.isEmpty()) {

			List<ShoppinglistItem> delItems = new ArrayList<ShoppinglistItem>();
			for (Shoppinglist sl : deleted) {
				delItems.addAll(db.getItems(sl, userId));
				db.deleteItems(sl.getId(), null, userId);
			}
			
			addItemNotification(null, delItems, null);

			// Bundle all this so items, lists e.t.c. is done syncing and in DB, before notifying anyone
			addListNotification(added, deleted, edited);
			
			for (Shoppinglist sl : added) {
				syncItems(sl, userId);
			}
			
			for (Shoppinglist sl : edited) {
				syncItems(sl, userId);
			}
			
		}
		
	}
	
	
	/**
	 * Sync all shopping list items, in all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 */
	public void syncListsModified(final int userId) {

		final DbHelper db = DbHelper.getInstance();
		List<Shoppinglist> currentList = db.getLists(userId);
		
		for (final Shoppinglist sl : currentList) {
			
			// If they are in the state of processing, then skip
			if (sl.getState() == Shoppinglist.State.SYNCING || sl.getState() == Shoppinglist.State.DELETE) 
				continue;
			
			// If it obviously needs to sync, then just do it
			if (sl.getState() == Shoppinglist.State.TO_SYNC) {
				// New shopping lists must always sync
				syncItems(sl, userId);
				continue;
			} 
			
			// Run the check 
			sl.setState(Shoppinglist.State.SYNCING);
			db.editList(sl, userId);
			
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, final EtaError error) {
			
					if (Utils.isSuccess(statusCode)) {
						
						sl.setState(Shoppinglist.State.SYNCED);
						try {
							String modified = data.getString(Shoppinglist.S_MODIFIED);
							Date date = Utils.parseDate(modified);
							if (sl.getModified().before(date)) {
								syncItems(sl, userId);
							}
						} catch (JSONException e) {
							EtaLog.d(TAG, e);
						}
						db.editList(sl, userId);
						pushNotifications();
					} else {
						popRequest();
						revertList(sl);
					}
					
				}
			};
			
			addRequest(api().get(Endpoint.getListModifiedById(mEta.getUser().getId(), sl.getId()), cb).execute());
			
		}
				
	}
	
	/**
	 * Sync all shopping list items, associated with the given shopping list.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param sl shoppinglist to update
	 */
	public void syncItems(final Shoppinglist sl, final int userId) {

		final DbHelper db = DbHelper.getInstance();
		
		sl.setState(Shoppinglist.State.SYNCING);
		db.editList(sl, userId);
		
		JsonArrayListener itemListener = new JsonArrayListener() {
			
			public void onComplete(final boolean isCache, final int statusCode, final JSONArray data, final EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					sl.setState(Shoppinglist.State.SYNCED);
					db.editList(sl, userId);
					List<ShoppinglistItem> items = db.getItems(sl, userId);
					mergeShoppinglistItems( ShoppinglistItem.fromJSON(data), items, userId );
					pushNotifications();
				} else {
					popRequest();
					revertList(sl);
				}
				
			}
		};
		
		addRequest(api().get(Endpoint.getItemByListId(mEta.getUser().getId(), sl.getId()), itemListener).execute());
				
	}

	private void mergeShoppinglistItems(List<ShoppinglistItem> newList, List<ShoppinglistItem> oldList, int userId) {
		
		if (newList.isEmpty() && oldList.isEmpty())
			return;

		DbHelper db = DbHelper.getInstance();
		
		HashMap<String, ShoppinglistItem> localSet = new HashMap<String, ShoppinglistItem>();
		HashMap<String, ShoppinglistItem> serverSet = new HashMap<String, ShoppinglistItem>();
		HashSet<String> union = new HashSet<String>();
		
		for (ShoppinglistItem sli : oldList) {
			localSet.put(sli.getId(), sli);
		}

		for (ShoppinglistItem sli : newList) {
			sli.setState(ShoppinglistItem.State.SYNCED);
			serverSet.put(sli.getId(), sli);
		}
		
		union.addAll(serverSet.keySet());
		union.addAll(localSet.keySet());

		List<ShoppinglistItem> added = new ArrayList<ShoppinglistItem>();
		List<ShoppinglistItem> deleted = new ArrayList<ShoppinglistItem>();
		List<ShoppinglistItem> edited = new ArrayList<ShoppinglistItem>();
		
		for (String key : union) {
			
			if (localSet.containsKey(key)) {

				ShoppinglistItem localSli = localSet.get(key);
				
				if (serverSet.containsKey(key)) {

					ShoppinglistItem serverSli = serverSet.get(key);
					
					if (localSli.getModified().before(serverSli.getModified())) {
						edited.add(serverSli);
						db.editItem(serverSli, userId);
					} else if (!localSli.getPreviousId().equals(serverSli.getPreviousId())) {
						
						// This is a special case, thats only relevant as long as the
						// server isn't sending previous_id's
						localSli.setPreviousId(serverSli.getPreviousId());
						db.editItem(localSli, userId);
						
					}
				} else {
					deleted.add(localSet.get(key));
					db.deleteItem(localSet.get(key), userId);
				}
			} else {
				ShoppinglistItem serverSli = serverSet.get(key);
				added.add(serverSli);
				db.insertItem(serverSli, userId);
			}
		}
		
		// If no changes has been registeres, ship the rest
		if (!added.isEmpty() || !deleted.isEmpty() || !edited.isEmpty()) {
			addItemNotification(added, deleted, edited);
		}
		
	}
	
	/**
	 * Method for pushing all local changes to server.
	 * @return true if there was changes, else false
	 */
	private boolean syncLocalListChanges(List<Shoppinglist> lists) {
		
		int count = lists.size();
		
		for (Shoppinglist sl : lists) {

			switch (sl.getState()) {

			case Shoppinglist.State.TO_SYNC:
				putList(sl);
				break;

			case Shoppinglist.State.DELETE:
				delList(sl);
				break;

			case Shoppinglist.State.ERROR:
				revertList(sl);
				break;

			default:
				count--;
				break;
			}
			
		}
		
		return count != 0;
		
	}
	
	/**
	 * Pushes any local changes to the server.
	 * @param sl to get items from
	 * @return true if there was changes, else false
	 */
	private boolean syncLocalItemChanges(Shoppinglist sl) {
		
		DbHelper db = DbHelper.getInstance();
		List<ShoppinglistItem> items = db.getItems(sl, sl.getUserId(), true);
		int count = items.size();
		
		for (ShoppinglistItem sli : items) {

			switch (sli.getState()) {
			case ShoppinglistItem.State.TO_SYNC:
				putItem(sli);
				break;

			case ShoppinglistItem.State.DELETE:
				delItem(sli);
				break;

			case ShoppinglistItem.State.ERROR:
				revertItem(sli);
				break;

			default:
				count--;
				break;
			}

		}
		
		return count != 0;
		
	}

	private void putList(final Shoppinglist sl) {

		final DbHelper db = DbHelper.getInstance();
		
		sl.setState(Shoppinglist.State.SYNCING);
		db.editList(sl, sl.getUserId());
		
		JsonObjectListener editList = new JsonObjectListener() {

			public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
				Shoppinglist s = sl;
				if (Utils.isSuccess(statusCode)) {
					s = Shoppinglist.fromJSON(data);
					Shoppinglist dbList = db.getList(s.getId(), sl.getUserId());
					if (dbList != null && !s.getModified().before(dbList.getModified()) ) {
						s.setState(Shoppinglist.State.SYNCED);
						// If server havent delivered an prev_id, then use old id
						s.setPreviousId(s.getPreviousId() == null ? sl.getPreviousId() : s.getPreviousId());
						db.editList(s, sl.getUserId());
					}
					popRequest();
					syncLocalItemChanges(sl);
				} else {
					popRequest();
					revertList(sl);
				}

			}
		};
		String url = Endpoint.getListById(mEta.getUser().getId(), sl.getId());
		addRequest(api().put(url, editList, sl.getApiParams()).execute());
		
	}

	private void delList(final Shoppinglist sl) {

		final DbHelper db = DbHelper.getInstance();

		JsonObjectListener cb = new JsonObjectListener() {

			public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, final EtaError error) {

				if (Utils.isSuccess(statusCode)) {
					db.deleteList(sl, sl.getUserId());
					db.deleteItems(sl.getId(), null, sl.getUserId());
					popRequest();
				} else {
					popRequest();
					revertList(sl);
				}

			}
		};
		String url = Endpoint.getListById(mEta.getUser().getId(), sl.getId());
		addRequest(api().delete(url, cb, sl.getApiParams()).execute());

	}

	private void revertList(final Shoppinglist sl) {
		
		final DbHelper db = DbHelper.getInstance();
		
		if (sl.getState() != Shoppinglist.State.ERROR) {
			sl.setState(Shoppinglist.State.ERROR);
			db.editList(sl, sl.getUserId());
		}
		
		JsonObjectListener listListener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {

				Shoppinglist s = null;
				if (Utils.isSuccess(statusCode)) {
					s = Shoppinglist.fromJSON(item);
					s.setState(Shoppinglist.State.SYNCED);
					s.setPreviousId(s.getPreviousId() == null ? sl.getPreviousId() : s.getPreviousId());
					db.editList(s, sl.getUserId());
					addListNotification(null, null, idToList(s));
					syncLocalItemChanges(sl);
				} else {
					db.deleteList(sl, sl.getUserId());
					addListNotification(null, idToList(s), null);
				}
				pushNotifications();
			}
		};
		String url = Endpoint.getListById(mEta.getUser().getId(), sl.getId());
		addRequest(api().get(url, listListener).execute());
		
	}

	private void putItem(final ShoppinglistItem sli) {

		final DbHelper db = DbHelper.getInstance();
		
		sli.setState(ShoppinglistItem.State.SYNCING);
		db.editItem(sli, sli.getUserId());
		
		JsonObjectListener cb = new JsonObjectListener() {

			public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, EtaError error) {
				
				ShoppinglistItem s = sli;
				
				if (Utils.isSuccess(statusCode)) {
					
					s = ShoppinglistItem.fromJSON(data);
					ShoppinglistItem local = db.getItem(sli.getId(), sli.getUserId());
					if (local != null && local.getModified().after(s.getModified()) ) {
						s.setState(ShoppinglistItem.State.SYNCED);
						// If server havent delivered an prev_id, then use old id
						s.setPreviousId(s.getPreviousId() == null ? sli.getPreviousId() : s.getPreviousId());
						db.editItem(s, sli.getUserId());
					}
					popRequest();
				} else {
					popRequest();
					revertItem(s);
				}

			}
		};
		String url = Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId());
		addRequest(api().put(url, cb, sli.getApiParams()).execute());

	}

	private void delItem(final ShoppinglistItem sli) {

		final DbHelper db = DbHelper.getInstance();

		JsonObjectListener cb = new JsonObjectListener() {

			public void onComplete(final boolean isCache,final  int statusCode,final  JSONObject item,final  EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					db.deleteItem(sli, sli.getUserId());
					popRequest();
				} else {
					popRequest();
					revertItem(sli);
				}

			}
		};
		String url = Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId());
		addRequest(api().delete(url, cb, sli.getApiParams()).execute());
	}
	
	private void revertItem(final ShoppinglistItem sli) {
		
		final int userId = sli.getUserId();
		final DbHelper db = DbHelper.getInstance();
		
		if (sli.getState() != ShoppinglistItem.State.ERROR) {
			sli.setState(ShoppinglistItem.State.ERROR);
			db.editItem(sli, userId);
		}
		
		JsonObjectListener itemListener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {
				
				ShoppinglistItem s = null;
				if (Utils.isSuccess(statusCode)) {
					s = ShoppinglistItem.fromJSON(item);
					s.setState(ShoppinglistItem.State.SYNCED);
					s.setPreviousId(s.getPreviousId() == null ? sli.getPreviousId() : s.getPreviousId());
					db.editItem(s, userId);
					addItemNotification(null, null, idToList(s));
				} else {
					db.deleteItem(sli, userId);
					addItemNotification(null, idToList(s), null);
				}
				pushNotifications();
			}
		};

		String url = Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId());
		addRequest(api().get(url, itemListener).execute());
		
	}
	
	private Api api() {
		return Eta.getInstance().getApi().setHandler(mHandler);
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

	List<ShoppinglistItem> mItemAdded = new ArrayList<ShoppinglistItem>(0);
	List<ShoppinglistItem> mItemDeleted = new ArrayList<ShoppinglistItem>(0);
	List<ShoppinglistItem> mItemEdited = new ArrayList<ShoppinglistItem>(0);

	List<Shoppinglist> mListAdded = new ArrayList<Shoppinglist>(0);
	List<Shoppinglist> mListDeleted = new ArrayList<Shoppinglist>(0);
	List<Shoppinglist> mListEdited = new ArrayList<Shoppinglist>(0);
	
	private void addItemNotification(List<ShoppinglistItem> added, List<ShoppinglistItem> deleted, List<ShoppinglistItem> edited) {
		mItemAdded.addAll(added == null ? new ArrayList<ShoppinglistItem>(0) : added);
		mItemDeleted.addAll(deleted == null ? new ArrayList<ShoppinglistItem>(0) : deleted);
		mItemEdited.addAll(edited == null ? new ArrayList<ShoppinglistItem>(0) : edited);
	}
	
	private void addListNotification(List<Shoppinglist> added, List<Shoppinglist> deleted, List<Shoppinglist> edited) {
		mListAdded.addAll(added == null ? new ArrayList<Shoppinglist>(0) : added);
		mListDeleted.addAll(deleted == null ? new ArrayList<Shoppinglist>(0) : deleted);
		mListEdited.addAll(edited == null ? new ArrayList<Shoppinglist>(0) : edited);
	}
	
	private void pushNotifications() {
		
		popRequest();
		if (mCurrentRequests.isEmpty()) {
			
			if (!mListAdded.isEmpty() || !mListDeleted.isEmpty() || !mListEdited.isEmpty()) {
				Eta.getInstance().getListManager().notifyListSubscribers(true, mListAdded, mListDeleted, mListEdited);
				mListAdded.clear();
				mListDeleted.clear();
				mListEdited.clear();
			}
			
			if (!mItemAdded.isEmpty() || !mItemDeleted.isEmpty() || !mItemEdited.isEmpty()) {
				Eta.getInstance().getListManager().notifyItemSubscribers(true, mItemAdded, mItemDeleted, mItemEdited);
				mItemAdded.clear();
				mItemDeleted.clear();
				mItemEdited.clear();
			}
			
		}
	}
	
}
