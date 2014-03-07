package com.eTilbudsavis.etasdk.Utils;


/**
 * This class contains a basic list of endpoints, and some methods to generate
 * various URL's to request API data.
 * 
 * <p>This list of endpoints is fra from complete, but a full list of endpoints
 * and their capabilities can be found via the 
 * <a href="https://edge.etilbudsavis.dk/v2/help">Endpoint Documentation</a>.
 * The documentation isn't complete but, if questions arise please feel free to
 * contact us :-)</p>
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public class Endpoint {

	/** Variable controlling whether messages are printed. Set to true to print messages */
	public static boolean DEBUG_HOST = false;

	/** Set to true to enable usage of staging endpoint for pageflip-html. */
	public static boolean DEBUG_PAGEFLIP = false;
	
	public class Prefix {
		
		public static final String API_PRODUCTION = "https://api.etilbudsavis.dk";
		public static final String API_EDGE = "https://edge.etilbudsavis.dk";
		
		public static final String PAGEFLIP_PRODUCTION = "https://etilbudsavis.dk";
		public static final String PAGEFLIP_STAGING = "https://staging.etilbudsavis.dk";
		public static final String PAGEFLIP_DEV = "http://10.0.1.41:3000";
		
		public static final String THEMES_PRODUCTION = "https://etilbudsavis.dk";
		public static final String THEMES_STAGING = "https://staging.etilbudsavis.dk";
		
	}
	
	public static String API_HOST_PREFIX = Prefix.API_PRODUCTION;
	public static String PAGEFLIP_HOST_PREFIX = Prefix.PAGEFLIP_PRODUCTION;
	public static String THEMES_HOST_PREFIX = Prefix.THEMES_PRODUCTION;
	
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
	 * Get the current host to use. This can be changed by editing Endpoint.API_HOST_PREFIX.
	 * https://{prefix}.etilbudsavis.dk
	 * @return A string
	 */
	public static String getHost() {
		return API_HOST_PREFIX;
	}
	
	/** {pageflip_host_prefix}/proxy/{id}/ */
	public static String pageflipProxy(String id) {
		return String.format("%s/proxy/%s/", PAGEFLIP_HOST_PREFIX, id);
	}
	
	/** {theme_host_prefix}/utils/ajax/lists/themes/ */
	public static String themes() {
		return String.format("%s/utils/ajax/lists/themes/", THEMES_HOST_PREFIX);
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
