package com.eTilbudsavis.etasdk.Utils;

import java.net.URI;

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Api.ApiListener;

import android.net.Uri;


/**
 * {@link com.eTilbudsavis.etasdk.Utils.Endpoint Endpoint} holds most endpoint-links for API v2.<br><br>
 * 
 * This class should be referenced, when using the 
 * {@link com.eTilbudsavis.etasdk.Api #request(String, com.eTilbudsavis.etasdk.Api.RequestListener, android.os.Bundle, com.eTilbudsavis.etasdk.Api.RequestType, android.os.Bundle) Api.request()}
 * to avoid invalid parameters and improve caching.<br><br>
 * 
 * {@link com.eTilbudsavis.etasdk.Utils.Endpoint Endpoint}
 * is referenced in all classes that has an endpoint on the server. 
 * This minimizes confusion about what endpoints exists for each class.
 * An example getting a list of catalogs, here {@link com.eTilbudsavis.etasdk.EtaObjects.Catalog Catalog}
 * will hold an appropriate endpoint, in this case 
 * {@link com.eTilbudsavis.etasdk.EtaObjects.Catalog #ENDPOINT_LIST Catalog.ENDPOINT_LIST}<br><br>
 * 
 * NOT ALL endpoints is defined in the SDK, please reference the online documentation for more options.
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public final class Endpoint {

	private static final String ITEM = "/";
	private static final String CAT = "catalogs";
	
	public class Path {
		
		public static final String CATALOGS		= "/v2/" + CAT;
		public static final String DEALERS		= "/v2/dealers";
		public static final String OFFERS		= "/v2/offers";
		public static final String STORES		= "/v2/stores";
		public static final String SESSIONS		= "/v2/sessions";
		public static final String USERS		= "/v2/users";
		public static final String CATEGORIES	= "/v2/categories";

		public static final String ENDPOINTS	= "/v2/endpoints";
		
		public static final String SHOPPINGLIST	= "/shoppinglists";
		public static final String SEARCH = "/search";
		public static final String QUICK_SEARCH = "/quicksearch";
		public static final String TYPEAHEAD = "/typeahead";
		public static final String FACEBOOK = "/facebook";
		public static final String MODIFIED = "/modified";
		public static final String SHARES = "/shares";
		public static final String ITEMS = "/items";
		public static final String EMPTY = "/empty";
		public static final String RESET = "/reset";
		public static final String PAGEFLIP_PROXY = "http://etilbudsavis.dk/proxy/";
		public static final String PAGEFLIP_PROXY_DEBUG = "http://192.168.1.119:3000/proxy/";
	}
	
	
	// GLOBALS
	public static final String HOST = "https://edge.etilbudsavis.dk";
	
	// LISTS
	public static final String CATALOG_LIST = Path.CATALOGS;
	public static final String DEALER_LIST = Path.DEALERS;
	public static final String OFFER_LIST = Path.OFFERS;
	public static final String STORE_LIST = Path.STORES;
	
	// SINGLE ID
	public static final String CATALOG_ID = Path.CATALOGS + ITEM;
	public static final String DEALER_ID = Path.DEALERS + ITEM;
	public static final String OFFER_ID = Path.OFFERS + ITEM;
	public static final String STORE_ID = Path.STORES + ITEM;
	
	public static final String SESSIONS = Path.SESSIONS;
	
	public static final String USER_ID = Path.USERS + ITEM;
	public static final String USER_RESET = Path.USERS + Path.RESET;
	
	// SEARCH
	public static final String CATALOG_SEARCH = Path.CATALOGS + Path.SEARCH;
	public static final String DEALER_SEARCH = Path.DEALERS + Path.SEARCH;
	public static final String OFFER_SEARCH = Path.OFFERS + Path.SEARCH;
	public static final String STORE_SEARCH = Path.STORES + Path.SEARCH;
	
	// Typeahead
	public static final String OFFER_TYPEAHEAD = Path.OFFERS + Path.TYPEAHEAD;
	
	// QUICK SEARCH
	public static final String STORE_QUICK_SEARCH = Path.STORES + Path.QUICK_SEARCH;

	/**
	 * https://etilbudsavis.dk/proxy/{id}/
	 * @param id of pageflip proxy (can be random)
	 * @return https://etilbudsavis.dk/proxy/{id}/
	 */
	public static String getPageflipProxy(String id, boolean debug) {
		return debug ? (Path.PAGEFLIP_PROXY_DEBUG + id + ITEM) : (Path.PAGEFLIP_PROXY + id + ITEM);
	}

	/**
	 * /v2/users/{user_id}/facebook
	 * @param userId
	 * @return
	 */
	public static String getFacebookByUserId(int userId) {
		return USER_ID + String.valueOf(userId) + Path.FACEBOOK;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists
	 * @param userId
	 * @return
	 */
	public static String getListsByUserId(int userId) {
		return USER_ID + String.valueOf(userId) + Path.SHOPPINGLIST;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}
	 * @param userId
	 * @param listId
	 * @return
	 */
	public static String getListById(int userId, String listId) {
		return getListsByUserId(userId) + ITEM + listId;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/modified
	 * @param userId
	 * @param listId
	 * @return
	 */
	public static String getListModifiedById(int userId, String listId) {
		return getListById(userId, listId) + Path.MODIFIED;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/empty
	 * @param userId
	 * @param listId
	 * @return
	 */
	public static String getListEmpty(int userId, String listId) {
		return getListById(userId, listId) + Path.EMPTY;
	}
	
	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/shares
	 * @param userId
	 * @param listId
	 * @return
	 */
	public static String getSharesByListId(int userId, String listId) {
		return getListById(userId, listId) + Path.SHARES;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/shares/{email}
	 * @param userId
	 * @param listUuid
	 * @param shareEmail
	 * @return
	 */
	public static String getShoppinglistShareById(int userId, String listUuid, String shareEmail) {
		return getSharesByListId(userId, listUuid) + ITEM + shareEmail;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/items
	 * @param userId
	 * @param listId
	 * @return
	 */
	public static String getItemByListId(int userId, String listId) {
		return getListById(userId, listId) + Path.ITEMS;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid}
	 * @param userId
	 * @param listId
	 * @param itemId
	 * @return
	 */
	public static String getItemById(int userId, String listId, String itemId) {
		return getItemByListId(userId, listId) + ITEM + itemId;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid}/modified
	 * @param userId
	 * @param listId
	 * @param itemId
	 * @return
	 */
	public static String getItemModifiedById(int userId, String listId, String itemId) {
		return getItemById(userId, listId, itemId) + Path.MODIFIED;
	}

	public static boolean isItemEndpoint(String path) {
		return path.contains(CATALOG_ID) || 
				path.contains(OFFER_ID) ||
				path.contains(DEALER_ID) || 
				path.contains(STORE_ID);
	}
	
	public static boolean isListEndpoint(String path) {
		return !isItemEndpoint(path) &&
				(path.matches(CATALOG_LIST) || 
				path.matches(OFFER_LIST) || 
				path.matches(DEALER_LIST) || 
				path.matches(STORE_LIST));
	}
	
	public static boolean isMatch(ApiListener<?> listener) {
		
		return false;
	}
	
	public static boolean s(String path) {
		return false;
	}
}
