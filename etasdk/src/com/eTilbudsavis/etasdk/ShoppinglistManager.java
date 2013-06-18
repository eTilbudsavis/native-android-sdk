package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Tools.Endpoint;
import com.eTilbudsavis.etasdk.Tools.Utilities;

public class ShoppinglistManager {

	public static final String TAG = "ShoppinglistManager";

	private static final String SHOPPINGLIST = DbHelper.SL;
	private static final String SHOPINGLISTITEM = DbHelper.SLI;

	private Eta mEta;
	private DbHelper mDbHelper;
	private SQLiteDatabase mDatabase;
	private int mListSyncInterval = 20000;
	private int mItemSyncInterval = 6000;
	private ArrayList<ShoppinglistListener> mSubscribers = new ArrayList<ShoppinglistManager.ShoppinglistListener>();
	
	private Runnable mListSync = new Runnable() {
		
		public void run() {
			syncLists();
			mEta.getHandler().postDelayed(mListSync, mListSyncInterval);
		}
	};

	private Runnable mItemSync = new Runnable() {
		
		public void run() {
			syncItems();
			mEta.getHandler().postDelayed(mItemSync, mItemSyncInterval);
		}
	};
	
	public ShoppinglistManager(Eta eta) {
		mEta = eta;
		mDbHelper = new DbHelper(mEta.getContext());
	}

	public Shoppinglist getList(String id) {
		Cursor c = dbGetList(id);
		return c.moveToFirst() == true ? DbHelper.curToSl(c) : null;
	}

	public boolean addList(Shoppinglist list) {
		
		if (!mustSync()) return false;
		
		// Make sure the UUID does not exist, before adding it
		while (getList(list.getId()) != null) {
			list.setId(Utilities.createUUID());
		};
		
		// Insert the new list to DB
		dbInsertList(list);
		
		// Sync online if possible
		mEta.api().put(Endpoint.getShoppinglistId(user().getId(), list.getId()), new Api.CallbackString() {
			
			public void onComplete(int statusCode, String data, EtaError error) {
				
				Utilities.logd(TAG, statusCode, data, error);
				
			}
		}, list.getApiParams()).setDebug(true).execute();
		
		return true;
	}

	public boolean editList(Shoppinglist list) {
		
		long l = dbEditList(list);
		
		if (mustSync()) {
			
			mEta.api().put(Endpoint.getShoppinglistId(user().getId(), list.getId()), new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {

					Utilities.logd(TAG, statusCode, data, error);
					
				}
			}, list.getApiParams()).execute();
		}
		
