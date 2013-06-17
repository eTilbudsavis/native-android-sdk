package com.eTilbudsavis.etasdk;

import java.util.ArrayList;

import Utils.Endpoint;
import Utils.Utilities;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.User;

public class ShoppinglistManager {

	public static final String TAG = "ShoppinglistManager";

	private Eta mEta;
	private DbHelper mDbHelper;
	private SQLiteDatabase mDatabase;
	private int mListSyncInterval = 20000;
	private int mItemSyncInterval = 6000;
	private ArrayList<ShoppinglistListener> mSubscribers = new ArrayList<ShoppinglistManager.ShoppinglistListener>();
	
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
		mDbHelper = new DbHelper(mEta.getContext());
	}

	public Shoppinglist getList(String id) {
		Cursor c = mDatabase.rawQuery("SELECT * FROM " + DbHelper.TABLE_SL + " WHERE " + DbHelper.SL_ID + "=" + id, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			return DbHelper.curToSl(c);
		}
		return null;
	}

	public void addList(String name) {
		addList(Shoppinglist.fromName(name));
	}
	
	public void addList(Shoppinglist list) {
		
		// Make sure the UUID does not exist, before adding it
		while (getList(list.getId()) != null) {
			list.setId(Utilities.createUUID());
		};
		
		// Insert the new list to DB
		mDatabase.insert(DbHelper.TABLE_SL, null, DbHelper.slToCV(list));
		
		// Sync online if possible
		if (maySync()) {
			Bundle apiParams = new Bundle();
			apiParams.putString(Shoppinglist.PARAM_MODIFIED, list.getModifiedString());
			apiParams.putString(Shoppinglist.PARAM_NAME, list.getName());
			apiParams.putString(Shoppinglist.PARAM_ACCESS, list.getAccess());
			
			mEta.api().put(Endpoint.getShoppinglistId(user().getId(), list.getId()), new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {
					
					if (statusCode == 200) {
						Utilities.logd(TAG, data);
					} else {
						Utilities.logd(TAG, error.toString());
					}
				}
			}, apiParams).setDebug(true).execute();
		}
	}

	public void editList() {
		
	}

	public void deleteList() {
		
	}

	public void addItem() {
		
	}

	public void editItem() {
		
	}

	public void deleteItem() {
		
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
					
					ArrayList<Shoppinglist> sl = Shoppinglist.fromJSONArray(data);
					listCheck(sl);
					
				} else {
					mEta.addError(error);
					Utilities.logd(TAG, error.toString());
				}
				notifySubscribers();
				
			}
		};
		mEta.api().get(Endpoint.getShoppinglistList(user().getId()), listener).execute();
	}
	
	private void listCheck(ArrayList<Shoppinglist> lists) {
		
		ArrayList<Shoppinglist> old = getAllLists();
		
		
		
	}
	
	public void itemSync() {

		if (!maySync()) return;
		
		for (Shoppinglist sl : getAllLists()) {

			Api.CallbackString listener = new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {
					
					if (statusCode == 200) {
						
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
	
	/**
	 * Start synchronization with server
	 */
	public void startSync() {
		if (user().isLoggedIn()) {
			mListSync.run();
			mItemSync.run();
		}
	}
	
	/**
	 * Stop synchronization to server
	 */
	public void stopSync() {
		mEta.getHandler().removeCallbacks(mListSync);
		mEta.getHandler().removeCallbacks(mItemSync);
	}

	/**
	 * Open a connection to database
	 */
	public void openDB() {
		mDatabase = mDbHelper.getWritableDatabase();
		if (getAllLists().size() == 0) {
			
		}
	}
	
	/**
	 * Close the database connection
	 */
	public void closeDB() {
		mDbHelper.close();
	}
	
	/**
	 * Get a shopping list from it's ID
	 * @param id of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public Shoppinglist getShoppinglist(String id) {
		Cursor c = mDatabase.rawQuery("SELECT * FROM " + DbHelper.TABLE_SL + " WHERE " + DbHelper.SL_ID + "=" + id, null);
		if (c.moveToFirst() ) {
			return DbHelper.curToSl(c);
		}
		return null;
	}

	/**
	 * Get a shopping list from it's human readable name
	 * @param id of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public Shoppinglist getShoppinglistFromName(String name) {
		Cursor c = mDatabase.rawQuery("SELECT * FROM " + DbHelper.TABLE_SL + " WHERE " + DbHelper.SL_NAME + "=" + name, null);
		if (c.moveToFirst() ) {
			return DbHelper.curToSl(c);
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
	public ArrayList<Shoppinglist> getAllLists() {
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		Cursor c = mDatabase.rawQuery("SELECT * FROM " + DbHelper.TABLE_SL + " WHERE 1", null);
		if (c.moveToFirst() ) {
			do {
				list.add(DbHelper.curToSl(c));
			} while (c.moveToNext());
		}
		return list;
	}
	
	public void clearDatabase() {
		mDatabase.execSQL("DROP TABLE IF EXISTS " + DbHelper.TABLE_SL);
		mDatabase.execSQL("DROP TABLE IF EXISTS " + DbHelper.TABLE_SLI);
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
