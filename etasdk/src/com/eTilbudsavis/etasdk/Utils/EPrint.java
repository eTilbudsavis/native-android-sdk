package com.eTilbudsavis.etasdk.Utils;

import java.util.List;

import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;

/**
 * A class to help print debug messages.
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class EPrint {
	
	public static final String TAG = "EPrint";
	
	/**
	 * Prints the essential parameters (for debugging) a list of ShoppinglistItems
	 * @param name A name, an action perhaps.
	 * @param items The list to print
	 */
	public static void printItems(String name, List<ShoppinglistItem> items) {
		
		StringBuilder sb = new StringBuilder();
		for (ShoppinglistItem sli : items) {
			sb.append(shoppinglistItemToString(sli)).append("\n");
		}
		EtaLog.d(TAG, name + "\n" + sb.toString());
	}
	
	/**
	 * Method for printing some essentials of a ShoppinglistItem.
	 * <p>Example: "Deleted - item[cola      ] prev[00000000] id[36473829] modified[2014-03-11T14:39:25+0100]"</p> 
	 * @param name A name for the item (an action perhaps)
	 * @param sli A ShoppinglistItem to print
	 */
	public static void printItem(String name, ShoppinglistItem sli)  {
		EtaLog.d(TAG, String.format("%s - %s", name, shoppinglistItemToString(sli)));
	}
	
	/**
	 * Get the essential parameters from a shoppinglistItem.
	 * <p>Example: "item[cola      ] prev[00000000] id[36473829] modified[2014-03-11T14:39:25+0100]"</p>
	 * @param sli
	 * @return
	 */
	public static String shoppinglistItemToString(ShoppinglistItem sli)  {
		String id = sli.getId().substring(0, 8);
		String prev = sli.getPreviousId() == null ? "null" : sli.getPreviousId().substring(0, 8);
		String title = sli.getTitle();
		if (sli.getTitle().length() > 8) {
			title = sli.getTitle().substring(0, 8);
		}
		String resp = "item[%-8s] prev[%s] id[%s] modified[%s]";
		resp = String.format(resp, title, prev, id, Utils.parseDate(sli.getModified()));
		return resp;
	}
	
	/**
	 * 
	 * @param type
	 * @param isServer
	 * @param added
	 * @param deleted
	 * @param edited
	 */
	public static void printListenerCallback(String type, boolean isServer, List<?> added, List<?> deleted, List<?> edited) {
		String text = "type[%s], isServer[%s], added[%s], deleted[%s], edited[%s]";
		EtaLog.d(TAG, String.format(text, type, isServer, added.size(), deleted.size(), edited.size()));
	}
	

}
