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

import android.annotation.SuppressLint;
import android.app.Activity;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.bus.SgnBus;
import com.shopgun.android.sdk.bus.ShoppinglistEvent;
import com.shopgun.android.sdk.corekit.LifecycleManager;
import com.shopgun.android.sdk.database.DatabaseWrapper;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.model.interfaces.SyncState;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.ListUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides methods, for easily handling of
 * {@link Shoppinglist Shoppinglists}, {@link ShoppinglistItem ShoppinglistItems},
 * and {@link Share Shares}, without having to worry about keeping a sane, and
 * synchronizing state with both the {@link DatabaseWrapper database} and, the
 * ShopGun API.
 */
public class ListManager {

    public static final String TAG = Constants.getTag(ListManager.class);

    /** The global {@link ShopGun} object */
    private ShopGun mShopGun;
    private DatabaseWrapper mDatabase;

    /** The notification service for ListManager, this allows for bundling
     * list and item notifications, to avoid multiple updates for a single operation */
    private ShoppinglistEvent.Builder mBuilder = new ShoppinglistEvent.Builder(false);

    /**
     * Default constructor for ListManager.
     * @param shopGun The {@link ShopGun} instance to use
     * @param db A database
     */
    public ListManager(ShopGun shopGun, DatabaseWrapper db) {
        mShopGun = shopGun;
        mDatabase = db;
        mShopGun.getLifecycleManager().registerCallback(new LifecycleManager.SimpleCallback() {

            @Override
            public void onCreate(Activity activity) {
                mDatabase.open();
            }

            @Override
            public void onDestroy(Activity activity) {
                mDatabase.close();
            }
        });
    }

    /**
     * Get a {@link Shoppinglist} from it's ID.
     * @param id A {@link Shoppinglist} id
     * @return A shopping list, or {@code null}
     */
    public Shoppinglist getList(String id) {
        return mDatabase.getList(id, user());
    }

    /**
     * The complete set of {@link Shoppinglist Shoppinglists}, that the current
     * user has.
     * @return A {@link List} of {@link Shoppinglist}, for current {@link User}
     */
    public List<Shoppinglist> getLists() {
        return mDatabase.getLists(user());
    }

    /**
     * The complete set of {@link Shoppinglist Shoppinglists}, that a given {@link User} has
     * @param user A {@link User}
     * @return A {@link List} of {@link Shoppinglist}, for the given {@link User}
     */
    public List<Shoppinglist> getLists(User user) {
        return mDatabase.getLists(user);
    }

    /**
     * Add a new {@link Shoppinglist} to the current {@link User}
     *
     * <p>If owner haven't been set, we will assume that it is the current
     * {@link User} being used by the SDK, is the owner.</p>
     *
     * <p>Changes are synchronized to the API when and if possible.<br>
     *
     * @param sl A shoppinglist to add to the database
     * @return {@code true} if the action was performed, else {@code false}
     */
    public boolean addList(final Shoppinglist sl) {

        List<Shoppinglist> lists = new ArrayList<Shoppinglist>();
        lists.add(sl);

        sl.setModified(new Date());

        final User user = user();

        Share owner = sl.getOwner();
        if (owner == null || owner.getEmail() == null) {
            owner = new Share(user.getEmail(), Share.ACCESS_OWNER, null);
            owner.setName(user.getName());
            owner.setAccepted(true);
            owner.setShoppinglistId(sl.getId());
            sl.putShare(owner);
        }

        sl.setPreviousId(ListUtils.FIRST_ITEM);
        sl.setState(SyncState.TO_SYNC);

        Shoppinglist first = mDatabase.getFirstList(user);
        if (first != null) {
            first.setPreviousId(sl.getId());
            first.setModified(new Date());
            first.setState(SyncState.TO_SYNC);
            lists.add(first);
        }

        boolean success = mDatabase.insertLists(lists, user);
        if (success) {
            mBuilder.add(sl);
        }
        postShoppinglistEvent();
        return success;
    }

    /**
     * Edit a shopping list already in the database.
     *
     * <p>The {@link Shoppinglist} will replace data already in the
     * {@link DatabaseWrapper database}, and changes will later be synchronized to the
     * API if possible.</p>
     * @param sl A shoppinglist that have been edited
     * @return {@code true} if the action was performed, else {@code false}
     */
    public boolean editList(Shoppinglist sl) {
        return editList(sl, user());
    }

