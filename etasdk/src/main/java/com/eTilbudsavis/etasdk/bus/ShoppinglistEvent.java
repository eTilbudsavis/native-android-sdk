package com.eTilbudsavis.etasdk.bus;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.ListNotification;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by oizo on 13/05/15.
 */
public class ShoppinglistEvent extends EtaEvent {

    public static final String TAG = Constants.getTag(ListNotification.class);

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

    public static void cleanItems(List<ShoppinglistItem> list, String shoppinglistId) {
        Iterator<ShoppinglistItem> i = list.iterator();
        while (i.hasNext()) {
            ShoppinglistItem item = i.next();
            if (!item.getShoppinglistId().equals(shoppinglistId)) {
                i.remove();
            }
        }
    }

    public static List<ShoppinglistItem> getCleanValues(Map<String, StateWrapper<ShoppinglistItem>> map, int action, String shoppinglistId) {
        List<ShoppinglistItem> list = getValues(map, action);
        cleanItems(list, shoppinglistId);
        return list;
    }

    public List<ShoppinglistItem> getItems() {
        return getValues(mItems, StateWrapper.Action.ALL);
    }

    public List<ShoppinglistItem> getItems(String shoppinglistId) {
        return getCleanValues(mItems, StateWrapper.Action.ALL, shoppinglistId);
    }

    public List<ShoppinglistItem> getAddedItems() {
        return getValues(mItems, StateWrapper.Action.ADDED);
    }

    public List<ShoppinglistItem> getAddedItems(String shoppinglistId) {
        return getCleanValues(mItems, StateWrapper.Action.ADDED, shoppinglistId);
    }

    public List<ShoppinglistItem> getDeletedItems() {
        return getValues(mItems, StateWrapper.Action.DELETED);
    }

    public List<ShoppinglistItem> getDeletedItems(String shoppinglistId) {
        return getCleanValues(mItems, StateWrapper.Action.DELETED, shoppinglistId);
    }

    public List<ShoppinglistItem> getEditedItems() {
        return getValues(mItems, StateWrapper.Action.EDITED);
    }

    public List<ShoppinglistItem> getEditedItems(String shoppinglistId) {
        return getCleanValues(mItems, StateWrapper.Action.EDITED, shoppinglistId);
    }

    public List<Shoppinglist> getLists() {
        return getValues(mLists, StateWrapper.Action.ALL);
    }

    public List<Shoppinglist> getAddedLists() {
        return getValues(mLists, StateWrapper.Action.ADDED);
    }

    public List<Shoppinglist> getDeletedLists() {
        return getValues(mLists, StateWrapper.Action.DELETED);
    }

    public List<Shoppinglist> getEditedLists() {
        return getValues(mLists, StateWrapper.Action.EDITED);
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
}
