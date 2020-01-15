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

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.bus.SgnBus;
import com.shopgun.android.sdk.bus.ShoppinglistEvent;
import com.shopgun.android.sdk.corekit.LifecycleManager;
import com.shopgun.android.sdk.database.SgnDatabase;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.model.interfaces.SyncState;
import com.shopgun.android.sdk.network.Delivery;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestQueue;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.ShopGunError.Code;
import com.shopgun.android.sdk.network.impl.HandlerDelivery;
import com.shopgun.android.sdk.network.impl.JsonArrayRequest;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.ListUtils;
import com.shopgun.android.sdk.utils.PermissionUtils;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.ConnectivityUtils;

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
 * @deprecated No longer maintained
 *
 * The {@link SyncManager} class performs asynchronous synchronization with the
 * ShopGun API, to propagate all {@link Shoppinglist} and {@link ShoppinglistItem}
 * changes that a user may have done in the {@link SgnDatabase database}.
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
 * When {@link LifecycleManager} calls destroy, all local pending changes are pushed to
 * the API if possible to ensure a correct state on the server (and other devices).
 */
@SuppressWarnings("deprecation")
@Deprecated
public class SyncManager {

    public static final String TAG = Constants.getTag(SyncManager.class);

    private static final boolean SAVE_NETWORK_LOG = false;

    private final Object RESUME_LOCK = new Object();
    private final Stack<Request<?>> mCurrentRequests = new Stack<>();
    private int mSyncInterval = Integer.MIN_VALUE; // we'll cheat a bit here
    private SyncLooper mSyncLooper;
    private ShopGun mShopGun;
    private SgnDatabase mDatabase;
    /** The handler used to send messages to the sync thread */
    private Handler mHandler;
    /** Variable to determine if offline lists should automatically be synchronized if certain criteria are met. */
    private boolean mMigrateOfflineLists = false;
    /** A tag for identifying all requests originating from this {@link SyncManager} in the {@link RequestQueue} */
    private Object mRequestTag = new Object();
    /** The notification object, used to combine and collect notifications */
    private ShoppinglistEvent.Builder mBuilder = new ShoppinglistEvent.Builder(true);
    private Delivery mDelivery;

    /**
     * Default constructor for the {@link SyncManager}
     * @param shopGun An ShopGun instance
     * @param db A database
     */
    public SyncManager(ShopGun shopGun, SgnDatabase db) {
        mShopGun = shopGun;
        mShopGun.getLifecycleManager().registerCallback(new LifecycleCallback());
        mDatabase = db;
        mSyncLooper = new SyncLooper();
        // Create a new thread for a handler, so that i can later post content to that thread.
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mHandler = new Handler(thread.getLooper());
        mDelivery = new HandlerDelivery(mHandler);
    }

    private class LifecycleCallback extends LifecycleManager.SimpleCallback {

        @Override
        public void onCreate(Activity activity) {
            mDatabase.open();
            // Set a SyncInterval if user haven't set one yet, else just force a sync cycle
            int interval = mSyncInterval == Integer.MIN_VALUE ? SyncInterval.SLOW : mSyncInterval;
            setSyncInterval(interval);
        }

        @Override
        public void onDestroy(Activity activity) {
            mSyncLooper.forceSync();
            mDatabase.close();
        }

    }

    public void restartSyncLoop() {
        mSyncLooper.restart();
    }

    public boolean isPaused() {
        synchronized (RESUME_LOCK) {
            return getSyncInterval() == SyncInterval.PAUSED;
        }
    }

    /**
     * Method for determining if the first sync cycle to the API is done.
     * @return True if the first sync is complete, or there is no user to sync.
     */
    public boolean hasFirstSync() {
        return mSyncLooper.mSyncCount > 0;
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
        mSyncLooper.forceSync();
    }

    /**
     * Set synchronization interval for {@link Shoppinglist}, and {@link ShoppinglistItem}.
     * The speed must be a value defined in {@link SyncInterval}.
     * @param time A synchronization interval in milliseconds
     */
    public void setSyncInterval(int time) {
        if (time != SyncInterval.SLOW && time != SyncInterval.MEDIUM &&
                time != SyncInterval.FAST && time != SyncInterval.PAUSED) {
            throw new IllegalArgumentException("The sync time must be one of SyncManager.SyncInterval");
        }
        mSyncInterval = time;
        mSyncLooper.forceSync();
    }

