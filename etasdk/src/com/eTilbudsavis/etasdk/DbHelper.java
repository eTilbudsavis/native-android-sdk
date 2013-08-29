package com.eTilbudsavis.etasdk;

import java.util.ArrayList;

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
	
	private static DbHelper mDbHelper;
	private SQLiteDatabase mDatabase;
	
	private DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
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
		db.execSQL("DELETE FROM " + SL);
		db.execSQL("DELETE FROM " + SL_OFFLINE);
		db.execSQL("DELETE FROM " + SLI);
		db.execSQL("DELETE FROM " + SLI_OFFLINE);
	}

	public static DbHelper getInstance() {
		if (mDbHelper == null) {
			mDbHelper = new DbHelper(Eta.getInstance().getContext());
		}
		return mDbHelper;
	}
	
	/**
	 * Open a database connection to the shoppinglists and items.
	 */
	public void openDB() {
		mDatabase = getWritableDatabase();
	}
	
	/**
	 * Close the database connection.
	 */
	public void closeDB() {
		mDatabase.close();
	}
	
	/**
	 * Deletes all tables (<i>shoppinglists</i> and <i>shoppinglistitems</i>)
	 * and creates two new tables<br>
	 * This cannot be undone.
	 */
	public void clear() {
		clearUserDB();
		clearNonUserDB();
	}

	public void clearUserDB() {
		Cursor c = query("DELETE FROM " + SL);
		c.close();
		c = query("DELETE FROM " + SLI);
		c.close();
	}

	public void clearNonUserDB() {
		Cursor c = query("DELETE FROM " + SL_OFFLINE);
		c.close();
		c = query("DELETE FROM " + SLI_OFFLINE);
		c.close();
	}
	
	public static Shoppinglist cursorToSl(Cursor c) {
		Shoppinglist sl = Shoppinglist.fromName(c.getString(3));
		sl.setId(c.getString(0));
		sl.setErn(c.getString(1));
		sl.setModified(c.getString(2));
		sl.setAccess(c.getString(4));
		sl.setState(c.getInt(5));
		sl.getOwner().setEmail(c.getString(6));
		sl.getOwner().setAccess(c.getString(7));
		sl.getOwner().setAccepted(0 < c.getInt(8));
		return sl;
	}

	/**
	 * Creates a string of values, to insert into a table.<br>
	 * e.g.: (column1, column2) VALUES (value1, value2)
	 * @param sli to convert
	 * @return a string of the format: (column1, column2) VALUES (value1, value2)
	 */
	public static String listToValues(Shoppinglist sl) {
		return new StringBuilder()
		.append("(")
		.append(ID).append(",")
		.append(ERN).append(",")
		.append(MODIFIED).append(",")
		.append(NAME).append(",")
		.append(ACCESS).append(",")
		.append(STATE).append(",")
		.append(OWNER_USER).append(",")
		.append(OWNER_ACCESS).append(",")
		.append(OWNER_ACCEPTED)
		.append(") VALUES (")
		.append("'").append(sl.getId()).append("',")
		.append("'").append(sl.getErn()).append("',")
		.append("'").append(Utils.formatDate(sl.getModified())).append("',")
		.append("'").append(sl.getName()).append("',")
		.append("'").append(sl.getAccess()).append("',")
		.append(sl.getState()).append(",")
		.append("'").append(sl.getOwner().getEmail()).append("',")
		.append("'").append(sl.getOwner().getAccess()).append("',")
		.append(sl.getOwner().getAccepted() ? 1 : 0).append(")")
		.toString();
	}
	
	/**
	 * Method does not close the Cursor.
	 * @param cursor with data
	 * @return A shoppinglistitem
	 */
	public static ShoppinglistItem cursorToSli(Cursor cursor) {
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
	
	/**
	 * Creates a string of values, to insert into a table.<br>
	 * e.g.: (column1, column2) VALUES (value1, value2)
	 * @param sli to convert
	 * @return a string of the format: (column1, column2) VALUES (value1, value2)
	 */
	public static String itemToValues(ShoppinglistItem sli) {
		return new StringBuilder()
		.append("(")
		.append(ID).append(",")
		.append(ERN).append(",")
		.append(MODIFIED).append(",")
		.append(DESCRIPTION).append(",")
		.append(COUNT).append(",")
		.append(TICK).append(",")
		.append(OFFER_ID).append(",")
		.append(CREATOR).append(",")
		.append(SHOPPINGLIST_ID).append(",")
		.append(STATE)
		.append(") VALUES (")
		.append("'").append(sli.getId()).append("',")
		.append("'").append(sli.getErn()).append("',")
		.append("'").append(Utils.formatDate(sli.getModified())).append("',")
		.append("'").append(sli.getDescription()).append("',")
		.append(sli.getCount()).append(",")
		.append(sli.isTicked() ? 1 : 0).append(",")
		.append("'").append(sli.getOfferId()).append("',")
		.append("'").append(sli.getCreator()).append("',")
		.append("'").append(sli.getShoppinglistId()).append("',")
		.append(sli.getState()).append(")")
		.toString();
	}
	
	/**
	 * Insert new shopping list into DB
	 * @param list to insert
	 * @return number of affected rows
	 */
	public int insertList(Shoppinglist list) {
		String q = "INSERT INTO " + getTableList() + " " + listToValues(list) + ";";
		Cursor c = query(q);
		return cursorCount(c);
	}

	/**
	 * Get a shoppinglist by it's id
	 * @param id to get from db
	 * @return A shoppinglist or null if no march is found
	 */
	public Shoppinglist getList(String id) {
		String q = "SELECT * FROM " + getTableList() + " WHERE " + ID + "='" + id + "';";
		Cursor c = query(q);
		Shoppinglist sl = c.moveToFirst() ? cursorToSl(c) : null;
		c.close();
		return sl;
	}

	/**
	 * Get all Shoppinglists that match the given name.
	 * @param name to match up against
	 * @return A list of shoppinglists
	 */
	public ArrayList<Shoppinglist> getListFromName(String name) {
		String q = "SELECT * FROM " + getTableList() + " WHERE " + NAME + "='" + name + "';";
		Cursor c = query(q);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.cursorToSl(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}
	
	/**
	 * Get all shoppinglists
	 * @return A list of shoppinglists
	 */
	public ArrayList<Shoppinglist> getLists() {
		String q = "SELECT * FROM " + getTableList() + ";";
		Cursor c = query(q);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.cursorToSl(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}
	
	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return number of affected rows
	 */
	public int deleteList(String shoppinglistId) {
		String q = "DELETE FROM " + getTableList() + " WHERE " + ID + "='" + shoppinglistId + "';";
		Cursor c = query(q);
		return cursorCount(c);
	}
	
	/**
	 * Replaces a shoppinglist, that have been updated in some way
	 * @param list that have been edited
	 * @return number of affected rows
	 */
	public int editList(Shoppinglist list) {
		String q = "REPLACE INTO " + getTableList() + " " + listToValues(list) + ";";
		Cursor c = query(q);
		return cursorCount(c);
	}

	/**
	 * Adds item to db, IF it does not yet exist, else nothing
	 * @param sli to add to db
	 * @return number of affected rows
	 */
	public int insertItem(ShoppinglistItem sli) {
		String q = "INSERT INTO " + getTableItem() + " " + itemToValues(sli) + ";";
		Cursor c = query(q);
		return cursorCount(c);
	}
	
	/**
	 * Adds a list of items to db, IF they do not yet exist, else nothing
	 * @param items to insert
	 * @return number of affected rows
	 */
	public int insertItems(ArrayList<ShoppinglistItem> items) {
		int count = 0;
		for (ShoppinglistItem sli : items) {
			count += insertItem(sli);
		}
		return count;
	}

	/**
	 * Get a shoppinglistitem from the db
	 * @param itemId to get from db
	 * @return A shoppinglistitem or null if no match is found
	 */
	public ShoppinglistItem getItem(String itemId) {
		String q = "SELECT * FROM " + getTableItem() + " WHERE " + ID + "='" + itemId + "';";
		Cursor c = query(q);
		ShoppinglistItem sli = c.moveToFirst() ? cursorToSli(c) : null;
		c.close();
		return sli;
	}

	/**
	 * Get all Shoppinglistitems that match the given description.
	 * @param description from which to get items
	 * @return A list of shoppinglistitems
	 */
	public ArrayList<ShoppinglistItem> getItemFromDescription(String description) {
		String q = "SELECT * FROM " + getTableItem() + " WHERE " + DESCRIPTION + "='" + description + "';";
		Cursor c = query(q);
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.cursorToSli(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}
	
	/**
	 * Get all Shoppinglistitems from a shoppinglist.
	 * @param sl from which to get items
	 * @return A list of shoppinglistitems
	 */
	public ArrayList<ShoppinglistItem> getItems(Shoppinglist sl) {
		String q = "SELECT * FROM " + getTableItem() + " WHERE " + SHOPPINGLIST_ID + "='" + sl.getId() + "';";
		Cursor c = query(q);
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.cursorToSli(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}

	/**
	 * Deletes an item from db
	 * @param id of the item to delete
	 * @return the number of rows affected
	 */
	public int deleteItem(String id) {
		String q = "DELETE FROM " + getTableItem() + " WHERE " + ID + "='" + id + "';";
		Cursor c = query(q);
		return cursorCount(c);
	}

	/**
	 * Deletes all items from a specific shopping list<br>
	 * true = Ticked<br>
	 * false = Unticked<br>
	 * null = All items<br>
	 * @param shoppinglistId to remove items from
	 * @param state that items must have to be removed
	 * @return number of affected rows
	 */
	public int deleteItems(String shoppinglistId, Boolean state) {
		String q = "DELETE FROM " + getTableItem() + " WHERE " + SHOPPINGLIST_ID + "='" + shoppinglistId;
		if (state != null) {
			q += "' AND " + TICK + "='" + String.valueOf(state);
		}
		q += "';";
		Cursor c = query(q);
		return cursorCount(c);
	}

	/**
	 * replaces an item in db
	 * @param sli to insert
	 * @return number of affected rows
	 */
	public int editItem(ShoppinglistItem sli) {
		String q = "REPLACE INTO " + getTableItem() + " " + itemToValues(sli) + ";";
		Cursor c = query(q);
		return cursorCount(c);
	}

	private Cursor query(String query) {
		Cursor c;
		if (mDatabase.isOpen()) {
			c = mDatabase.rawQueryWithFactory(null, query, null, null);
		} else {
			Utils.logd(TAG, "Closed DB: " + query);
			mDatabase = getWritableDatabase();
			c = mDatabase.rawQueryWithFactory(null, query, null, null);
			mDatabase.close();
		}
		return c;
	}
	
	private int cursorCount(Cursor c) {
		int i = c == null ? 0 : c.getCount();
		c.close();
		return i;
	}
	
	/**
	 * Get the table that should currently be used for shopping list items
	 * @return table name
	 */
	private String getTableItem() {
		return Eta.getInstance().getUser().isLoggedIn() ? SLI : SLI_OFFLINE;
	}

	/**
	 * Get the table that should currently be used for shopping list 
	 * @return table name
	 */
	private String getTableList() {
		return Eta.getInstance().getUser().isLoggedIn() ? SL : SL_OFFLINE;
	}
	
}
