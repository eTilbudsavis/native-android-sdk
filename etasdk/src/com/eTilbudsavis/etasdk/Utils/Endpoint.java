package com.eTilbudsavis.etasdk.Utils;


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

	// GLOBALS
	public static final String MAIN_URL = "https://etilbudsavis.dk";
	public static final String API = "https://api.etilbudsavis.dk";
	private static final String V2 = "/v2";
	
	// RESOURCES AND SUB RESOURCES
	private static final String CATALOG = "/catalogs";
	private static final String DEALER = "/dealers";
	private static final String OFFER = "/offers";
	private static final String STORE = "/stores";
	private static final String SHOPPINGLIST = "/shoppinglists";
	private static final String ITEM = "/";
	private static final String USER = API + V2 + "/users";
	public static final String SEARCH = "/search";
	public static final String QUICK_SEARCH = "/quicksearch";
	public static final String TYPEAHEAD = "/typeahead";
	public static final String FACEBOOK = "/facebook";
	public static final String PROXY = MAIN_URL + "/proxy/";
	
	// Shoppinglist
	public static final String MODIFIED = "/modified";
	public static final String SHARES = "/shares";
	public static final String ITEMS = "/items";
	public static final String EMPTY = "/empty";
	
	// LISTS
	public static final String CATALOG_LIST = API + V2 + CATALOG;
	public static final String DEALER_LIST = API + V2 + DEALER;
	public static final String OFFER_LIST = API + V2 + OFFER;
	public static final String STORE_LIST =API +  V2 + STORE;
	
	// SINGLE ID
	public static final String CATALOG_ID = CATALOG_LIST + ITEM;
	public static final String DEALER_ID = DEALER_LIST + ITEM;
	public static final String OFFER_ID = OFFER_LIST + ITEM;
	public static final String STORE_ID = STORE_LIST + ITEM;
	public static final String SESSION = API + V2 + "/sessions";
	public static final String USER_RESET = USER + "/reset";
	public static final String USER_ID = USER + ITEM;
	
	// SEARCH
	public static final String CATALOG_SEARCH = CATALOG_LIST + SEARCH;
	public static final String DEALER_SEARCH = DEALER_LIST + SEARCH;
	public static final String OFFER_SEARCH = OFFER_LIST + SEARCH;
	public static final String STORE_SEARCH = STORE_LIST + SEARCH;
	
	// Typeahead
	public static final String OFFER_TYPEAHEAD = OFFER_LIST + TYPEAHEAD;
	
	// QUICK SEARCH
	public static final String STORE_QUICK_SEARCH = STORE_LIST + QUICK_SEARCH;

	/**
	 * https://etilbudsavis.dk/proxy/{id}/
	 * @param id of pageflip proxy (can be random)
	 * @return https://etilbudsavis.dk/proxy/{id}/
	 */
	public static String getPageflipProxy(String id) {
		return PROXY + id + ITEM;
	}

	/**
	 * /v2/users/{user_id}/facebook
	 * @param userId
	 * @return
	 */
	public static String getFacebookEndpoint(int userId) {
		return USER_ID + String.valueOf(userId) + FACEBOOK;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists
	 * @param userId
	 * @return
	 */
	public static String getListList(int userId) {
		return USER_ID + String.valueOf(userId) + SHOPPINGLIST;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}
	 * @param userId
	 * @param listUuid
	 * @return
	 */
	public static String getListFromId(int userId, String listUuid) {
		return getListList(userId) + ITEM + listUuid;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/modified
	 * @param userId
	 * @param listUuid
	 * @return
	 */
	public static String getListModified(int userId, String listUuid) {
		return getListFromId(userId, listUuid) + MODIFIED;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/empty
	 * @param userId
	 * @param listUuid
	 * @return
	 */
	public static String getListEmpty(int userId, String listUuid) {
		return getListFromId(userId, listUuid) + EMPTY;
	}
	
	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/shares
	 * @param userId
	 * @param listUuid
	 * @return
	 */
	public static String getListShares(int userId, String listUuid) {
		return getListFromId(userId, listUuid) + SHARES;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/shares/{email}
	 * @param userId
	 * @param listUuid
	 * @param shareEmail
	 * @return
	 */
	public static String getListSharesId(int userId, String listUuid, String shareEmail) {
		return getListShares(userId, listUuid) + ITEM + shareEmail;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/items
	 * @param userId
	 * @param listUuid
	 * @return
	 */
	public static String getItemList(int userId, String listUuid) {
		return getListFromId(userId, listUuid) + ITEMS;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid}
	 * @param userId
	 * @param listUuid
	 * @param itemUuid
	 * @return
	 */
	public static String getItemID(int userId, String listUuid, String itemUuid) {
		return getItemList(userId, listUuid) + ITEM + itemUuid;
	}

	/**
	 * /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid}/modified
	 * @param userId
	 * @param listUuid
	 * @param itemUuid
	 * @return
	 */
	public static String getItemModified(int userId, String listUuid, String itemUuid) {
		return getItemID(userId, listUuid, itemUuid) + MODIFIED;
	}

	public static boolean isItemEndpoint(String url) {
		return url.contains(CATALOG_ID) || 
				url.contains(OFFER_ID) ||
				url.contains(DEALER_ID) || 
				url.contains(STORE_ID);
	}
	
	public static boolean isListEndpoint(String url) {
		return !isItemEndpoint(url) &&
				(url.matches(CATALOG_LIST) || 
				url.matches(OFFER_LIST) || 
				url.matches(DEALER_LIST) || 
				url.matches(STORE_LIST));
	}
}