		return l == -1 ? false : true;
		
	}

	public void deleteList(Shoppinglist list) {
		deleteList(list.getId());
	}

	public void deleteList(String id) {
		int count = dbDeleteList(id);
		Utilities.logd(TAG, String.valueOf(count));
		if (mustSync()) {
			mEta.api().delete(Endpoint.getShoppinglistId(user().getId(), id), new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {
					
					Utilities.logd(TAG, statusCode, data, error);
					
				}
			}, new Bundle()).setDebug(true).execute();
		}
	}

	public void addItem(Shoppinglist sl, ShoppinglistItem sli) {
		
	}

	public void editItem(ShoppinglistItem sli) {
		
	}

	public void deleteAllItems(Shoppinglist sl) {
		
	}
	
	public void deleteItem(ShoppinglistItem sli) {
		
	}

	private long dbInsertList(Shoppinglist list) {
		return mDatabase.insert(SHOPPINGLIST, null, DbHelper.slToCV(list));
	}

	private Cursor dbGetList(String shoppinglistId) {
		return mDatabase.query(SHOPPINGLIST, null, DbHelper.ID, new String[]{shoppinglistId}, null, null, null);
	}

	private Cursor dbGetAllLists() {
		return mDatabase.query(SHOPPINGLIST, null, null, null, null, null, null);
	}
	
	private int dbDeleteList(String shoppinglistId) {
		return mDatabase.delete(SHOPPINGLIST, DbHelper.ID, new String[]{shoppinglistId});
	}
	
	private long dbEditList(Shoppinglist list) {
		return mDatabase.replace(SHOPPINGLIST, null, DbHelper.slToCV(list));
	}

	private boolean dbAddItems(ArrayList<ShoppinglistItem> items) {
		for (ShoppinglistItem sli : items)
			if (dbAddItem(sli) == -1)
				return false;
		
		return true;
	}
	
	private long dbAddItem(ShoppinglistItem sli) {
		return mDatabase.insert(SHOPINGLISTITEM, null, DbHelper.sliToCV(sli));
	}
	
	private int dbDeleteItems(String shoppinglistId) {
		return mDatabase.delete(SHOPINGLISTITEM, DbHelper.SHOPPINGLIST_ID, new String[]{shoppinglistId});
	}

	private int dbDeleteItem(String id) {
		return mDatabase.delete(SHOPINGLISTITEM, DbHelper.ID, new String[]{id});
	}
	
	public void syncLists() {
		
		if (!mustSync()) return;
		
		Api.CallbackString listener = new Api.CallbackString() {
			
			public void onComplete(int statusCode, String data, EtaError error) {

				if (statusCode == 200) {
					
					ArrayList<Shoppinglist> sl = Shoppinglist.fromJSONArray(data);
					mergeLists(sl);
					
				} else {
					mEta.addError(error);
					Utilities.logd(TAG, error.toString());
				}
				
			}
		};
		mEta.api().get(Endpoint.getShoppinglistList(user().getId()), listener).execute();
	}
	
	private void mergeLists(ArrayList<Shoppinglist> lists) {
		
		HashMap<String, Shoppinglist> dblist = new HashMap<String, Shoppinglist>();
		for (Shoppinglist sl : getAllLists()) {
			dblist.put(sl.getId(), sl);
		}

		HashMap<String, Shoppinglist> tmplists = new HashMap<String, Shoppinglist>();
		for (Shoppinglist sl : lists) {
			tmplists.put(sl.getId(), sl);
		}
		
		HashSet<String> union = new HashSet<String>();
		union.addAll(tmplists.keySet());
		union.addAll(dblist.keySet());

		List<String> added = new ArrayList<String>();
		List<String> deleted = new ArrayList<String>();
		List<String> edited = new ArrayList<String>();

		for (String key : union) {
			
			if (dblist.containsKey(key)) {
				if (tmplists.containsKey(key)) {
					if (!dblist.get(key).equals(tmplists.get(key))) {
						Shoppinglist nsl = tmplists.get(key);
						edited.add(key);
						dbEditList(nsl);
					}
				} else {
					deleted.add(key);
					dbDeleteList(key);
				}
			} else {
				added.add(key);
				dbInsertList(tmplists.get(key));
			}
		}
		
		// If no changes has been registeres, ship the rest
		if (added.isEmpty() && deleted.isEmpty() && edited.isEmpty())
			return;

		if (!added.isEmpty())
			for (String s : added)
				syncItems(getList(s));

		if (!deleted.isEmpty())
			for (String s : added)
				dbDeleteItems(s);

		if (!edited.isEmpty())
			for (String s : added)
				syncItems(getList(s));
		
		notifyListUpdate(added, deleted, edited);
		
	}
	
	public void syncItems() {

		if (!mustSync()) return;
		
		for (final Shoppinglist sl : getAllLists()) {
			if (!sl.isSynced()) {
				syncItems(sl);
			} else {
				
				Api.CallbackString cb = new Api.CallbackString() {
					
					public void onComplete(int statusCode, String data, EtaError error) {
						
						if (Utilities.isSuccess(statusCode)) {
							long m = sl.getModified();
							sl.setModified(data);
							if (m < sl.getModified())
								syncItems(sl);
						} else {
							mEta.addError(error);
							Utilities.logd(TAG, error.toString());
						}
					}
				};
				
				mEta.api().get(Endpoint.getShoppinglistModified(user().getId(), sl.getId()), cb).execute();
			}
			
		}
		
	}
	
	private void syncItems(final Shoppinglist sl) {

		Api.CallbackString listener = new Api.CallbackString() {
			
			public void onComplete(int statusCode, String data, EtaError error) {
				
				if (Utilities.isSuccess(statusCode)) {
					dbDeleteItems(sl.getId());
					dbAddItems(ShoppinglistItem.fromJSONArray(data, sl.getId()));
					sl.setSynced(true);
					notifyItemUpdate(sl.getId());
				} else {
					mEta.addError(error);
					Utilities.logd(TAG, error.toString());
				}
			}
		};
		
		mEta.api().get(Endpoint.getShoppinglistItemList(user().getId(), sl.getId()), listener).execute();
		
	}
	
	/**
	 * Start synchronization with server
	 */
	public void startSync() {
		if (mustSync()) {
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
			//TODO: Get shopping list name from localized string instead of hardcoding
			Shoppinglist sl = Shoppinglist.fromName("Shoppinglist");
			sl.setOffline(true);
			dbInsertList(sl);
		}
		syncLists();
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
		Cursor c = mDatabase.query(SHOPPINGLIST, null, DbHelper.ID, new String[]{id}, null, null, null);
		Shoppinglist sl = c.moveToFirst() == true ? DbHelper.curToSl(c) : null;
		c.close();
		return sl;
	}

	/**
	 * Get a shopping list from it's human readable name
	 * @param id of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public ArrayList<Shoppinglist> getShoppinglistFromName(String name) {
		Cursor c = mDatabase.query(SHOPPINGLIST, null, DbHelper.NAME, new String[]{name}, null, null, null);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.curToSl(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}

	private User user() {
		return mEta.getSession().getUser();
	}

	private boolean mustSync() {

		if (!user().isLoggedIn()) {
			Utilities.logd(TAG, "No user loggedin, cannot sync shoppinglists");
			stopSync();
			return false;
		}
		return true;
	}
	
	/**
	 * The complete set of shopping lists
	 * @return <li>All shopping lists
	 */
	public ArrayList<Shoppinglist> getAllLists() {
		Cursor c = dbGetAllLists();
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.curToSl(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}
	
	public void clearDatabase() {
		mDbHelper.onUpgrade(mDatabase, 0, 0);
	}
	
	public ShoppinglistManager subscribe(ShoppinglistListener listener) {
		mSubscribers.add(listener);
		return this;
	}

	public boolean unsubscribe(ShoppinglistListener listener) {
		return mSubscribers.remove(listener);
	}

	public ShoppinglistManager notifyListUpdate(List<String> added, List<String> deleted, List<String> edited) {
		for (ShoppinglistListener s : mSubscribers)
			s.onListUpdate(added, deleted, edited);
		
		return this;
	}

	public ShoppinglistManager notifyItemUpdate(String shoppinglistId) {
		for (ShoppinglistListener s : mSubscribers)
			s.onItemUpdate(shoppinglistId);
		
		return this;
	}
	
	public interface ShoppinglistListener {
		public void onListUpdate(List<String> added, List<String> deleted, List<String> edited);
		public void onItemUpdate(String shoppinglistId);
	}
	
}