    private boolean editList(Shoppinglist sl, User user) {

        Shoppinglist original = mDatabase.getList(sl.getId(), user);
        // Check for changes in previous item, and update surrounding
        if (original == null) {
            SgnLog.i(TAG, "No such list exists in the database. To add new items, use addList().");
            return false;
        }

        Map<String, Share> dbShares = original.getShares();
        Map<String, Share> slShares = sl.getShares();

		/* User have remove it self. Then only set the DELETE state on the share,
		 * SyncManager will delete from DB Once it's synced the changes to API
		 */
        if (!slShares.containsKey(user.getEmail())) {
            Share dbShare = dbShares.get(user.getEmail());
            if (dbShare != null) {
                dbShare.setState(SyncState.DELETE);
                mDatabase.editShare(dbShare, user);
                mBuilder.del(sl);
                postShoppinglistEvent();
                return true;
            } else {
                // The user isn't in either the Shoppinglist shares,
                // or our list of shares in DB
                return false;
            }
        }

        // Do edit check after the share check, the user should always be allowed to remove it self
        mDatabase.allowEditOrThrow(sl, user);

        HashSet<String> union = new HashSet<String>();
        union.addAll(slShares.keySet());
        union.addAll(dbShares.keySet());

        for (String shareId : union) {

            if (dbShares.containsKey(shareId)) {

                Share dbShare = dbShares.get(shareId);

                if (slShares.containsKey(shareId)) {

                    Share slShare = slShares.get(shareId);
                    if (!dbShare.equals(slShare)) {
                        slShare.setState(SyncState.TO_SYNC);
                        // mDatabase.editShare(slShare, user);
                    }

                } else if (dbShare.getAccess().equals(Share.ACCESS_OWNER)) {
                    // If owner was removed, then re-insert it.
                    sl.putShare(dbShare);
                    SgnLog.i(TAG, "Owner cannot be removed from lists, owner will be reattached");
                } else if (user.isLoggedIn()) {
                    // We'll have to add the share (in deleted state) to have it updated in the DB
                    // it should be removed as soon as the list have been inserted to DB.
                    dbShare.setState(SyncState.DELETE);
                    //mDatabase.editShare(dbShare, user);
                    sl.putShare(dbShare);
                }

            }

        }

        List<Shoppinglist> lists = new ArrayList<Shoppinglist>();
        lists.add(sl);

        Date now = new Date();
        sl.setModified(now);
        sl.setState(SyncState.TO_SYNC);

        if (original.getPreviousId() != null && !original.getPreviousId().equals(sl.getPreviousId())) {
            // lucky for us, this shouldn't happen too often, so the double query to DB isn't gonna kill us

            // If there is an item pointing at sl, it needs to point at the oldList.prev
            Shoppinglist slAfter = mDatabase.getListPrevious(sl.getId(), user);
            if (slAfter != null) {
                slAfter.setPreviousId(original.getPreviousId());
                slAfter.setModified(now);
                slAfter.setState(SyncState.TO_SYNC);
                lists.add(slAfter);
            }

            // If some another sl was pointing at the same item, it should be pointing at sl
            Shoppinglist slSamePointer = mDatabase.getListPrevious(sl.getPreviousId(), user);
            if (slSamePointer != null) {
                slSamePointer.setPreviousId(sl.getId());
                slSamePointer.setModified(now);
                slSamePointer.setState(SyncState.TO_SYNC);
                lists.add(slSamePointer);
            }

        }

        try {
            boolean success = mDatabase.insertLists(lists, user);
            if (success) {
                for (Shoppinglist edited : lists) {
                    mBuilder.edit(edited);
                }
            }

            // Clean up the shares we have added to have their state updated in the DB
            for (Iterator<Map.Entry<String, Share>> it = sl.getShares().entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Share> entry = it.next();
                if (entry.getValue().getState() == SyncState.DELETE) {
                    it.remove();
                }
            }

            return success;
        } finally {
            postShoppinglistEvent();
        }
    }