    /**
     * Get the current sync interval
     * @return The sync interval in milliseconds
     */
    public int getSyncInterval() {
        return mSyncInterval == Integer.MIN_VALUE ? SyncInterval.PAUSED : mSyncInterval;
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
        r.setDebugger(new SyncDebugger(SyncDebugger.TAG)
                .setSkipMethods(Request.Method.GET));
        mShopGun.add(r);

//		if (!isPullReq(r) && r.getMethod() != Request.Method.GET) {
//			SgnLog.d(TAG, r.toString());
//		}

    }

    private void popRequest() {
        popRequestAndPostShoppinglistEvent();
    }

    /**
     * Pops one request off the request-stack, and sends out a notification if
     * the stack is empty (all requests are done, and all notifications are ready)
     */
    private void popRequestAndPostShoppinglistEvent() {

        synchronized (mCurrentRequests) {
            try {
                mCurrentRequests.pop();
            } catch (Exception e) {
                SgnLog.e(TAG, e.getMessage(), e);
            }
        }

        mBuilder.firstSync = mSyncLooper.mSyncCount == 1;
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

    private class SyncLooper implements Runnable {

        private int mSyncCount = 0;

        private void restart() {
            mSyncCount = 0;
            forceSync();
        }

        private void forceSync() {
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }

        @Override
        public void run() {

            int interval = getSyncInterval();
            if (interval == SyncInterval.PAUSED) {
                // Sync paused, quit the loop
                SyncLog.syncLooper(TAG, mSyncCount, "skip-loop-cycle (Paused)");
                return;
            }

            User user = mShopGun.getUser();
            // If it's an offline user, then stop sync loop
            // we'll keep listening for session changes and restart if needed
            if (user == null || !user.isLoggedIn()) {
                mSyncCount++;
                SyncLog.syncLooper(TAG, mSyncCount, "quit-loop-cycle (NotLoggedIn)");
                return;
            }

            // Prepare for next iteration if app is still resumed.
            // By not doing a return statement we allow for a final sync,
            // and sending local changes to server
            if (mShopGun.getLifecycleManager().isActive()) {
                mHandler.postDelayed(this, interval);
            }

            // Only do an update, if there are no pending transactions, and we are online
            if (!mCurrentRequests.isEmpty()) {
                SyncLog.syncLooper(TAG, mSyncCount, "skip-loop-cycle (ReqInFlight)");
                return;
            }
            if (!ConnectivityUtils.isOnline(ShopGun.getInstance().getContext())) {
                SyncLog.syncLooper(TAG, mSyncCount, "skip-loop-cycle (Offline)");
                return;
            }

            SgnDatabase database = mDatabase;
            // If there are local changes to a list, then syncLocalListChanges will handle it: return
            List<Shoppinglist> lists = database.getLists(user, true);
            if (syncLocalListChanges(database, lists, user)) {
                mSyncCount++;
                SyncLog.syncLooper(TAG, mSyncCount, "syncLocalListChanges");
                return;
            }

            // If there are changes to any items, then syncLocalItemChanges will handle it: return
            boolean hasLocalChanges = false;
            for (Shoppinglist sl : lists) {
                boolean itemChanges = syncLocalItemChanges(database, sl, user);
                boolean shareChanges = syncLocalShareChanges(database, sl, user);
//                if (itemChanges || shareChanges) {
//                    L.d(TAG, sl.getName() + "[ itemChanges: " + itemChanges + ", shareChanges: " + shareChanges + " ]");
//                }
                hasLocalChanges = itemChanges || shareChanges || hasLocalChanges;
            }

            // Skip further sync if we just posted our own changes
            if (hasLocalChanges) {
                mSyncCount++;
                SyncLog.syncLooper(TAG, mSyncCount, "hasLocalChanges");
                return;
            }

            // Finally ready to get server changes
            if (mSyncCount % 3 == 0) {

                // Get a new set of lists
                SyncLog.syncLooper(TAG, mSyncCount, "syncAllLists");
                addRequest(new ListSyncRequest(database, user, Integer.MAX_VALUE));

            } else if (mSyncCount % 10 == 0) {

                SyncLog.syncLooper(TAG, mSyncCount, "syncAllItems");
                // Because we update modified on lists, on all changes, we might
                // have a situation where two devices have set the same modified
                // on a list, and therefore won't try to get a new list of items
                // So we force the SDK to get a new set once in a while
                List<Shoppinglist> localLists = database.getLists(user);
                for (Shoppinglist sl : localLists) {
                    addRequest(new ItemSyncRequest(database, sl, user));
                }

            } else {

                SyncLog.syncLooper(TAG, mSyncCount, "checkModified");
                // Base case, just check if there is changes
                syncListsModifiedTimestamp(database, user);

            }

            mSyncCount++;

        }
    }

    private class ListSyncRequest extends JsonArrayRequest {

        private ListSyncRequest(SgnDatabase database, User user, int syncCount) {
            super(Endpoints.lists(user.getId()), new ListSyncListener(database, user, syncCount));
            // Offset and limit are set to default values, we want to ignore this.
            getParameters().remove(Parameters.OFFSET);
            getParameters().remove(Parameters.LIMIT);
            setSaveNetworkLog(SAVE_NETWORK_LOG);
        }
    }

    private void syncListsModifiedTimestamp(SgnDatabase database, User user) {
        List<Shoppinglist> lists = database.getLists(user);
        for (Iterator<Shoppinglist> it = lists.iterator(); it.hasNext();) {
            Shoppinglist sl = it.next();
            // If they are in the state of processing, then skip
            if (sl.getState() == SyncState.SYNCING) {
                it.remove();
            }
            // If state has changed locally, then sync then items
            if (sl.getState() == SyncState.TO_SYNC) {
                addRequest(new ItemSyncRequest(database, sl, user));
                it.remove();
            }
            // Run the check
            sl.setState(SyncState.SYNCING);
        }
        database.insertLists(lists, user);
        for (Shoppinglist sl : lists) {
            addRequest(new ListModifiedRequest(database, user, sl));
        }
    }

    private boolean syncLocalListChanges(SgnDatabase database, List<Shoppinglist> lists, User user) {
        int count = lists.size();
        for (Shoppinglist sl : lists) {
            switch (sl.getState()) {
                case SyncState.TO_SYNC: addRequest(new ListPutRequest(database, user, sl)); break;
                case SyncState.DELETE: addRequest(new ListDelRequest(database, user, sl)); break;
                case SyncState.ERROR: addRequest(new ListRevertRequest(database, user, sl)); break;
                default: count--; break;
            }
        }
        return count != 0;
    }

    private boolean syncLocalItemChanges(SgnDatabase database, Shoppinglist sl, User user) {
        List<ShoppinglistItem> items = database.getItems(sl, user, true);
        int count = items.size();
        for (ShoppinglistItem item : items) {
            switch (item.getState()) {
                case SyncState.TO_SYNC: addRequest(new ItemPutRequest(database, user, item)); break;
                case SyncState.DELETE: addRequest(new ItemDelRequest(database, user, item)); break;
                case SyncState.ERROR: addRequest(new ItemRevertRequest(database, user, item)); break;
                default: count--; break;
            }
        }
        return count != 0;
    }

    private boolean syncLocalShareChanges(SgnDatabase database, Shoppinglist sl, User user) {
        List<Share> shares = database.getShares(sl, user, true);
        int count = shares.size();
        for (Share s : shares) {
            if (s.isAccessOwner()) {
                // The API doesn't allow the owner-share to be edited/deleted as it's technically
                // not a share in the API-share-table. So we will not be sending any request from
                // the owner to the API. But rather do a herd coding of the object.
                if (s.getState() == SyncState.DELETE) {
                    database.deleteShare(s, user);
                    SgnLog.v(TAG, "API doesn't allow owner to be 'deleted'. Deleting from own DB and ignoring.");
                } else if (s.getState() != SyncState.SYNCED) {
                    s.setState(SyncState.SYNCED);
                    s.setShoppinglistId(sl.getId());
                    database.editShare(s, user);
                    SgnLog.v(TAG, "Owner cannot be edited. Resetting share.state and ignoring.");
                }
                count--;
                continue;
            }

            switch (s.getState()) {
                case SyncState.TO_SYNC: addRequest(new SharePutRequest(database, user, s)); break;
                case SyncState.DELETE: addRequest(new ShareDelRequest(database, user, s)); break;
                case SyncState.ERROR: addRequest(new ShareRevertRequest(database, user, s)); break;
                default: count--; break;
            }
        }
        return count != 0;
    }

    private class ListSyncListener extends ListArrayListener {

        private ListSyncListener(SgnDatabase database, User user, int syncCount) {
            super(database, user, null);
        }

        @Override
        public void onSuccess(List<Shoppinglist> serverLists) {

            // Get ALL lists including deleted, to avoid adding them again
            List<Shoppinglist> localLists = mDatabase.getLists(mUser, true);

            // Server usually returns items in the order oldest to newest (not guaranteed)
            // We want them to be reversed
            Collections.reverse(serverLists);

            // Set the state of all shares in the serverList to SYNCED,
            // before inserting into the DB, as this is actually correct
            for (Shoppinglist sl : serverLists) {
                for (Share share : sl.getShares().values()) {
                    share.setState(SyncState.SYNCED);
                }
            }

            mergeListsToDbAndFetchItems(mDatabase, serverLists, localLists, mUser);

            popRequestAndPostShoppinglistEvent();
//            mSyncController.decrementAndPost();

        }

        @Override
        public void onError(ShopGunError error) {
            popRequest();
        }

    }

    private void mergeListsToDbAndFetchItems(SgnDatabase database, List<Shoppinglist> serverList, List<Shoppinglist> localList, User user) {

        if (serverList.isEmpty() && localList.isEmpty()) {
            return;
        }

        HashMap<String, Shoppinglist> localMap = new HashMap<>();
        HashMap<String, Shoppinglist> serverMap = new HashMap<>();
        HashSet<String> union = new HashSet<>();

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
                        database.editList(serverSl, user);
                        database.cleanShares(serverSl, user);
                    }
                    // else: Don't do anything, next iteration will put local changes to API

                } else {
                    mBuilder.del(localSl);
                    for (ShoppinglistItem sli : database.getItems(localSl, user)) {
                        mBuilder.del(sli);
                    }
                    database.deleteItems(localSl.getId(), null, user);
                    database.deleteList(localSl, user);
                }

            } else {

                Shoppinglist add = serverMap.get(key);
                add.setState(SyncState.TO_SYNC);
                mBuilder.add(add);
                database.insertList(add, user);

            }

        }

        for (Shoppinglist sl : mBuilder.getAddedLists()) {
            addRequest(new ItemSyncRequest(database, sl, user));
        }

        for (Shoppinglist sl : mBuilder.getEditedLists()) {
            addRequest(new ItemSyncRequest(database, sl, user));
        }

    }

    private class ListModifiedRequest extends JsonObjectRequest {
        private ListModifiedRequest(SgnDatabase database, User user, Shoppinglist shoppinglist) {
            super(Endpoints.listModified(user.getId(), shoppinglist.getId()),
                    new ListModifiedListener(database, user, shoppinglist));
            setSaveNetworkLog(SAVE_NETWORK_LOG);
        }
    }

    private class ListModifiedListener extends JSONObjectListener<JSONObject> {

        Shoppinglist mShoppinglist;

        private ListModifiedListener(SgnDatabase database, User user, Shoppinglist shoppinglist) {
            super(database, user, null);
            mShoppinglist = shoppinglist;
        }

        @Override
        public void onSuccess(JSONObject response) {
            mShoppinglist.setState(SyncState.SYNCED);
            try {
                String modifiedString = response.getString(SgnJson.MODIFIED);
                Date modified = SgnUtils.stringToDate(modifiedString);
                // If local list has been modified before the server list, then sync items
                if (mShoppinglist.getModified().before(modified)) {
                    // If there are changes, update items (this will update list-state in DB)
                    addRequest(new ItemSyncRequest(mDatabase, mShoppinglist, mUser));
                } else {
                    // if no changes, just write new state to DB
                    mDatabase.editList(mShoppinglist, mUser);
                }
            } catch (JSONException e) {
                SgnLog.e(TAG, e.getMessage(), e);
                // error? just write new state to DB, next iteration will fix it
                mDatabase.editList(mShoppinglist, mUser);
            }
            popRequestAndPostShoppinglistEvent();
        }

        @Override
        public void onError(ShopGunError error) {
            popRequest();
            addRequest(new ListPutRequest(mDatabase, mUser, mShoppinglist));
        }

        @Override
        public void onComplete(JSONObject response, ShopGunError error) {
            if (response != null) {
                onSuccess(response);
            } else {
                onError(error);
            }
        }

    }

    private class ItemSyncRequest extends JsonArrayRequest {

        private ItemSyncRequest(SgnDatabase database, Shoppinglist shoppinglist, User user) {
            super(Endpoints.listitems(user.getId(), shoppinglist.getId()), new ItemSyncListener(database, shoppinglist, user));
            // Offset and limit are set to default values, we want to ignore this.
            getParameters().remove(Parameters.OFFSET);
            getParameters().remove(Parameters.LIMIT);
            setSaveNetworkLog(SAVE_NETWORK_LOG);
            // update database state for the list
            shoppinglist.setState(SyncState.SYNCING);
            database.editList(shoppinglist, user);
        }

    }

    private class ItemSyncListener implements Listener<JSONArray> {

        private SgnDatabase mDatabase;
        private User mUser;
        private Shoppinglist mShoppinglist;

        private ItemSyncListener(SgnDatabase database, Shoppinglist shoppinglist, User user) {
            mDatabase = database;
            mShoppinglist = shoppinglist;
            mUser = user;
        }

        public void onComplete(JSONArray response, ShopGunError error) {

            if (response == null) {
                popRequest();
                addRequest(new ListPutRequest(mDatabase, mUser, mShoppinglist));
                return;
            }

            mShoppinglist.setState(SyncState.SYNCED);
            mDatabase.editList(mShoppinglist, mUser);

            // Get ALL items including deleted, to avoid adding them again
            List<ShoppinglistItem> localItems = mDatabase.getItems(mShoppinglist, mUser, true);
            List<ShoppinglistItem> serverItems = ShoppinglistItem.fromJSON(response);

            // So far, we get items in reverse order, well just keep reversing it for now.
            Collections.reverse(serverItems);

            for (ShoppinglistItem sli : serverItems) {
                sli.setState(SyncState.SYNCED);
            }

            mergeItemsToDb(mDatabase, serverItems, localItems, mUser);

            // fetch updated items from DB, as the state might be a bit whack after the merging of items
            localItems = mDatabase.getItems(mShoppinglist, mUser);
            ListUtils.sortItems(localItems);

            if (PermissionUtils.allowEdit(mShoppinglist, mUser)) {

                // Update previous_id's, modified and state if needed
                String tmp = ListUtils.FIRST_ITEM;
                for (ShoppinglistItem sli : localItems) {

                    if (!tmp.equals(sli.getPreviousId())) {
                        sli.setPreviousId(tmp);
                        sli.setModified(new Date());
                        sli.setState(SyncState.TO_SYNC);

                        // If it's a new item, it's already in the added list, then we'll override it
                        // else add it to the edited as a new item to the edited list
                        if (mBuilder.items.containsKey(sli.getId())) {
                            mBuilder.add(sli);
                        } else {
                            mBuilder.edit(sli);
                        }

                        mDatabase.editItems(sli, mUser);
                    }
                    tmp = sli.getId();
                }
            }

            popRequestAndPostShoppinglistEvent();

        }
    }

    private void mergeItemsToDb(SgnDatabase database, List<ShoppinglistItem> serverItems, List<ShoppinglistItem> localItems, User user) {

        if (serverItems.isEmpty() && localItems.isEmpty()) {
            return;
        }

        HashMap<String, ShoppinglistItem> localMap = new HashMap<>();
        HashMap<String, ShoppinglistItem> serverMap = new HashMap<>();
        HashSet<String> union = new HashSet<>();

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
                        database.editItems(serverSli, user);

                    } else if (!localSli.getMeta().toString().equals(serverSli.getMeta().toString())) {
                        // Migration code, to get comments into the DB
                        mBuilder.edit(serverSli);
                        database.editItems(serverSli, user);
                    } else if (localSli.equals(serverSli)) {
                        SgnLog.d(TAG, "We have a mismatch");
                    }

                } else {
                    ShoppinglistItem delSli = localMap.get(key);
                    if (delSli.getState() != SyncState.TO_SYNC) {
                        // If the item have been added while request was in flight it will
                        // have the state TO_SYNC, and will just ignore it for now
                        mBuilder.del(delSli);
                        database.deleteItem(delSli, user);
                    }
                }

            } else {
                ShoppinglistItem serverSli = serverMap.get(key);
                mBuilder.add(serverSli);
                database.insertItem(serverSli, user);
            }
        }

    }

    private class ListPutListener extends ListObjectListener {

        private ListPutListener(SgnDatabase database, User user, Shoppinglist shoppinglist) {
            super(database, user, shoppinglist);
        }

        @Override
        public void onError(ShopGunError error) {
            popRequest();

            switch (error.getCode()) {

                case Code.INVALID_RESOURCE_ID:
                    // maybe deleted from another device - delete and ignore
                    mDatabase.deleteList(mLocalCopy, mUser);
                    break;

                case Code.NETWORK_ERROR:
                    // Ignore missing network, wait for next iteration
                    break;

                default:
                    addRequest(new ListRevertRequest(mDatabase, mUser, mLocalCopy));
                    break;

            }
        }

        @Override
        public void onSuccess(Shoppinglist response) {
            // If local isn't equal to server version, take server version.
            // Don't push changes yet, we want to check item state too.
            Shoppinglist localSl = mDatabase.getList(response.getId(), mUser);
            if (localSl != null && !response.getModified().equals(localSl.getModified())) {
                response.setState(SyncState.SYNCED);
                // If server haven't delivered an prev_id, then use old id
                response.setPreviousId(response.getPreviousId() == null ? mLocalCopy.getPreviousId() : response.getPreviousId());
                mDatabase.editList(response, mUser);
                mBuilder.edit(response);
            }
            popRequest();
            syncLocalItemChanges(mDatabase, mLocalCopy, mUser);
        }

    }

    private class ListPutRequest extends JsonObjectRequest {

        private ListPutRequest(SgnDatabase database, User user, Shoppinglist shoppinglist) {
            super(Method.PUT, Endpoints.list(user.getId(), shoppinglist.getId()),
                    shoppinglist.toJSON(), new ListPutListener(database, user, shoppinglist));
            shoppinglist.setState(SyncState.SYNCING);
            database.editList(shoppinglist, user);
        }

    }

    private class ListDelRequest extends JsonObjectRequest {

        private ListDelRequest(SgnDatabase database, User user, Shoppinglist local) {
            super(Method.DELETE, Endpoints.list(user.getId(), local.getId()),
                    null, new ListDelListener(database, user, local));
            getParameters().put(Parameters.MODIFIED, SgnUtils.dateToString(local.getModified()));
        }
    }

    private class ListDelListener extends ListObjectListener {

        private ListDelListener(SgnDatabase database, User user, Shoppinglist local) {
            super(database, user, local);
        }

        @Override
        public void onComplete(JSONObject response, ShopGunError error) {
            if (response != null) {
                onSuccess(null);
            } else {
                onError(error);
            }
        }

        @Override
        public void onSuccess(Shoppinglist response) {
            mDatabase.deleteList(mLocalCopy, mUser);
            mDatabase.deleteShares(mLocalCopy, mUser);
            mDatabase.deleteItems(mLocalCopy.getId(), null, mUser);
            popRequest();
        }

        @Override
        public void onError(ShopGunError error) {
            popRequest();

            switch (error.getCode()) {

                case Code.INVALID_RESOURCE_ID:
                    // Resource already gone (or have never been synchronized) delete local version and ignore
                    mDatabase.deleteList(mLocalCopy, mUser);
                    break;

                case Code.NETWORK_ERROR:
                    // Ignore missing network, wait for next iteration
                    break;

                default:
                    addRequest(new ListRevertRequest(mDatabase, mUser, mLocalCopy));
                    break;

            }

        }
    }

    private class ListRevertRequest extends JsonObjectRequest {

        private ListRevertRequest(SgnDatabase database, User user, Shoppinglist shoppinglist) {
            super(Endpoints.list(user.getId(), shoppinglist.getId()),
                    new ListRevertListener(database, user, shoppinglist));
            if (shoppinglist.getState() != SyncState.ERROR) {
                shoppinglist.setState(SyncState.ERROR);
                database.editList(shoppinglist, user);
            }
        }
    }

    private class ListRevertListener extends ListObjectListener {

        private ListRevertListener(SgnDatabase database, User user, Shoppinglist local) {
            super(database, user, local);
        }

        public void onComplete(JSONObject response, ShopGunError error) {
            super.onComplete(response, error);
            popRequestAndPostShoppinglistEvent();
        }

        @Override
        public void onSuccess(Shoppinglist response) {
            response.setState(SyncState.SYNCED);
            response.setPreviousId(response.getPreviousId() == null ?
                    mLocalCopy.getPreviousId() : response.getPreviousId());
            mDatabase.editList(response, mUser);
            mBuilder.add(response);
            syncLocalItemChanges(mDatabase, mLocalCopy, mUser);
        }

        @Override
        public void onError(ShopGunError error) {
            if (error.getCode() != Code.NETWORK_ERROR) {
                // Only network errors are allowed here
                mDatabase.deleteList(mLocalCopy, mUser);
                mBuilder.del(mLocalCopy);
            }
        }
    }

    private class ItemPutRequest extends JsonObjectRequest {

        private ItemPutRequest(SgnDatabase database, User user, ShoppinglistItem item) {
            super(Method.PUT, Endpoints.listitem(user.getId(), item.getShoppinglistId(), item.getId()),
                    item.toJSON(), new ItemPutListener(database, user, item));
            item.setState(SyncState.SYNCING);
            database.editItems(item, user);
        }
    }

    private class ItemPutListener extends ItemListener {

        private ItemPutListener(SgnDatabase database, User user, ShoppinglistItem item) {
            super(database, user, item);
        }

        @Override
        public void onSuccess(ShoppinglistItem response) {

            ShoppinglistItem local = mDatabase.getItem(mLocalCopy.getId(), mUser);

            if (local != null && !local.getModified().equals(response.getModified())) {

                // The server has a 'better' state, we should use this
                response.setState(SyncState.SYNCED);
                // If server haven't delivered an prev_id, then use old id
                if (response.getPreviousId() == null) {
                    response.setPreviousId(mLocalCopy.getPreviousId());
                }
                mDatabase.editItems(response, mUser);
                mBuilder.edit(response);

            }

            popRequestAndPostShoppinglistEvent();

        }

        @Override
        public void onError(ShopGunError error) {

            popRequest();
            if (error.getCode() != Code.NETWORK_ERROR) {
                // Only ignore missing network, wait for next iteration
                addRequest(new ItemRevertRequest(mDatabase, mUser, mLocalCopy));
            }

        }

    }

    private class ItemDelRequest extends JsonObjectRequest {

        private ItemDelRequest(SgnDatabase database, User user, ShoppinglistItem item) {
            super(Method.DELETE, Endpoints.listitem(user.getId(), item.getShoppinglistId(), item.getId()),
                    null, new ItemDelListener(database, user, item));
            getParameters().put(Parameters.MODIFIED, SgnUtils.dateToString(item.getModified()));
        }
    }

    private class ItemDelListener extends ItemListener {

        private ItemDelListener(SgnDatabase database, User user, ShoppinglistItem item) {
            super(database, user, item);
        }

        @Override
        public void onSuccess(ShoppinglistItem response) {
            mDatabase.deleteItem(mLocalCopy, mUser);
            popRequest();
        }

        @Override
        public void onError(ShopGunError error) {
            popRequest();
            switch (error.getCode()) {

                case Code.INVALID_RESOURCE_ID:
                    // Resource already gone (or have never been synchronized) delete local version and ignore
                    mDatabase.deleteItem(mLocalCopy, mUser);
                    break;

                case Code.NETWORK_ERROR:
                    // Ignore missing network, wait for next iteration
                    break;

                default:
                    addRequest(new ItemRevertRequest(mDatabase, mUser, mLocalCopy));
                    break;

            }
        }

    }

    private class ItemRevertRequest extends JsonObjectRequest {

        private ItemRevertRequest(SgnDatabase database, User user, ShoppinglistItem item) {
            super(Endpoints.listitem(user.getId(), item.getShoppinglistId(), item.getId()),
                    new ItemRevertListener(database, user, item));
            if (item.getState() != SyncState.ERROR) {
                item.setState(SyncState.ERROR);
                database.editItems(item, user);
            }
        }
    }

    private class ItemRevertListener extends ItemListener {

        private ItemRevertListener(SgnDatabase database, User user, ShoppinglistItem item) {
            super(database, user, item);
        }

        @Override
        public void onSuccess(ShoppinglistItem response) {
            response.setState(SyncState.SYNCED);
            response.setPreviousId(response.getPreviousId() == null ? mLocalCopy.getPreviousId() : response.getPreviousId());
            mBuilder.edit(response);
        }

        @Override
        public void onError(ShopGunError error) {
            // Something bad happened, delete item to keep DB sane
            mDatabase.deleteItem(mLocalCopy, mUser);
            mBuilder.del(mLocalCopy);
        }

        public void onComplete(JSONObject response, ShopGunError error) {
            super.onComplete(response, error);

            // Update shopping list modified to match the latest date of the
            // items, so that we get as close to API state as possible
            Shoppinglist sl = mDatabase.getList(mLocalCopy.getShoppinglistId(), mUser);
            if (sl != null) {
                List<ShoppinglistItem> items = mDatabase.getItems(sl, mUser, true);
                if (!items.isEmpty()) {
                    Collections.sort(items, ShoppinglistItem.MODIFIED_DESCENDING);
                    ShoppinglistItem newestItem = items.get(0);
                    sl.setModified(newestItem.getModified());
                    mDatabase.editList(sl, mUser);
                    mBuilder.edit(sl);
                }
            }
            popRequestAndPostShoppinglistEvent();
        }
    }

    private class SharePutRequest extends JsonObjectRequest {
        private SharePutRequest(SgnDatabase database, User user, Share share) {
            super(Method.PUT, Endpoints.listShareEmail(user.getId(), share.getShoppinglistId(), share.getEmail()),
                    share.toJSON(), new SharePutListener(database, user, share));
            share.setState(SyncState.SYNCING);
            database.editShare(share, user);
        }
    }

    private class SharePutListener extends ShareListener {

        private SharePutListener(SgnDatabase database, User user, Share share) {
            super(database, user, share);
        }

        @Override
        public void onSuccess(Share response) {
            response.setState(SyncState.SYNCED);
            response.setShoppinglistId(mLocalCopy.getShoppinglistId());
            mDatabase.editShare(response, mUser);
            popRequest();
        }

        @Override
        public void onError(ShopGunError error) {
            if (error.getFailedOnField() != null) {
                // If it's a FailedOnField, we can't do anything, yet. Remove the share to keep the DB sane.
                mDatabase.deleteShare(mLocalCopy, mUser);
                // No need to edit the SL in DB, as shares are disconnected
                Shoppinglist sl = mDatabase.getList(mLocalCopy.getShoppinglistId(), mUser);
                if (sl != null) {
                    sl.removeShare(mLocalCopy);
                    mBuilder.edit(sl);
                    popRequestAndPostShoppinglistEvent();
                } else {
                    popRequest();
                    // Nothing, shoppinglist might have been deleted
                }

            } else {
                popRequest();
                addRequest(new ShareRevertRequest(mDatabase, mUser, mLocalCopy));
            }
        }
    }

    private class ShareDelRequest extends JsonObjectRequest {

        private ShareDelRequest(SgnDatabase database, User user, Share local) {
            super(Method.DELETE, Endpoints.listShareEmail(user.getId(), local.getShoppinglistId(), local.getEmail()),
                    null, new ShareDelListener(database, user, local));
        }
    }

    private class ShareDelListener extends ShareListener {

        private ShareDelListener(SgnDatabase database, User user, Share local) {
            super(database, user, local);
        }

        @Override
        public void onSuccess(Share response) {

            if (mUser.getEmail().equals(mLocalCopy.getEmail())) {
                // if share.email == user.email, the user have been
                // removed from the list, delete list, items, and shares
                mDatabase.deleteList(mLocalCopy.getShoppinglistId(), mUser);
                mDatabase.deleteItems(mLocalCopy.getShoppinglistId(), null, mUser);
                mDatabase.deleteShares(mLocalCopy.getShoppinglistId(), mUser);

            } else {
                // Else just remove the share in question
                mDatabase.deleteShare(mLocalCopy, mUser);
            }
            popRequest();

        }

        @Override
        public void onError(ShopGunError error) {

            popRequest();

            switch (error.getCode()) {

                case Code.NETWORK_ERROR:
                    // Ignore missing network, wait for next iteration
                    break;

                case Code.INVALID_RESOURCE_ID:
                    // Resource gone (or have never been synchronized) delete local version and ignore
                    mDatabase.deleteShare(mLocalCopy, mUser);
                    break;

                default:
                    addRequest(new ShareRevertRequest(mDatabase, mUser, mLocalCopy));
                    break;

            }

        }
    }

    private class ShareRevertRequest extends JsonObjectRequest {

        private ShareRevertRequest(SgnDatabase database, User user, Share share) {
            super(Endpoints.listShareEmail(user.getId(), share.getShoppinglistId(), share.getEmail()),
                    new ShareRevertListener(database, user, share));
            if (share.getState() != SyncState.ERROR) {
                share.setState(SyncState.ERROR);
                database.editShare(share, user);
            }
        }

    }

    private class ShareRevertListener extends ShareListener {

        private ShareRevertListener(SgnDatabase database, User user, Share local) {
            super(database, user, local);
        }

        @Override
        public void onSuccess(Share response) {
            response.setState(SyncState.SYNCED);
            response.setShoppinglistId(mLocalCopy.getShoppinglistId());
            mDatabase.editShare(response, mUser);

            // No need to edit the SL in DB, as shares are disconnected
            Shoppinglist sl = mDatabase.getList(response.getShoppinglistId(), mUser);
            if (sl != null) {

                sl.removeShare(response);
                mBuilder.edit(sl);
                popRequestAndPostShoppinglistEvent();

            } else {
                // Nothing, shoppinglist might have been deleted
                popRequest();
            }

        }

        @Override
        public void onError(ShopGunError error) {
            mDatabase.deleteShare(mLocalCopy, mUser);
            popRequest();
        }
    }

}
