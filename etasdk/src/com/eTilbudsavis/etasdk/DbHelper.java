package com.eTilbudsavis.etasdk;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class DbHelper extends SQLiteOpenHelper {

	public static final String TAG = "DbHelper";
	
	private static final String DB_NAME = "shoppinglist.db";
	private static final int DB_VERSION = 1;

	public static final String SL = "shoppinglists";
	public static final String SLI = "shoppinglistitems";
	
	public static final String SL_OFFLINE = "shoppinglists_offline";
	public static final String SLI_OFFLINE = "shoppinglistitems_offline";
	
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
	private Eta mEta;
	
	// Shoppinglist table
	private String dbCreateSL(String name) {
		return "create table if not exists " + name + "(" + 
				ID + " text primary key, " + 
				ERN + " text, " + 
				MODIFIED + " text not null, " + 
				NAME + " text not null, " + 
				ACCESS + " text not null, " + 
				STATE + " integer not null, " + 
				OWNER_USER + " text, " + 
				OWNER_ACCESS + " text, " + 
				OWNER_ACCEPTED + " integer " + 
				");";
	}
	
	private String dbCreateSLI(String name) {
		return "create table if not exists " + name + "(" + 
				ID + " text not null primary key, " + 
				ERN + " text not null, " + 
				MODIFIED + " text not null, " + 
				DESCRIPTION + " text, " + 
				COUNT + " integer not null, " + 
				TICK + " integer not null, " + 
				OFFER_ID + " text, " + 
				CREATOR + " text, " + 
				SHOPPINGLIST_ID + " text not null, " + 
				STATE + " integer not null " + 
				");";
	}
	
	public DbHelper(Context context, Eta eta) {
		super(context, DB_NAME, null, DB_VERSION);
		mEta = eta;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(dbCreateSL(SL));
		database.execSQL(dbCreateSL(SL_OFFLINE));
		database.execSQL(dbCreateSLI(SLI));
		database.execSQL(dbCreateSLI(SLI_OFFLINE));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Utils.logd(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DELETE FROM " + SL + " WHERE 1");
		db.execSQL("DELETE FROM " + SL_OFFLINE + " WHERE 1");
		db.execSQL("DELETE FROM " + SLI + " WHERE 1");
		db.execSQL("DELETE FROM " + SLI_OFFLINE + " WHERE 1");
	}
	
	public void clear() {
		onUpgrade(mDatabase, 0, 0);
	}

	public static Shoppinglist curToSl(Cursor c) {
		Shoppinglist sl = Shoppinglist.fromName(c.getString(3));
		sl.setId(c.getString(0));
		sl.setErn(c.getString(1));
		sl.setModified(c.getString(2));
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
		c.put(ERN, sl.getErn());
		c.put(MODIFIED, Utils.formatDate(sl.getModified()));
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
		sli.setModified(Utils.parseDate(cursor.getString(2)));
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
		c.put(MODIFIED, Utils.formatDate(sli.getModified()));
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
	 * Insert new shopping list into DB
	 * @param list to insert
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long insertList(Shoppinglist list) {
		return mDatabase.insert(listTable(), null, slToCV(list));
	}

	/**
	 * Get a shopping list by it's id
	 * @param id to get
	 * @return A cursor with a shopping list that matches the id, or empty cursor 
	 */
	public Cursor getList(String id) {
		return mDatabase.query(listTable(), null, ID + "=?", new String[]{id}, null, null, null, null);
	}

	/**
	 * Get a shopping list from it's readable name
	 * @param name of the shopping list
	 * @return Cursor with shopping lists that matches name
	 */
	public Cursor getListFromName(String name) {
		return mDatabase.query(listTable(), null, NAME + "=?", new String[]{name}, null, null, null);
	}
	
	/**
	 * Get all shoppinglists
	 * @return Cursor with shoppinglists from db
	 */
	public Cursor getLists() {
		return mDatabase.query(listTable(), null, null, null, null, null, null);
	}
	
	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return the number of rows affected.
	 */
	public int deleteList(String shoppinglistId) {
		return mDatabase.delete(listTable(), ID + "=?", new String[]{shoppinglistId});
	}
	
	/**
	 * Replaces a shoppinglist, that have been updated in some way
	 * @param list that have been edited
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long editList(Shoppinglist list) {
		return mDatabase.replace(listTable(), null, slToCV(list));
	}

	/**
	 * Adds item to db, IF it does not yet exist, else nothing
	 * @param sli to add to db
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long insertItem(ShoppinglistItem sli) {
		return mDatabase.insert(itemTable(), null, sliToCV(sli));
	}
	
	/**
	 * Adds a list of items to db, IF they do not yet exist, else nothing
	 * @param items to insert
	 * @return true if all items have successfully been inserted, else false
	 */
	public boolean insertItems(ArrayList<ShoppinglistItem> items) {
		boolean resp = true;
		for (ShoppinglistItem sli : items) {
			if (insertItem(sli) == -1)
				resp = false;
		}
		
		return resp;
	}

	/**
	 * Get a shopping list item from the db
	 * @param itemId to get from db
	 * @return A Cursor object, which is positioned before the first entry
	 */
	public Cursor getItem(String itemId) {
		return mDatabase.query(itemTable(), null, ID + "=?", new String[]{itemId}, null, null, null);
	}

	/**
	 * Get a shopping list from it's readable name
	 * @param name of the shopping list
	 * @return Cursor with shopping lists that matches name
	 */
	public Cursor getItemFromDescription(String description) {
		String q = "SELECT * FROM " + itemTable() + " WHERE " + DESCRIPTION + "=" + description;
		mDatabase.rawQuery(q, null);
		return mDatabase.query(itemTable(), null, DESCRIPTION + "=?", new String[]{description}, null, null, null);
	}
	
	/**
	 * Get a shopping lite item from the db
	 * @param id to get from db
	 * @return A Cursor object, which is positioned before the first entry
	 */
	public Cursor getItems(Shoppinglist sl) {
		return mDatabase.query(itemTable(), null, SHOPPINGLIST_ID + "=?", new String[]{sl.getId()}, null, null, null);
	}

	/**
	 * Deletes an item from db
	 * @param id of the item to delete
	 * @return the number of rows affected
	 */
	public int deleteItem(String id) {
//		String q = "DELETE FROM " + itemTable() + " WHERE " + ID + "=" + id;
//		return cursorCount(query(q));
		return mDatabase.delete(itemTable(), ID + "=?", new String[]{id});
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
			return mDatabase.delete(itemTable(), SHOPPINGLIST_ID + "=?", new String[]{shoppinglistId});
		} else {
			return mDatabase.delete(itemTable(), SHOPPINGLIST_ID + "=? AND " + TICK + "=?", new String[]{shoppinglistId, String.valueOf(state)});
		}
	}

	/**
	 * replaces an item in db
	 * @param sli to insert
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long editItem(ShoppinglistItem sli) {
		return mDatabase.replace(SLI, null, sliToCV(sli));
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
	
	private Cursor query(String query) {
		return mDatabase.rawQueryWithFactory(null, query, null, null);
	}
	
	private int cursorCount(Cursor c) {
		if (c == null) {
			return 0;
		} else {
			int i = c.getCount();
			c.close();
			return i;
		}
	}
	/**
	 * Get the table that should currently be used for shopping list items
	 * @return table name
	 */
	private String itemTable() {
		return mEta.getUser().isLoggedIn() ? SLI : SLI_OFFLINE;
	}

	/**
	 * Get the table that should currently be used for shopping list 
	 * @return table name
	 */
	private String listTable() {
		return mEta.getUser().isLoggedIn() ? SL : SL_OFFLINE;
	}

}
