package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.eTilbudsavis.etasdk.SessionManager.OnSessionChangeListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaListObject.State;
import com.eTilbudsavis.etasdk.EtaObjects.EtaListObject;
import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.EtaObjects.Share;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkHelpers.JsonArrayRequest;
import com.eTilbudsavis.etasdk.NetworkHelpers.JsonObjectRequest;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ListSyncManager {
	
	public static final String TAG = "ListSyncManager";
	
	private static final String THREAD_NAME = "ListSyncManager";
	
	private static final boolean USE_LOG_SUMMARY = true;
	
	private int mSyncSpeed = 3000;
	
	/** Simple counter keeping track of sync loop */
	private int mSyncCount = 0;
	
	private Stack<Request<?>> mCurrentRequests = new Stack<Request<?>>();
	
	/** Reference to the main Eta object, for Api calls, and Shoppinglistmanager */
	private Eta mEta;
	
	/** The Handler instantiated on the sync Thread */
	private Handler mHandler;
	
	private User mUser;
	
	private Object mRequestTag = new Object();
	
	/** Listening for session changes, starting and stopping sync as needed */
	private OnSessionChangeListener sessionListener = new OnSessionChangeListener() {

		public void onChange() {
			if (mUser == null || mUser.getUserId() != mEta.getUser().getUserId()) {
				mSyncCount = 0;
				runSyncLoop();
			}
		}
	};
	
	/** The actual sync loop running every x seconds*/
	private Runnable mSyncLoop = new Runnable() {
		
		public void run() {
			
			mUser = mEta.getUser();
			
			if (!mEta.getUser().isLoggedIn() || !mEta.isResumed() )
				return;
			
			User user = mEta.getUser();
			
			mHandler.postDelayed(mSyncLoop, mSyncSpeed);
			
			// Only do an update, if there are no pending transactions, and we are online
			if (!mCurrentRequests.isEmpty() || !mEta.isOnline()) 
				return;
			
			// If there are local changes to a list, then syncLocalListChanges will handle it: return
			List<Shoppinglist> lists = DbHelper.getInstance().getLists(mEta.getUser(), true);

			if (syncLocalListChanges(lists, user))
				return;
			
			// If there are changes to any items, then syncLocalItemChanges will handle it: return
			boolean hasLocalChanges = false;
			for (Shoppinglist sl : lists) {
				hasLocalChanges = syncLocalItemChanges(sl, user) || hasLocalChanges;
				hasLocalChanges = syncLocalShareChanges(sl, user) || hasLocalChanges;
			}
			
			if (hasLocalChanges)
				return;
			
			// Now finally we can query the server for any remote changes
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
		// Create a new thread for a handler, so that i can later post content to that thread.
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
	public void runSyncLoop() {
		// First make sure, that we do not leak memory by posting the runnable multiple times
		mHandler.removeCallbacks(mSyncLoop);
		mHandler.post(mSyncLoop);
	}
	
	public void onResume() {
		mEta.getSessionManager().subscribe(sessionListener);
		runSyncLoop();
	}
	
	public void onPause() {
		mEta.getSessionManager().unSubscribe(sessionListener);
	}
	
	/**
	 * Set the speed, at which the SyncManager should do updates.
	 * NOTE: lower bound on sync time is 3 seconds (3000ms), to spare both the phone and the server
	 * @param time in milliseconds between sync loops
	 */
	public void setSyncSpeed(int time) {
		mSyncSpeed = time < 3000 ? 3000 : time;
	}
	
	private void addRequest(Request<?> r) {
		// No request from here should return a result from cache
		r.setIgnoreCache(true);
		synchronized (mCurrentRequests) {
			mCurrentRequests.add(r);
		}
		
		// Make sure, that requests will return to this thread
		r.setHandler(mHandler);
		
		r.setTag(mRequestTag);
		
		boolean isPullRequest = r.getUrl().contains("modified") || r.getUrl().endsWith("shoppinglists") || r.getUrl().endsWith("items");
		if (!isPullRequest) {
			EtaLog.d(TAG, r.getMethodString() + ": " + r.getUrl());
		}
		
		mEta.add(r);
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
	public void syncLists(final User user) {
		
		Listener<JSONArray> listListener = new Listener<JSONArray>() {
			
			public void onComplete(JSONArray response, EtaError error) {
				
				if (response != null) {
					
					DbHelper db = DbHelper.getInstance();
					List<Shoppinglist> localList = db.getLists(user);
					List<Shoppinglist> serverList = Shoppinglist.fromJSON(response);
					
					// Server usually returns items in the order oldest to newest (not guaranteed)
					// We want them to be reversed
					Collections.reverse(serverList);
					
					prepareServerList(serverList);
					
					mergeListsToDb(serverList, localList, user);
					
					// On first iteration, check and merge lists plus notify subscribers of first sync event
					if (mSyncCount == 1) {
						
						if (serverList.isEmpty() && localList.isEmpty()) {
							migrateOfflineLists();
						}
						
						mEta.getListManager().notifyFirstSync();
						
					}
					
					pushNotifications();
					
				} else {
					popRequest();
				}
			}
		};
		
		JsonArrayRequest listRequest = new JsonArrayRequest(Method.GET, Endpoint.lists(mEta.getUser().getUserId()), listListener);
		listRequest.logSummary(USE_LOG_SUMMARY);
		addRequest(listRequest);
		
	}
	
	private void prepareServerList(List<Shoppinglist> serverList) {

		for (Shoppinglist sl : serverList) {
			/* Set the state of all shares in the serverList to SYNCED, 
			 * before inserting into the DB, as this is actually correct
			 */
			for (Share share : sl.getShares().values()) {
				share.setState(State.SYNCED);
			}
			
		}
		
	}
	
	private void mergeListsToDb(List<Shoppinglist> serverList, List<Shoppinglist> localList, User user) {
		
		if (serverList.isEmpty() && localList.isEmpty()) {
			return;
		}
		
		DbHelper db = DbHelper.getInstance();
		
		HashMap<String, Shoppinglist> localMap = new HashMap<String, Shoppinglist>();
		HashMap<String, Shoppinglist> serverMap = new HashMap<String, Shoppinglist>();
		HashSet<String> union = new HashSet<String>();
		
		for (Shoppinglist sl : localList) {
			localMap.put(sl.getId(), sl);
		}
		
		for (Shoppinglist sl : serverList) {
			serverMap.put(sl.getId(), sl);
		}
		
		union.addAll(serverMap.keySet());
		union.addAll(localMap.keySet());
		
		for (String key : union) {
			
			if (localMap.containsKey(key)) {

				Shoppinglist localSl = localMap.get(key);
				
				if (serverMap.containsKey(key)) {
					
					Shoppinglist serverSl = serverMap.get(key);
					
					if (localSl.getModified().before(serverSl.getModified())) {
						serverSl.setState(State.SYNCED);
						mListEdited.put(serverSl.getId(), serverSl);
						db.editList(serverSl, user);
						db.cleanShares(serverSl, user);
					} else {
						// Don't do anything, next iteration will put local changes to API
					}
					
				} else {
					
					mListDeleted.put(localSl.getId(), localSl);
					for (ShoppinglistItem sli : db.getItems(localSl, user)) {
						mItemDeleted.put(sli.getId(), sli);
					}
					db.deleteItems(localSl.getId(), null, user);
					db.deleteList(localSl, user);
				}
				
			} else {
				
				Shoppinglist add = serverMap.get(key);
				add.setState(State.TO_SYNC);
				mListAdded.put(add.getId(), add);
				db.insertList(add, user);
				
			}
			
		}
		
		for (Shoppinglist sl : mListAdded.values()) {
			syncItems(sl, user);
		}
			
		for (Shoppinglist sl : mListEdited.values()) {
			syncItems(sl, user);
		}
		
	}
	
	/**
	 * 
	 * @param serverList
	 * @param localList
	 */
	private void migrateOfflineLists() {
		
		DbHelper db = DbHelper.getInstance();
		
		User offlineUser = new User();
		List<Shoppinglist> offlineUserLists = db.getLists(offlineUser);
		
		if (offlineUserLists.isEmpty()) {
			return;
		}
		
		for (Shoppinglist sl : offlineUserLists) {
			
			List<ShoppinglistItem> noUserItems = db.getItems(sl, offlineUser);
			if (noUserItems.isEmpty()) {
				continue;
			}
			
			Shoppinglist tmpSl = Shoppinglist.fromName(sl.getName());
			tmpSl.setType(sl.getType());
			
			mEta.getListManager().addList(tmpSl);
			
			for (ShoppinglistItem sli : noUserItems) {
				sli.setShoppinglistId(tmpSl.getId());
				sli.setId(Utils.createUUID());
				mEta.getListManager().addItem(sli);
			}
		}
			
	}
	
	/**
	 * Sync all shopping list items, in all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 */
	public void syncListsModified(final User user) {

		final DbHelper db = DbHelper.getInstance();
		List<Shoppinglist> localLists = db.getLists(user);
		
		for (final Shoppinglist sl : localLists) {
			
			// If they are in the state of processing, then skip
			if (sl.getState() == State.SYNCING) 
				continue;
			
			// If it obviously needs to sync, then just do it
			if (sl.getState() == State.TO_SYNC) {
				// New shopping lists must always sync
				syncItems(sl, user);
				continue;
			} 
			
			// Run the check 
			sl.setState(State.SYNCING);
			db.editList(sl, user);
			
			Listener<JSONObject> modifiedListener = new Listener<JSONObject>() {

				public void onComplete( JSONObject response, EtaError error) {
					

					if (response != null) {
						
						sl.setState(State.SYNCED);
						try {
							String modified = response.getString(EtaObject.ServerKey.MODIFIED);
							// If local list has been modified before the server list, then sync items
							if (sl.getModified().before(Utils.parseDate(modified))) {
								// If there are changes, update items (this will update list-state in DB)
								syncItems(sl, user);
							} else {
								// if no changes, just write new state to DB
								db.editList(sl, user);
							}
						} catch (JSONException e) {
							EtaLog.d(TAG, e);
							// error? just write new state to DB, next iteration will fix it
							db.editList(sl, user);
						}
						pushNotifications();
						
					} else {
						
						popRequest();
						revertList(sl, user);
						
					}
					
					
				}
			};
			
			JsonObjectRequest modifiedRequest = new JsonObjectRequest(Endpoint.listModified(mEta.getUser().getUserId(), sl.getId()), modifiedListener);
			modifiedRequest.logSummary(USE_LOG_SUMMARY);
			addRequest(modifiedRequest);
			
		}
				
	}
	
	/**
	 * Sync all shopping list items, associated with the given shopping list.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param sl shoppinglist to update
	 */
	public void syncItems(final Shoppinglist sl, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		sl.setState(State.SYNCING);
		db.editList(sl, user);
		
		Listener<JSONArray> itemListener = new Listener<JSONArray>() {

			public void onComplete( JSONArray response, EtaError error) {

				if (response != null) {
					
					sl.setState(State.SYNCED);
					db.editList(sl, user);
					
					List<ShoppinglistItem> localItems = db.getItems(sl, user);
					List<ShoppinglistItem> serverItems = ShoppinglistItem.fromJSON(response);
					
					// So far, we get items in reverse order, well just keep reversing it for now.
					Collections.reverse(serverItems);

					for (ShoppinglistItem sli : serverItems) {
						sli.setState(State.SYNCED);
					}
					
					mergeItemsToDb(serverItems, localItems, user);
					
					/* fetch updated items from DB, as the state might be a bit
					 * whack after the merging of items */
					localItems = db.getItems(sl, user);
					Utils.sortItems(localItems);
					
					/* Update previous_id's, modified and state if needed */
					String tmp = EtaListObject.FIRST_ITEM;
					for (ShoppinglistItem sli : localItems) {
						
						if (!tmp.equals(sli.getPreviousId())) {
							sli.setPreviousId(tmp);
							sli.setModified(new Date());
							sli.setState(State.TO_SYNC);
							
							/* If it's a new item, it's already in the added list,
							 * then we'll override it else add it to the edited
							 * as a new item to the edited list */
							if (mItemAdded.containsKey(sli.getId())) {
								mItemAdded.put(sli.getId(), sli);
							} else {
								mItemEdited.put(sli.getId(), sli);
							}
							
							db.editItem(sli, user);
						}
						tmp = sli.getId();
					}
					
					pushNotifications();
					
				} else {
					popRequest();
					revertList(sl, user);
				}
				
			}
		};
		
		JsonArrayRequest itemRequest = new JsonArrayRequest(Method.GET, Endpoint.listitems(mEta.getUser().getUserId(), sl.getId()), itemListener);
		itemRequest.logSummary(USE_LOG_SUMMARY);
		addRequest(itemRequest);
		
	}
	
	private void mergeItemsToDb(List<ShoppinglistItem> serverItems, List<ShoppinglistItem> localItems, User user) {
		
		if (serverItems.isEmpty() && localItems.isEmpty()) {
			return;
		}
		
		DbHelper db = DbHelper.getInstance();
		
		HashMap<String, ShoppinglistItem> localMap = new HashMap<String, ShoppinglistItem>();
		HashMap<String, ShoppinglistItem> serverMap = new HashMap<String, ShoppinglistItem>();
		HashSet<String> union = new HashSet<String>();
		
		for (ShoppinglistItem sli : localItems) {
			localMap.put(sli.getId(), sli);
		}

		for (ShoppinglistItem sli : serverItems) {
			serverMap.put(sli.getId(), sli);
		}
		
		union.addAll(serverMap.keySet());
		union.addAll(localMap.keySet());
		
		for (String key : union) {
			
			if (localMap.containsKey(key)) {

				ShoppinglistItem localSli = localMap.get(key);
				
				if (serverMap.containsKey(key)) {
					
					ShoppinglistItem serverSli = serverMap.get(key);
					
					if (localSli.getModified().before(serverSli.getModified())) {
						mItemEdited.put(serverSli.getId(), serverSli);
						db.editItem(serverSli, user);
						
					}
					
				} else {
					ShoppinglistItem delSli = localMap.get(key);
					mItemDeleted.put(delSli.getId(), delSli);
					db.deleteItem(delSli, user);
				}
				
			} else {
				ShoppinglistItem serverSli = serverMap.get(key);
				mItemAdded.put(serverSli.getId(), serverSli);
				db.insertItem(serverSli, user);
			}
		}
		
	}
	
	/**
	 * Method for pushing all local changes to server.
	 * @return true if there was changes, else false
	 */
	private boolean syncLocalListChanges(List<Shoppinglist> lists, User user) {
		
		int count = lists.size();
		
		for (Shoppinglist sl : lists) {

			switch (sl.getState()) {

			case State.TO_SYNC:
				putList(sl, user);
				break;

			case State.DELETE:
				delList(sl, user);
				break;

			case State.ERROR:
				revertList(sl, user);
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
	private boolean syncLocalItemChanges(Shoppinglist sl, User user) {
		
		DbHelper db = DbHelper.getInstance();
		List<ShoppinglistItem> items = db.getItems(sl, user, true);
		int count = items.size();
		
		for (ShoppinglistItem sli : items) {

			switch (sli.getState()) {
			case State.TO_SYNC:
				putItem(sli, user);
				break;

			case State.DELETE:
				delItem(sli, user);
				break;

			case State.ERROR:
				revertItem(sli, user);
				break;

			default:
				count--;
				break;
			}

		}
		
		return count != 0;
		
	}
	
	private void putList(final Shoppinglist sl, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		sl.setState(State.SYNCING);
		db.editList(sl, user);
		
		Listener<JSONObject> listListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {
				
				Shoppinglist s = sl;
				if (response != null) {
					
					s = Shoppinglist.fromJSON(response);
					Shoppinglist dbList = db.getList(s.getId(), user);
					if (dbList != null && !s.getModified().before(dbList.getModified()) ) {
						s.setState(State.SYNCED);
						// If server haven't delivered an prev_id, then use old id
						s.setPreviousId(s.getPreviousId() == null ? sl.getPreviousId() : s.getPreviousId());
						db.editList(s, user);
					}
					popRequest();
					syncLocalItemChanges(sl, user);
					
				} else {
					popRequest();
					
					if (error.getCode() == -1) {
						// TODO: Need better error code definitions, are they going to be types?
					} else {
						revertList(sl, user);
					}
					
				}

				
			}
		};

		String url = Endpoint.list(user.getUserId(), sl.getId());
		JsonObjectRequest listReq = new JsonObjectRequest(Method.PUT, url, sl.toJSON(), listListener);
		
		addRequest(listReq);
		
	}

	private void delList(final Shoppinglist sl, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		Listener<JSONObject> listListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {

				if (response != null) {
					db.deleteList(sl, user);
					db.deleteShares(sl, user);
					db.deleteItems(sl.getId(), null, user);
					popRequest();
				} else {
					popRequest();
					if (error.getCode() != 1501) {
						db.deleteList(sl, user);
					} else if (error.getCode() == -1) {
						// TODO: Need better error code definitions, are they going to be types?
					} else {
						revertList(sl, user);
					}
				}

			}
		};
		
		String url = Endpoint.list(user.getUserId(), sl.getId());
		
		JsonObjectRequest listReq = new JsonObjectRequest(Method.DELETE, url, null, listListener);
		listReq.putQueryParam(Param.MODIFIED, Utils.parseDate(sl.getModified()));
		addRequest(listReq);
		
	}

	private void revertList(final Shoppinglist sl, final User user) {
		
		final DbHelper db = DbHelper.getInstance();
		
		if (sl.getState() != State.ERROR) {
			sl.setState(State.ERROR);
			db.editList(sl, user);
		}
		
		Listener<JSONObject> listListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {

				Shoppinglist s = null;
				if (response != null) {
					s = Shoppinglist.fromJSON(response);
					s.setState(State.SYNCED);
					s.setPreviousId(s.getPreviousId() == null ? sl.getPreviousId() : s.getPreviousId());
					db.editList(s, user);
					mListAdded.put(s.getId(), s);
					syncLocalItemChanges(sl, user);
				} else {
					db.deleteList(sl, user);
					mListAdded.put(s.getId(), s);
				}
				pushNotifications();
			}
		};
		
		String url = Endpoint.list(user.getUserId(), sl.getId());
		JsonObjectRequest listReq = new JsonObjectRequest(url, listListener);

		addRequest(listReq);
		
	}

	private void putItem(final ShoppinglistItem sli, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		sli.setState(State.SYNCING);
		db.editItem(sli, user);
		
		Listener<JSONObject> itemListener = new Listener<JSONObject>() {
			
			public void onComplete(JSONObject response, EtaError error) {
				
				if (response != null) {
					
					ShoppinglistItem server = ShoppinglistItem.fromJSON(response);
					ShoppinglistItem local = db.getItem(sli.getId(), user);
					if (local != null && local.getModified().after(server.getModified()) ) {
						server.setState(State.SYNCED);
						// If server havent delivered an prev_id, then use old id
						if (server.getPreviousId() == null) {
							server.setPreviousId(sli.getPreviousId());
						}
						db.editItem(server, user);
					}
					popRequest();
					
				} else {
					popRequest();
					if (error.getCode() != -1) {
						revertItem(sli, user);
					}
				}

			}
		};
		
		String url = Endpoint.listitem(user.getUserId(), sli.getShoppinglistId(), sli.getId());
		JsonObjectRequest itemReq = new JsonObjectRequest(Method.PUT, url, sli.toJSON(), itemListener);
		addRequest(itemReq);
		
	}

	private void delItem(final ShoppinglistItem sli, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		Listener<JSONObject> itemListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {

				if (response != null) {
					db.deleteItem(sli, user);
					popRequest();
				} else {
					popRequest();
					
					if(error.getCode() == 1501) {
						db.deleteItem(sli, user);
					} else if (error.getCode() == -1) {
						// Nothing
					} else {
						revertItem(sli, user);
					}
					
				}
				
			}
		};
		
		String url = Endpoint.listitem(user.getUserId(), sli.getShoppinglistId(), sli.getId());
		JsonObjectRequest itemReq = new JsonObjectRequest(Method.DELETE, url, null, itemListener);
		itemReq.putQueryParam(Param.MODIFIED, Utils.parseDate(sli.getModified()));
		addRequest(itemReq);
		
	}
	
	private void revertItem(final ShoppinglistItem sli, final User user) {
		
		final DbHelper db = DbHelper.getInstance();
		
		if (sli.getState() != State.ERROR) {
			sli.setState(State.ERROR);
			db.editItem(sli, user);
		}
		
		Listener<JSONObject> itemListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {

				ShoppinglistItem s = null;
				if (response != null) {
					s = ShoppinglistItem.fromJSON(response);
					s.setState(State.SYNCED);
					s.setPreviousId(s.getPreviousId() == null ? sli.getPreviousId() : s.getPreviousId());
					db.editItem(s, user);
					mItemEdited.put(s.getId(), s);
				} else {
					db.deleteItem(sli, user);
					mItemDeleted.put(s.getId(), s);
				}
				pushNotifications();
			}
		};
		
		String url = Endpoint.listitem(user.getUserId(), sli.getShoppinglistId(), sli.getId());
		JsonObjectRequest itemReq = new JsonObjectRequest(url, itemListener);

		addRequest(itemReq);
		
	}

	private boolean syncLocalShareChanges(Shoppinglist sl, User user) {
		
		DbHelper db = DbHelper.getInstance();
		List<Share> shares = db.getShares(sl, user, true);
		
		int count = shares.size();
		
		for (Share s : shares) {
			
			switch (s.getState()) {
			case State.TO_SYNC:
				putShare(sl, s, user);
				break;

			case State.DELETE:
				delShare(s, user);
				break;
				
			case State.ERROR:
				revertShare(s, user);
				break;
				
			default:
				count--;
				break;
			}
			
		}
		
		return count != 0;
		
	}

	private void putShare(final Shoppinglist sl, final Share s, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		s.setState(State.SYNCING);
		db.editShare(s, user);
		
		Listener<JSONObject> shareListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {

				if (response != null) {
					Share tmp = Share.fromJSON(response);
					tmp.setState(State.SYNCED);
					tmp.setShoppinglistId(s.getShoppinglistId());
					popRequest();
				} else {
					popRequest();
					if (error.getFailedOnField() != null) {
						// If the request failed on a field, then we cannot really do anything, but delete
						db.deleteShare(s, user);
						sl.removeShare(s);
						Eta.getInstance().getListManager().notifyListSubscribers(true, null, null, idToList(sl));
					} else {
						revertShare(s, user);
					}
				}

			}
		};
		
		String url = Endpoint.listShareEmail(user.getUserId(), s.getShoppinglistId(), s.getEmail());
		JsonObjectRequest shareReq = new JsonObjectRequest(Method.PUT, url, s.toJSON(), shareListener);
		addRequest(shareReq);
		
	}

	private void delShare(final Share s, final User user) {

		final DbHelper db = DbHelper.getInstance();

		Listener<JSONObject> shareListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {

				if (response != null) {
					
					if (user.getEmail().equals(s.getEmail())) {
						// If the share.email == user.email, then we want to remove list, items and shares
						// As the user no longer has access (have removed him self from shares)
						db.deleteList(s.getShoppinglistId(), user);
						db.deleteItems(s.getShoppinglistId(), null, user);
						db.deleteShares(s.getShoppinglistId(), user);
					} else {
						// Else just remove the share in question
						db.deleteShare(s, user);
					}
					popRequest();
				} else {
					popRequest();
					if (error.getCode() == -1) {
						// Nothing
					} else if (error.getCode() == 1501) {
						db.deleteShare(s, user);
					} else {
						revertShare(s, user);
					}
				}
				
			}
		};
		
		String url = Endpoint.listShareEmail(user.getUserId(), s.getShoppinglistId(), s.getEmail());
		JsonObjectRequest shareReq = new JsonObjectRequest(Method.DELETE, url, null, shareListener);
		addRequest(shareReq);
		
	}
	
	private void revertShare(final Share s, final User user) {
		
		final DbHelper db = DbHelper.getInstance();
		
		if (s.getState() != State.ERROR) {
			s.setState(State.ERROR);
			db.editShare(s, user);
		}
		
		Listener<JSONObject> shareListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {

				Share tmp = null;
				if (response != null) {
					tmp = Share.fromJSON(response);
					tmp.setState(State.SYNCED);
					tmp.setShoppinglistId(s.getShoppinglistId());
					db.editShare(tmp, user);
				} else {
					db.deleteShare(s, user);
				}
				popRequest();
			}
		};
		
		String url = Endpoint.listShareEmail(user.getUserId(), s.getShoppinglistId(), s.getEmail());
		JsonObjectRequest shareReq = new JsonObjectRequest(url, shareListener);
		addRequest(shareReq);
		
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
	
	Map<String, ShoppinglistItem> mItemAdded = Collections.synchronizedMap(new HashMap<String, ShoppinglistItem>());
	Map<String, ShoppinglistItem> mItemDeleted = Collections.synchronizedMap(new HashMap<String, ShoppinglistItem>());
	Map<String, ShoppinglistItem> mItemEdited = Collections.synchronizedMap(new HashMap<String, ShoppinglistItem>());
	
	Map<String, Shoppinglist> mListAdded = Collections.synchronizedMap(new HashMap<String, Shoppinglist>());
	Map<String, Shoppinglist> mListDeleted = Collections.synchronizedMap(new HashMap<String, Shoppinglist>());
	Map<String, Shoppinglist> mListEdited = Collections.synchronizedMap(new HashMap<String, Shoppinglist>());
	
	private void pushNotifications() {
		
		popRequest();
		if (!mCurrentRequests.isEmpty()) {
			return;
		}
			
		ListManager lm = Eta.getInstance().getListManager();
		boolean listsEmpty = mListAdded.isEmpty() && mListDeleted.isEmpty() && mListEdited.isEmpty();
		if (!listsEmpty) {
			lm.notifyListSubscribers(
					true, 
					new ArrayList<Shoppinglist>(mListAdded.values()), 
					new ArrayList<Shoppinglist>(mListDeleted.values()),
					new ArrayList<Shoppinglist>(mListEdited.values()));
			mListAdded.clear();
			mListDeleted.clear();
			mListEdited.clear();
		}
		
		boolean itemssEmpty = mItemAdded.isEmpty() && mItemDeleted.isEmpty() && mItemEdited.isEmpty();
		if (!itemssEmpty) {
			lm.notifyItemSubscribers(true, 
					new ArrayList<ShoppinglistItem>(mItemAdded.values()), 
					new ArrayList<ShoppinglistItem>(mItemDeleted.values()),
					new ArrayList<ShoppinglistItem>(mItemEdited.values()));
			mItemAdded.clear();
			mItemDeleted.clear();
			mItemEdited.clear();
		}
		
	}
	
}