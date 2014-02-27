package com.eTilbudsavis.etasdk.Utils;


/**
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public class Endpoint {

	/** Variable controlling whether messages are printed. Set to true to print messages */
	public static boolean DEBUG_HOST = false;

	/** Set to true to enable usage of staging endpoint for pageflip-html. */
	public static boolean DEBUG_PAGEFLIP = false;
	
	public static String HOST_PREFIX = "api";
	
	public static final String HOST_PREFIX_PRODUCTION = "api";
	public static final String HOST_PREFIX_EDGE = "edge";

	public static final String CATALOG_LIST = "/v2/catalogs";
	public static final String CATALOG_ID = "/v2/catalogs/";
	public static final String CATALOG_SEARCH = "/v2/catalogs/search";
	public static final String CATALOG_TYPEAHEAD = "/v2/catalogs/typeahead";
	
	public static final String DEALER_LIST = "/v2/dealers";
	public static final String DEALER_ID = "/v2/dealers/";
	public static final String DEALER_SEARCH = "/v2/dealers/search";

	public static final String OFFER_LIST = "/v2/offers";
	public static final String OFFER_ID = "/v2/offers/";
	public static final String OFFER_SEARCH = "/v2/offers/search";
	public static final String OFFER_TYPEAHEAD = "/v2/offers/typeahead";

	public static final String STORE_LIST = "/v2/stores";
	public static final String STORE_ID = "/v2/stores/";
	public static final String STORE_SEARCH = "/v2/stores/search";
	public static final String STORE_QUICK_SEARCH = "/v2/stores/quicksearch";

	public static final String SESSIONS = "/v2/sessions";

	public static final String USER = "/v2/users";

	public static final String USER_RESET = "/v2/users/reset";

	public static final String CATEGORIES	= "/v2/categories";

	public static final String COUNTRIES = "/v2/countries";
	
	/**
	 * Get the current host to use. This can be changed by editing Endpoint.HOST_PREFIX.
	 * https://{prefix}.etilbudsavis.dk
	 * @return
	 */
	public static String getHost() {
		return String.format("https://%s.etilbudsavis.dk", HOST_PREFIX);
	}
	
	/** /v2/offers/{offer_id} */
	public static String offerId(String offerId) {
		return String.format("/v2/offers/%s", offerId);
	}

	/** /v2/stores/{store_id} */
	public static String storeId(String storeId) {
		return String.format("/v2/stores/%s", storeId);
	}

	/** /v2/dealers/{dealer_id} */
	public static String dealerId(String dealerId) {
		return String.format("/v2/dealers/%s", dealerId);
	}

	/** /v2/catalogs/{catalog_id} */
	public static String catalogId(String catalogId) {
		return String.format("/v2/catalogs/%s", catalogId);
	}

	/** /v2/catalogs/{catalog_id}/collect */
	public static String catalogCollect(String catalogId) {
		return String.format("/v2/catalogs/%s/collect", catalogId);
	}
	
	/** /v2/offers/{offer_id}/collect */
	public static String offerCollect(String offerId) {
		return String.format("/v2/offers/%s/collect", offerId);
	}
	
	/**
	 * /v2/stores/{offer_id}/collect
	 */
	public static String storeCollect(String storeId) {
		return String.format("/v2/stores/%s/collect", storeId);
	}

	/** https://etilbudsavis.dk/proxy/{id}/ */
	public static String pageflipProxy(String id) {
		String production = "https://etilbudsavis.dk/proxy/%s/";
		String staging = "https://staging.etilbudsavis.dk/proxy/%s/";
		return String.format( DEBUG_PAGEFLIP ? staging : production, id);
	}

	/** https://staging.etilbudsavis.dk/utils/ajax/lists/themes/ */
	public static String themes() {
		String production = "https://etilbudsavis.dk/utils/ajax/lists/themes/";
		String staging = "https://staging.etilbudsavis.dk/utils/ajax/lists/themes/";
		return production;
	}

	/** /v2/users/{user_id}/facebook */
	public static String facebook(int userId) {
		return String.format("/v2/users/%s/facebook", userId);
	}

	/** /v2/users/{user_id}/shoppinglists */
	public static String lists(int userId) {
		return String.format("/v2/users/%s/shoppinglists", userId);
	}

	/** /v2/users/{user_id}/shoppinglists/{list_uuid} */
	public static String list(int userId, String listId) {
		return String.format("/v2/users/%s/shoppinglists/%s", userId, listId);
	}

	/** /v2/users/{user_id}/shoppinglists/{list_uuid}/modified */
	public static String listModified(int userId, String listId) {
		return String.format("/v2/users/%s/shoppinglists/%s/modified", userId, listId);
	}

	/** /v2/users/{user_id}/shoppinglists/{list_uuid}/empty */
	public static String listEmpty(int userId, String listId) {
		return String.format("/v2/users/%s/shoppinglists/%s/empty", userId, listId);
	}

	/** /v2/users/{user_id}/shoppinglists/{list_uuid}/shares */
	public static String listShares(int userId, String listId) {
		return String.format("/v2/users/%s/shoppinglists/%s/shares", userId, listId);
	}

	/** /v2/users/{user_id}/shoppinglists/{list_uuid}/shares/{email} */
	public static String listShareEmail(int userId, String listId, String email) {
		return String.format("/v2/users/%s/shoppinglists/%s/shares/%s", userId, listId, email);
	}

	/** /v2/users/{user_id}/shoppinglists/{list_uuid}/items */
	public static String listitems(int userId, String listId) {
		return String.format("/v2/users/%s/shoppinglists/%s/items", userId, listId);
	}

	/** /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid} */
	public static String listitem(int userId, String listId, String itemId) {
		return String.format("/v2/users/%s/shoppinglists/%s/items/%s", userId, listId, itemId);
	}

	/** /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid}/modified */
	public static String listitemModifiedById(int userId, String listId, String itemId) {
		return String.format("/v2/users/%s/shoppinglists/%s/items/%s/modified", userId, listId, itemId);
	}

}
