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

import java.util.Collections;
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

import com.eTilbudsavis.etasdk.SessionManager.OnSessionChangeListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaListObject;
import com.eTilbudsavis.etasdk.EtaObjects.EtaListObject.State;
import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.EtaObjects.Share;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError.Code;
import com.eTilbudsavis.etasdk.NetworkHelpers.JsonArrayRequest;
import com.eTilbudsavis.etasdk.NetworkHelpers.JsonObjectRequest;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.NetworkInterface.RequestQueue;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.Utils.Utils;

/**
 * The {@link SyncManager} class performs asynchronous synchronization with the
 * eTilbudsavis API, to propagate all {@link Shoppinglist} and {@link ShoppinglistItem}
 * changes that a user may have done in the {@link DbHelper database}.
 * 
 * <p>
 * Notifications about {@link Shoppinglist} and {@link ShoppinglistItem}
 * changes, are relayed through the subscriber system in the {@link ListManager}.
 * </p>
 * 
 * <p>
 * There are four types of synchronization that will be performed:
 * 
 * <ul>
 * <li>
 * Local changes - Sending local changes to the API has precedence over the other
 * types of synchronization.
 * </li>
 * 
 * <li>
 * {@link Shoppinglist#getModified()} - This is the default action, this will be
 * performed if no other type of synchronization is chosen. This in practice
 * means that this will be performed on almost every iteration. If there is any
 * changes in the modified, both the {@link Shoppinglist} and it's
 * {@link ShoppinglistItem ShoppinglistItems} will be synchronized.
 * </li>
 * 
 * <li>
 * {@link Shoppinglist} - Shoppinglists will be performed on every 3rd
 * iteration the {@link SyncManager} does. As there are less chance of adding,
 * and removing {@link Shoppinglist Shoppinglists} than that of having a change
 * to an existing {@link Shoppinglist} (which will be handled by the synchronization
 * option above.
 * </li>
 * 
 * <li>
 * Full sync loop - The full sync loop is performed on every 10th iteration, and
 * ensures that the state is completely up to date. The local state can become
 * decoupled from the server state in certain situations, where we are not able
 * to determine and fix the situation in any other way, e.g.: If two devices
 * makes a change to a list at the exact same second, and both devices therefore
 * has a valid/correct {@link Shoppinglist#getModified() modified}.
 * </li>
 * </ul>
 * 
 * </p>
 * 
 * <p>
 * When {@link #onPause()} is triggered, all local pending changes are pushed to
 * the API if possible to ensure a correct state on the server (and other devices).
 * </p>
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class SyncManager {
	
	public static final String TAG = "SyncManager";
	
	private static final boolean USE_LOG_SUMMARY = false;

	/** Supported sync speeds for {@link SyncManager} */
	public interface SyncSpeed {
		int SLOW = 10000;
		int MEDIUM = 6000;
		int FAST = 3000;
	}
	
	/** The current sync speed */
	private int mSyncSpeed = 3000;
	
	/** Simple counter keeping track of sync loop */
	private int mSyncCount = 0;
	
	private Stack<Request<?>> mCurrentRequests = new Stack<Request<?>>();
	
	/** Reference to the {@link Eta} object */
	private Eta mEta;
	
	/** The Handler instantiated on the sync Thread */
	private Handler mHandler;
	
	/** A user object, for identifying the current user during a request cycle */
	private User mUser;
	
	/** 
	 * A tag for identifying all requests originating from this {@link SyncManager}
	 * in the {@link RequestQueue}
	 */
	private Object mRequestTag = new Object();
	
	/** The notification object, used to combine and collect notifications */
	private ListNotification mNotification = new ListNotification(true);
	
	/** Listening for session changes, starting and stopping sync as needed */
	private OnSessionChangeListener sessionListener = new OnSessionChangeListener() {

		public void onChange() {
			if (mUser == null || mUser.getUserId() != mEta.getUser().getUserId()) {
				mSyncCount = 0;
				forceSync();
			}
		}
	};
	
	/** The actual sync loop running every x seconds*/
	private Runnable mSyncLoop = new Runnable() {
		
		public void run() {
			
			mUser = mEta.getUser();
			
			// If it's an offline user, then just quit it
			if ( !mUser.isLoggedIn() ) {
				return;
			}
			
			/* 
			 * Prepare for next iteration if app is still resumed.
			 * By not doing a return statement we allow for a final sync, and
			 * sending local changes to server
			 */
			if ( mEta.isResumed() ) {
				mHandler.postDelayed(mSyncLoop, mSyncSpeed);
			}
			
			// Only do an update, if there are no pending transactions, and we are online
			if (!mCurrentRequests.isEmpty() || !mEta.isOnline()) {
				return;
			}
			
			// Perform the actual sync cycle
			performSyncCycle(mUser);
			
		}
		
	};
	
	private void performSyncCycle(User user) {
		
		// If there are local changes to a list, then syncLocalListChanges will handle it: return
		List<Shoppinglist> lists = DbHelper.getInstance().getLists(mEta.getUser(), true);
		if (syncLocalListChanges(lists, user)) {
			return;
		}
		
		// If there are changes to any items, then syncLocalItemChanges will handle it: return
		boolean hasLocalChanges = false;
		for (Shoppinglist sl : lists) {
			hasLocalChanges = syncLocalItemChanges(sl, user) || hasLocalChanges;
			hasLocalChanges = syncLocalShareChanges(sl, user) || hasLocalChanges;
		}
		
		// Skip further sync if we just posted our own changes
		if (hasLocalChanges) {
			return;
		}
		
		// Finally ready to get server changes
        if (mSyncCount%3 == 0) {
        	
        	// Get a new set of lists
            syncLists(user);
            
        } else if (mSyncCount%10 == 0) {
        	
        	/* Because we update modified on lists, on all changes, we might
        	 * have a situation where two devices have set the same modified
        	 * on a list, and therefore won't try to get a new list of items
        	 * So we force the SDK to get a new set once in a while */
    		List<Shoppinglist> localLists = DbHelper.getInstance().getLists(user);
    		for (Shoppinglist sl : localLists) {
    			syncItems(sl, user);
    		}
    		
        } else {
        	
        	// Base case, just check if there is changes
            syncListsModified(user);
            
        }
        mSyncCount++;
        
	}
	
	/**
	 * Default constructor for the {@link SyncManager}
	 * @param eta An Eta instance
	 */
	public SyncManager(Eta eta) {
		mEta = eta;
		// Create a new thread for a handler, so that i can later post content to that thread.
		HandlerThread t = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
		t.start();
		mHandler = new Handler(t.getLooper());
	}

	/**
	 * Method for determining if the first sync cycle is done.
	 * 
	 * <p>This is dependent on, both:
	 * <ul>
	 * 		<li>the {@link SyncManager} having performed the first sync cycle, and </li>
	 * 		<li>whether a {@link User} is {@link User#isLoggedIn() logged in}</li>
	 * </ul>
	 * </p>
	 * @return True if the first sync is complete, or there is no user to sync.
	 */
	public boolean hasFirstSync() {
		return mSyncCount > 0;
	}
	
	/**
	 * Method for forcing a new synchronization iteration.
	 * <p>This method will trigger the synchronization loop, so it's essentially
	 * also the method for starting the {@link SyncManager} sync loop</p>
	 * 
	 * <p>This method should only be used in special cases, as the
	 * synchronization is (within reasonably time intervals) being handled
	 * automatically by the {@link SyncManager}</p>
	 */
	public void forceSync() {
		// First make sure, that we do not leak memory by posting the runnable multiple times
		mHandler.removeCallbacks(mSyncLoop);
		mHandler.post(mSyncLoop);
	}

	/**
	 * Method to call on all onResume events.
	 * <p>This is implicitly handled by the {@link Eta} instance</p>
	 */
	public void onResume() {
		mEta.getSessionManager().subscribe(sessionListener);
		forceSync();
	}

	/**
	 * Method to call on all onPause events.
	 * <p>This is implicitly handled by the {@link Eta} instance</p>
	 */
	public void onPause() {
		forceSync();
		mEta.getSessionManager().unSubscribe(sessionListener);
	}
	
	/**
	 * Set synchronization interval for {@link Shoppinglist}, and {@link ShoppinglistItem}.
	 * 
	 * <p>Also time must be greater than or equal to 3000 (milliseconds)</p>
	 * @param time A synchronization interval in milliseconds
	 */
	public void setSyncSpeed(int time) {
		if (time == SyncSpeed.SLOW || time == SyncSpeed.MEDIUM || time == SyncSpeed.FAST ) {
			mSyncSpeed = time;
		}
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
		
		mEta.add(r);
		
//		boolean isPullRequest = r.getUrl().contains("modified") || r.getUrl().endsWith("shoppinglists") || r.getUrl().endsWith("items");
//		if (!isPullRequest) {
//			EtaLog.d(TAG, r.toString());
//		}
		
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
	
	private void syncLists(final User user) {
		
		Listener<JSONArray> listListener = new Listener<JSONArray>() {
			
			public void onComplete(JSONArray response, EtaError error) {
				
				if (response != null) {
					
					DbHelper db = DbHelper.getInstance();
					// Get ALL lists including deleted, to avoid adding them again
					List<Shoppinglist> localLists = db.getLists(user, true);
					List<Shoppinglist> serverLists = Shoppinglist.fromJSON(response);
					
					// Server usually returns items in the order oldest to newest (not guaranteed)
					// We want them to be reversed
					Collections.reverse(serverLists);
					
					prepareServerList(serverLists);
					
					mergeListsToDb(serverLists, localLists, user);
					
					// On first iteration, check and merge lists plus notify subscribers of first sync event
					if (mSyncCount == 1) {
						
						if (serverLists.isEmpty() && localLists.isEmpty()) {
							migrateOfflineLists();
						}
						
						mNotification.setFirstSync(true);
						
					}
					
					popRequestAndPushNotifications();
					
				} else {
					popRequest();
				}
			}
		};
		
		JsonArrayRequest listRequest = new JsonArrayRequest(Method.GET, Endpoint.lists(mEta.getUser().getUserId()), listListener);
		// Offset and limit are set to default values, we want to ignore this.
		listRequest.getQueryParameters().remove(Param.OFFSET);
		listRequest.getQueryParameters().remove(Param.LIMIT);
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
						mNotification.edit(serverSl);
						db.editList(serverSl, user);
						db.cleanShares(serverSl, user);
					} else {
						// Don't do anything, next iteration will put local changes to API
					}
					
				} else {
					mNotification.del(localSl);
					for (ShoppinglistItem sli : db.getItems(localSl, user)) {
						mNotification.del(sli);
					}
					db.deleteItems(localSl.getId(), null, user);
					db.deleteList(localSl, user);
				}
				
			} else {
				
				Shoppinglist add = serverMap.get(key);
				add.setState(State.TO_SYNC);
				mNotification.add(add);
				db.insertList(add, user);
				
			}
			
		}
		
		for (Shoppinglist sl : mNotification.getAddedLists()) {
			syncItems(sl, user);
		}
			
		for (Shoppinglist sl : mNotification.getEditedLists()) {
			syncItems(sl, user);
		}
		
	}
	
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
	
	
	private void syncListsModified(final User user) {

		final DbHelper db = DbHelper.getInstance();
		List<Shoppinglist> localLists = db.getLists(user);
		
		for (final Shoppinglist sl : localLists) {
			
			// If they are in the state of processing, then skip
			if (sl.getState() == State.SYNCING) {
				continue;
			}
			
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
						popRequestAndPushNotifications();
						
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
	
	
	private void syncItems(final Shoppinglist sl, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		sl.setState(State.SYNCING);
		db.editList(sl, user);
		
		Listener<JSONArray> itemListener = new Listener<JSONArray>() {

			public void onComplete( JSONArray response, EtaError error) {
				
				if (response != null) {
					
					sl.setState(State.SYNCED);
					db.editList(sl, user);
					
					// Get ALL items including deleted, to avoid adding them again
					List<ShoppinglistItem> localItems = db.getItems(sl, user, true);
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
							if (mNotification.mItemAdded.containsKey(sli.getId())) {
								mNotification.add(sli);
							} else {
								mNotification.edit(sli);
							}
							
							db.editItem(sli, user);
						}
						tmp = sli.getId();
					}
					
					popRequestAndPushNotifications();
					
				} else {
					popRequest();
					revertList(sl, user);
				}
				
			}
		};
		
		JsonArrayRequest itemRequest = new JsonArrayRequest(Method.GET, Endpoint.listitems(mEta.getUser().getUserId(), sl.getId()), itemListener);
		// Offset and limit are set to default values, we want to ignore this.
		itemRequest.getQueryParameters().remove(Param.OFFSET);
		itemRequest.getQueryParameters().remove(Param.LIMIT);
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
						mNotification.edit(serverSli);
						db.editItem(serverSli, user);
						
					} else if (!localSli.getMeta().toString().equals(serverSli.getMeta().toString())) {
						// Migration code, to get comments into the DB
						mNotification.edit(serverSli);
						db.editItem(serverSli, user);
					}
					
				} else {
					ShoppinglistItem delSli = localMap.get(key);
					if (delSli.getState() == State.TO_SYNC) {
						/* 
						 * Item have been added while request was in flight
						 * ignore it for now
						 */
					} else {
						/* Else delete the item */
						mNotification.del(delSli);
						db.deleteItem(delSli, user);
					}
				}
				
			} else {
				ShoppinglistItem serverSli = serverMap.get(key);
				mNotification.add(serverSli);
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
				
				if (response != null) {

					/* 
					 * If local isn't equal to server version, take server version.
					 * Don't push changes yet, we want to check item state too.
					 */
					
					Shoppinglist serverSl = Shoppinglist.fromJSON(response);
					Shoppinglist localSl = db.getList(serverSl.getId(), user);
					if (localSl != null && !serverSl.getModified().equals(localSl.getModified()) ) {
						serverSl.setState(State.SYNCED);
						// If server haven't delivered an prev_id, then use old id
						serverSl.setPreviousId(serverSl.getPreviousId() == null ? sl.getPreviousId() : serverSl.getPreviousId());
						db.editList(serverSl, user);
						mNotification.edit(serverSl);
					}
					popRequest();
					syncLocalItemChanges(sl, user);
					
				} else {
					
					popRequest();
					if (error.getCode() == Code.NETWORK_ERROR) {
						/* Ignore missing network, wait for next iteration */
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
					
					switch (error.getCode()) {
					
						case Code.INVALID_RESOURCE_ID:
							/* Resource already gone (or have never been synchronized)
							 * delete local version and ignore */
							db.deleteList(sl, user);
							break;
							
						case Code.NETWORK_ERROR:
							/* Ignore missing network, wait for next iteration */
							break;
							
						default:
							revertList(sl, user);
							break;
							
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
				
				if (response != null) {
					Shoppinglist serverSl = Shoppinglist.fromJSON(response);
					serverSl.setState(State.SYNCED);
					serverSl.setPreviousId(serverSl.getPreviousId() == null ? sl.getPreviousId() : serverSl.getPreviousId());
					db.editList(serverSl, user);
					mNotification.add(serverSl);
					syncLocalItemChanges(sl, user);
				} else {
					db.deleteList(sl, user);
					mNotification.del(sl);
				}
				popRequestAndPushNotifications();
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
					
					if (local != null && !local.getModified().equals(server.getModified()) ) {
						
						// The server has a 'better' state, we should use this
						server.setState(State.SYNCED);
						// If server havent delivered an prev_id, then use old id
						if (server.getPreviousId() == null) {
							server.setPreviousId(sli.getPreviousId());
						}
						db.editItem(server, user);
						mNotification.edit(server);
						
					}
					
					popRequestAndPushNotifications();
					
				} else {
					
					popRequest();
					
					switch (error.getCode()) {
					
						case Code.NETWORK_ERROR:
							/* Ignore missing network, wait for next iteration */
							break;
							
						default:
							revertItem(sli, user);
							break;
							
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
					
					switch (error.getCode()) {

					case Code.INVALID_RESOURCE_ID:
						/* Resource already gone (or have never been synchronized)
						 * delete local version and ignore */
						db.deleteItem(sli, user);
						break;
						
					case Code.NETWORK_ERROR:
						/* Ignore missing network, wait for next iteration */
						break;
						
					default:
						revertItem(sli, user);
						break;
						
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
				
				if (response != null) {
					
					// Take server response, insert it into DB, post notification
					ShoppinglistItem serverSli = ShoppinglistItem.fromJSON(response);
					serverSli.setState(State.SYNCED);
					serverSli.setPreviousId(serverSli.getPreviousId() == null ? sli.getPreviousId() : serverSli.getPreviousId());
					db.editItem(serverSli, user);
					mNotification.edit(serverSli);
					
				} else {
					
					// Something bad happened, delete item to keep DB sane
					db.deleteItem(sli, user);
					mNotification.del(sli);
					
				}
				
				/* 
				 * Update shopping list modified to match the latest date of the
				 * items, so that we get as close to API state as possible
				 */
				Shoppinglist sl = db.getList(sli.getShoppinglistId(), user);
				if (sl != null) {
					
					List<ShoppinglistItem> items = db.getItems(sl, user, true);
					if (!items.isEmpty()) {
						Collections.sort(items, ShoppinglistItem.ModifiedDescending);
						ShoppinglistItem newestItem = items.get(0);
						sl.setModified(newestItem.getModified());
						db.editList(sl, user);
						mNotification.edit(sl);
					}
					
				}
				
				popRequestAndPushNotifications();
				
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
					putShare(s, user);
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

	private void putShare(final Share s, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		s.setState(State.SYNCING);
		db.editShare(s, user);
		
		Listener<JSONObject> shareListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {

				if (response != null) {
					Share serverShare = Share.fromJSON(response);
					serverShare.setState(State.SYNCED);
					serverShare.setShoppinglistId(s.getShoppinglistId());
					db.editShare(serverShare, user);
					popRequest();
				} else {
					if (error.getFailedOnField() != null) {
						
						/* If it's a FailedOnField, we can't do anything, yet.
						 * Remove the share to keep the DB sane.
						 */
						db.deleteShare(s, user);
						// No need to edit the SL in DB, as shares are disconnected
						Shoppinglist sl = db.getList(s.getShoppinglistId(), user);
						if (sl != null) {
							sl.removeShare(s);
							mNotification.edit(sl);
							popRequestAndPushNotifications();
						} else {
							popRequest();
							/* Nothing, shoppinglist might have been deleted */
						}
						
					} else {
						popRequest();
						revertShare(s, user);
					}
				}

			}
		};
		
		/* Hack for edge, where accept URL is required */
		if (s.getAcceptUrl() == null) {
			s.setAcceptUrl("https://www.etilbudsavis.dk/");
		}
		
		String url = Endpoint.listShareEmail(user.getUserId(), s.getShoppinglistId(), s.getEmail());
		JsonObjectRequest shareReq = new JsonObjectRequest(Method.PUT, url, s.toJSON(), shareListener);
		shareReq.debugNetwork(true);
		addRequest(shareReq);
		
	}

	private void delShare(final Share s, final User user) {
		
		Listener<JSONObject> shareListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {
				
				DbHelper db = DbHelper.getInstance();
				
				if (response != null) {
					
					if (user.getEmail().equals(s.getEmail())) {
						
						/* 
						 * if share.email == user.email, the user have been
						 * removed from the list, delete list, items, and shares
						 */
						db.deleteList(s.getShoppinglistId(), user);
						db.deleteItems(s.getShoppinglistId(), null, user);
						db.deleteShares(s.getShoppinglistId(), user);
						
					} else {
						
						/* Else just remove the share in question */
						db.deleteShare(s, user);
						
					}
					popRequest();
					
				} else {
					
					popRequest();
					
					switch (error.getCode()) {

						case Code.NETWORK_ERROR:
							/* Ignore missing network, wait for next iteration */
							break;
	
						case Code.INVALID_RESOURCE_ID:
							/* Resource  gone (or have never been synchronized)
							 * delete local version and ignore */
							db.deleteShare(s, user);
							break;
							
						default:
							revertShare(s, user);
							break;
						
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
				
				if (response != null) {
					
					Share serverShare = Share.fromJSON(response);
					serverShare.setState(State.SYNCED);
					serverShare.setShoppinglistId(s.getShoppinglistId());
					db.editShare(serverShare, user);

					// No need to edit the SL in DB, as shares are disconnected
					Shoppinglist sl = db.getList(s.getShoppinglistId(), user);
					if (sl != null) {
						
						sl.removeShare(s);
						mNotification.edit(sl);
						popRequestAndPushNotifications();
						
					} else {
						
						/* Nothing, shoppinglist might have been deleted */
						popRequest();
						
					}
					
				} else {
					
					db.deleteShare(s, user);
					popRequest();
					
				}
				
			}
		};
		
		String url = Endpoint.listShareEmail(user.getUserId(), s.getShoppinglistId(), s.getEmail());
		JsonObjectRequest shareReq = new JsonObjectRequest(url, shareListener);
		addRequest(shareReq);
		
	}
	
	/**
	 * Pops one request off the request-stack, and sends out a notification if 
	 * the stack is empty (all requests are done, and all notifications are ready)
	 */
	private void popRequestAndPushNotifications() {
		
		popRequest();
		if (mCurrentRequests.isEmpty()) {
			Eta.getInstance().getListManager().notifySubscribers(mNotification);
		}
		
	}
	
}
