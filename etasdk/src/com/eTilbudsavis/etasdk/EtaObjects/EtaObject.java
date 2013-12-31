package com.eTilbudsavis.etasdk.EtaObjects;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class EtaObject {
	
	public static final String TAG = "EtaBaseObject";
	
	/*
	 * All possible JSON keys returned from server
	 */
	protected static final String S_ID = "id";
	protected static final String S_ERN = "ern";
	protected static final String S_NAME = "name";
	protected static final String S_RUN_FROM = "run_from";
	protected static final String S_RUN_TILL = "run_till";
	protected static final String S_DEALER_ID = "dealer_id";
	protected static final String S_DEALER_URL = "dealer_url";
	protected static final String S_STORE_ID = "store_id";
	protected static final String S_STORE_URL = "store_url";
	protected static final String S_IMAGES = "images";
	protected static final String S_BRANDING = "branding";
	public static final String S_MODIFIED = "modified";
	protected static final String S_DESCRIPTION = "description";
	protected static final String S_URL_NAME = "url_name";
	protected static final String S_WEBSITE = "website";
	protected static final String S_LOGO = "logo";
	protected static final String S_COLOR = "color";
	protected static final String S_PAGEFLIP = "pageflip";
	protected static final String S_COUNTRY = "country";
	protected static final String S_ACCESS = "access";
	protected static final String S_LABEL = "label";
	protected static final String S_BACKGROUND = "background";
	protected static final String S_PAGE_COUNT = "page_count";
	protected static final String S_OFFER_COUNT = "offer_count";
	protected static final String S_DIMENSIONS = "dimensions";
	protected static final String S_PAGES = "pages";
	protected static final String P_PAGE = "page";
	protected static final String S_OWNER = "owner";
	protected static final String S_TICK = "tick";
	protected static final String S_OFFER_ID = "offer_id";
	protected static final String S_COUNT = "count";
	protected static final String S_SHOPPINGLIST_ID = "shopping_list_id";
	protected static final String S_CREATOR = "creator";
	protected static final String S_HEADING = "heading";
	protected static final String S_CATALOG_PAGE = "catalog_page";
	protected static final String S_PRICING = "pricing";
	protected static final String S_QUANTITY = "quantity";
	protected static final String S_LINKS = "links";
	protected static final String S_CATALOG_URL = "catalog_url";
	protected static final String S_CATALOG_ID = "catalog_id";
	protected static final String S_STREET = "street";
	protected static final String S_CITY = "city";
	protected static final String S_ZIP_CODE = "zip_code";
	protected static final String S_LATITUDE = "latitude";
	protected static final String S_LONGITUDE = "longitude";
	protected static final String S_CONTACT = "contact";
	protected static final String S_WEBSHOP = "webshop";
	protected static final String S_WIDTH = "width";
	protected static final String S_HEIGHT = "height";
	protected static final String S_CODE = "code";
	protected static final String S_MESSAGE = "message";
	protected static final String S_DETAILS = "details";
	protected static final String S_VIEW = "view";
	protected static final String S_ZOOM = "zoom";
	protected static final String S_THUMB = "thumb";
	protected static final String S_FROM = "from";
	protected static final String S_TO = "to";
	protected static final String S_UNIT = "unit";
	protected static final String S_SIZE = "size";
	protected static final String S_PIECES = "pieces";
	protected static final String S_USER = "user";
	protected static final String S_ACCEPTED = "accepted";
	protected static final String S_SYMBOL = "symbol";
	protected static final String S_GENDER = "gender";
	protected static final String S_BIRTH_YEAR = "birth_year";
	protected static final String S_EMAIL = "email";
	protected static final String S_PERMISSIONS = "permissions";
	protected static final String S_PREVIOUS_ID = "previous_id";
	protected static final String S_SI = "si";
	protected static final String S_FACTOR = "factor";
	protected static final String SERVER_UNSUBSCRIBE_PRINT_URL = "unsubscribe_print_url";
	protected static final String SERVER_TYPE = "type";
	protected static final String SERVER_META = "meta";
	
	public JSONObject serverData;
	
	public EtaObject() { }
	
	/**
	 * More or less a static factory method, for ease of creating lists of objects, in situations where
	 * the type is irrelevant, like conversion of objects in the shoppinglist manager.
	 * @param objects to be converted
	 * @return List of something that extends EtaBaseObjects
	 */
	@SuppressWarnings("unchecked")
	public static <T extends List<? extends EtaObject>> T fromJSON(JSONArray objects) {
		ArrayList<EtaObject> list = new ArrayList<EtaObject>(0);
		try {
			JSONObject o = objects.getJSONObject(0);
			if (o.has(ServerKey.ERN)) {
				list = EtaErnObject.fromJSON(objects);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return (T) list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends EtaObject> T fromJSON(JSONObject object) {
		EtaObject item = new EtaObject();
		if (object.has(ServerKey.ERN)) {
			item = EtaErnObject.fromJSON(object);
		}
		return (T) item;
	}
	
	public JSONObject toJSON() {
		return new JSONObject();
	}

	protected static String getJsonString(JSONObject object, String name) {
		if (object == null || name == null) 
			return null;
		
		try {
			if (!object.has(name))
				return null;
			return object.isNull(name) ? null : object.getString(name);
		} catch (JSONException e) { }
		return null;
	}

	protected static boolean stringCompare(String first, String second) {
		return first == null ? second == null : first.equals(second);
	}

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
		
	}
}
