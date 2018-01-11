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

package com.shopgun.android.sdk.utils;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ListUtils {

    public static final String TAG = Constants.getTag(ListUtils.class);

    public final static String FIRST_ITEM = "00000000-0000-0000-0000-000000000000";

    public static void printItem(String tag, ShoppinglistItem s) {
        SgnLog.d(tag, "Item " + s.getDescription() + " prevId( " + s.getPreviousId() + ") - modified( " + s.getModified().toGMTString() + " ) ");
    }

    public static List<ShoppinglistItem> setPrevious(List<ShoppinglistItem> list, int prevPos, int movePos) {

        List<ShoppinglistItem> tmp = new ArrayList<ShoppinglistItem>();

        if (list == null || list.isEmpty() || movePos < 0) {
            return tmp;
        }

        ShoppinglistItem move = list.get(movePos);
//		printItem(TAG, move);
//		SgnLog.d(TAG, "list-size: " + list.size() + ", prevPos: " + prevPos + ", movePos: " + movePos);

        boolean isFirst = prevPos < 0;
        ShoppinglistItem prev = isFirst ? null : list.get(prevPos);
        String prevId = isFirst ? FIRST_ITEM : prev.getId();

        for (ShoppinglistItem sli : list) {
            if (sli.getPreviousId().equals(prevId)) {
                sli.setPreviousId(move.getId());
                tmp.add(sli);
            } else if (sli.getPreviousId().equals(move.getId())) {
                sli.setPreviousId(move.getPreviousId());
                tmp.add(sli);
            }
        }

        move.setPreviousId(prevId);
        tmp.add(move);

        return tmp;

    }


    /**
     * Sorts {@link ShoppinglistItem}, according to what ShopGun have defined
     * the order of a list should look be. This method does <b>not</b> update
     * the objects (as in {@link ShoppinglistItem#getPreviousId() previous_id}
     * isn't updated automatically).
     *
     * <p>There is no requirement to use this sorting method. This is only meant
     * as a nice to have.</p>
     *
     * @param items A {@link List} to sort
     */
    public static void sortItems(List<ShoppinglistItem> items) {
        int size = items.size();

        HashSet<String> allId = new HashSet<String>(size);
        for (ShoppinglistItem sli : items) {
            allId.add(sli.getId());
        }

		/* List of items that have not been given a previous id yet (e.g. items
		 * from website). These are the ones we assume have been added last, and
		 * thereby must be first in the final list*/
        List<ShoppinglistItem> nil = new ArrayList<ShoppinglistItem>(size);

		/* List of 'first' items, these are or have been first items in our list
		 * These are prioritized to be appended after all nil items*/
        List<ShoppinglistItem> first = new ArrayList<ShoppinglistItem>(size);
		
		/* Items that have for some reason been orphaned, e.g. their previous is
		 * have been removed, but they haven't been updated) these will be
		 * appended last*/
        List<ShoppinglistItem> orphan = new ArrayList<ShoppinglistItem>(size);
		
		/* Items that seems to be fine, in the way that they have a valid prevId */
        HashMap<String, ShoppinglistItem> prevItems = new HashMap<String, ShoppinglistItem>(size);
		
		/* Looping over all items, and categorizing them into the lists above */
        for (ShoppinglistItem sli : items) {

            String prevId = sli.getPreviousId();

            if (prevId == null) {
                nil.add(sli);
            } else if (FIRST_ITEM.equals(prevId)) {
                first.add(sli);
            } else if (!prevItems.containsKey(prevId) && allId.contains(prevId)) {
                prevItems.put(prevId, sli);
            } else {
                orphan.add(sli);
            }

        }

        // Clear the original items list, items in this list is to be restored shortly
        items.clear();
		
		/* SortBy the lists we're uncertain about by their title (this is as good as any sort) */
        Collections.sort(first, ShoppinglistItem.TITLE_ASCENDING);
        Collections.sort(nil, ShoppinglistItem.TITLE_ASCENDING);
        Collections.sort(orphan, ShoppinglistItem.TITLE_ASCENDING);
		
		/* All items that need to have their  */
        List<ShoppinglistItem> newItems = new ArrayList<ShoppinglistItem>(size);
        newItems.addAll(nil);
        newItems.addAll(first);
        newItems.addAll(orphan);
		
		/* Next item to check */
        ShoppinglistItem next;
		/* Id of 'next' item */
        String id;
        for (ShoppinglistItem sli : newItems) {
            next = sli;
            // If we still have items
            while (next != null) {
                id = next.getId();
                items.add(next);
                next = prevItems.get(id);
                prevItems.remove(id);
            }
        }

        for (ShoppinglistItem s : prevItems.values()) {
            items.add(s);
        }

    }

    public static long getLargestTimeStamp(List<ShoppinglistItem> list) {
        long t = 0;
        for (ShoppinglistItem sli : list) {
            t = Math.max(sli.getModified().getTime(), t);
        }
        return t;
    }

    /**
     * Get a {@link java.util.Set} of  {@link Shoppinglist#getId()} from a list of {@link ShoppinglistItem}
     *
     * @param items A list of {@link ShoppinglistItem}
     * @return A {@link java.util.Set} of ids
     */
    public static HashSet<String> getShoppinglistIdsFromItems(List<ShoppinglistItem> items) {
        HashSet<String> ids = new HashSet<String>(items.size());
        for (ShoppinglistItem sli : items) {
            ids.add(sli.getShoppinglistId());
        }
        return ids;
    }

    /**
     * Get a {@link java.util.Set} of  {@link Shoppinglist#getId()} from a list of {@link Shoppinglist}
     *
     * @param lists A list of Shoppinglist
     * @return A {@link java.util.Set} of ids
     */
    public static HashSet<String> getShoppinglistIdsFromLists(List<Shoppinglist> lists) {
        HashSet<String> ids = new HashSet<String>(lists.size());
        for (Shoppinglist sl : lists) {
            ids.add(sl.getId());
        }
        return ids;
    }

}
