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

package com.shopgun.android.sdk.database;

import android.content.Context;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.utils.ListUtils;
import com.shopgun.android.sdk.utils.PermissionUtils;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A middle layer for performing certain transformations on the DB data, prior to returning it to the caller.
 */
public class DatabaseWrapper {

    public static final String TAG = Constants.getTag(DatabaseWrapper.class);

    private static DatabaseWrapper mWrapper;
    private DataSource mDataSource;

    private DatabaseWrapper(Context c) {
        mDataSource = new DataSource(c);
    }

    public static DatabaseWrapper getInstance(Context c) {
        if (mWrapper == null) {
            mWrapper = new DatabaseWrapper(c);
        }
        return mWrapper;
    }

    public static DatabaseWrapper getInstance(ShopGun shopGun) {
        return getInstance(shopGun.getContext());
    }

    public void open() {
        mDataSource.open();
    }

    public void close() {
        mDataSource.close();
    }

    private boolean successId(long id) {
        return id > -1;
    }

    private boolean successCount(int count) {
        return count > 0;
    }

    private boolean successCount(int count, List list) {
        return count == list.size();
    }

    /**
     * Clear all data in the database. This operation cannot be undone.
     * @return number of changes
     */
    public int clear() {
        return mDataSource.clear();
    }

    /**
     * Clear all data from a given {@link User}. This operation cannot be undone.
     * @param userId A {@link User#getId()}
     * @return number of changes
     */
    public int clear(int userId) {
        return mDataSource.clear(userId);
    }

    /**
     * Insert a {@link Shoppinglist}, into the database
     * @param sl A {@link Shoppinglist}
     * @param user A {@link User}
     * @return {@code true} if the operation was successful, else {@code false}
     */
    public boolean insertList(Shoppinglist sl, User user) {
        long id = mDataSource.insertList(sl, String.valueOf(user.getUserId()));
        return successId(id);
    }

    /**
     * Insert a list of {@link Shoppinglist}, into the database
     * @param lists A list of {@link Shoppinglist}
     * @param user A {@link User}
     * @return {@code true} if the operation was successful, else {@code false}
     */
    public boolean insertLists(List<Shoppinglist> lists, User user) {
        if (lists.isEmpty()) {
            return true;
        }
        int count = mDataSource.insertList(lists, String.valueOf(user.getUserId()));
        return successCount(count, lists);
    }

    /**
     * Get a {@link Shoppinglist} object by it's {@link Shoppinglist#getId()}
     * @param id A {@link Shoppinglist#getId()}
     * @param user A {@link User}
     * @return A {@link Shoppinglist} if one exists, else {@code null}
     */
    public Shoppinglist getList(String id, User user) {
        Shoppinglist sl = mDataSource.getList(id, String.valueOf(user.getUserId()));
        if (sl != null) {
            /* Remove the list, if the user isn't in the shares.
            This happens when the user, have removed him/her self from shares,
            or deletes a list, and the action haven't been synced to the API yet */
            if (!sl.getShares().containsKey(user.getEmail())) {
                return null;
            }
        }
        return sl;
    }

    /**
     * Get the {@link Shoppinglist}'s that belong to tha given {@link User}
     * @param user A {@link User}
     * @return A list of {@link Shoppinglist}
     */
    public List<Shoppinglist> getLists(User user) {
        return getLists(user, false);
    }

    /**
     * Get the {@link Shoppinglist}'s that belong to tha given {@link User}
     * @param user A {@link User}
     * @param includeDeleted {@code true} to include the items that have locally been marked as deleted, else {@code false}
     * @return A list of {@link Shoppinglist}
     */
    public List<Shoppinglist> getLists(User user, boolean includeDeleted) {
        List<Shoppinglist> lists = mDataSource.getLists(String.valueOf(user.getUserId()), includeDeleted);
        Iterator<Shoppinglist> it = lists.iterator();
        while (it.hasNext()) {
            Shoppinglist sl = it.next();
            /* Remove the list, if the user isn't in the shares.
            This happens when the user, have removed him/her self from shares,
            or deletes a list, and the action haven't been synced to the API yet */
            if (!sl.getShares().containsKey(user.getEmail())) {
                String format = "Shoppinglist %s does not contain a share for %s, removing Shoppinglist from the final list.";
                String text = String.format(format, sl.getName(), user.getEmail());
                SgnLog.d(TAG, text);
                it.remove();
            }
        }
        // they should be sorted from the DB
//		Collections.sort(lists);
        return lists;
    }

