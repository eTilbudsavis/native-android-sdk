package com.eTilbudsavis.etasdk.EtaObjects;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.Utils;


public class EtaObject {
	
	public static final String TAG = "EtaBaseObject";
	
	// GENERIC
	protected static final String S_ID = "id";
	protected static final String S_ERN = "ern";
	protected static final String S_NAME = "name";
	
	// Catalogs
	protected static final String S_LABEL = "label";
	protected static final String S_BACKGROUND = "background";
	protected static final String S_RUN_FROM = "run_from";
	protected static final String S_RUN_TILL = "run_till";
	protected static final String S_PAGE_COUNT = "page_count";
	protected static final String S_OFFER_COUNT = "offer_count";
	protected static final String S_BRANDING = "branding";
	protected static final String S_DEALER_ID = "dealer_id";
	protected static final String S_DEALER_URL = "dealer_url";
	protected static final String S_STORE_ID = "store_id";
	protected static final String S_STORE_URL = "store_url";
	protected static final String S_DIMENSIONS = "dimensions";
	protected static final String S_IMAGES = "images";
	protected static final String S_PAGES = "pages";
	protected static final String P_PAGE = "page";
	
	// Dealers
	protected static final String S_URL_NAME = "url_name";
	protected static final String S_WEBSITE = "website";
	protected static final String S_LOGO = "logo";
	protected static final String S_COLOR = "color";
	protected static final String S_PAGEFLIP = "pageflip";
	
	// Shoppinglist
	protected static final String S_ACCESS = "access";
	protected static final String S_MODIFIED = "modified";
	protected static final String S_OWNER = "owner";
	
	
	
	protected static final String ERN_CATALOG = "catalog";
	protected static final String ERN_OFFER = "offer";
	protected static final String ERN_DEALER = "dealer";
	protected static final String ERN_STORE = "store";
	protected static final String ERN_SHOPPINGLIST = "shoppinglist";
	protected static final String ERN_SHOPPINGLISTITEM = "shoppinglistitem";
	
	private JSONObject data = null;
	
	public EtaObject() {
		
	}
	
	/**
	 * More or less a static factory method, for ease of creating lists of objects, in situations where
	 * the type is irrelevant, like conversion of objects in the shoppinglist manager.
	 * @param objects to be converted
	 * @return List of something that extends EtaBaseObjects
	 */
	@SuppressWarnings("unchecked")
	public static <T extends List<? extends EtaObject>> T fromJSON(JSONArray objects) {
		
		List<? extends EtaObject> list = new ArrayList<EtaObject>(0);
		
		if (objects.length() == 0) {
			Utils.logd(TAG, "Array is empty");
			return (T) list;
		}

		try {
			JSONObject o = objects.getJSONObject(0);
			if (o.has(S_ERN)) {
				
				String[] split = o.getString(S_ERN).split(":");
				
				if (split.length >1) {
					
					if (split[1].equals(ERN_CATALOG)) {
						list = Catalog.fromJSON(objects);
					} else if (split[1].equals(ERN_DEALER)) {
						list = Dealer.fromJSON(objects);
					} else if (split[1].equals(ERN_OFFER)) {
						list = Offer.fromJSON(objects);
					} else if (split[1].equals(ERN_STORE)) {
						list = Store.fromJSON(objects);
					} else if (split[1].equals(ERN_SHOPPINGLIST)) {
						list = Shoppinglist.fromJSON(objects);
					} else if (split[1].equals(ERN_SHOPPINGLISTITEM)) {
						list = ShoppinglistItem.fromJSON(objects);
					} 
					
				} else {
					Utils.logd(TAG, "ArrayElements does not contain an ERN");
				}
				
			} else {
				Utils.logd(TAG, "ArrayElements does not contain an ERN");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return (T) list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends EtaObject> T fromJSON(JSONObject object) {
		EtaObject item = new EtaObject();
		try {
			if (object.has(S_ERN)) {
				Utils.logd(TAG, object.getString(S_ERN));
				String[] split = object.getString(S_ERN).split(":");
				if (split.length >1) {
					
					if (split[1].equals(ERN_CATALOG)) {
						item = Catalog.fromJSON(object);
					} else if (split[1].equals(ERN_DEALER)) {
						item = Dealer.fromJSON(object);
					} else if (split[1].equals(ERN_OFFER)) {
						item = Offer.fromJSON(object);
					} else if (split[1].equals(ERN_STORE)) {
						item = Store.fromJSON(object);
					} else if (split[1].equals(ERN_SHOPPINGLIST)) {
						item = Shoppinglist.fromJSON(object);
					} else if (split[1].equals(ERN_SHOPPINGLISTITEM)) {
						item = ShoppinglistItem.fromJSON(object);
					} 
					
				} else {
					Utils.logd(TAG, "JSONObject does not contain an ERN");
				}
				
			} else {
				Utils.logd(TAG, "JSONObject does not contain an ERN");
			}
					
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return (T) item;
	}
	
	public JSONObject toJSON() {
		return data;
	}
	
	public static <T extends EtaObject> JSONObject toJSON(T object) {
		return object.toJSON();
	}
	
}
