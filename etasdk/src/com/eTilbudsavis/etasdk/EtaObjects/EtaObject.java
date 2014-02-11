package com.eTilbudsavis.etasdk.EtaObjects;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class EtaObject {
	
	public static final String TAG = "EtaObject";
	
	public EtaObject() { }
	
	protected static String jsonToString(JSONObject object, String key) {
		if (object == null || key == null) 
			return null;
		
		try {
			if (!object.has(key))
				return null;
			return object.isNull(key) ? null : object.getString(key);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return null;
	}
	
	protected static int jsonToInt(JSONObject object, String key, int defValue) {
		try {
			return object.isNull(key) ? defValue : object.getInt(key);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return defValue;
	}
	
	protected static double jsonToDouble(JSONObject object, String key, double defValue) {
		try {
			return object.isNull(key) ? defValue : object.getDouble(key);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return defValue;
	}
	
	protected static boolean jsonToBoolean(JSONObject object, String key, boolean defValue) {
		try {
			return object.isNull(key) ? defValue : object.getBoolean(key);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return defValue;
	}
	
	/**
	 * This class contains all strings, that eTilbudsavis API v2 can return as keys in any JSONObject.
	 * <br><br>
	 * Note, that this is a convenience class, and that the keys aren't necessarily a full
	 * set of the available keys in the API v2, but covers all keys used for creation of any Android SDK object.
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
	}
}