    /**
     * Delete a shopping list
     * <p>The {@link Shoppinglist shoppinglist} is deleted from the local database,
     * and changes are later synchronized to the server, when and if possible.</p>
     * <p>All {@link ShoppinglistItem shoppinglistitems} associated with the
     * {@link Shoppinglist shoppinglist} are also deleted.</p>
     * @param sl A shoppinglist to delete
     */
    public void deleteList(Shoppinglist sl) {
        User u = user();
        mDatabase.allowEditOrThrow(sl, u);
        deleteList(sl, u);
    }

    private boolean deleteList(Shoppinglist sl, User user) {

        List<Shoppinglist> editedLists = new ArrayList<Shoppinglist>();
        Date now = new Date();
        sl.setModified(now);
        editedLists.add(sl);

        // Update previous pointer, to preserve order
        Shoppinglist after = mDatabase.getListPrevious(sl.getId(), user);
        if (after != null) {
            after.setPreviousId(sl.getPreviousId());
            after.setModified(now);
            after.setState(SyncState.TO_SYNC);
            editedLists.add(after);
            mBuilder.edit(after);
        }

        // Read items, edit items, and re-insert items (a bit redundant, but we need them for the callback)
        List<ShoppinglistItem> items = getItems(sl, user);
        for (ShoppinglistItem sli : items) {
            sli.setState(SyncState.DELETE);
            sli.setModified(now);
        }
        boolean success = mDatabase.editItems(items, user);
        if (success) {
            for (ShoppinglistItem sli : items) {
                mBuilder.del(sli);
            }
        }

        // Update local version of shoppinglist
        sl.setState(SyncState.DELETE);
        success = mDatabase.insertLists(editedLists, user);
        if (success) {
            for (Shoppinglist s : editedLists) {
                mBuilder.del(s);
            }
        }

        postShoppinglistEvent();
        return success;
    }

    /**
     * Get a {@link ShoppinglistItem} item by it's ID
     * @param id A {@link ShoppinglistItem} id
     * @return A shopping list item, or {@code null}
     */
    public ShoppinglistItem getItem(String id) {
        return mDatabase.getItem(id, user());
    }

    /**
     * Get all {@link ShoppinglistItem ShoppinglistItems} associated with a
     * {@link Shoppinglist}.
     * @param sl A {@link Shoppinglist} to get {@link ShoppinglistItem ShoppinglistItems} from
     * @return A list of {@link ShoppinglistItem ShoppinglistItems}
     */
    public List<ShoppinglistItem> getItems(Shoppinglist sl) {
        return getItems(sl.getId(), user());
    }

    /**
     * Get all {@link ShoppinglistItem ShoppinglistItems} associated with a
     * {@link Shoppinglist}.
     * @param shoppinglistId A {@link Shoppinglist#getId()} shoppinglist.id} to get {@link ShoppinglistItem ShoppinglistItems} from
     * @return A list of {@link ShoppinglistItem ShoppinglistItems}
     */
    public List<ShoppinglistItem> getItems(String shoppinglistId) {
        return getItems(shoppinglistId, user());
    }

    private List<ShoppinglistItem> getItems(Shoppinglist sl, User user) {
        return getItems(sl.getId(), user);
    }

    private List<ShoppinglistItem> getItems(String shoppinglistId, User user) {
        List<ShoppinglistItem> items = mDatabase.getItems(shoppinglistId, user, false);
        ListUtils.sortItems(items);
        return items;
    }

    /**
     * Add a {@link ShoppinglistItem} to a {@link Shoppinglist}
     *
     * <p>{@link ShoppinglistItem ShoppinglistItems} are inserted into the
     * database, and changes are synchronized to the server when and if possible.</p>
     * @param sli A {@link ShoppinglistItem} to add to a {@link Shoppinglist}
     * @return {@code true} if the action was performed, else {@code false}
     */
    public boolean addItem(ShoppinglistItem sli) {
        return addItem(sli, true, user());
    }

    private boolean isStringEqual(String first, String last) {
        return first == null ? last == null : first.equalsIgnoreCase(last);
    }

