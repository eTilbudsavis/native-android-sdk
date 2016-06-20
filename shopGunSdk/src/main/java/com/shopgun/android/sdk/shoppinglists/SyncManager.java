/*******************************************************************************
 * Copyright 2015 ShopGun
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
 ******************************************************************************/

package com.shopgun.android.sdk.shoppinglists;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.bus.SessionEvent;
import com.shopgun.android.sdk.bus.SgnBus;
import com.shopgun.android.sdk.bus.ShoppinglistEvent;
import com.shopgun.android.sdk.database.DatabaseWrapper;
import com.shopgun.android.sdk.database.DbUtils;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.model.interfaces.SyncState;
import com.shopgun.android.sdk.network.Delivery;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Request.Method;
import com.shopgun.android.sdk.network.RequestQueue;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.ShopGunError.Code;
import com.shopgun.android.sdk.network.impl.HandlerDelivery;
import com.shopgun.android.sdk.network.impl.JsonArrayRequest;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.utils.Api.Endpoint;
import com.shopgun.android.sdk.utils.ListUtils;
import com.shopgun.android.sdk.utils.PermissionUtils;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * The {@link SyncManager} class performs asynchronous synchronization with the
 * ShopGun API, to propagate all {@link Shoppinglist} and {@link ShoppinglistItem}
 * changes that a user may have done in the {@link DatabaseWrapper database}.
 *
 * <p>
 * Notifications about {@link Shoppinglist} and {@link ShoppinglistItem}
 * changes, are relayed through the subscriber system in the {@link ListManager}.
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
 * <p>
 * When {@link #onStop()} is triggered, all local pending changes are pushed to
 * the API if possible to ensure a correct state on the server (and other devices).
 */
public class SyncManager {

    public static final String TAG = Constants.getTag(SyncManager.class);

    private static final boolean SAVE_NETWORK_LOG = false;

    final Object RESUME_LOCK = new Object();
    private final Stack<Request<?>> mCurrentRequests = new Stack<Request<?>>();
    /** Thread running the options */
    HandlerThread mThread;
    boolean isPaused = false;
    /** The current sync speed */
    private int mSyncSpeed = SyncSpeed.SLOW;
    /** Sync iteration counter */
    private int mSyncCount = 0;
    /** Has sync got the first sync */
    private boolean mHasFirstSync = false;
    /** Reference to the {@link ShopGun} object */
    private ShopGun mShopGun;
    private DatabaseWrapper mDatabase;
    /** The Handler instantiated on the sync Thread */
    private Handler mHandler;
    /**
     * Variable to determine if offline lists should automatically be
     * synchronized if certain criteria are met.
     */
    private boolean mMigrateOfflineLists = false;
    /**
     * A tag for identifying all requests originating from this {@link SyncManager}
     * in the {@link RequestQueue}
     */
    private Object mRequestTag = new Object();
    /** The notification object, used to combine and collect notifications */
    private ShoppinglistEvent.Builder mBuilder = new ShoppinglistEvent.Builder(true);
    private Delivery mDelivery;
    /** The actual sync loop running every x seconds*/
    private Runnable mSyncLoop = new Runnable() {

        public void run() {

            User u = mShopGun.getUser();
            // If it's an offline user, then just quit it
            if (!u.isLoggedIn()) {
                SyncLog.sync(TAG, "SyncManager(" + mSyncCount + ") - skip-loop-cycle (NotLoggedIn)");
                return;
            }

			/*
			 * Prepare for next iteration if app is still resumed.
			 * By not doing a return statement we allow for a final sync, and
			 * sending local changes to server
			 */
            if (mShopGun.isStarted()) {
                mHandler.postDelayed(mSyncLoop, mSyncSpeed);
            }

            // Only do an update, if there are no pending transactions, and we are online
            if (!mCurrentRequests.isEmpty()) {
                SyncLog.sync(TAG, "SyncManager(" + mSyncCount + ") - skip-loop-cycle (ReqInFlight)");
                return;
            }
            if (!mShopGun.isOnline()) {
                SyncLog.sync(TAG, "SyncManager(" + mSyncCount + ") - skip-loop-cycle (Offline)");
                return;
            }
            if (isPaused()) {
                SyncLog.sync(TAG, "SyncManager(" + mSyncCount + ") - skip-loop-cycle (Paused)");
                return;
            }

            // Perform the actual sync cycle
            performSyncCycle(u);

        }

    };

    /**
     * Default constructor for the {@link SyncManager}
     * @param shopGun An ShopGun instance
     * @param db A database
     */
    public SyncManager(ShopGun shopGun, DatabaseWrapper db) {
        mShopGun = shopGun;
        mDatabase = db;
        // Create a new thread for a handler, so that i can later post content to that thread.
        mThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        mDelivery = new HandlerDelivery(mHandler);
    }

    /**
     * Listening for session changes, starting and stopping sync as needed
     * @param e The event we are listening for
     */
    public void onEvent(SessionEvent e) {
        if (e.isNewUser()) {
            mHasFirstSync = false;
            mSyncCount = 0;
            forceSync();
        }
    }

    private void performSyncCycle(User user) {

        // If there are local changes to a list, then syncLocalListChanges will handle it: return
        List<Shoppinglist> lists = mDatabase.getLists(user, true);
        if (syncLocalListChanges(lists, user)) {
            SyncLog.sync(TAG, "SyncManager(" + mSyncCount + ") - syncLocalListChanges");
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
            SyncLog.sync(TAG, "SyncManager(" + mSyncCount + ") - hasLocalChanges");
            return;
        }

        // Finally ready to get server changes
        if (mSyncCount % 3 == 0) {

            // Get a new set of lists
            SyncLog.sync(TAG, "SyncManager(" + mSyncCount + ") - syncAllLists");
            syncLists(user);

        } else if (mSyncCount % 10 == 0) {

            SyncLog.sync(TAG, "SyncManager(" + mSyncCount + ") - syncAllItems");
        	/* Because we update modified on lists, on all changes, we might
        	 * have a situation where two devices have set the same modified
        	 * on a list, and therefore won't try to get a new list of items
        	 * So we force the SDK to get a new set once in a while */
            List<Shoppinglist> localLists = mDatabase.getLists(user);
            for (Shoppinglist sl : localLists) {
                syncItems(sl, user);
            }

        } else {

            SyncLog.sync(TAG, "SyncManager(" + mSyncCount + ") - checkModified");
            // Base case, just check if there is changes
            syncListsModified(user);

        }
        mSyncCount++;

    }

    public boolean isPaused() {
        synchronized (RESUME_LOCK) {
            return isPaused;
        }
    }

    public void pauseSync() {
        synchronized (RESUME_LOCK) {
            isPaused = true;
        }
    }

    public void resumeSync() {
        synchronized (RESUME_LOCK) {
            isPaused = false;
        }
    }

    /**
     * Method for determining if the first sync cycle is done.
     *
     * <p>This is dependent on, both:
     * <ul>
     * 		<li>the {@link SyncManager} having performed the first sync cycle, and </li>
     * </ul>
     * @return True if the first sync is complete, or there is no user to sync.
     */
    public boolean hasFirstSync() {
        return mHasFirstSync;
    }

    /**
     * Method for forcing a new synchronization iteration.
     * <p>
     *     This method will trigger the synchronization loop, so it's essentially
     *     also the method for starting the {@link SyncManager} sync loop
     *
     * <p>
     *     This method should only be used in special cases, as the
     *     synchronization is (within reasonably time intervals) being handled
     *     automatically by the {@link SyncManager}
     */
    public void forceSync() {
        // First make sure, that we do not leak memory by posting the runnable multiple times
        mHandler.removeCallbacks(mSyncLoop);
        mHandler.post(mSyncLoop);
    }

    /**
     * Method to call on all onResume events.
     * <p>This is implicitly handled by the {@link ShopGun} instance
     */
    public void onStart() {
        mDatabase.open();
        SgnBus.getInstance().register(this);
        forceSync();
    }

    /**
     * Method to call on all onPause events.
     * <p>This is implicitly handled by the {@link ShopGun} instance
     */
    public void onStop() {
        mDatabase.close();
        forceSync();
        SgnBus.getInstance().unregister(this);
        mHasFirstSync = false;
        mSyncCount = -1;
    }

    /**
     * Set synchronization interval for {@link Shoppinglist}, and {@link ShoppinglistItem}.
     *
     * <p>Also time must be greater than or equal to 3000 (milliseconds)
     * @param time A synchronization interval in milliseconds
     */
    public void setSyncSpeed(int time) {
        if (time == SyncSpeed.SLOW || time == SyncSpeed.MEDIUM || time == SyncSpeed.FAST) {
            mSyncSpeed = time;
        } else {
            mSyncSpeed = SyncSpeed.SLOW;
        }
    }

    /**
     * Method for easily migrating "offline" lists to "online" lists.
     *
     * <p>If true, the {@link SyncManager} ensures the merging of any
     * (non-empty) "offline" lists, into a new "online" user's
     * {@link Shoppinglist shoppinglists}, on the first completed sync cycle.
     * <p>
     * (a user is considered a "new" if he/she haven't got any lists on the
     * server already)
     *
     * @param migrate {@code true} if to do automatic migration of offline
     *                 {@link Shoppinglist shoppinglists}, else {@code false}
     */
    public void setMigrateOfflineLists(boolean migrate) {
        mMigrateOfflineLists = migrate;
    }

    /**
     * True if "offline" {@link Shoppinglist} will be migrated on first sync with
     * a new "online" user's {@link Shoppinglist shoppinglists}.
     * @return true if the SyncManager will migrate any offline lists, else false
     */
    public boolean isMigratingOfflineLists() {
        return mMigrateOfflineLists;
    }

    private void addRequest(Request<?> r) {
        // No request from here should return a result from cache
        r.setIgnoreCache(true);
        synchronized (mCurrentRequests) {
            mCurrentRequests.add(r);
        }

        // Make sure, that requests will return to this thread
        r.setDelivery(mDelivery);

        r.setTag(mRequestTag);
        mShopGun.add(r);

//		if (!isPullReq(r) && r.getMethod() != Request.Method.GET) {
//			SgnLog.d(TAG, r.toString());
//		}

    }

    private boolean isPullReq(Request<?> r) {
        String u = r.getUrl();
        return u.contains("modified") || u.endsWith("shoppinglists") || u.endsWith("items");
    }

    private void popRequest() {
        synchronized (mCurrentRequests) {
            try {
                mCurrentRequests.pop();
            } catch (Exception e) {
                SgnLog.e(TAG, e.getMessage(), e);
            }
        }
    }

    private void syncLists(final User user) {

        Listener<JSONArray> listListener = new Listener<JSONArray>() {

            public void onComplete(JSONArray response, ShopGunError error) {

                if (response != null) {

                    // Get ALL lists including deleted, to avoid adding them again
                    List<Shoppinglist> localLists = mDatabase.getLists(user, true);
                    List<Shoppinglist> serverLists = Shoppinglist.fromJSON(response);

                    // Server usually returns items in the order oldest to newest (not guaranteed)
                    // We want them to be reversed
                    Collections.reverse(serverLists);

                    prepareServerList(serverLists);

                    mergeListsToDb(serverLists, localLists, user);

                    // On first iteration, check and merge lists plus notify subscribers of first sync event
                    if (mSyncCount == 1) {

                        if (mMigrateOfflineLists && serverLists.isEmpty() && localLists.isEmpty()) {
                            DbUtils.migrateOfflineLists(mShopGun.getListManager(), mDatabase, false);
                        }

                        mHasFirstSync = true;
                        mBuilder.firstSync = true;

                    }

                    popRequestAndPostShoppinglistEvent();

                } else {
                    popRequest();
                }
            }
        };

        JsonArrayRequest listRequest = new JsonArrayRequest(Method.GET, Endpoint.lists(user.getUserId()), listListener);
        // Offset and limit are set to default values, we want to ignore this.
        listRequest.getParameters().remove(Parameters.OFFSET);
        listRequest.getParameters().remove(Parameters.LIMIT);
        listRequest.setSaveNetworkLog(SAVE_NETWORK_LOG);
        addRequest(listRequest);

    }

    private void prepareServerList(List<Shoppinglist> serverList) {

        for (Shoppinglist sl : serverList) {
			/* Set the state of all shares in the serverList to SYNCED,
			 * before inserting into the DB, as this is actually correct
			 */
            for (Share share : sl.getShares().values()) {
                share.setState(SyncState.SYNCED);
            }

        }

    }

    private void mergeListsToDb(List<Shoppinglist> serverList, List<Shoppinglist> localList, User user) {

        if (serverList.isEmpty() && localList.isEmpty()) {
            return;
        }

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
                        serverSl.setState(SyncState.SYNCED);
                        mBuilder.edit(serverSl);
                        mDatabase.editList(serverSl, user);
                        mDatabase.cleanShares(serverSl, user);
                    } else {
                        // Don't do anything, next iteration will put local changes to API
                    }

                } else {
                    mBuilder.del(localSl);
                    for (ShoppinglistItem sli : mDatabase.getItems(localSl, user)) {
                        mBuilder.del(sli);
                    }
                    mDatabase.deleteItems(localSl.getId(), null, user);
                    mDatabase.deleteList(localSl, user);
                }

            } else {

                Shoppinglist add = serverMap.get(key);
                add.setState(SyncState.TO_SYNC);
                mBuilder.add(add);
                mDatabase.insertList(add, user);

            }

        }

        for (Shoppinglist sl : mBuilder.getAddedLists()) {
            syncItems(sl, user);
        }

        for (Shoppinglist sl : mBuilder.getEditedLists()) {
            syncItems(sl, user);
        }

    }

    private void syncListsModified(final User user) {

        List<Shoppinglist> lists = mDatabase.getLists(user);
        Iterator<Shoppinglist> it = lists.iterator();

        while (it.hasNext()) {
            Shoppinglist sl = it.next();

            // If they are in the state of processing, then skip
            if (sl.getState() == SyncState.SYNCING) {
                it.remove();
            }

            // If it obviously needs to sync, then just do it
            if (sl.getState() == SyncState.TO_SYNC) {
                // New shopping lists must always sync
                syncItems(sl, user);
                it.remove();
            }

            // Run the check
            sl.setState(SyncState.SYNCING);

        }

        mDatabase.insertLists(lists, user);

        for (Shoppinglist sl : lists) {
            requestListsModified(sl, user);
        }

    }

    private void requestListsModified(final Shoppinglist sl, final User user) {

        Listener<JSONObject> modifiedListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {

                    sl.setState(SyncState.SYNCED);
                    try {
                        String modified = response.getString(SgnJson.MODIFIED);
                        // If local list has been modified before the server list, then sync items
                        if (sl.getModified().before(Utils.stringToDate(modified))) {
                            // If there are changes, update items (this will update list-state in DB)
                            syncItems(sl, user);
                        } else {
                            // if no changes, just write new state to DB
                            mDatabase.editList(sl, user);
                        }
                    } catch (JSONException e) {
                        SgnLog.e(TAG, e.getMessage(), e);
                        // error? just write new state to DB, next iteration will fix it
                        mDatabase.editList(sl, user);
                    }
                    popRequestAndPostShoppinglistEvent();

                } else {

                    popRequest();
                    revertList(sl, user);

                }


            }
        };

        JsonObjectRequest modifiedRequest = new JsonObjectRequest(Endpoint.listModified(user.getUserId(), sl.getId()), modifiedListener);
        modifiedRequest.setSaveNetworkLog(SAVE_NETWORK_LOG);
        addRequest(modifiedRequest);

    }

    private void syncItems(final Shoppinglist sl, final User user) {

        sl.setState(SyncState.SYNCING);
        mDatabase.editList(sl, user);

        Listener<JSONArray> itemListener = new Listener<JSONArray>() {

            public void onComplete(JSONArray response, ShopGunError error) {

                if (response != null) {

                    sl.setState(SyncState.SYNCED);
                    mDatabase.editList(sl, user);

                    // Get ALL items including deleted, to avoid adding them again
                    List<ShoppinglistItem> localItems = mDatabase.getItems(sl, user, true);
                    List<ShoppinglistItem> serverItems = ShoppinglistItem.fromJSON(response);

                    // So far, we get items in reverse order, well just keep reversing it for now.
                    Collections.reverse(serverItems);

                    for (ShoppinglistItem sli : serverItems) {
                        sli.setState(SyncState.SYNCED);
                    }

                    mergeItemsToDb(serverItems, localItems, user);

					/* fetch updated items from DB, as the state might be a bit
					 * whack after the merging of items */
                    localItems = mDatabase.getItems(sl, user);
                    ListUtils.sortItems(localItems);

                    if (PermissionUtils.allowEdit(sl, user)) {

						/* Update previous_id's, modified and state if needed */
                        String tmp = ListUtils.FIRST_ITEM;
                        for (ShoppinglistItem sli : localItems) {

                            if (!tmp.equals(sli.getPreviousId())) {
                                sli.setPreviousId(tmp);
                                sli.setModified(new Date());
                                sli.setState(SyncState.TO_SYNC);

								/* If it's a new item, it's already in the added list,
								 * then we'll override it else add it to the edited
								 * as a new item to the edited list */
                                if (mBuilder.items.containsKey(sli.getId())) {
                                    mBuilder.add(sli);
                                } else {
                                    mBuilder.edit(sli);
                                }

                                mDatabase.editItems(sli, user);
                            }
                            tmp = sli.getId();
                        }
                    }

                    popRequestAndPostShoppinglistEvent();

                } else {
                    popRequest();
                    revertList(sl, user);
                }

            }
        };

        JsonArrayRequest itemRequest = new JsonArrayRequest(Method.GET, Endpoint.listitems(user.getUserId(), sl.getId()), itemListener);
        // Offset and limit are set to default values, we want to ignore this.
        itemRequest.getParameters().remove(Parameters.OFFSET);
        itemRequest.getParameters().remove(Parameters.LIMIT);
        itemRequest.setSaveNetworkLog(SAVE_NETWORK_LOG);
        addRequest(itemRequest);

    }

    private void mergeItemsToDb(List<ShoppinglistItem> serverItems, List<ShoppinglistItem> localItems, User user) {

        if (serverItems.isEmpty() && localItems.isEmpty()) {
            return;
        }

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
                        mBuilder.edit(serverSli);
                        mDatabase.editItems(serverSli, user);

                    } else if (!localSli.getMeta().toString().equals(serverSli.getMeta().toString())) {
                        // Migration code, to get comments into the DB
                        mBuilder.edit(serverSli);
                        mDatabase.editItems(serverSli, user);
                    } else if (localSli.equals(serverSli)) {
                        SgnLog.d(TAG, "We have a mismatch");
                    }

                } else {
                    ShoppinglistItem delSli = localMap.get(key);
                    if (delSli.getState() == SyncState.TO_SYNC) {
						/*
						 * Item have been added while request was in flight
						 * ignore it for now
						 */
                    } else {
						/* Else delete the item */
                        mBuilder.del(delSli);
                        mDatabase.deleteItem(delSli, user);
                    }
                }

            } else {
                ShoppinglistItem serverSli = serverMap.get(key);
                mBuilder.add(serverSli);
                mDatabase.insertItem(serverSli, user);
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

                case SyncState.TO_SYNC:
                    putList(sl, user);
                    break;

                case SyncState.DELETE:
                    delList(sl, user);
                    break;

                case SyncState.ERROR:
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

        List<ShoppinglistItem> items = mDatabase.getItems(sl, user, true);
        int count = items.size();

        for (ShoppinglistItem sli : items) {

            switch (sli.getState()) {
                case SyncState.TO_SYNC:
                    putItem(sli, user);
                    break;

                case SyncState.DELETE:
                    delItem(sli, user);
                    break;

                case SyncState.ERROR:
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

        sl.setState(SyncState.SYNCING);
        mDatabase.editList(sl, user);

        Listener<JSONObject> listListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {

					/*
					 * If local isn't equal to server version, take server version.
					 * Don't push changes yet, we want to check item state too.
					 */

                    Shoppinglist serverSl = Shoppinglist.fromJSON(response);
                    Shoppinglist localSl = mDatabase.getList(serverSl.getId(), user);
                    if (localSl != null && !serverSl.getModified().equals(localSl.getModified())) {
                        serverSl.setState(SyncState.SYNCED);
                        // If server haven't delivered an prev_id, then use old id
                        serverSl.setPreviousId(serverSl.getPreviousId() == null ? sl.getPreviousId() : serverSl.getPreviousId());
                        mDatabase.editList(serverSl, user);
                        mBuilder.edit(serverSl);
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

        Listener<JSONObject> listListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {

                    mDatabase.deleteList(sl, user);
                    mDatabase.deleteShares(sl, user);
                    mDatabase.deleteItems(sl.getId(), null, user);
                    popRequest();

                } else {

                    popRequest();

                    switch (error.getCode()) {

                        case Code.INVALID_RESOURCE_ID:
							/* Resource already gone (or have never been synchronized)
							 * delete local version and ignore */
                            mDatabase.deleteList(sl, user);
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
        listReq.getParameters().put(Parameters.MODIFIED, Utils.dateToString(sl.getModified()));
        addRequest(listReq);

    }

    private void revertList(final Shoppinglist sl, final User user) {

        if (sl.getState() != SyncState.ERROR) {
            sl.setState(SyncState.ERROR);
            mDatabase.editList(sl, user);
        }

        Listener<JSONObject> listListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {
                    Shoppinglist serverSl = Shoppinglist.fromJSON(response);
                    serverSl.setState(SyncState.SYNCED);
                    serverSl.setPreviousId(serverSl.getPreviousId() == null ? sl.getPreviousId() : serverSl.getPreviousId());
                    mDatabase.editList(serverSl, user);
                    mBuilder.add(serverSl);
                    syncLocalItemChanges(sl, user);
                } else {
                    mDatabase.deleteList(sl, user);
                    mBuilder.del(sl);
                }
                popRequestAndPostShoppinglistEvent();
            }
        };

        String url = Endpoint.list(user.getUserId(), sl.getId());
        JsonObjectRequest listReq = new JsonObjectRequest(url, listListener);
        addRequest(listReq);

    }

    private void putItem(final ShoppinglistItem sli, final User user) {

        sli.setState(SyncState.SYNCING);
        mDatabase.editItems(sli, user);

        Listener<JSONObject> itemListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {

                    ShoppinglistItem server = ShoppinglistItem.fromJSON(response);
                    ShoppinglistItem local = mDatabase.getItem(sli.getId(), user);

                    if (local != null && !local.getModified().equals(server.getModified())) {

                        // The server has a 'better' state, we should use this
                        server.setState(SyncState.SYNCED);
                        // If server havent delivered an prev_id, then use old id
                        if (server.getPreviousId() == null) {
                            server.setPreviousId(sli.getPreviousId());
                        }
                        mDatabase.editItems(server, user);
                        mBuilder.edit(server);

                    }

                    popRequestAndPostShoppinglistEvent();

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
//		SgnLog.d(TAG, sli.toJSON().toString());
        addRequest(itemReq);

    }

    private void delItem(final ShoppinglistItem sli, final User user) {

        Listener<JSONObject> itemListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {
                    mDatabase.deleteItem(sli, user);
                    popRequest();
                } else {
                    popRequest();

                    switch (error.getCode()) {

                        case Code.INVALID_RESOURCE_ID:
						/* Resource already gone (or have never been synchronized)
						 * delete local version and ignore */
                            mDatabase.deleteItem(sli, user);
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
        itemReq.getParameters().put(Parameters.MODIFIED, Utils.dateToString(sli.getModified()));
        addRequest(itemReq);

    }

    private void revertItem(final ShoppinglistItem sli, final User user) {

        if (sli.getState() != SyncState.ERROR) {
            sli.setState(SyncState.ERROR);
            mDatabase.editItems(sli, user);
        }

        Listener<JSONObject> itemListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {

                    // Take server response, insert it into DB, post notification
                    ShoppinglistItem serverSli = ShoppinglistItem.fromJSON(response);
                    serverSli.setState(SyncState.SYNCED);
                    serverSli.setPreviousId(serverSli.getPreviousId() == null ? sli.getPreviousId() : serverSli.getPreviousId());
                    mBuilder.edit(serverSli);

                } else {

                    // Something bad happened, delete item to keep DB sane
                    mDatabase.deleteItem(sli, user);
                    mBuilder.del(sli);

                }

				/*
				 * Update shopping list modified to match the latest date of the
				 * items, so that we get as close to API state as possible
				 */
                Shoppinglist sl = mDatabase.getList(sli.getShoppinglistId(), user);
                if (sl != null) {

                    List<ShoppinglistItem> items = mDatabase.getItems(sl, user, true);
                    if (!items.isEmpty()) {
                        Collections.sort(items, ShoppinglistItem.MODIFIED_DESCENDING);
                        ShoppinglistItem newestItem = items.get(0);
                        sl.setModified(newestItem.getModified());
                        mDatabase.editList(sl, user);
                        mBuilder.edit(sl);
                    }

                }

                popRequestAndPostShoppinglistEvent();

            }
        };

        String url = Endpoint.listitem(user.getUserId(), sli.getShoppinglistId(), sli.getId());
        JsonObjectRequest itemReq = new JsonObjectRequest(url, itemListener);
        addRequest(itemReq);

    }

    private boolean syncLocalShareChanges(Shoppinglist sl, User user) {

        List<Share> shares = mDatabase.getShares(sl, user, true);

        int count = shares.size();

        for (Share s : shares) {

            if (s.isAccessOwner()) {
                /*
                The API doesn't allow the owner-share to be edited/deleted as it's technically
                not a share in the API-share-table. So we will not be sending any request from
                the owner to the API. But rather do a herd coding of the object.
                 */
                if (s.getState() == SyncState.DELETE) {
                    mDatabase.deleteShare(s, user);
                    SgnLog.v(TAG, "API doesn't allow owner to be 'deleted'. Deleting from own DB and ignoring.");
                } else if (s.getState() != SyncState.SYNCED) {
                    s.setState(SyncState.SYNCED);
                    s.setShoppinglistId(sl.getId());
                    mDatabase.editShare(s, user);
                    SgnLog.v(TAG, "Owner cannot be edited. Resetting share.state and ignoring.");
                }
                count--;
                continue;
            }

            switch (s.getState()) {

                case SyncState.TO_SYNC:
                    putShare(s, user);
                    break;

                case SyncState.DELETE:
                    delShare(s, user);
                    break;

                case SyncState.ERROR:
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

        s.setState(SyncState.SYNCING);
        mDatabase.editShare(s, user);

        Listener<JSONObject> shareListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {
                    Share serverShare = Share.fromJSON(response);
                    serverShare.setState(SyncState.SYNCED);
                    serverShare.setShoppinglistId(s.getShoppinglistId());
                    mDatabase.editShare(serverShare, user);
                    popRequest();
                } else {
                    if (error.getFailedOnField() != null) {

						/* If it's a FailedOnField, we can't do anything, yet.
						 * Remove the share to keep the DB sane.
						 */
                        mDatabase.deleteShare(s, user);
                        // No need to edit the SL in DB, as shares are disconnected
                        Shoppinglist sl = mDatabase.getList(s.getShoppinglistId(), user);
                        if (sl != null) {
                            sl.removeShare(s);
                            mBuilder.edit(sl);
                            popRequestAndPostShoppinglistEvent();
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

        String url = Endpoint.listShareEmail(user.getUserId(), s.getShoppinglistId(), s.getEmail());
        JsonObjectRequest shareReq = new JsonObjectRequest(Method.PUT, url, s.toJSON(), shareListener);
        addRequest(shareReq);

    }

    private void delShare(final Share s, final User user) {

        Listener<JSONObject> shareListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {

                    if (user.getEmail().equals(s.getEmail())) {

						/*
						 * if share.email == user.email, the user have been
						 * removed from the list, delete list, items, and shares
						 */
                        mDatabase.deleteList(s.getShoppinglistId(), user);
                        mDatabase.deleteItems(s.getShoppinglistId(), null, user);
                        mDatabase.deleteShares(s.getShoppinglistId(), user);

                    } else {

						/* Else just remove the share in question */
                        mDatabase.deleteShare(s, user);

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
                            mDatabase.deleteShare(s, user);
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

        if (s.getState() != SyncState.ERROR) {
            s.setState(SyncState.ERROR);
            mDatabase.editShare(s, user);
        }

        Listener<JSONObject> shareListener = new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {

                    Share serverShare = Share.fromJSON(response);
                    serverShare.setState(SyncState.SYNCED);
                    serverShare.setShoppinglistId(s.getShoppinglistId());
                    mDatabase.editShare(serverShare, user);

                    // No need to edit the SL in DB, as shares are disconnected
                    Shoppinglist sl = mDatabase.getList(s.getShoppinglistId(), user);
                    if (sl != null) {

                        sl.removeShare(s);
                        mBuilder.edit(sl);
                        popRequestAndPostShoppinglistEvent();

                    } else {

						/* Nothing, shoppinglist might have been deleted */
                        popRequest();

                    }

                } else {

                    mDatabase.deleteShare(s, user);
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
    private void popRequestAndPostShoppinglistEvent() {

        popRequest();
        if (mCurrentRequests.isEmpty() && !isPaused() && mBuilder.hasChanges()) {
            final ShoppinglistEvent e = mBuilder.build();
            mBuilder = new ShoppinglistEvent.Builder(true);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    SgnBus.getInstance().post(e);
                }
            });
        }

    }

    /** Supported sync speeds for {@link SyncManager} */
    public interface SyncSpeed {
        int SLOW = 10000;
        int MEDIUM = 6000;
        int FAST = 3000;
    }

    static class SyncLog {

        private static final boolean LOG_SYNC = false;
        private static final boolean LOG = false;

        public static int sync(String tag, String msg) {
            return (LOG_SYNC ? SgnLog.v(tag, msg) : 0);
        }

        public static int log(String tag, String msg) {
            return (LOG ? SgnLog.v(tag, msg) : 0);
        }

    }

}