    /**
     * Delete a given {@link Shoppinglist} from the {@link User}'s lists
     * @param sl A {@link Shoppinglist}
     * @param user A {@link User}
     * @return {@code true} if the operation was successful, else {@code false}
     */
    public boolean deleteList(Shoppinglist sl, User user) {
        return deleteList(sl.getId(), user);
    }

    /**
     * Delete a given {@link Shoppinglist} from the {@link User}'s lists
     * @param shoppinglistId A {@link Shoppinglist#getId()}
     * @param user A {@link User}
     * @return {@code true} if the operation was successful, else {@code false}
     */
    public boolean deleteList(String shoppinglistId, User user) {
        return deleteList(shoppinglistId, String.valueOf(user.getUserId()));
    }

    /**
     * Delete a given {@link Shoppinglist#getId()} from the {@link User}'s lists
     * @param shoppinglistId A {@link Shoppinglist#getId()}
     * @param userId A {@link User#getId()}
     * @return {@code true} if the operation was successful, else {@code false}
     */
    public boolean deleteList(String shoppinglistId, String userId) {
        // TODO: Do we need to remove shares?
        int count = mDataSource.deleteList(shoppinglistId, userId);
        return successCount(count);
    }

    /**
     * Replaces a {@link Shoppinglist}, that have been modified
     * @param sl A {@link Shoppinglist} that have been modified
     * @param user A {@link User}
     * @return {@code true} if the operation was successful, else {@code false}
     */
    public boolean editList(Shoppinglist sl, User user) {
        return insertList(sl, user);
    }

    /**
     * Adds item to db, <b>if and only if</b> it does not yet exist, else nothing
     *
     * @param sli A {@link ShoppinglistItem} to add to the database
     * @param user A {@link User}
     * @return {@code true} if the operation was successful, else {@code false}
     */
    public boolean insertItem(ShoppinglistItem sli, User user) {
        long id = mDataSource.insertItem(sli, String.valueOf(user.getUserId()));
        return successId(id);
    }

    /**
     * Adds a list of items to db, IF they do not yet exist, else nothing
     * @param items to insert
     * @param user A {@link User}
     * @return number of affected rows
     */
    public boolean insertItems(List<ShoppinglistItem> items, User user) {
        int count = mDataSource.insertItem(items, String.valueOf(user.getUserId()));
        return successCount(count, items);
    }

    /**
     * Get a {@link ShoppinglistItem} from the database
     * @param itemId to get from db
     * @param user A {@link User}
     * @return A {@link ShoppinglistItem} if one matches the criteria, else {@code null}
     */
    public ShoppinglistItem getItem(String itemId, User user) {
        return mDataSource.getItem(itemId, String.valueOf(user.getUserId()));
    }

    /**
     * Get all {@link ShoppinglistItem} from a {@link Shoppinglist}.
     * This does not include i{@link ShoppinglistItem}'s that have locally been marked at deleted
     * @param sl A {@link Shoppinglist}
     * @param user A {@link User}
     * @return A list of {@link ShoppinglistItem}
     */
    public List<ShoppinglistItem> getItems(Shoppinglist sl, User user) {
        return getItems(sl.getId(), user, false);
    }

    /**
     * Get all {@link ShoppinglistItem} from a {@link Shoppinglist}.
     * @param sl A {@link Shoppinglist}
     * @param user A {@link User}
     * @param includeDeleted {@code true} to include the items that have locally been marked as deleted, else {@code false}
     * @return A list of {@link ShoppinglistItem}
     */
    public List<ShoppinglistItem> getItems(Shoppinglist sl, User user, boolean includeDeleted) {
        return getItems(sl.getId(), user, includeDeleted);
    }

    /**
     * Get all {@link ShoppinglistItem} from a {@link Shoppinglist}.
     * @param shoppinglistId a {@link Shoppinglist#getId()}
     * @param user A {@link User}
     * @param includeDeleted {@code true} to include the items that have locally been marked as deleted, else {@code false}
     * @return A list of {@link ShoppinglistItem}
     */
    public List<ShoppinglistItem> getItems(String shoppinglistId, User user, boolean includeDeleted) {
        return mDataSource.getItems(shoppinglistId, String.valueOf(user.getUserId()), includeDeleted);
    }