    /**
     * Add a {@link ShoppinglistItem} to a {@link Shoppinglist}
     *
     * <p>{@link ShoppinglistItem ShoppinglistItems} are inserted into the
     * database, and changes are synchronized to the server when and if possible.</p>
     * @param sli A {@link ShoppinglistItem} to add to a {@link Shoppinglist}
     * @param incrementCount Increment the count on the {@link ShoppinglistItem}
     * if an item like it exists, instead of adding new item.
     * @param user A user that owns the {@link ShoppinglistItem}
     * @return {@code true} if the action was performed, else {@code false}
     */
    @SuppressLint("DefaultLocale")
    public boolean addItem(ShoppinglistItem sli, boolean incrementCount, User user) {

        mDatabase.allowEditOrThrow(sli.getShoppinglistId(), user);

        if (sli.getOfferId() == null && sli.getDescription() == null) {
            SgnLog.i(TAG, "The ShoppinglistItem neither has offerId, or"
                    + "description, one or the other this is required by the API");
            return false;
        }

        // If the item exists in DB, then just increase count and edit the item
        if (incrementCount) {

            List<ShoppinglistItem> items = mDatabase.getItems(sli.getShoppinglistId(), user, false);
            for (ShoppinglistItem s : items) {
                boolean descriptionEqual = isStringEqual(s.getDescription(), sli.getDescription());
                boolean idEqual = isStringEqual(sli.getOfferId(), s.getOfferId());
                if (idEqual && descriptionEqual) {
                    s.setCount(s.getCount() + 1);
                    s.setTick(false);
                    return editItem(s);
                }
            }

        }

        Shoppinglist sl = mDatabase.getList(sli.getShoppinglistId(), user);

        if (sl == null) {
            SgnLog.i(TAG, "The shoppinglist id on the shoppinglist item, could"
                    + "not be found, please add a shoppinglist before adding items");
            return false;
        }

        List<ShoppinglistItem> editedItems = new ArrayList<ShoppinglistItem>();

        Date now = new Date();
        sli.setModified(now);
        sli.setState(SyncState.TO_SYNC);

        editedItems.add(sli);

        // Set the creator of not done yet
        if (sli.getCreator() == null) {
            if (user.getName() != null && user.getName().length() > 0) {
                sli.setCreator(user.getName());
            } else {
                sli.setCreator(user.getEmail());
            }
        }

        sli.setPreviousId(ListUtils.FIRST_ITEM);
        ShoppinglistItem first = mDatabase.getFirstItem(sli.getShoppinglistId(), user);
        if (first != null) {
            first.setPreviousId(sli.getId());
            first.setModified(now);
            first.setState(SyncState.TO_SYNC);
            editedItems.add(first);
            mBuilder.edit(first);
        }

        boolean success = mDatabase.insertItems(editedItems, user);
        if (success) {
			/* Update SL info, but not state. This will prevent sync, and API
			 * will auto update the modified tag, nice! */
            sl.setModified(now);
            mDatabase.editList(sl, user);
            mBuilder.edit(sl);
            mBuilder.add(sli);
        }
        postShoppinglistEvent();
        return success;
    }

    /**
     * Insert an updated {@link ShoppinglistItem} into the database.
     * <p>The {@link ShoppinglistItem} is replaced in the database, and changes
     * is synchronized to the server when, and if possible.</p>
     * @param sli An edited {@link ShoppinglistItem}
     * @return {@code true} if the action was performed, else {@code false}
     */
    public boolean editItem(ShoppinglistItem sli) {
        try {
            return editItem(sli, user());
        } finally {
            postShoppinglistEvent();
        }
    }

    /**
     * Replace an item, in the database
     * @param items A list of ShoppinglistItem to edit
     * @return true if the ShoppinglistItems was edited successful
     */
    public boolean editItems(List<ShoppinglistItem> items) {
        long s = System.currentTimeMillis();
        try {
            return editItems(items, user());
        } finally {
            SgnLog.d(TAG, "editItems.time: " + (System.currentTimeMillis() - s) + "ms");
        }
    }

    private boolean editItems(List<ShoppinglistItem> items, User user) {

        // Validate and get response in one step
        List<Shoppinglist> lists = mDatabase.allowEditItemsOrThrow(items, user);

        HashMap<String, ShoppinglistItem> dbItems = new HashMap<String, ShoppinglistItem>();
        for (Shoppinglist sl : lists) {
            for (ShoppinglistItem sli : getItems(sl, user)) {
                dbItems.put(sli.getId(), sli);
            }
        }

        Date now = new Date();
        for (ShoppinglistItem sli : items) {

            if (!dbItems.containsKey(sli.getId())) {
                SgnLog.i(TAG, "No such item exists, consider addItem() instead: " + sli.toString());
                return false;
            }

            sli.setModified(now);
            sli.setState(SyncState.TO_SYNC);

        }

        boolean success = mDatabase.editItems(items, user);

        if (success) {

            /* API will auto-update modified on the List, so we'll do the same and save a sync. */
            for (Shoppinglist sl : lists) {
                // This is a bit expensive, if there is more than one shoppinglist
                sl.setModified(now);
                mDatabase.editList(sl, user);
                mBuilder.edit(sl);
            }

            for (ShoppinglistItem sli : items) {
                mBuilder.edit(sli);
            }
        }

        postShoppinglistEvent();
        return success;
    }

