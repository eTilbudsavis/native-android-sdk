package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import Utils.Endpoint;
import Utils.Utilities;

import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.EtaObjects.User;

public class ShoppinglistManager {

	public static final String TAG = "ShoppinglistManager";
	
	private Eta mEta;
	private HashMap<String, Shoppinglist> mLists = new HashMap<String, Shoppinglist>();
	private ArrayList<ShoppinglistListener> mSubscribers = new ArrayList<ShoppinglistManager.ShoppinglistListener>();

	private int mListSyncInterval = 20000;
	private int mItemSyncInterval = 20000;

	private Runnable mListSync = new Runnable() {
		
		public void run() {
			listSync();
			mEta.getHandler().postDelayed(mListSync, mListSyncInterval);
		}
	};

	private Runnable mItemSync = new Runnable() {
		
		public void run() {
			itemSync();
			mEta.getHandler().postDelayed(mItemSync, mItemSyncInterval);
		}
	};
	
	public ShoppinglistManager(Eta eta) {
		mEta = eta;
	}
	
	private boolean maySync() {

		if (!user().isLoggedIn()) {
			Utilities.logd(TAG, "No user loggedin, cannot sync shoppinglists");
			stopSync();
			return false;
		}
		return true;
	}
	
	public void listSync() {
		
		if (!maySync()) return;
		
		Api.CallbackString listener = new Api.CallbackString() {
			
			public void onComplete(int statusCode, String data, EtaError error) {
				
				if (statusCode == 200) {
					for (Shoppinglist sl : Shoppinglist.fromJSONArray(data))
						mLists.put(sl.getId(), sl);
						
				} else {
					mEta.addError(error);
					Utilities.logd(TAG, error.toString());
				}
				notifySubscribers();
				
			}
		};
		mEta.api().get(Endpoint.getShoppinglistList(user().getId()), listener).execute();
	}
	
	public void itemSync() {

		if (!maySync()) return;
		
		for (Shoppinglist sl : mLists.values()) {

			Api.CallbackString listener = new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {
					
					if (statusCode == 200) {
						for (ShoppinglistItem sli : ShoppinglistItem.fromJSONArray(data)) {
							Utilities.logd(TAG, sli.getTitle());
						}
					} else {
						mEta.addError(error);
						Utilities.logd(TAG, error.toString());
					}
					notifySubscribers();
				}
			};
			
			mEta.api().get(Endpoint.getShoppinglistItemList(user().getId(), sl.getId()), listener).execute();
		}
		
	}
	
	public void startSync() {
		if (user().isLoggedIn()) {
			mListSync.run();
			mItemSync.run();
		}
	}
	
	public void stopSync() {
		mEta.getHandler().removeCallbacks(mListSync);
		mEta.getHandler().removeCallbacks(mItemSync);
	}
	
	/**
	 * Get a shopping list from it's ID
	 * @param id of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public Shoppinglist getShoppinglist(String id) {
		return mLists.get(id);
	}

	/**
	 * Get a shopping list from it's human readable name
	 * @param id of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public Shoppinglist getShoppinglistFromName(String name) {
		for (Shoppinglist sl : mLists.values()) {
			if (sl.getName() != null)
				if (sl.getName().equals(name))
					return sl;
		}
		return null;
	}

	private User user() {
		return mEta.getSession().getUser();
	}
	/**
	 * The complete set of shopping lists
	 * @return <li>All shopping lists
	 */
	public HashMap<String, Shoppinglist> getShoppinglists() {
		return mLists;
	}
	
	public ShoppinglistManager subscribe(ShoppinglistListener listener) {
		mSubscribers.add(listener);
		return this;
	}

	public boolean unsubscribe(ShoppinglistListener listener) {
		return mSubscribers.remove(listener);
	}
	
	public ShoppinglistManager notifySubscribers() {
		for (ShoppinglistListener s : mSubscribers)
			s.onUpdate();
		
		return this;
	}
	
	public interface ShoppinglistListener {
		public void onUpdate();
	}
}