    /**
     * Get the {@link ShoppinglistItem} that is marked as the first ({@link ListUtils#FIRST_ITEM})
     * in the given {@link Shoppinglist}.
     * @param shoppinglistId a {@link Shoppinglist#getId()}
     * @param user A {@link User}
     * @return A {@link ShoppinglistItem} if one exists with the {@code previousId}, else {@code null}
     */
    public ShoppinglistItem getFirstItem(String shoppinglistId, User user) {
        return getItemPrevious(shoppinglistId, ListUtils.FIRST_ITEM, user);
    }

    /**
     * Get the {@link ShoppinglistItem} that matches the given set of criteria.
     * @param shoppinglistId A {@link Shoppinglist#getId()}
     * @param previousId A {@link ShoppinglistItem#getPreviousId()}
     * @param user A {@link User}
     * @return A {@link ShoppinglistItem} if one exists with the {@code previousId}, else {@code null}
     */
    public ShoppinglistItem getItemPrevious(String shoppinglistId, String previousId, User user) {
        return mDataSource.getItemPrevious(shoppinglistId, previousId, String.valueOf(user.getUserId()));
    }

    /**
     * Get the {@link Shoppinglist} that is marked as the first ({@link ListUtils#FIRST_ITEM}).
     * @param user A {@link User}
     * @return A {@link Shoppinglist} if one exists with the {@code previousId}, else {@code null}
     */
    public Shoppinglist getFirstList(User user) {
        return getListPrevious(ListUtils.FIRST_ITEM, user);
    }

    /**
     * Get the {@link Shoppinglist} with the given {@code previousId}
     * @param previousId An {@link Shoppinglist#getId()} or {@link com.shopgun.android.sdk.utils.ListUtils#FIRST_ITEM}
     * @param user A {@link User}
     * @return A {@link Shoppinglist} if one exists with the {@code previousId}, else {@code null}
     */
    public Shoppinglist getListPrevious(String previousId, User user) {
        return mDataSource.getListPrevious(previousId, String.valueOf(user.getUserId()));
    }

    /**
     * Deletes an {@link ShoppinglistItem} from db
     * @param sli An item to delete
     * @param user A {@link User}
     * @return {@code true} if the operation was successful, else {@code false}
     */
    public boolean deleteItem(ShoppinglistItem sli, User user) {
        int count = mDataSource.deleteItem(sli.getId(), String.valueOf(user.getUserId()));
        return successCount(count);
    }

    /**
     * Deletes all items, in a given state, from a {@link Shoppinglist}
     *
     * <ul>
     * 		<li>{@code true} - delete ticked items</li>
     * 		<li>{@code false} - delete unticked items</li>
     * 		<li>{@code null} - delete all items</li>
     * </ul>
     *
     * @param shoppinglistId to remove items from
     * @param state that items must have to be removed
     * @param user A {@link User}
     * @return number of affected rows
     */
    public int deleteItems(String shoppinglistId, Boolean state, User user) {
        return mDataSource.deleteItems(shoppinglistId, state, String.valueOf(user.getUserId()));
    }

    /**
     * Adds item to db, <b>if and only if</b> it does not yet exist, else nothing
     * @param sli to add to db
     * @param user A {@link User}
     * @return {@code true} if the edit was successful, else {@code true}
     */
    public boolean editItems(ShoppinglistItem sli, User user) {
        long id = mDataSource.insertItem(sli, String.valueOf(user.getUserId()));
        return successId(id);
    }

    /**
     * Insert a list of {@link ShoppinglistItem} into the database
     * @param list A list of {@link ShoppinglistItem}
     * @param user A {@link User}
     * @return {@code true} if the edit was successful, else {@code true}
     */
    public boolean editItems(List<ShoppinglistItem> list, User user) {
        int count = mDataSource.insertItem(list, String.valueOf(user.getUserId()));
        return successCount(count, list);
    }

