package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.eTilbudsavis.etasdk.Api.JsonArrayListener;
import com.eTilbudsavis.etasdk.Api.JsonObjectListener;
import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.SessionManager.OnSessionChangeListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Share;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.EtaObjects.EtaObject.ServerKey;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ListSyncManager {

	public static final String TAG = "ListSyncManager";

	private static final String THREAD_NAME = "ListSyncManager";
	
	private int mSyncSpeed = 3000;
	
	/** Simple counter keeping track of sync loop */
	private int mSyncCount = 0;
	
	private Stack<Api> mCurrentRequests = new Stack<Api>();
	
	/** Reference to the main Eta object, for Api calls, and Shoppinglistmanager */
	private Eta mEta;
	
	/** The Handler instantiated on the sync Thread */
	private Handler mHandler;
	
	private User mUser;
	
	/** Listening for session changes, starting and stopping sync as needed */
	private OnSessionChangeListener sessionListener = new OnSessionChangeListener() {

		public void onUpdate() {
			if (mUser.getId() != mEta.getUser().getId()) {
				mSyncCount = 0;
				runSyncLoop();
			}
		}
	};
	
	/** The actual sync loop running every x seconds*/
	private Runnable mSyncLoop = new Runnable() {
		
		public void run() {
			
			mUser = mEta.getUser();
			
			if (!mEta.getUser().isLoggedIn() || !mEta.isResumed())
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
	public void syncLists(final User user) {
		
		ListListener<Shoppinglist> listListener = new ListListener<Shoppinglist>() {
			
			public void onComplete(boolean isCache, int statusCode, List<Shoppinglist> serverList, EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					
					// Server usually returns items in the order oldest to newest (not guaranteed)
					// We want them to be reversed
					Collections.reverse(serverList);
					
					for (Shoppinglist sl : serverList) {
						if (sl.getPreviousId() == null)
						sl.setPreviousId(Shoppinglist.FIRST_ITEM);
					}
					
					DbHelper db = DbHelper.getInstance();
					List<Shoppinglist> localList = db.getLists(user);
					mergeShoppinglists(serverList, localList, user);
					
					if (serverList.size() == 0 && localList.size() == 0) {
						
						User nou = new User();
						List<Shoppinglist> noUserLists = db.getLists(nou);
						
						if (noUserLists.size() == 0) {
							return; 
						}
						
						for (Shoppinglist sl : noUserLists) {
							
							List<ShoppinglistItem> noUserItems = db.getItems(sl, nou);
							if (noUserItems.size() == 0)
								return;
								
							Shoppinglist tmpSl = Shoppinglist.fromName(sl.getName());
							
							mEta.getListManager().addList(tmpSl);
							
							for (ShoppinglistItem sli : noUserItems) {
								sli.setShoppinglistId(tmpSl.getId());
								sli.setId(Utils.createUUID());
								sli.setErn("ern:shopping:item:" + sli.getId());
								mEta.getListManager().addItem(sli);
							}
						}
					}
					
					pushNotifications();
					
				} else {
					popRequest();
				}
			}
		};
		addRequest(api().get(Request.Endpoint.lists(mEta.getUser().getId()), listListener).execute());
		
	}

	private void mergeShoppinglists(List<Shoppinglist> serverList, List<Shoppinglist> localList, User user) {
		
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
					Shoppinglist localSl = localset.get(key);
					
					if (localSl.getModified().before(serverSl.getModified())) {
						serverSl.setState(Shoppinglist.State.SYNCED);
						edited.add(serverSl);
						db.editList(serverSl, user);
						db.cleanShares(serverSl, user);
					}
					
				} else {
					deleted.add(localset.get(key));
					db.deleteList(localset.get(key), user);
				}
			} else {
				Shoppinglist sl = serverset.get(key);
				sl.setState(Shoppinglist.State.TO_SYNC);
				added.add(sl);
				db.insertList(sl, user);
			}
			
		}
		
		// If no changes has been registeres, ship the rest
		if (!added.isEmpty() || !deleted.isEmpty() || !edited.isEmpty()) {

			List<ShoppinglistItem> delItems = new ArrayList<ShoppinglistItem>();
			for (Shoppinglist sl : deleted) {
				delItems.addAll(db.getItems(sl, user));
				db.deleteItems(sl.getId(), null, user);
			}
			
			addItemNotification(null, delItems, null);

			// Bundle all this so items, lists e.t.c. is done syncing and in DB, before notifying anyone
			addListNotification(added, deleted, edited);
			
			for (Shoppinglist sl : added) {
				syncItems(sl, user);
			}
			
			for (Shoppinglist sl : edited) {
				syncItems(sl, user);
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
		List<Shoppinglist> currentList = db.getLists(user);
		
		for (final Shoppinglist sl : currentList) {
			
			// If they are in the state of processing, then skip
			if (sl.getState() == Shoppinglist.State.SYNCING || sl.getState() == Shoppinglist.State.DELETE) 
				continue;
			
			// If it obviously needs to sync, then just do it
			if (sl.getState() == Shoppinglist.State.TO_SYNC) {
				// New shopping lists must always sync
				syncItems(sl, user);
				continue;
			} 
			
			// Run the check 
			sl.setState(Shoppinglist.State.SYNCING);
			db.editList(sl, user);
			
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, final EtaError error) {
			
					if (Utils.isSuccess(statusCode)) {
						
						sl.setState(Shoppinglist.State.SYNCED);
						try {
							String modified = data.getString(Shoppinglist.S_MODIFIED);
							Date date = Utils.parseDate(modified);
							if (sl.getModified().before(date)) {
								syncItems(sl, user);
							}
						} catch (JSONException e) {
							EtaLog.d(TAG, e);
						}
						db.editList(sl, user);
						pushNotifications();
					} else {
						popRequest();
						revertList(sl, user);
					}
					
				}
			};
			
			addRequest(api().get(Request.Endpoint.listModified(mEta.getUser().getId(), sl.getId()), cb).execute());
			
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
		
		sl.setState(Shoppinglist.State.SYNCING);
		db.editList(sl, user);
		
		JsonArrayListener itemListener = new JsonArrayListener() {
			
			public void onComplete(final boolean isCache, final int statusCode, final JSONArray data, final EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					
					sl.setState(Shoppinglist.State.SYNCED);
					db.editList(sl, user);
					
					List<ShoppinglistItem> localItems = db.getItems(sl, user);
					List<ShoppinglistItem> serverItems = ShoppinglistItem.fromJSON(data);
					
					// So far, we get items in reverse order, well just keep reversing it for now.
					Collections.reverse(serverItems);
					
					// Sort items according to our definition of correct ordering
					Utils.sortItems(localItems);
					Utils.sortItems(serverItems);
					
					// Update previous_id's (and modified) if needed
					String id = ShoppinglistItem.FIRST_ITEM;
					for (ShoppinglistItem sli : serverItems) {
						if (!id.equals(sli.getPreviousId())) {
							sli.setPreviousId(id);
							sli.setModified(new Date());
						}
						id = sli.getId();
					}
					
					diffItems(serverItems, localItems, user);
					
					pushNotifications();
					
				} else {
					popRequest();
					revertList(sl, user);
				}
				
			}
		};
		
		addRequest(api().get(Request.Endpoint.items(mEta.getUser().getId(), sl.getId()), itemListener).execute());
	}
	
	private void diffItems(List<ShoppinglistItem> newList, List<ShoppinglistItem> oldList, User user) {
		
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
						db.editItem(serverSli, user);
					} else if (!localSli.getPreviousId().equals(serverSli.getPreviousId())) {
						
						// This is a special case, thats only relevant as long as the
						// server isn't sending previous_id's
						localSli.setPreviousId(serverSli.getPreviousId());
						db.editItem(localSli, user);
						
					}
				} else {
					deleted.add(localSet.get(key));
					db.deleteItem(localSet.get(key), user);
				}
			} else {
				ShoppinglistItem serverSli = serverSet.get(key);
				added.add(serverSli);
				db.insertItem(serverSli, user);
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
	private boolean syncLocalListChanges(List<Shoppinglist> lists, User user) {
		
		int count = lists.size();
		
		for (Shoppinglist sl : lists) {

			switch (sl.getState()) {

			case Shoppinglist.State.TO_SYNC:
				putList(sl, user);
				break;

			case Shoppinglist.State.DELETE:
				delList(sl, user);
				break;

			case Shoppinglist.State.ERROR:
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
			case ShoppinglistItem.State.TO_SYNC:
				putItem(sli, user);
				break;

			case ShoppinglistItem.State.DELETE:
				delItem(sli, user);
				break;

			case ShoppinglistItem.State.ERROR:
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
		
		sl.setState(Shoppinglist.State.SYNCING);
		db.editList(sl, user);
		
		JsonObjectListener editList = new JsonObjectListener() {

			public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
				Shoppinglist s = sl;
				if (Utils.isSuccess(statusCode)) {
					s = Shoppinglist.fromJSON(data);
					Shoppinglist dbList = db.getList(s.getId(), user);
					if (dbList != null && !s.getModified().before(dbList.getModified()) ) {
						s.setState(Shoppinglist.State.SYNCED);
						// If server havent delivered an prev_id, then use old id
						s.setPreviousId(s.getPreviousId() == null ? sl.getPreviousId() : s.getPreviousId());
						db.editList(s, user);
					}
					popRequest();
					syncLocalItemChanges(sl, user);
				} else {
					popRequest();
					if (statusCode == -1) {
						
					} else {
						revertList(sl, user);
					}
				}

			}
		};
		String url = Request.Endpoint.list(user.getId(), sl.getId());
		addRequest(api().put(url, editList, sl.getApiParams()).execute());
		
	}

	private void delList(final Shoppinglist sl, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		EtaLog.d(TAG, "Delete: " + sl.getName());
		
		JsonObjectListener cb = new JsonObjectListener() {

			public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, final EtaError error) {

				if (Utils.isSuccess(statusCode)) {
					db.deleteList(sl, user);
					db.deleteShares(sl, user);
					db.deleteItems(sl.getId(), null, user);
					popRequest();
				} else {
					popRequest();
					if (error.getCode() != 1501) {
						db.deleteList(sl, user);
					} else if (statusCode == -1) {
						// nothing, trying again later
					} else {
						revertList(sl, user);
					}
				}

			}
		};
		String url = Request.Endpoint.list(user.getId(), sl.getId());
		addRequest(api().delete(url, cb, sl.getApiParams()).setFlag(Api.FLAG_PRINT_DEBUG).execute());

	}

	private void revertList(final Shoppinglist sl, final User user) {
		
		final DbHelper db = DbHelper.getInstance();
		
		if (sl.getState() != Shoppinglist.State.ERROR) {
			sl.setState(Shoppinglist.State.ERROR);
			db.editList(sl, user);
		}
		
		JsonObjectListener listListener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {

				Shoppinglist s = null;
				if (Utils.isSuccess(statusCode)) {
					s = Shoppinglist.fromJSON(item);
					s.setState(Shoppinglist.State.SYNCED);
					s.setPreviousId(s.getPreviousId() == null ? sl.getPreviousId() : s.getPreviousId());
					db.editList(s, user);
					addListNotification(null, null, idToList(s));
					syncLocalItemChanges(sl, user);
				} else {
					db.deleteList(sl, user);
					addListNotification(null, idToList(s), null);
				}
				pushNotifications();
			}
		};
		String url = Request.Endpoint.list(user.getId(), sl.getId());
		addRequest(api().get(url, listListener).execute());
		
	}

	private void putItem(final ShoppinglistItem sli, final User user) {

		final DbHelper db = DbHelper.getInstance();
		
		sli.setState(ShoppinglistItem.State.SYNCING);
		db.editItem(sli, user);
		
		JsonObjectListener cb = new JsonObjectListener() {

			public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, EtaError error) {
				
				ShoppinglistItem s = sli;
				
				if (Utils.isSuccess(statusCode)) {
					
					s = ShoppinglistItem.fromJSON(data);
					ShoppinglistItem local = db.getItem(sli.getId(), user);
					if (local != null && local.getModified().after(s.getModified()) ) {
						s.setState(ShoppinglistItem.State.SYNCED);
						// If server havent delivered an prev_id, then use old id
						s.setPreviousId(s.getPreviousId() == null ? sli.getPreviousId() : s.getPreviousId());
						db.editItem(s, user);
					}
					popRequest();
				} else {
					popRequest();
					if (statusCode != -1) {
						revertItem(s, user);
					}
				}

			}
		};
		String url = Request.Endpoint.item(user.getId(), sli.getShoppinglistId(), sli.getId());
		addRequest(api().put(url, cb, sli.getApiParams()).execute());

	}

	private void delItem(final ShoppinglistItem sli, final User user) {

		final DbHelper db = DbHelper.getInstance();

		JsonObjectListener cb = new JsonObjectListener() {

			public void onComplete(final boolean isCache,final  int statusCode,final  JSONObject item,final  EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					db.deleteItem(sli, user);
					popRequest();
				} else {
					popRequest();
					
					if(error.getCode() == 1501) {
						db.deleteItem(sli, user);
					} else if (statusCode == -1) {
						// Nothing
					} else {
						revertItem(sli, user);
					}
					
				}

			}
		};
		String url = Request.Endpoint.item(user.getId(), sli.getShoppinglistId(), sli.getId());
		addRequest(api().delete(url, cb, sli.getApiParams()).execute());
	}
	
	private void revertItem(final ShoppinglistItem sli, final User user) {
		
		final DbHelper db = DbHelper.getInstance();
		
		if (sli.getState() != ShoppinglistItem.State.ERROR) {
			sli.setState(ShoppinglistItem.State.ERROR);
			db.editItem(sli, user);
		}
		
		JsonObjectListener itemListener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {
				
				ShoppinglistItem s = null;
				if (Utils.isSuccess(statusCode)) {
					s = ShoppinglistItem.fromJSON(item);
					s.setState(ShoppinglistItem.State.SYNCED);
					s.setPreviousId(s.getPreviousId() == null ? sli.getPreviousId() : s.getPreviousId());
					db.editItem(s, user);
					addItemNotification(null, null, idToList(s));
				} else {
					db.deleteItem(sli, user);
					addItemNotification(null, idToList(s), null);
				}
				pushNotifications();
			}
		};

		String url = Request.Endpoint.item(user.getId(), sli.getShoppinglistId(), sli.getId());
		addRequest(api().get(url, itemListener).execute());
		
	}

	private boolean syncLocalShareChanges(Shoppinglist sl, User user) {
		
		DbHelper db = DbHelper.getInstance();
		List<Share> shares = db.getShares(sl, user, true);
		
		int count = shares.size();
		
		for (Share s : shares) {
			
			switch (s.getState()) {
			case Share.State.TO_SYNC:
				EtaLog.d(TAG, "# Shares - " + sl.getName() + " - " + s.getEmail() + " TO_SYNC");
				putShare(s, user);
				break;

			case Share.State.DELETE:
				EtaLog.d(TAG, "# Shares - " + sl.getName() + " - " + s.getEmail() + " DELETE");
				delShare(s, user);
				break;
				
			case Share.State.ERROR:
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
		
		s.setState(Share.State.SYNCING);
		db.editShare(s, user);
		
		JsonObjectListener shareListener = new JsonObjectListener() {
			
			public void onComplete(final boolean isCache, final int statusCode, final JSONObject data, EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					Share tmp = Share.fromJSON(data);
					tmp.setState(Share.State.SYNCED);
					tmp.setShoppinglistId(s.getShoppinglistId());
					int count = db.editShare(tmp, user);
					EtaLog.d(TAG, "ShareEditCount: " + String.valueOf(count));
					popRequest();
				} else {
					popRequest();
					revertShare(s, user);
				}

			}
		};
		
		String url = Request.Endpoint.listShareEmail(user.getId(), s.getShoppinglistId(), s.getEmail());
		addRequest(api().put(url, shareListener, s.getApiParams()).setFlag(Api.FLAG_PRINT_DEBUG).execute());
		
	}

	private void delShare(final Share s, final User user) {

		final DbHelper db = DbHelper.getInstance();

		JsonObjectListener shareListener = new JsonObjectListener() {

			public void onComplete(final boolean isCache,final  int statusCode,final  JSONObject item,final  EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					
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
					if (statusCode == -1) {
						// Nothing
					} else if (error.getCode() == 1501) {
						db.deleteShare(s, user);
					} else {
						revertShare(s, user);
					}
				}
				
			}
		};
		String url = Request.Endpoint.listShareEmail(user.getId(), s.getShoppinglistId(), s.getEmail());
		addRequest(api().delete(url, shareListener, new Bundle()).setFlag(Api.FLAG_PRINT_DEBUG).execute());
	}
	
	private void revertShare(final Share s, final User user) {
		
		final DbHelper db = DbHelper.getInstance();
		
		if (s.getState() != Share.State.ERROR) {
			s.setState(Share.State.ERROR);
			db.editShare(s, user);
		}
		
		JsonObjectListener shareListener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {
				
				Share tmp = null;
				if (Utils.isSuccess(statusCode)) {
					tmp = Share.fromJSON(item);
					tmp.setState(ShoppinglistItem.State.SYNCED);
					tmp.setShoppinglistId(s.getShoppinglistId());
					db.editShare(tmp, user);
				} else {
					db.deleteShare(s, user);
				}
				popRequest();
			}
		};
		
		String url = Request.Endpoint.listShareEmail(user.getId(), s.getShoppinglistId(), s.getEmail());
		addRequest(api().get(url, shareListener).execute());
		
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
