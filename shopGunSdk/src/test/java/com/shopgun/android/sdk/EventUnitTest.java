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

package com.shopgun.android.sdk;


import com.shopgun.android.sdk.bus.SessionEvent;
import com.shopgun.android.sdk.bus.ShoppinglistEvent;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class EventUnitTest {

    @Test
    public void testShoppinglistEvent() throws Exception {

        ShoppinglistEvent.Builder b = new ShoppinglistEvent.Builder(true);
        // check the constructor
        Assert.assertTrue(b.isServer);

        // Shouldn't have any changes at this point
        Assert.assertFalse(b.hasChanges());
        Assert.assertFalse(b.firstSync);

        b.firstSync = true;
        // This is a change, should now have changes
        Assert.assertTrue(b.hasChanges());
        b.firstSync = false;

        Shoppinglist added = Shoppinglist.fromName("added");
        added.setType(Shoppinglist.TYPE_WISH_LIST);
        b.add(added);

        Shoppinglist edited = Shoppinglist.fromName("edited");
        edited.setType(Shoppinglist.TYPE_WISH_LIST);
        b.edit(edited);

        Shoppinglist deleted = Shoppinglist.fromName("deleted");
        deleted.setType(Shoppinglist.TYPE_WISH_LIST);
        b.del(deleted);

        // Even though we reset firstSync to false, there should still be changes at this point
        Assert.assertTrue(b.hasChanges());

        List<Shoppinglist> list = b.getLists();
        // We added three items
        Assert.assertEquals(3, list.size());
        // Both the list, and the map must contain the same number of elements
        Assert.assertEquals(list.size(), b.lists.size());

        List<Shoppinglist> listAdded = b.getAddedLists();
        // We added one list
        Assert.assertEquals(1, listAdded.size());
        Shoppinglist addedEdited = listAdded.get(0);
        // Edit the type of the added item
        addedEdited.setType(Shoppinglist.TYPE_SHOPPING_LIST);
        // now added and addedEdited should still be the same, as they both refer to the same object in memory
        Assert.assertEquals(added, addedEdited);

        // Add a bunch of ShoppinglistItems to the Builder
        for (int i = 0; i < 10; i++) {
            b.add(new ShoppinglistItem(added, ("item-" + i)));
        }
        // And then add one item from another list - with different list-id
        String id = "myspeciallistid";
        Shoppinglist specialSlId = Shoppinglist.fromName(id);
        specialSlId.setId(id);
        b.add(new ShoppinglistItem(specialSlId, id));

        ShoppinglistEvent e = b.build();
        Assert.assertEquals(e.getItems().size(), b.getItems().size());
        Assert.assertEquals(e.getAddedItems().size(), b.getAddedItems().size());
        Assert.assertEquals(e.isServer(), b.isServer);
        Assert.assertEquals(e.isFirstSync(), b.firstSync);
        Assert.assertEquals(e.getLists().size(), b.getLists().size());

        // We added one item from a special list - this should be returned
        Assert.assertEquals(1, e.getAddedItems(id).size());

    }

    @Test
    public void testSessionEvent() throws Exception {

        int oldUser = 0;
        int newUser = 100;
        SessionEvent e = new SessionEvent(oldUser, newUser);
        Assert.assertEquals(oldUser, e.getOldUser());
        Assert.assertEquals(newUser, e.getNewUser());
        Assert.assertTrue(e.isNewUser());

        oldUser = 1;
        newUser = 1;
        e = new SessionEvent(oldUser, newUser);
        Assert.assertEquals(oldUser, e.getOldUser());
        Assert.assertEquals(newUser, e.getNewUser());
        Assert.assertFalse(e.isNewUser());

    }

}
