package com.eTilbudsavis.etasdk;

import java.util.ArrayList;

import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.Tools.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	public static final String TAG = "DbHelper";
	
	private static final String DB_NAME = "shoppinglist.db";
	private static final int DB_VERSION = 1;
	
	public static final String SL = "shoppinglists";
	public static final String SLI = "shoppinglistitems";
	
	public static final String ID = "id";
	public static final String MODIFIED = "modified";
	public static final String ERN = "ern";
	public static final String NAME = "name";
	public static final String ACCESS = "access";
	public static final String STATE = "state";
	public static final String OWNER_USER = "owner_user";
	public static final String OWNER_ACCESS = "owner_access";
	public static final String OWNER_ACCEPTED = "owner_accepted";
	public static final String DESCRIPTION = "description";
	public static final String COUNT = "count";
	public static final String TICK = "tick";
	public static final String OFFER_ID = "offer_id";
	public static final String CREATOR = "creator";
	public static final String SHOPPINGLIST_ID = "shopping_list_id";

	private SQLiteDatabase mDatabase;
	
	// Shoppinglist table
	private static final String DB_CREATE_SL = 
		"create table if not exists " + SL + "(" + 
		ID + " text primary key, " + 
		MODIFIED + " text not null, " + 
		ERN + " text, " + 
		NAME + " text not null, " + 
		ACCESS + " text not null, " + 
		STATE + " integer not null, " + 
		OWNER_USER + " text, " + 
		OWNER_ACCESS + " text, " + 
		OWNER_ACCEPTED + " integer " + 
		");";

	// Shoppinglist item table
	private static final String DB_CREATE_SLI = 
		"create table if not exists " + SLI + "(" + 
		ID + " text primary key, " + 
		ERN + " text not null, " + 
		MODIFIED + " text not null, " + 
		DESCRIPTION + " text, " + 
		COUNT + " integer not null, " + 
		TICK + " integer not null, " + 
		OFFER_ID + " text, " + 
		CREATOR + " text not null, " + 
		SHOPPINGLIST_ID + " text not null, " + 
		STATE + " integer not null " + 
		");";

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DB_CREATE_SL);
		database.execSQL(DB_CREATE_SLI);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Utilities.logd(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + SL);
		db.execSQL("DROP TABLE IF EXISTS " + SLI);
		onCreate(db);
	}

	public static Shoppinglist curToSl(Cursor c) {
		Shoppinglist sl = Shoppinglist.fromName("");
		sl.setId(c.getString(0));
		sl.setModified(c.getString(1));
		sl.setErn(c.getString(2));
		sl.setName(c.getString(3));
		sl.setAccess(c.getString(4));
		sl.setState(c.getInt(5));
		sl.getOwner().setUser(c.getString(6));
		sl.getOwner().setAccess(c.getString(7));
		sl.getOwner().setAccepted(0 < c.getInt(8));
		return sl;
	}
	
	public static ContentValues slToCV(Shoppinglist sl) {
		ContentValues c = new ContentValues();
		c.put(ID, sl.getId());
		c.put(MODIFIED, sl.getModifiedString());
		c.put(ERN, sl.getErn());
		c.put(NAME, sl.getName());
		c.put(ACCESS, sl.getAccess());
		c.put(STATE, sl.getState());
		c.put(OWNER_USER, sl.getOwner().getUser());
		c.put(OWNER_ACCESS, sl.getOwner().getAccess());
		c.put(OWNER_ACCEPTED, sl.getOwner().getAccepted());
		return c;
	}

	/**
	 * Method does not close the Cursor.
	 * @param cursor with data
	 * @return A shoppinglistitem
	 */
	public static ShoppinglistItem curToSli(Cursor cursor) {
		ShoppinglistItem sli = new ShoppinglistItem();
		sli.setId(cursor.getString(0));
		sli.setErn(cursor.getString(1));
		sli.setModified(cursor.getLong(2));
		sli.setDescription(cursor.getString(3));
		sli.setCount(cursor.getInt(4));
		sli.setTick(0 < cursor.getInt(5));
		sli.setOfferId(cursor.getString(6));
		sli.setCreator(cursor.getString(7));
		sli.setShoppinglistId(cursor.getString(8));
		sli.setState(cursor.getInt(9));
		return sli;
	}

	public static ContentValues sliToCV(ShoppinglistItem sli) {
		ContentValues c = new ContentValues();
		c.put(ID, sli.getId());
		c.put(ERN, sli.getErn());
		c.put(MODIFIED, sli.getModified());
		c.put(DESCRIPTION, sli.getDescription());
		c.put(COUNT, sli.getCount());
		c.put(TICK, sli.isTicked());
		c.put(OFFER_ID, sli.getOfferId());
		c.put(CREATOR, sli.getCreator());
		c.put(SHOPPINGLIST_ID, sli.getShoppinglistId());
		c.put(STATE, sli.getState());
		return c;
	}

	/**
	 * Open a connection to database<br>
	 * If Eta.onResume() is called, there should be no need to call this.
	 */
	public void openDB() {
		mDatabase = getWritableDatabase();
	}
	
	/**
	 * Close the database connection<br>
	 * If Eta.onPause() is called, there should be no need to call this.
	 */
	public void closeDB() {
		close();
	}

	/**
	 * Deletes all tables (<i>shoppinglists</i> and <i>shoppinglistitems</i>)
	 * and creates two new tables<br>
	 * This cannot be undone.
	 */
	public void clearDatabase() {
		onUpgrade(mDatabase, 0, 0);
	}
	
	/**
	 * Insert new shoppinglist into db
	 * @param list to insert
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long insertList(Shoppinglist list) {
		return mDatabase.insert(SL, null, DbHelper.slToCV(list));
	}

	/**
	 * Get a shopping list by it's id
	 * @param id to get
	 * @return A cursor with a shopping list that matches the id, or empty cursor 
	 */
	public Cursor getList(String id) {
		return mDatabase.query(SL, null, DbHelper.ID + "=?", new String[]{id}, null, null, null, null);
	}

	/**
	 * Check if a list by a given id exists in db.<br>
	 * Good for checking if a UUID exists before inserting a new list.
	 * @param id to check for
	 * @return true if it exists, else false
	 */
	public boolean existsList(String id) {
		Cursor c = getList(id);
		boolean res = c.moveToFirst();
		c.close();
		return res;
	}
	
	/**
	 * Get a shopping list from it's readable name
	 * @param name of the shopping list
	 * @return Cursor with shopping lists that matches name
	 */
	public Cursor getListFromName(String name) {
		return mDatabase.query(SL, null, DbHelper.NAME + "=?", new String[]{name}, null, null, null);
	}
	
	/**
	 * Get all shoppinglists
	 * @return Cursor with shoppinglists from db
	 */
	public Cursor getLists() {
		return mDatabase.query(SL, null, null, null, null, null, null);
	}
	
	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return the number of rows affected.
	 */
	public int deleteList(String shoppinglistId) {
		return mDatabase.delete(SL, DbHelper.ID + "=?", new String[]{shoppinglistId});
	}
	
	/**
	 * Replaces a shoppinglist, that have been updated in some way
	 * @param list that have been edited
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long editList(Shoppinglist list) {
		return mDatabase.replace(SL, null, DbHelper.slToCV(list));
	}

	/**
	 * Adds a list of items to db, IF they do not yet exist, else nothing
	 * @param items to insert
	 * @return true if all items have successfully been inserted, else false
	 */
	public boolean addItems(ArrayList<ShoppinglistItem> items) {
		boolean resp = true;
		for (ShoppinglistItem sli : items)
			if (addItem(sli) == -1)
				resp = false;
		
		return resp;
	}

	/**
	 * Get a shopping list item from the db
	 * @param itemId to get from db
	 * @return A Cursor object, which is positioned before the first entry
	 */
	public Cursor getItem(String itemId) {
		return mDatabase.query(SLI, null, DbHelper.ID + "=?", new String[]{itemId}, null, null, null);
	}

	/**
	 * Get a shopping lite item from the db
	 * @param id to get from db
	 * @return A Cursor object, which is positioned before the first entry
	 */
	public Cursor getItems(Shoppinglist sl) {
		return mDatabase.query(SLI, null, DbHelper.SHOPPINGLIST_ID + "=?", new String[]{sl.getId()}, null, null, null);
	}

	/**
	 * Check if a given item id exists in db
	 * @param id to check for
	 * @return true, if it exists, else false
	 */
	public boolean existsItem(String id) {
		Cursor c = getItem(id);
		boolean res = c.moveToFirst();
		c.close();
		return res;
	}
	
	/**
	 * Adds item to db, IF it does not yet exist, else nothing
	 * @param sli to add to db
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long addItem(ShoppinglistItem sli) {
		return existsItem(sli.getId()) ? -1 : mDatabase.insert(SLI, null, DbHelper.sliToCV(sli));
	}
	
	/**
	 * Deletes all items from a specific shopping list<br>
	 * true = Ticked<br>
	 * false = Unticked<br>
	 * null = All items<br>
	 * @param shoppinglistId to remove items from
	 * @return number of affected rows
	 */
	public int deleteItems(String shoppinglistId, Boolean state) {
		
		if (state == null) {
			return mDatabase.delete(SLI, DbHelper.ID + "=?", new String[]{shoppinglistId});
		} else {
			return mDatabase.delete(SLI, DbHelper.ID + "=? AND " + DbHelper.TICK + "=?", new String[]{shoppinglistId, String.valueOf(state)});
		}
	}

	/**
	 * Deletes an item from db
	 * @param id of the item to delete
	 * @return the number of rows affected
	 */
	public int deleteItem(String id) {
		return mDatabase.delete(SLI, DbHelper.ID + "=?", new String[]{id});
	}

	/**
	 * replaces an item in db
	 * @param sli to insert
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long editItem(ShoppinglistItem sli) {
		return mDatabase.replace(SLI, null, DbHelper.sliToCV(sli));
	}

	
}
