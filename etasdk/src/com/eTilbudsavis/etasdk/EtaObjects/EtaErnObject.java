package com.eTilbudsavis.etasdk.EtaObjects;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;


/**
 * EtaErnObject is an object, is one of the objects which can which JSON representation can be easily 
 * identified by the 'ern' key. 
 * @author oizo
 *
 */
public class EtaErnObject extends EtaObject {

	public static final String TAG = "EtaErnObject";

	protected String mId;
	protected String mErn;
	
	private static final String ERN_CATALOG = "ern:catalog";
	private static final String ERN_OFFER = "ern:offer";
	private static final String ERN_DEALER = "ern:dealer";
	private static final String ERN_STORE = "ern:store";
	private static final String ERN_SHOPPINGLIST = "ern:shopping:list";
	private static final String ERN_SHOPPINGLISTITEM = "ern:shoppinglist:item";

	@SuppressWarnings("unchecked")
	public <T extends EtaErnObject> T setId(String id) {
		this.mId = id;
		return (T)this;
	}

	public String getId() {
		return mId;
	}

	@SuppressWarnings("unchecked")
	public <T extends EtaErnObject> T setErn(String ern) {
		mErn = ern;
		return (T)this;
	}

	public String getErn() {
		return mErn;
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
			return (T) list;
		}
		
		try {
			JSONObject o = objects.getJSONObject(0);
			if (o.has(Key.ERN)) {
				
				String ern = o.getString(Key.ERN);
				
				if (ern.startsWith(ERN_CATALOG)) {
					list = Catalog.fromJSON(objects);
				} else if (ern.startsWith(ERN_DEALER)) {
					list = Dealer.fromJSON(objects);
				} else if (ern.startsWith(ERN_OFFER)) {
					list = Offer.fromJSON(objects);
				} else if (ern.startsWith(ERN_STORE)) {
					list = Store.fromJSON(objects);
				} else if (ern.startsWith(ERN_SHOPPINGLIST)) {
					list = Shoppinglist.fromJSON(objects);
				} else if (ern.startsWith(ERN_SHOPPINGLISTITEM)) {
					list = ShoppinglistItem.fromJSON(objects);
				}
				
			} else {
				EtaLog.d(TAG, "ArrayElements does not contain an ERN");
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
			if (object.has(Key.ERN)) {
				
				String ern = object.getString(Key.ERN);
					
				if (ern.startsWith(ERN_CATALOG)) {
					item = Catalog.fromJSON(object);
				} else if (ern.startsWith(ERN_DEALER)) {
					item = Dealer.fromJSON(object);
				} else if (ern.startsWith(ERN_OFFER)) {
					item = Offer.fromJSON(object);
				} else if (ern.startsWith(ERN_STORE)) {
					item = Store.fromJSON(object);
				} else if (ern.startsWith(ERN_SHOPPINGLIST)) {
					item = Shoppinglist.fromJSON(object);
				} else if (ern.startsWith(ERN_SHOPPINGLISTITEM)) {
					item = ShoppinglistItem.fromJSON(object);
				} 
					
				
			} else {
				EtaLog.d(TAG, "JSONObject does not contain an ERN");
			}
					
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return (T) item;
	}
	
}
