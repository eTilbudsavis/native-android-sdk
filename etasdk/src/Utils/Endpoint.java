package Utils;

public class Endpoint {

	// GLOBALS
	public static final String MAIN_URL = "https://etilbudsavis.dk";
	public static final String PROVIDER_URL = MAIN_URL + "/connect/";
	public static final String V2 = "/v2";
	public static final String ID = "/id";
	public static final String IDS = "/ids";
	public static final String SEARCH = "/search";

	// RESOURCE
	public static final String SESSION = V2 + "/sessions";
	public static final String CATALOG = "/catalogs";
	public static final String DEALER = "/dealers";
	public static final String OFFER = "/offers";
	public static final String STORE = "/stores";
	
	// LISTS
	public static final String CATALOG_LIST = V2 + CATALOG;
	public static final String DEALER_LIST = V2 + DEALER;
	public static final String OFFER_LIST = V2 + OFFER;
	public static final String STORE_LIST = V2 + STORE;
	
	// SINGLE ID
	public static final String CATALOG_ID = CATALOG_LIST + ID;
	public static final String DEALER_ID = DEALER_LIST + ID;
	public static final String OFFER_ID = OFFER_LIST + ID;
	public static final String STORE_ID = STORE_LIST + ID;
	
	// MULTIPLE IDS
	public static final String CATALOG_IDS = CATALOG_LIST + IDS;
	public static final String DEALER_IDS = DEALER_LIST + IDS;
	public static final String OFFER_IDS = OFFER_LIST + IDS;
	public static final String STORE_IDS = STORE_LIST + IDS;
	
	// SEARCH
	public static final String CATALOG_SEARCH = CATALOG_LIST + IDS;
	public static final String DEALER_SEARCH = DEALER_LIST + IDS;
	public static final String OFFER_SEARCH = OFFER_LIST + IDS;
	public static final String STORE_SEARCH = STORE_LIST + IDS;

}
