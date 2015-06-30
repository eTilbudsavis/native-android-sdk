package com.eTilbudsavis.etasdk.test;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.utils.ListUtils;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListUtilsTest {

    public static final String TAG = Constants.getTag(ListUtilsTest.class);

    public static void test() {

        EtaSdkTest.start(TAG);
        testGetShoppinglistIdsFromItems();
        testGetShoppinglistIdsFromLists();

    }

    public static void testGetShoppinglistIdsFromItems() {

        List<ShoppinglistItem> items = new ArrayList<ShoppinglistItem>();
        int count = 10;
        int modulo = 10;
        for (int i = 0; i < count; i++) {
            int id = i%modulo;
            ShoppinglistItem sli = ModelCreator.getShoppinglistItem("item-id-"+id, "description-"+id);
            sli.setShoppinglistId("list-id-"+id);
            items.add(sli);
        }
        Set<String> listIds = ListUtils.getShoppinglistIdsFromItems(items);
        Assert.assertEquals(modulo, listIds.size());

        items = new ArrayList<ShoppinglistItem>();
        count = 10;
        modulo = 10;
        for (int i = 0; i < count; i++) {
            int id = i%modulo;
            ShoppinglistItem sli = ModelCreator.getShoppinglistItem("item-id-"+id, "description-"+id);
            sli.setShoppinglistId("list-id-"+id);
            items.add(sli);
        }
        listIds = ListUtils.getShoppinglistIdsFromItems(items);
        Assert.assertEquals(modulo, listIds.size());

        EtaSdkTest.logTest(TAG, "GetShoppinglistIdsFromItems");

    }

    public static void testGetShoppinglistIdsFromLists() {


        List<Shoppinglist> lists = new ArrayList<Shoppinglist>();
        int count = 10;
        for (int i = 0; i < count; i++) {
            Shoppinglist sl = ModelCreator.getShoppinglist("id"+i, "name-"+i);
            lists.add(sl);
        }
        Set<String> listIds = ListUtils.getShoppinglistIdsFromLists(lists);
        Assert.assertEquals(lists.size(), listIds.size());

        EtaSdkTest.logTest(TAG, "GetShoppinglistIdsFromLists");

    }

}
