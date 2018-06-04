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

package com.shopgun.android.sdk.bus;

import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @deprecated No longer maintained
 */
@Deprecated
public class ShoppinglistEvent extends ShopGunEvent {

    public static final String TAG = Constants.getTag(ShoppinglistEvent.class);

    /*
     * Lists for collecting individual changes in items and lists, to do a single
     * notification to any subscribers
     */
    public Map<String, StateWrapper<ShoppinglistItem>> mItems = Collections.synchronizedMap(new HashMap<String, StateWrapper<ShoppinglistItem>>());
    public Map<String, StateWrapper<Shoppinglist>> mLists = Collections.synchronizedMap(new HashMap<String, StateWrapper<Shoppinglist>>());
    boolean mIsServer = false;
    boolean mFirstSync = false;

    public ShoppinglistEvent(boolean isServer) {
        mIsServer = isServer;
    }

    public static <T> List<T> getValues(Map<String, StateWrapper<T>> map, int action) {
        List<T> list = new ArrayList<T>(map.size());
        for (Map.Entry<String, StateWrapper<T>> e : map.entrySet()) {
            if (e.getValue().getAction() == action || action == StateWrapper.Action.ALL) {
                list.add(e.getValue().getItem());
            }
        }
        return list;
    }

    public static List<ShoppinglistItem> getCleanItemValues(Map<String, StateWrapper<ShoppinglistItem>> map, int action, String shoppinglistId) {
        List<ShoppinglistItem> list = getValues(map, action);
        Iterator<ShoppinglistItem> i = list.iterator();
        while (i.hasNext()) {
            ShoppinglistItem item = i.next();
            if (!item.getShoppinglistId().equals(shoppinglistId)) {
                i.remove();
            }
        }
        return list;
    }

    public static List<Shoppinglist> getCleanListValues(Map<String, StateWrapper<Shoppinglist>> map, int action, String shoppinglistId) {
        List<Shoppinglist> list = getValues(map, action);
        Iterator<Shoppinglist> i = list.iterator();
        while (i.hasNext()) {
            Shoppinglist item = i.next();
            if (!item.getId().equals(shoppinglistId)) {
                i.remove();
            }
        }
        return list;
    }

    public boolean isServer() {
        return mIsServer;
    }

    public boolean isFirstSync() {
        return mFirstSync;
    }

    public void setFirstSync(boolean firstSync) {
        mFirstSync = firstSync;
    }

    /*
    Add new notification items to the maps
     */
    public void add(Shoppinglist s) {
        mLists.put(s.getId(), new StateWrapper<Shoppinglist>(StateWrapper.Action.ADDED, s));
    }

    public void del(Shoppinglist s) {
        mLists.put(s.getId(), new StateWrapper<Shoppinglist>(StateWrapper.Action.DELETED, s));
    }

    public void edit(Shoppinglist s) {
        mLists.put(s.getId(), new StateWrapper<Shoppinglist>(StateWrapper.Action.EDITED, s));
    }

    public void add(ShoppinglistItem s) {
        mItems.put(s.getId(), new StateWrapper<ShoppinglistItem>(StateWrapper.Action.ADDED, s));
    }

    public void del(ShoppinglistItem s) {
        mItems.put(s.getId(), new StateWrapper<ShoppinglistItem>(StateWrapper.Action.DELETED, s));
    }

    public void edit(ShoppinglistItem s) {
        mItems.put(s.getId(), new StateWrapper<ShoppinglistItem>(StateWrapper.Action.EDITED, s));
    }

    public List<ShoppinglistItem> getItems() {
        return getValues(mItems, StateWrapper.Action.ALL);
    }

    public List<ShoppinglistItem> getItems(String shoppinglistId) {
        return getCleanItemValues(mItems, StateWrapper.Action.ALL, shoppinglistId);
    }

    public List<ShoppinglistItem> getAddedItems() {
        return getValues(mItems, StateWrapper.Action.ADDED);
    }

    public List<ShoppinglistItem> getAddedItems(String shoppinglistId) {
        return getCleanItemValues(mItems, StateWrapper.Action.ADDED, shoppinglistId);
    }

    public List<ShoppinglistItem> getDeletedItems() {
        return getValues(mItems, StateWrapper.Action.DELETED);
    }

    public List<ShoppinglistItem> getDeletedItems(String shoppinglistId) {
        return getCleanItemValues(mItems, StateWrapper.Action.DELETED, shoppinglistId);
    }

    public List<ShoppinglistItem> getEditedItems() {
        return getValues(mItems, StateWrapper.Action.EDITED);
    }

    public List<ShoppinglistItem> getEditedItems(String shoppinglistId) {
        return getCleanItemValues(mItems, StateWrapper.Action.EDITED, shoppinglistId);
    }

    public List<Shoppinglist> getLists() {
        return getValues(mLists, StateWrapper.Action.ALL);
    }

    public List<Shoppinglist> getLists(String shoppinglistId) {
        return getCleanListValues(mLists, StateWrapper.Action.ALL, shoppinglistId);
    }

    public List<Shoppinglist> getAddedLists() {
        return getValues(mLists, StateWrapper.Action.ADDED);
    }

    public List<Shoppinglist> getAddedLists(String shoppinglistId) {
        return getCleanListValues(mLists, StateWrapper.Action.ADDED, shoppinglistId);
    }

    public List<Shoppinglist> getDeletedLists() {
        return getValues(mLists, StateWrapper.Action.DELETED);
    }

