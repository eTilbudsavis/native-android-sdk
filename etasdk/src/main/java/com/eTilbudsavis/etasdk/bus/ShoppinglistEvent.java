package com.eTilbudsavis.etasdk.bus;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ShoppinglistEvent extends EtaEvent {

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

    public boolean isServer() {
        return mIsServer;
    }

    public void setFirstSync(boolean firstSync) {
        mFirstSync = firstSync;
    }

    public boolean isFirstSync() {
        return mFirstSync;
    }

    /*
     * Add new notification items to the maps
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

        public interface Action {
            int ALL = 0;
            int EDITED = 1;
            int ADDED = 2;
            int DELETED = 4;
        }

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