    /**
     * Method for updating a state for all ShoppinglistItem with a given User and Shoppinglist.id
     *
     * @param list A list of {@link ShoppinglistItem} to edit
     * @param user A {@link User}
     * @param modified The new Modified for the items
     * @param syncState The new SyncState for the items
     * @return {@code true} if the edit was successful, else {@code true}
     */
    public boolean editItemState(List<ShoppinglistItem> list, User user, Date modified, int syncState) {
        Map<String, List<ShoppinglistItem>> map = new HashMap<String, List<ShoppinglistItem>>();
        for (ShoppinglistItem sli : list) {
            String key = sli.getShoppinglistId();
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<ShoppinglistItem>());
            }
            map.get(key).add(sli);
        }
        int count = 0;
        for (Map.Entry<String, List<ShoppinglistItem>> e : map.entrySet()) {
            count += mDataSource.editItemState(e.getKey(), user.getId(), modified, syncState);
        }
        return successCount(count, list);
    }

    /**
     * Method for updating a state for all ShoppinglistItem with a given User and Shoppinglist.id
     *
     * @param sl A {@link Shoppinglist}
     * @param user A {@link User}
     * @param modified The new Modified for the items
     * @param syncState The new SyncState for the items
     * @return the number of rows affected
     */
    public int editItemState(Shoppinglist sl, User user, Date modified, int syncState) {
        modified = Utils.roundTime(modified);
        return mDataSource.editItemState(sl.getId(), user.getId(), modified, syncState);
    }

    /**
     *
     * @param sl A shoppinglist
     * @param user A user
     * @param includeDeleted Whether to include deleted shares
     * @return A list of Share
     */
    public List<Share> getShares(Shoppinglist sl, User user, boolean includeDeleted) {
        return mDataSource.getShares(sl.getId(), String.valueOf(user.getUserId()), includeDeleted);
    }

    public boolean insertShare(Share s, User user) {
        long id = mDataSource.insertShare(s, String.valueOf(user.getUserId()));
        return successId(id);
    }

    public int cleanShares(Shoppinglist sl, User user) {
        deleteShares(sl, user);
        int count = 0;
        for (Share s : sl.getShares().values()) {
            if (editShare(s, user)) {
                count++;
            }
        }
        return count;
    }

    public boolean editShare(Share s, User user) {
        deleteShare(s, user);
        return insertShare(s, user);
    }

    public int deleteShare(Share s, User user) {
        return mDataSource.deleteShare(s, user);
    }

    public int deleteShares(Shoppinglist sl, User user) {
        return deleteShares(sl.getId(), String.valueOf(user.getUserId()));
    }

    public int deleteShares(String shoppinglistId, User user) {
        return deleteShares(shoppinglistId, String.valueOf(user.getUserId()));
    }

    public int deleteShares(String shoppinglistId, String userId) {
        return mDataSource.deleteShares(shoppinglistId, userId);
    }

    public Shoppinglist allowEditOrThrow(ShoppinglistItem sli, User user) {
        return allowEditOrThrow(sli.getShoppinglistId(), user);
    }

    public Shoppinglist allowEditOrThrow(Shoppinglist sl, User user) {
        return allowEditOrThrow(sl.getId(), user);
    }

    public Shoppinglist allowEditOrThrow(String shoppinglistId, User user) {
        Shoppinglist sl = getList(shoppinglistId, user);
        PermissionUtils.allowEditOrThrow(sl, user);
        return sl;
    }

    public List<Shoppinglist> allowEditItemsOrThrow(List<ShoppinglistItem> items, User user) {
        HashSet<String> ids = ListUtils.getShoppinglistIdsFromItems(items);
        return allowEditOrThrow(ids, user);
    }

    public List<Shoppinglist> allowEditListOrThrow(List<Shoppinglist> lists, User user) {
        HashSet<String> ids = ListUtils.getShoppinglistIdsFromLists(lists);
        return allowEditOrThrow(ids, user);
    }

    public List<Shoppinglist> allowEditOrThrow(Set<String> shoppinglistIds, User user) {
        HashMap<String, Shoppinglist> map = new HashMap<String, Shoppinglist>(shoppinglistIds.size());
        for (String id : shoppinglistIds) {
            if (!map.containsKey(id)) {
                Shoppinglist sl = getList(id, user);
                map.put(sl.getId(), sl);
            }
        }
        List<Shoppinglist> lists = new ArrayList<Shoppinglist>(map.values());
        for (Shoppinglist sl : lists) {
            PermissionUtils.allowEditOrThrow(sl, user);
        }
        return lists;
    }

    public JSONArray dumpListTable() {
        return mDataSource.dumpListTable();
    }

    public JSONArray dumpShareTable() {
        return mDataSource.dumpShareTable();
    }

    public JSONArray dumpItemTable() {
        return mDataSource.dumpItemTable();
    }

}