    public List<Shoppinglist> getDeletedLists(String shoppinglistId) {
        return getCleanListValues(mLists, StateWrapper.Action.DELETED, shoppinglistId);
    }

    public List<Shoppinglist> getEditedLists() {
        return getValues(mLists, StateWrapper.Action.EDITED);
    }

    public List<Shoppinglist> getEditedLists(String shoppinglistId) {
        return getCleanListValues(mLists, StateWrapper.Action.EDITED, shoppinglistId);
    }

    public boolean hasListNotifications() {
        return !mLists.isEmpty();
    }

    public boolean hasItemNotifications() {
        return !mItems.isEmpty();
    }

    public static class StateWrapper<T> {

        int mAction = -1;
        T mItem;

        public StateWrapper(int action, T item) {
            this.mAction = action;
            this.mItem = item;
        }

        public int getAction() {
            return mAction;
        }

        public T getItem() {
            return mItem;
        }

        public interface Action {
            int ALL = 0;
            int EDITED = 1;
            int ADDED = 2;
            int DELETED = 4;
        }

    }

    @Override
    public String toString() {
        String format = "%s[ isServer: %s, isFirstSync: %s, list.count: %s, item.count: %s ]";
        return String.format(format, getType(), mIsServer, mFirstSync, mLists.size(), mItems.size());
    }

    public String toString(boolean printAll) {
        if (!printAll) {
            return toString();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("isServer: ").append(mIsServer).append("\n");
        sb.append("isFirstSync: ").append(mFirstSync).append("\n");

        if (hasListNotifications()) {
            sb.append("# LISTS: ").append("\n");
            buildList(sb, "List.Del", getDeletedLists());
            buildList(sb, "List.Edit", getEditedLists());
            buildList(sb, "List.Add", getAddedLists());
        }
        if (hasItemNotifications()) {
            sb.append("# ITEMS: ").append("\n");
            buildItem(sb, "Item.Del", getDeletedItems());
            buildItem(sb, "Item.Edit", getEditedItems());
            buildItem(sb, "Item.Add", getAddedItems());
        }
        return sb.toString();
    }

    private void buildItem(StringBuilder sb, String action, List<ShoppinglistItem> list) {
        for (ShoppinglistItem sli : list) {
            sb.append(action).append(": ").append(sli.toString()).append("\n");
        }
    }

    private void buildList(StringBuilder sb, String action, List<Shoppinglist> list) {
        for (Shoppinglist sli : list) {
            sb.append(action).append(": ").append(sli.toString()).append("\n");
        }
    }

    public static class Builder {

        public Map<String, StateWrapper<ShoppinglistItem>> items = Collections.synchronizedMap(new HashMap<String, StateWrapper<ShoppinglistItem>>());
        public Map<String, StateWrapper<Shoppinglist>> lists = Collections.synchronizedMap(new HashMap<String, StateWrapper<Shoppinglist>>());
        public boolean isServer = false;
        public boolean firstSync = false;

        public Builder(boolean isServer) {
            this.isServer = isServer;
        }

        public List<ShoppinglistItem> getItems() {
            return getValues(items, StateWrapper.Action.ALL);
        }

        public List<ShoppinglistItem> getAddedItems() {
            return getValues(items, StateWrapper.Action.ADDED);
        }

        public List<ShoppinglistItem> getDeletedItems() {
            return getValues(items, StateWrapper.Action.DELETED);
        }

        public List<ShoppinglistItem> getEditedItems() {
            return getValues(items, StateWrapper.Action.EDITED);
        }

        public List<Shoppinglist> getLists() {
            return getValues(lists, StateWrapper.Action.ALL);
        }

        public List<Shoppinglist> getAddedLists() {
            return getValues(lists, StateWrapper.Action.ADDED);
        }

        public List<Shoppinglist> getDeletedLists() {
            return getValues(lists, StateWrapper.Action.DELETED);
        }

        public List<Shoppinglist> getEditedLists() {
            return getValues(lists, StateWrapper.Action.EDITED);
        }

        public void add(Shoppinglist s) {
            lists.put(s.getId(), new StateWrapper<Shoppinglist>(StateWrapper.Action.ADDED, s));
        }

        public void del(Shoppinglist s) {
            lists.put(s.getId(), new StateWrapper<Shoppinglist>(StateWrapper.Action.DELETED, s));
        }

        public void edit(Shoppinglist s) {
            lists.put(s.getId(), new StateWrapper<Shoppinglist>(StateWrapper.Action.EDITED, s));
        }

        public void add(ShoppinglistItem s) {
            items.put(s.getId(), new StateWrapper<ShoppinglistItem>(StateWrapper.Action.ADDED, s));
        }

        public void del(ShoppinglistItem s) {
            items.put(s.getId(), new StateWrapper<ShoppinglistItem>(StateWrapper.Action.DELETED, s));
        }

        public void edit(ShoppinglistItem s) {
            items.put(s.getId(), new StateWrapper<ShoppinglistItem>(StateWrapper.Action.EDITED, s));
        }

        public boolean hasChanges() {
            return !lists.isEmpty() || !items.isEmpty() || firstSync;
        }

        public ShoppinglistEvent build() {
            ShoppinglistEvent e = new ShoppinglistEvent(isServer);
            e.mItems = items;
            e.mLists = lists;
            e.mFirstSync = firstSync;
            return e;
        }

    }

}