    private boolean editItem(ShoppinglistItem sli, User user) {

        mDatabase.allowEditOrThrow(sli.getShoppinglistId(), user);

        Date now = new Date();
        sli.setModified(now);
        sli.setState(SyncState.TO_SYNC);

        // Check for changes in previous item, and update surrounding
        ShoppinglistItem oldItem = mDatabase.getItem(sli.getId(), user);
        if (oldItem == null) {
            SgnLog.i(TAG, "No such item exists, consider addItem() instead: " + sli.toString());
            return false;
        }

        boolean success = mDatabase.editItems(sli, user);

        if (success) {

            /* API will auto-update modified on the List, so we'll do the same and save a sync */
            Shoppinglist sl = mDatabase.getList(sli.getShoppinglistId(), user);

            if (sl != null) {
                sl.setModified(now);
                mDatabase.editList(sl, user);
                mBuilder.edit(sl);
            }
            mBuilder.edit(sli);
        }

        postShoppinglistEvent();
        return success;
    }


    /**
     * Delete all {@link ShoppinglistItem ShoppinglistItems} from a
     * {@link Shoppinglist} where {@link ShoppinglistItem#isTicked() isTicked()}
     * is {@code true}.
     * <P>Changes are synchronized to the server when, and if possible.</p>
     * @param sl A {@link Shoppinglist} to delete the
     * 			{@link ShoppinglistItem ShoppinglistItems} from
     */
    public void deleteItemsTicked(Shoppinglist sl) {
        deleteItems(sl, true, user());
    }

    /**
     * Delete all {@link ShoppinglistItem ShoppinglistItems} from a
     * {@link Shoppinglist} where {@link ShoppinglistItem#isTicked() isTicked()}
     * is {@code false}.
     * <P>Changes are synchronized to the server when, and if possible.</p>
     * @param sl A {@link Shoppinglist} to delete the
     * 			{@link ShoppinglistItem ShoppinglistItems} from
     */
    public void deleteItemsUnticked(Shoppinglist sl) {
        deleteItems(sl, false, user());
    }

    /**
     * Delete all {@link ShoppinglistItem ShoppinglistItems} from a {@link Shoppinglist}
     *
     * <p>Changes are synchronized to the server when, and if possible.</p>
     * @param sl A {@link Shoppinglist} to delete the
     * 				{@link ShoppinglistItem ShoppinglistItems} from
     */
    public void deleteItemsAll(Shoppinglist sl) {
        deleteItems(sl, null, user());
    }

