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

package com.shopgun.android.sdk.test;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.utils.ListUtils;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListUtilsTest {

    public static final String TAG = Constants.getTag(ListUtilsTest.class);

    public static void test() {

        SdkTest.start(TAG);
        testGetShoppinglistIdsFromItems();
        testGetShoppinglistIdsFromLists();

    }

    public static void testGetShoppinglistIdsFromItems() {

        List<ShoppinglistItem> items = new ArrayList<ShoppinglistItem>();
        int count = 10;
        int modulo = 10;
        for (int i = 0; i < count; i++) {
            int id = i % modulo;
            ShoppinglistItem sli = ModelCreator.getShoppinglistItem("item-id-" + id, "description-" + id);
            sli.setShoppinglistId("list-id-" + id);
            items.add(sli);
        }
        Set<String> listIds = ListUtils.getShoppinglistIdsFromItems(items);
        Assert.assertEquals(modulo, listIds.size());

        items = new ArrayList<ShoppinglistItem>();
        count = 10;
        modulo = 10;
        for (int i = 0; i < count; i++) {
            int id = i % modulo;
            ShoppinglistItem sli = ModelCreator.getShoppinglistItem("item-id-" + id, "description-" + id);
            sli.setShoppinglistId("list-id-" + id);
            items.add(sli);
        }
        listIds = ListUtils.getShoppinglistIdsFromItems(items);
        Assert.assertEquals(modulo, listIds.size());

        SdkTest.logTest(TAG, "GetShoppinglistIdsFromItems");

    }

    public static void testGetShoppinglistIdsFromLists() {


        List<Shoppinglist> lists = new ArrayList<Shoppinglist>();
        int count = 10;
        for (int i = 0; i < count; i++) {
            Shoppinglist sl = ModelCreator.getShoppinglist("id" + i, "name-" + i);
            lists.add(sl);
        }
        Set<String> listIds = ListUtils.getShoppinglistIdsFromLists(lists);
        Assert.assertEquals(lists.size(), listIds.size());

        SdkTest.logTest(TAG, "GetShoppinglistIdsFromLists");

    }

}
