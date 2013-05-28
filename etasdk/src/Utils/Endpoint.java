package Utils;

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
	public static final String PROVIDER_URL = MAIN_URL + "/connect/";
	public static final String API = "http://api.etilbudsavis.dk";
	private static final String V2 = "/v2";
	
	// RESOURCES AND SUB RESOURCES
	private static final String CATALOG = "/catalogs";
	private static final String DEALER = "/dealers";
	private static final String OFFER = "/offers";
	private static final String STORE = "/stores";
	private static final String SHOPPINGLIST = "/shoppinglists";
	private static final String ITEM = "/";
	private static final String USER = V2 + "/users";
	public static final String SEARCH = "/search";
	public static final String FACEBOOK = "/facebook";
	public static final String MODIFIED = "/modified";
	public static final String SHARES = "/shares";
	public static final String ITEMS = "/items";
	
	// LISTS
	public static final String CATALOG_LIST = V2 + CATALOG;
	public static final String DEALER_LIST = V2 + DEALER;
	public static final String OFFER_LIST = V2 + OFFER;
	public static final String STORE_LIST = V2 + STORE;
	
	// SINGLE ID
	public static final String CATALOG_ID = CATALOG_LIST + ITEM;
	public static final String DEALER_ID = DEALER_LIST + ITEM;
	public static final String OFFER_ID = OFFER_LIST + ITEM;
	public static final String STORE_ID = STORE_LIST + ITEM;
	public static final String SESSION = V2 + "/sessions";
	public static final String USER_RESET = USER + "/reset";
	public static final String USER_ID = USER + ITEM;
	
	// SEARCH
	public static final String CATALOG_SEARCH = CATALOG_LIST + SEARCH;
	public static final String DEALER_SEARCH = DEALER_LIST + SEARCH;
	public static final String OFFER_SEARCH = OFFER_LIST + SEARCH;
	public static final String STORE_SEARCH = STORE_LIST + SEARCH;

	public static String getFacebookEndpoint(int userId) {
		return USER_ID + String.valueOf(userId) + FACEBOOK;
	}

	public static String getShoppinglistList(int userId) {
		return USER_ID + String.valueOf(userId) + SHOPPINGLIST;
	}

	public static String getShoppinglistId(int userId, String listUuid) {
		return getShoppinglistList(userId) + ITEM + listUuid;
	}

	public static String getShoppinglistModified(int userId, String listUuid) {
		return getShoppinglistId(userId, listUuid) + MODIFIED;
	}

	public static String getShoppinglistShares(int userId, String listUuid) {
		return getShoppinglistId(userId, listUuid) + SHARES;
	}

	public static String getShoppinglistSharesId(int userId, String listUuid, String shareEmail) {
		return getShoppinglistShares(userId, listUuid) + ITEM + shareEmail;
	}

	public static String getShoppinglistItemList(int userId, String listUuid) {
		return getShoppinglistId(userId, listUuid) + ITEMS;
	}

	public static String getShoppinglistItemID(int userId, String listUuid, String itemUuid) {
		return getShoppinglistItemList(userId, listUuid) + ITEM + itemUuid;
	}

	public static String getShoppinglistItemModified(int userId, String listUuid, String itemUuid) {
		return getShoppinglistItemID(userId, listUuid, itemUuid) + MODIFIED;
	}
	
}