    /**
     * Method to delete all {@link ShoppinglistItem} that matches a given state.
     *
     * <p>The possible states are:</p>
     * <ul>
     * 		<li>{@code true} - delete ticked items</li>
     * 		<li>{@code false} - delete unticked items</li>
     * 		<li>{@code null} - delete all items</li>
     * </ul>
     *
     * <p>Changes are synchronized to the server when, and if possible.</p>
     *
     * @param sl A {@link Shoppinglist} to delete
     * 				{@link ShoppinglistItem ShoppinglistItems} from
     * @param stateToDelete A state that describes what to delete
     * @param user the user that owns the {@link ShoppinglistItem ShoppinglistItems}
     */
    private boolean deleteItems(final Shoppinglist sl, Boolean stateToDelete, User user) {

        mDatabase.allowEditOrThrow(sl.getId(), user);

        Date now = new Date();

        // get it from this manager, to preserve order
        List<ShoppinglistItem> list = getItems(sl, user);

        String preGoodId = ListUtils.FIRST_ITEM;
        List<ShoppinglistItem> edited = new ArrayList<ShoppinglistItem>();

        for (ShoppinglistItem sli : list) {
            if (stateToDelete == null || sli.isTicked() == stateToDelete) {
                // Delete all items (null), or ones where ticked matches the requested state
                sli.setState(SyncState.DELETE);
                sli.setModified(now);
                edited.add(sli);
            } else {
                if (!sli.getPreviousId().equals(preGoodId)) {
                    sli.setPreviousId(preGoodId);
                    sli.setModified(now);
                    sli.setState(SyncState.TO_SYNC);
                    edited.add(sli);
                }
                preGoodId = sli.getId();
            }
        }


        boolean success = false;
        if (user.isLoggedIn()) {
            success = mDatabase.editItems(edited, user);
        } else {
            int rows = mDatabase.deleteItems(sl.getId(), stateToDelete, user);
            success = rows > 0;
        }

        if (success) {
			/* Update SL info, but not state. This will prevent sync, and API
			 * will auto update the modified tag, nice!
			 */
            sl.setModified(now);
            mDatabase.editList(sl, user);
            mBuilder.edit(sl);
            for (ShoppinglistItem sli : edited) {
                if (sli.getState() == SyncState.DELETE) {
                    mBuilder.del(sli);
                } else {
                    mBuilder.edit(sli);
                }
            }
        }

        postShoppinglistEvent();
        return success;
    }

    /**
     * Deletes a {@link ShoppinglistItem}
     * <p>The {@link ShoppinglistItem} is removed from the database, and later
     * changes is synchronized to the server when and if possible</p>
     * @param sli A {@link ShoppinglistItem} to delete
     * @return {@code true} if the action was performed, else {@code false}
     */
    public boolean deleteItem(ShoppinglistItem sli) {
        User u = user();
        mDatabase.allowEditOrThrow(sli.getShoppinglistId(), u);
        return deleteItem(sli, u);
    }

    private boolean deleteItem(ShoppinglistItem sli, User user) {

        Date now = new Date();

        List<ShoppinglistItem> edited = new ArrayList<ShoppinglistItem>();

        sli.setModified(now);
        sli.setState(SyncState.DELETE);
        edited.add(sli);

        // Update previous pointer
        ShoppinglistItem after = mDatabase.getItemPrevious(sli.getShoppinglistId(), sli.getId(), user);
        if (after != null) {
            after.setPreviousId(sli.getPreviousId());
            after.setModified(now);
            edited.add(after);
            mBuilder.edit(after);
        }

        boolean success = false;
        // Is user is logged in, then just delete the item completely
        // if not, then mark it to be deleted by the sync manager.
        if (user.isLoggedIn()) {
            success = mDatabase.editItems(edited, user);
        } else {
            success = mDatabase.deleteItem(sli, user);
            edited.remove(sli);
        }

        if (success) {
			/* Update shoppinglist modified, but not state, so we have correct
			 * state but won't have to sync changes to API.
			 * API will change state based on the synced item.
			 */
            Shoppinglist sl = getList(sli.getShoppinglistId());
            sl.setModified(now);
            mDatabase.editList(sl, user);
            mBuilder.edit(sl);
            mBuilder.del(sli);
        }
        postShoppinglistEvent();
        return success;
    }

    /**
     * Get the current user.
     * <p>wrapper method for: ShopGun.getInstance().getUser()</p>
     * @return A {@link User}
     */
    private User user() {
        return mShopGun.getSessionManager().getSession().getUser();
    }

    /**
     * Deletes all rows in the {@link DatabaseWrapper database}.
     */
    public void clear() {
        mDatabase.clear();
    }

    /**
     * Deletes all rows in the {@link DatabaseWrapper database} associated with a
     * given{@link User}.
     * @param userId A {@link User#getUserId()} to clear
     */
    public void clear(int userId) {
        mDatabase.clear(userId);
    }

    private void postShoppinglistEvent() {
        if (!mShopGun.getSyncManager().isPaused() && mBuilder.hasChanges()) {
            SgnBus.getInstance().post(mBuilder.build());
            mBuilder = new ShoppinglistEvent.Builder(false);
        }
    }

    public JSONArray dumpListTable() {
        return mDatabase.dumpListTable();
    }

    public JSONArray dumpShareTable() {
        return mDatabase.dumpShareTable();
    }

    public JSONArray dumpItemTable() {
        return mDatabase.dumpItemTable();
    }

}
