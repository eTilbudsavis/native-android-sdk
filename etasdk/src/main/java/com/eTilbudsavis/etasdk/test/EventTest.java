package com.eTilbudsavis.etasdk.test;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.bus.SessionEvent;
import com.eTilbudsavis.etasdk.bus.ShoppinglistEvent;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.utils.ColorUtils;

import junit.framework.Assert;

import java.util.List;

public class EventTest {

    public static final String TAG = Constants.getTag(EventTest.class);

    private EventTest() {
        // empty
    }

    public static void test() {

        EtaSdkTest.start(TAG);
        testShoppinglistEvent();
        testSessionEvent();

    }

    public static void testShoppinglistEvent() {

        ShoppinglistEvent.Builder b = new ShoppinglistEvent.Builder(true);
        // check the constructor
        Assert.assertTrue(b.isServer);

        // Shouldn't have any changes at this point
        Assert.assertFalse(b.hasChanges());
        Assert.assertFalse(!b.firstSync);

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

        EtaSdkTest.logTest(TAG, (new MethodNameHelper(){}).getName());
    }

    public static void testSessionEvent() {

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

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {}).getName());
    }

}
