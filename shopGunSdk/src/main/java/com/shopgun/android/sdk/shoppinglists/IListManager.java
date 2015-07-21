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

import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.User;

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
