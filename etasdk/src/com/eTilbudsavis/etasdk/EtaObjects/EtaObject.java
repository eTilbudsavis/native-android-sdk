package com.eTilbudsavis.etasdk.EtaObjects;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.NetworkHelpers.JsonArrayRequest;
import com.eTilbudsavis.etasdk.NetworkHelpers.JsonObjectRequest;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;

/**
 * <p>The super-class for all eTilbudsavis objects.<p>
 * 
 * <p>The object ensures the existens of a {@link #toJSON()} method on all objects, which proves useful when
 * communication with the API</p>
 * 
 * <p>The object also contains {@link ServerKey}, a class containing a set of Strings that is meant as help when 
 * converting JSON-data received from the API.</p>
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public abstract class EtaObject {
	
	public static final String TAG = "EtaObject";
	
	/**
	 * <p>Method for converting this object into a JSONObject.</p>
	 * 
	 * <p>This is especially usable when {@link Request.Method.PUT PUT}- and {@link Request.Method.POST POST}ing data
	 * to API v2 with {@link JsonObjectRequest} and {@link JsonArrayRequest}, which ( not by coincidence ^_^ ) takes 
	 * {@link JSONObject} and {@link JSONArray} respectively as body.</p>
	 * @return A {@link JSONObject} representation of the object. Same structure as API v2, including any extra
	 * 			variables needed for successful requests)
	 */
	public abstract JSONObject toJSON();
	
	/**
	 * <p>This class contains most (but probably not all) strings, that eTilbudsavis API v2 can return as keys, in
	 * responses of the types {@link JSONObject} and {@link JSONArray}.
	 * </p>
	 * 
	 * <p><b>Note</b> not all keys are contained in this set, and for detailed documentation of each key, in a given
	 * context, we will refer you to the <a href="http://engineering.etilbudsavis.dk/eta-api/">API documentation</a></p>
	 * 
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public class ServerKey {
		
		public static final String ID = "id";
		public static final String ERN = "ern";
		public static final String NAME = "name";
		public static final String RUN_FROM = "run_from";
		public static final String RUN_TILL = "run_till";
		public static final String DEALER_ID = "dealer_id";
		public static final String DEALER_URL = "dealer_url";
		public static final String STORE_ID = "store_id";
		public static final String STORE_URL = "store_url";
		public static final String IMAGES = "images";
		public static final String BRANDING = "branding";
		public static final String MODIFIED = "modified";
		public static final String DESCRIPTION = "description";
		public static final String URL_NAME = "url_name";
		public static final String WEBSITE = "website";
		public static final String LOGO = "logo";
		public static final String COLOR = "color";
		public static final String PAGEFLIP = "pageflip";
		public static final String COUNTRY = "country";
		public static final String ACCESS = "access";
		public static final String LABEL = "label";
		public static final String BACKGROUND = "background";
		public static final String PAGE_COUNT = "page_count";
		public static final String OFFER_COUNT = "offer_count";
		public static final String DIMENSIONS = "dimensions";
		public static final String PAGES = "pages";
		public static final String PAGE = "page";
		public static final String OWNER = "owner";
		public static final String TICK = "tick";
		public static final String OFFER_ID = "offer_id";
		public static final String COUNT = "count";
		public static final String SHOPPINGLIST_ID = "shopping_list_id";
		public static final String CREATOR = "creator";
		public static final String HEADING = "heading";
		public static final String CATALOG_PAGE = "catalog_page";
		public static final String PRICING = "pricing";
		public static final String QUANTITY = "quantity";
		public static final String LINKS = "links";
		public static final String CATALOG_URL = "catalog_url";
		public static final String CATALOG_ID = "catalog_id";
		public static final String STREET = "street";
		public static final String CITY = "city";
		public static final String ZIP_CODE = "zip_code";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String CONTACT = "contact";
		public static final String WEBSHOP = "webshop";
		public static final String WIDTH = "width";
		public static final String HEIGHT = "height";
		public static final String CODE = "code";
		public static final String MESSAGE = "message";
		public static final String DETAILS = "details";
		public static final String VIEW = "view";
		public static final String ZOOM = "zoom";
		public static final String THUMB = "thumb";
		public static final String FROM = "from";
		public static final String TO = "to";
		public static final String UNIT = "unit";
		public static final String SIZE = "size";
		public static final String PIECES = "pieces";
		public static final String USER = "user";
		public static final String ACCEPTED = "accepted";
		public static final String SYMBOL = "symbol";
		public static final String GENDER = "gender";
		public static final String BIRTH_YEAR = "birth_year";
		public static final String EMAIL = "email";
		public static final String PERMISSIONS = "permissions";
		public static final String PREVIOUS_ID = "previous_id";
		public static final String SI = "si";
		public static final String FACTOR = "factor";
		public static final String UNSUBSCRIBE_PRINT_URL = "unsubscribe_print_url";
		public static final String TYPE = "type";
		public static final String META = "meta";
		public static final String SHARES = "shares";
		public static final String TOKEN = "token";
		public static final String EXPIRES = "expires";
		public static final String PROVIDER = "provider";
		public static final String PRICE = "price";
		public static final String PREPRICE = "pre_price";
		public static final String CURRENCY = "currency";
		public static final String LENGTH = "length";
		public static final String OFFSET = "offset";
		public static final String SUBJECT = "subject";
		public static final String ACCEPT_URL = "accept_url";
		
	}
	
}
