package com.eTilbudsavis.etasdk.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class ListUtils {
	
	public static final String TAG = Eta.TAG_PREFIX + ListUtils.class.getSimpleName();

	public static void printItem(String tag, ShoppinglistItem s) {
		EtaLog.d(tag, "Item " + s.getDescription() + " prevId( " + s.getPreviousId() + ") - modified( " + s.getModified().toGMTString() + " ) ");
	}
	
	/**
	 * 
	 * @param list
	 * @param prevPos
	 * @param movePos
	 * @return
	 */
	public static List<ShoppinglistItem> setPrevious(List<ShoppinglistItem> list, int prevPos, int movePos) {
		
		List<ShoppinglistItem> tmp = new ArrayList<ShoppinglistItem>();
		
		if (list == null || list.isEmpty() || movePos<0) {
			return new ArrayList<ShoppinglistItem>();
		}
		
		printItem(TAG, list.get(movePos));
		EtaLog.d(TAG, "list-size: " + list.size() + ", prevPos: " + prevPos + ", movePos: " + movePos);
		
		ShoppinglistItem move = list.get(movePos);
		boolean isFirst = prevPos < 0;
		ShoppinglistItem prev = isFirst ? null : list.get(prevPos);
		String prevId = isFirst ? ShoppinglistItem.FIRST_ITEM : prev.getId();
		
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
	 * Sorts {@link ShoppinglistItem}, according to what eTilbudsavis have defined
	 * the order of a list should look be. This method does <b>not</b> update
	 * the objects (as in {@link ShoppinglistItem#getPreviousId() previous_id}
	 * isn't updated automatically).
	 * 
	 * <p>There is no requirement to use this sorting method. This is only meant
	 * as a nice to have.</p>
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
			} else if (prevId.equals(ShoppinglistItem.FIRST_ITEM)) {
				first.add(sli);
			} else if ( !prevItems.containsKey(prevId) && allId.contains(prevId)) {
				prevItems.put(prevId, sli);
			} else {
				orphan.add(sli);
			}
			
		}
		
		// Clear the original items list, items in this list is to be restored shortly
		items.clear();
		
		/* Sort the lists we're uncertain about by their title (this is as good as any sort) */
		Collections.sort(first, ShoppinglistItem.TitleAscending);
		Collections.sort(nil, ShoppinglistItem.TitleAscending);
		Collections.sort(orphan, ShoppinglistItem.TitleAscending);
		
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
	
	
}
