package com.eTilbudsavis.etasdk;

import android.annotation.SuppressLint;

import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.User;

import java.util.List;

public interface IListManager {

    void clear(int userId);
    void clear();
    void postNotifications();

    boolean addItem(ShoppinglistItem sli);
    boolean addItem(ShoppinglistItem sli, boolean incrementCount);
    boolean addItem(ShoppinglistItem sli, boolean incrementCount, User user);
    boolean addItem(List<ShoppinglistItem> items);
    boolean addItem(List<ShoppinglistItem> items, boolean incrementCount);
    boolean addItem(List<ShoppinglistItem> items, boolean incrementCount, User user);
    boolean deleteItem(ShoppinglistItem sli);
    boolean deleteItem(ShoppinglistItem sli, User user);
    boolean deleteItem(List<ShoppinglistItem> items);
    boolean deleteItem(List<ShoppinglistItem> items, User user);
    int deleteItemsAll(Shoppinglist sl);
    int deleteItemsAll(Shoppinglist sl, User user);
    int deleteItemsTicked(Shoppinglist sl);
    int deleteItemsTicked(Shoppinglist sl, User user);
    int deleteItemsUnticked(Shoppinglist sl);
    int deleteItemsUnticked(Shoppinglist sl, User user);
    boolean editItem(ShoppinglistItem sli);
    boolean editItem(ShoppinglistItem sli, User user);

    boolean editList(Shoppinglist sl);
    boolean editList(Shoppinglist sl, User user);
    boolean editList(List<ShoppinglistItem> lists);
    boolean editList(List<ShoppinglistItem> lists, User user);
    boolean deleteList(Shoppinglist sl);
    boolean deleteList(Shoppinglist sl, User user);
    boolean deleteList(List<ShoppinglistItem> lists);
    boolean deleteList(List<ShoppinglistItem> lists, User user);

}
