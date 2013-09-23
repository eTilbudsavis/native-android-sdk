package com.eTilbudsavis.etasdk;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eTilbudsavis.etasdk.EtaObjects.Share;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class DbHelper extends SQLiteOpenHelper {

	public static final String TAG = "DbHelper";
	
	private static final String DB_NAME = "shoppinglist.db";
	private static final int DB_VERSION = 1;
	private static final String NUM_OF_AFFECTED_ROWS = "SELECT changes() AS 'changes';";
	
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
	public static final String PREVIOUS_ID = "previous_id";
	
	private Object LOCK = new Object();
	
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
				OWNER_ACCEPTED + " integer, " + 
				PREVIOUS_ID + " text " + 
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
				STATE + " integer not null, " + 
				PREVIOUS_ID + " text " + 
				");";
	}
	
	private static DbHelper mDbHelper;
	private SQLiteDatabase mDatabase;
	private boolean mCloseDb = false;
	
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
	public synchronized void closeDB() {
		mCloseDb = false;
		mDatabase.close();
	}
	
	/**
	 * Deletes all tables (<i>shoppinglists</i> and <i>shoppinglistitems</i>)
	 * and creates two new tables<br>
	 * This cannot be undone.
	 */
	public synchronized void clear() {
		clearUserDB();
		clearNonUserDB();
	}

	public synchronized void clearUserDB() {
		execQueryWithChangesCount("DELETE FROM " + SL);
		execQueryWithChangesCount("DELETE FROM " + SLI);
	}

	public synchronized void clearNonUserDB() {
		execQueryWithChangesCount("DELETE FROM " + SL_OFFLINE);
		execQueryWithChangesCount("DELETE FROM " + SLI_OFFLINE);
	}
	
	public static Shoppinglist cursorToSl(Cursor c) {
		Shoppinglist sl = Shoppinglist.fromName(c.getString(c.getColumnIndex(NAME)));
		sl.setId(c.getString(c.getColumnIndex(ID)));
		sl.setErn(c.getString(c.getColumnIndex(ERN)));
		sl.setModified(c.getString(c.getColumnIndex(MODIFIED)));
		sl.setAccess(c.getString(c.getColumnIndex(ACCESS)));
		sl.setState(c.getInt(c.getColumnIndex(STATE)));
		sl.getOwner().setEmail(c.getString(c.getColumnIndex(OWNER_USER)));
		sl.getOwner().setAccess(c.getString(c.getColumnIndex(OWNER_ACCESS)));
		sl.getOwner().setAccepted(0 < c.getInt(c.getColumnIndex(OWNER_ACCEPTED)));
		sl.setPreviousId(c.getString(c.getColumnIndex(PREVIOUS_ID)));
		return sl;
	}

	/**
	 * Creates a string of values, to insert into a table.<br>
	 * e.g.: (column1, column2) VALUES (value1, value2)
	 * @param sli to convert
	 * @return a string of the format: (column1, column2) VALUES (value1, value2)
	 */
	public static String listToValues(Shoppinglist sl) {

		Share s = sl.getOwner();
		
		StringBuilder sb = new StringBuilder();
		sb.append("(")
		.append(ID).append(",")
		.append(ERN).append(",")
		.append(MODIFIED).append(",")
		.append(NAME).append(",")
		.append(ACCESS).append(",")
		.append(STATE).append(",")
		.append(OWNER_USER).append(",")
		.append(OWNER_ACCESS).append(",")
		.append(OWNER_ACCEPTED).append(",")
		.append(PREVIOUS_ID)
		.append(") VALUES (")
		.append(escape(sl.getId())).append(",")
		.append(escape(sl.getErn())).append(",")
		.append(escape(Utils.formatDate(sl.getModified()))).append(",")
		.append(escape(sl.getName())).append(",")
		.append(escape(sl.getAccess())).append(",")
		.append(escape(sl.getState())).append(",")
		.append(escape(s.getEmail())).append(",")
		.append(escape(s.getAccess())).append(",")
		.append(escape(s.getAccepted())).append(",")
		.append(escape(sl.getPreviousId())).append(")");
		
		String str = sb.toString();
		
//		Utils.logd(TAG, str);
		
		return str;
	}
	
	/**
	 * Method does not close the Cursor.
	 * @param cursor with data
	 * @return A shoppinglistitem
	 */
	public static ShoppinglistItem cursorToSli(Cursor cursor) {
		ShoppinglistItem sli = new ShoppinglistItem();
		sli.setId(cursor.getString(cursor.getColumnIndex(ID)));
		sli.setErn(cursor.getString(cursor.getColumnIndex(ERN)));
		sli.setModified(Utils.parseDate(cursor.getString(cursor.getColumnIndex(MODIFIED))));
		sli.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
		sli.setCount(cursor.getInt(cursor.getColumnIndex(COUNT)));
		sli.setTick(0 < cursor.getInt(cursor.getColumnIndex(TICK)));
		sli.setOfferId(cursor.getString(cursor.getColumnIndex(OFFER_ID)));
		sli.setCreator(cursor.getString(cursor.getColumnIndex(CREATOR)));
		sli.setShoppinglistId(cursor.getString(cursor.getColumnIndex(SHOPPINGLIST_ID)));
		sli.setState(cursor.getInt(cursor.getColumnIndex(STATE)));
		sli.setPreviousId(cursor.getString(cursor.getColumnIndex(PREVIOUS_ID)));
		return sli;
	}
	
	/**
	 * Creates a string of values, to insert into a table.<br>
	 * e.g.: (column1, column2) VALUES (value1, value2)
	 * @param sli to convert
	 * @return a string of the format: (column1, column2) VALUES (value1, value2)
	 */
	public static String itemToValues(ShoppinglistItem sli) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("(")
		.append(ID).append(",")
		.append(ERN).append(",")
		.append(MODIFIED).append(",")
		.append(DESCRIPTION).append(",")
		.append(COUNT).append(",")
		.append(TICK).append(",")
		.append(OFFER_ID).append(",")
		.append(CREATOR).append(",")
		.append(SHOPPINGLIST_ID).append(",")
		.append(STATE).append(",")
		.append(PREVIOUS_ID)
		.append(") VALUES (")
		.append(escape(sli.getId())).append(",")
		.append(escape(sli.getErn())).append(",")
		.append(escape(Utils.formatDate(sli.getModified()))).append(",")
		.append(escape(sli.getDescription())).append(",")
		.append(escape(sli.getCount())).append(",")
		.append(escape(sli.isTicked())).append(",")
		.append(escape(sli.getOfferId())).append(",")
		.append(escape(sli.getCreator())).append(",")
		.append(escape(sli.getShoppinglistId())).append(",")
		.append(escape(sli.getState())).append(",")
		.append(escape(sli.getPreviousId())).append(")");
		return sb.toString();
	}
		
	/**
	 * Insert new shopping list into DB
	 * @param list to insert
	 * @return number of affected rows
	 */
	public int insertList(Shoppinglist list) {
		String q = "INSERT INTO " + getTableList() + " " + listToValues(list) + ";";
		
		int i = execQueryWithChangesCount(q);
		
		Utils.logd(TAG, "c: " + String.valueOf(i) + ", q: " + q);
		
		return i;
	}

	/**
	 * Get a shoppinglist by it's id
	 * @param id to get from db
	 * @return A shoppinglist or null if no march is found
	 */
	public Shoppinglist getList(String id) {
		id = escape(id);
		String q = "SELECT * FROM " + getTableList() + " WHERE " + ID + "=" + id + ";";
		Cursor c = execQuery(q);
		Shoppinglist sl = c.moveToFirst() ? cursorToSl(c) : null;
		cursorClose(c);
		return sl;
	}

	/**
	 * Get all Shoppinglists that match the given name.
	 * @param name to match up against
	 * @return A list of shoppinglists
	 */
	public ArrayList<Shoppinglist> getListFromName(String name) {
		name = escape(name);
		String q = "SELECT * FROM " + getTableList() + " WHERE " + NAME + "='" + name + "';";
		Cursor c = execQuery(q);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(cursorToSl(c));
			} while (c.moveToNext());
		}
		cursorClose(c);
		return list;
	}
	
	/**
	 * Get all shoppinglists
	 * @return A list of shoppinglists
	 */
	public ArrayList<Shoppinglist> getLists() {
		String q = "SELECT * FROM " + getTableList() + " WHERE " + STATE + "!=" + ShoppinglistManager.STATE_DELETE + ";";
		Cursor c = execQuery(q);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(cursorToSl(c));
			} while (c.moveToNext());
		}
		cursorClose(c);
		return list;
	}
	
	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return number of affected rows
	 */
	public int deleteList(String shoppinglistId) {
		shoppinglistId = escape(shoppinglistId);
		String q = "DELETE FROM " + getTableList() + " WHERE " + ID + "=" + shoppinglistId + ";";
		return execQueryWithChangesCount(q);
	}
	
	/**
	 * Replaces a shoppinglist, that have been updated in some way
	 * @param list that have been edited
	 * @return number of affected rows
	 */
	public int editList(Shoppinglist list) {
		String q = "REPLACE INTO " + getTableList() + " " + listToValues(list) + ";";
		return execQueryWithChangesCount(q);
	}

	/**
	 * Adds item to db, IF it does not yet exist, else nothing
	 * @param sli to add to db
	 * @return number of affected rows
	 */
	public int insertItem(ShoppinglistItem sli) {
		String q = "INSERT INTO " + getTableItem() + " " + itemToValues(sli) + ";";
		return execQueryWithChangesCount(q);
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
		itemId = escape(itemId);
		String q = "SELECT * FROM " + getTableItem() + " WHERE " + ID + "=" + itemId + ";";
		Cursor c = execQuery(q);
		ShoppinglistItem sli = c.moveToFirst() ? cursorToSli(c) : null;
		cursorClose(c);
		return sli;
	}
	
	/**
	 * Get all Shoppinglistitems that match the given description.
	 * @param description from which to get items
	 * @return A list of shoppinglistitems
	 */
	public ArrayList<ShoppinglistItem> getItemFromDescription(String description) {
		description = escape(description);
		String q = "SELECT * FROM " + getTableItem() + " WHERE " + DESCRIPTION + "=" + description + ";";
		Cursor c = execQuery(q);
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		if (c.moveToFirst() ) {
			do { 
				list.add(cursorToSli(c));
			} while (c.moveToNext());
		}
		cursorClose(c);
		return list;
	}
	
	/**
	 * Get all Shoppinglistitems from a shoppinglist.
	 * @param sl from which to get items
	 * @return A list of shoppinglistitems
	 */
	public ArrayList<ShoppinglistItem> getItems(Shoppinglist sl) {
		String id = escape(sl.getId());
		String q = "SELECT * FROM " + getTableItem() + " WHERE " + SHOPPINGLIST_ID + "=" + id + " AND " + STATE + "!=" + ShoppinglistManager.STATE_DELETE + ";";
		Cursor c = execQuery(q);
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		if (c.moveToFirst() ) {
			do { 
				list.add(cursorToSli(c));
			} while (c.moveToNext());
		}
		cursorClose(c);
		return list;
	}

	public ShoppinglistItem getFirstItem(String shoppinglistId) {
		return getItemPrevious(shoppinglistId, ShoppinglistItem.FIRST_ITEM);
	}
	
	public ShoppinglistItem getItemPrevious(String shoppinglistId, String previousId) {
		String id = escape(shoppinglistId);
		String prev = escape(previousId);
		String q = "SELECT * FROM " + getTableItem() + " WHERE " + SHOPPINGLIST_ID + "=" + id + " AND " + PREVIOUS_ID + "=" + prev + ";";
		Cursor c = execQuery(q);
		ShoppinglistItem sli = c.moveToFirst() ? cursorToSli(c) : null;
		cursorClose(c);
		return sli;
	}

	public Shoppinglist getFirstList() {
		return getListPrevious(Shoppinglist.FIRST_ITEM);
	}
	
	public Shoppinglist getListPrevious(String previousId) {
		String prev = escape(previousId);
		String q = "SELECT * FROM " + getTableList() + " WHERE " + PREVIOUS_ID + "=" + prev + ";";
		Cursor c = execQuery(q);
		Shoppinglist sl = c.moveToFirst() ? cursorToSl(c) : null;
		cursorClose(c);
		return sl;
	}
	
	/**
	 * Deletes an item from db
	 * @param id of the item to delete
	 * @return the number of rows affected
	 */
	public int deleteItem(String id) {
		id = escape(id);
		String q = "DELETE FROM " + getTableItem() + " WHERE " + ID + "=" + id + ";";
		return execQueryWithChangesCount(q);
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
		shoppinglistId = escape(shoppinglistId);
		String q = "DELETE FROM " + getTableItem() + " WHERE " + SHOPPINGLIST_ID + "=" + shoppinglistId;
		if (state != null) {
			q += " AND " + TICK + "=" + escape(state);
		}
		q += ";";
		return execQueryWithChangesCount(q);
	}

	/**
	 * replaces an item in db
	 * @param sli to insert
	 * @return number of affected rows
	 */
	public int editItem(ShoppinglistItem sli) {
		String q = "REPLACE INTO " + getTableItem() + " " + itemToValues(sli) + ";";
		return execQueryWithChangesCount(q);
	}
	
	private int execQueryWithChangesCount(String query) {
		
		int i = 0;
		
		synchronized (LOCK) {
			// Get the actual query out of the way...
			Cursor c = execQuery(query);
			if (c != null) {
				// Dunno why, but we need this, but we do...
				c.moveToFirst();
			}
			c.close();
			
			// Get number of affected rows in last statement (query)
			c = execQuery(NUM_OF_AFFECTED_ROWS);
			
			if (c.moveToFirst()) {
				i = c.getInt(0);
				if (i>1)
					Utils.logd(TAG, "Multi-hit: " + String.valueOf(i));
			}
			
			// Clean up
			cursorClose(c);
		}

		return i;
	}
	
	private Cursor execQuery(String query) {
		
		// If we are resuming the activity
		if (mDatabase == null) openDB();
		
		Cursor c;
		
		synchronized (LOCK) {
			if (mDatabase.isOpen()) {
				c = mDatabase.rawQueryWithFactory(null, query, null, null);
				mCloseDb = false;
			} else {
				mDatabase = getWritableDatabase();
				c = mDatabase.rawQueryWithFactory(null, query, null, null);
				mCloseDb = true;
			}
		}

		return c;
	}

	private void cursorClose(Cursor c) {
		if (c != null)
			c.close();
		
		if (mCloseDb)
			closeDB();
	}
	
	private static String escape(Object o) {
		
		if (o instanceof Integer) {
			return Integer.toString((Integer)o);
		}
		
		StringBuilder sb = new StringBuilder();
		DatabaseUtils.appendValueToSql(sb, o);
		return sb.toString();
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
