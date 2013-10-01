package com.eTilbudsavis.etasdk.EtaObjects;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class EtaObject {
	
	public static final String TAG = "EtaBaseObject";
	
	/** A set of possible JSON keys returned from server */
	
	public static final String S_MODIFIED = "modified";
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
	protected static final String S_AREA_ID = "area_id";
	protected static final String S_LANGUAGE = "language";
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
	protected static final String S_TOKEN = "token";
	protected static final String S_EXPIRES = "expires";
	protected static final String S_PROVIDER = "provider";
	
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
			if (o.has(S_ERN)) {
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
		if (object.has(S_ERN)) {
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
			return object.isNull(name) ? null : object.getString(name);
		} catch (JSONException e) { }
		return null;
	}

	protected static boolean stringCompare(String first, String second) {
		return first == null ? second == null : first.equals(second);
	}

}
