package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eTilbudsavis.etasdk.EtaObjects.Share;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class DbHelper extends SQLiteOpenHelper {

	public static final String TAG = "DbHelper";
	
	private static final String DB_NAME = "shoppinglist.db";
	private static final int DB_VERSION = 3;
	
	public static final String LIST_TABLE = "shoppinglists";
	public static final String ITEM_TABLE = "shoppinglistitems";
	
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
	public static final String TYPE = "type";
	public static final String META = "meta";
	public static final String USER = "user";
	
	private Object LOCK = new Object();
	
	HashMap<String, ArrayList<ShoppinglistItem>> lists = new HashMap<String, ArrayList<ShoppinglistItem>>(10);
	
	private void dirtyItems(String slId) {
		lists.remove(slId);
	}
	
	private void clean(String name, ArrayList<ShoppinglistItem> list) {
		lists.put(name, list);
	}

	private static final String CREATE_LIST_TABLE = 
			"create table if not exists " + LIST_TABLE + "(" + 
				ID + " text primary key, " + 
				ERN + " text, " + 
				MODIFIED + " text not null, " + 
				NAME + " text not null, " + 
				ACCESS + " text not null, " + 
				STATE + " integer not null, " + 
				OWNER_USER + " text, " + 
				OWNER_ACCESS + " text, " + 
				OWNER_ACCEPTED + " integer, " + 
				PREVIOUS_ID + " text, " + 
				TYPE + " text, " + 
				META + " text, " + 
				USER + " integer not null " + 
				");";
	
	private static final String CREATE_ITEM_TABLE = 
			"create table if not exists " + ITEM_TABLE + "(" + 
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
				PREVIOUS_ID + " text, " + 
				META + " text, " + 
				USER + "  integer not null " + 
				");";
	
	private static DbHelper mDbHelper;
	private SQLiteDatabase mDatabase;
	
	private DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		
		synchronized (LOCK) {
			database.execSQL(CREATE_LIST_TABLE);
			database.execSQL(CREATE_ITEM_TABLE);
		}
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		EtaLog.d(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		
		synchronized (LOCK) {
			db.execSQL("DROP TABLE " + LIST_TABLE);
			db.execSQL("DROP TABLE " + ITEM_TABLE);
		}
		
		onCreate(db);
	}

	public static DbHelper getInstance() {
		if (mDbHelper == null) {
			mDbHelper = new DbHelper(Eta.getInstance().getContext());
		}
		return mDbHelper;
	}
	
	/**
	 * Clears the whole DB. This cannot be undone.
	 */
	public synchronized void clear() {
		execQueryWithChangesCount("DELETE FROM " + LIST_TABLE);
		execQueryWithChangesCount("DELETE FROM " + ITEM_TABLE);
	}

	public synchronized void clear(int userId) {
		String query = String.format("DELETE FROM %s WHERE %s=%s", LIST_TABLE, USER, userId);
		execQuery(query);
		query = String.format("DELETE FROM %s WHERE %s=%s", ITEM_TABLE, USER, userId);
		execQuery(query);
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
		sl.setType(c.getString(c.getColumnIndex(TYPE)));
		String meta = c.getString(c.getColumnIndex(META));
		try {
			sl.setMeta(meta == null ? null : new JSONObject(meta));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return sl;
	}

	/**
	 * Creates a string of values, to insert into a table.<br>
	 * e.g.: (column1, column2) VALUES (value1, value2)
	 * @param sli to convert
	 * @return a string of the format: (column1, column2) VALUES (value1, value2)
	 */
	public static String listToValues(Shoppinglist sl, int userId) {

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
		.append(PREVIOUS_ID).append(",")
		.append(TYPE).append(",")
		.append(META).append(",")
		.append(USER)
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
		.append(escape(sl.getPreviousId())).append(",")
		.append(escape(sl.getType())).append(",")
		.append(escape(sl.getMeta() == null ? null : sl.getMeta().toString())).append(",")
		.append(escape(userId)).append(")");
		
		String str = sb.toString();
		
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
		String meta = cursor.getString(cursor.getColumnIndex(META));
		try {
			sli.setMeta(meta == null ? null : new JSONObject(meta));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return sli;
	}
	
	/**
	 * Creates a string of values, to insert into a table.<br>
	 * e.g.: (column1, column2) VALUES (value1, value2)
	 * @param sli to convert
	 * @return a string of the format: (column1, column2) VALUES (value1, value2)
	 */
	public static String itemToValues(ShoppinglistItem sli, int userId) {
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
		.append(PREVIOUS_ID).append(",")
		.append(META).append(",")
		.append(USER)
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
		.append(escape(sli.getPreviousId())).append(",")
		.append(escape(sli.getMeta() == null ? null : sli.getMeta().toString())).append(",")
		.append(escape(userId)).append(")");
		return sb.toString();
	}
		
	/**
	 * Insert new shopping list into DB
	 * @param list to insert
	 * @return number of affected rows
	 */
	public int insertList(Shoppinglist list, int userId) {
		String q = String.format("INSERT INTO %s %s", LIST_TABLE, listToValues(list, userId));
		int i = execQueryWithChangesCount(q);
		return i;
	}

	/**
	 * Get a shoppinglist by it's id
	 * @param id to get from db
	 * @return A shoppinglist or null if no march is found
	 */
	public Shoppinglist getList(String id, int userId) {
		id = escape(id);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", LIST_TABLE, ID, id, USER, userId);
		Cursor c = execQuery(q);
		Shoppinglist sl = null;
		sl = c.moveToFirst() ? cursorToSl(c) : null;
		close(c);
		return sl;
	}

	/**
	 * Get all Shoppinglists that match the given name.
	 * @param name to match up against
	 * @return A list of shoppinglists
	 */
	public ArrayList<Shoppinglist> getListFromName(String name, int userId) {
		name = escape(name);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", LIST_TABLE, NAME, name, USER, userId);
		Cursor c = execQuery(q);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(cursorToSl(c));
			} while (c.moveToNext());
		}
		close(c);
		return list;
	}
	
	/**
	 * Get all shoppinglists
	 * @return A list of shoppinglists
	 */
	public ArrayList<Shoppinglist> getLists(int userId) {
		String q = String.format("SELECT * FROM %s WHERE %s!=%s AND %s=%s", LIST_TABLE, STATE, Shoppinglist.State.DELETE, USER, userId);
		Cursor c = execQuery(q);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(cursorToSl(c));
			} while (c.moveToNext());
		}
		close(c);
		
		Collections.sort(list);
		
		return list;
	}
	
	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return number of affected rows
	 */
	public int deleteList(Shoppinglist sl, int userId) {
		dirtyItems(sl.getId());
		String id = escape(sl.getId());
		String q = String.format("DELETE FROM %s WHERE %s!=%s AND %s=%s", LIST_TABLE, ID, id, USER, userId);
		return execQueryWithChangesCount(q);
	}
	
	/**
	 * Replaces a shoppinglist, that have been updated in some way
	 * @param list that have been edited
	 * @return number of affected rows
	 */
	public int editList(Shoppinglist list, int userId) {
		String q = String.format("REPLACE INTO %s %s", LIST_TABLE, listToValues(list, userId));
		return execQueryWithChangesCount(q);
	}

	/**
	 * Adds item to db, IF it does not yet exist, else nothing
	 * @param sli to add to db
	 * @return number of affected rows
	 */
	public int insertItem(ShoppinglistItem sli, int userId) {
		dirtyItems(sli.getShoppinglistId());
		String q = String.format("INSERT INTO %s %s", ITEM_TABLE, itemToValues(sli, userId));
		return execQueryWithChangesCount(q);
	}
	
	/**
	 * Adds a list of items to db, IF they do not yet exist, else nothing
	 * @param items to insert
	 * @return number of affected rows
	 */
	public int insertItems(ArrayList<ShoppinglistItem> items, int userId) {
		int count = 0;
		for (ShoppinglistItem sli : items) {
			dirtyItems(sli.getShoppinglistId());
			count += insertItem(sli, userId);
		}
		return count;
	}

	/**
	 * Get a shoppinglistitem from the db
	 * @param itemId to get from db
	 * @return A shoppinglistitem or null if no match is found
	 */
	public ShoppinglistItem getItem(String itemId, int userId) {
		itemId = escape(itemId);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND  %s=%s", ITEM_TABLE, ID, itemId, USER , userId);
		Cursor c = execQuery(q);
		ShoppinglistItem sli = null;
		sli = c.moveToFirst() ? cursorToSli(c) : null;
		close(c);
		return sli;
	}
	
	/**
	 * Get all Shoppinglistitems that match the given description.
	 * @param description from which to get items
	 * @return A list of shoppinglistitems
	 */
	public ArrayList<ShoppinglistItem> getItemFromDescription(String description, int userId) {
		description = escape(description);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, DESCRIPTION, description, USER, userId);
		Cursor c = execQuery(q);
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		if (c.moveToFirst() ) {
			do { 
				list.add(cursorToSli(c));
			} while (c.moveToNext());
		}
		close(c);
		return list;
	}
	
	/**
	 * Get all Shoppinglistitems from a shoppinglist.
	 * @param sl from which to get items
	 * @return A list of shoppinglistitems
	 */
	public ArrayList<ShoppinglistItem> getItems(Shoppinglist sl, int userId) {
		ArrayList<ShoppinglistItem> list = lists.get(sl.getId());
		if (list != null) {
			return list;
		}
		String id = escape(sl.getId());
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s!=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, id, STATE, ShoppinglistItem.State.DELETE, USER, userId);
		Cursor c = execQuery(q);
		list = new ArrayList<ShoppinglistItem>();
		if (c.moveToFirst() ) {
			do { 
				list.add(cursorToSli(c));
			} while (c.moveToNext());
		}
		close(c);
		sortItemsByPrev(list);
		clean(sl.getId(), list);
		return list;
	}


	/** 
	 * Sorts an List of ShoppinglistItems according to their previous_id
	 * @param items items to sort
	 */
	public static void sortItemsByPrev(List<ShoppinglistItem> items) {
		
		if (items == null)
			return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("*** Sorting - hashmap ***").append("\n");
		
		HashMap<String, ShoppinglistItem> map = new HashMap<String, ShoppinglistItem>(items.size());
		for (ShoppinglistItem sli : items) {
			sb.append(sli.getTitle()).append(" - ").append("Prev: ").append(sli.getPreviousId()).append(" - ").append(sli.getId()).append("\n");
			map.put(sli.getPreviousId(), sli);
		}
		items.clear();
		
		sb.append("*** Sorting - while ***").append("\n");
		
		String prevId = ShoppinglistItem.FIRST_ITEM;
		
		int initCount= map.size();
		int c = 0;
		
		while (!map.isEmpty() && c != initCount+5) {
			c += 1;
			ShoppinglistItem s = map.get(prevId);
			sb.append(prevId).append(" - ").append(s == null ? "null" : s.getTitle()).append("\n");
			if (s != null) {
				map.remove(prevId);
				prevId = s.getId();
				items.add(s);
			} 
		}
		
		if (c>initCount) {
			EtaLog.d(TAG, sb.toString());
			EtaLog.printStackTrace();
		}
		
		
	}
	
	public ShoppinglistItem getFirstItem(String shoppinglistId, int userId) {
		return getItemPrevious(shoppinglistId, ShoppinglistItem.FIRST_ITEM, userId);
	}
	
	public ShoppinglistItem getItemPrevious(String shoppinglistId, String previousId, int userId) {
		String id = escape(shoppinglistId);
		String prev = escape(previousId);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, id, PREVIOUS_ID, prev, USER, userId);
		Cursor c = execQuery(q);
		ShoppinglistItem sli = c.moveToFirst() ? cursorToSli(c) : null;
		close(c);
		return sli;
	}

	public Shoppinglist getFirstList(int userId) {
		return getListPrevious(Shoppinglist.FIRST_ITEM, userId);
	}
	
	public Shoppinglist getListPrevious(String previousId, int userId) {
		String prev = escape(previousId);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", LIST_TABLE, PREVIOUS_ID, prev, USER, userId);
		Cursor c = execQuery(q);
		Shoppinglist sl = c.moveToFirst() ? cursorToSl(c) : null;
		close(c);
		return sl;
	}
	
	/**
	 * Deletes an item from db
	 * @param id of the item to delete
	 * @return the number of rows affected
	 */
	public int deleteItem(ShoppinglistItem sli, int userId) {
		dirtyItems(sli.getShoppinglistId());
		String id = escape(sli.getId());
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, ID, id, USER, userId);
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
	public int deleteItems(String shoppinglistId, Boolean state, int userId) {
		dirtyItems(shoppinglistId);
		shoppinglistId = escape(shoppinglistId);
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, shoppinglistId, USER, userId);
		if (state != null) {
			q += String.format(" AND %s=%s", TICK, escape(state));
		}
		return execQueryWithChangesCount(q);
	}

	/**
	 * replaces an item in db
	 * @param sli to insert
	 * @return number of affected rows
	 */
	public int editItem(ShoppinglistItem sli, int userId) {
		dirtyItems(sli.getShoppinglistId());
		String q = String.format("REPLACE INTO %s %s", ITEM_TABLE,  itemToValues(sli, userId));
		return execQueryWithChangesCount(q);
	}

//	/**
//	 * replaces an item in db
//	 * @param sli to insert
//	 * @return number of affected rows
//	 */
//	public int insertItems(List<ShoppinglistItem> list) {
//		dirty();
//		StringBuilder sb = new StringBuilder();
//		sb.append("INSERT INTO " + getTableItem() + " ");
//		boolean first = true;
//		for (ShoppinglistItem sli : list) {
//			if (first) {
//				first = false;
//				sb.append(" SELECT ").append(itemToMultiValuesAS(sli)).append(" ");
//			} else {
//				sb.append(" UNION SELECT ").append(itemToMultiValues(sli));
//			}
//		}
//		sb.append(";");
//		Utils.logd(TAG, sb.toString());
//		return execQueryWithChangesCount(sb.toString());
//	}
//
//	public static String itemToMultiValuesAS(ShoppinglistItem sli) {
//		StringBuilder sb = new StringBuilder();
//		sb.append(escape(sli.getId())).append(" AS ").append(ID).append(",")
//		.append(escape(sli.getErn())).append(" AS ").append(ERN).append(",")
//		.append(escape(Utils.formatDate(sli.getModified()))).append(" AS ").append(MODIFIED).append(",")
//		.append(escape(sli.getDescription())).append(" AS ").append(DESCRIPTION).append(",")
//		.append(escape(sli.getCount())).append(" AS ").append(COUNT).append(",")
//		.append(escape(sli.isTicked())).append(" AS ").append(TICK).append(",")
//		.append(escape(sli.getOfferId())).append(" AS ").append(OFFER_ID).append(",")
//		.append(escape(sli.getCreator())).append(" AS ").append(CREATOR).append(",")
//		.append(escape(sli.getShoppinglistId())).append(" AS ").append(SHOPPINGLIST_ID).append(",")
//		.append(escape(sli.getState())).append(" AS ").append(STATE).append(",")
//		.append(escape(sli.getPreviousId())).append(" AS ").append(PREVIOUS_ID);
//		return sb.toString();
//	}
//
//	public static String itemToMultiValues(ShoppinglistItem sli) {
//		StringBuilder sb = new StringBuilder();
//		sb.append(escape(sli.getId())).append(",")
//		.append(escape(sli.getErn())).append(",")
//		.append(escape(Utils.formatDate(sli.getModified()))).append(",")
//		.append(escape(sli.getDescription())).append(",")
//		.append(escape(sli.getCount())).append(",")
//		.append(escape(sli.isTicked())).append(",")
//		.append(escape(sli.getOfferId())).append(",")
//		.append(escape(sli.getCreator())).append(",")
//		.append(escape(sli.getShoppinglistId())).append(",")
//		.append(escape(sli.getState())).append(",")
//		.append(escape(sli.getPreviousId()));
//		return sb.toString();
//	}
		
	
	private int execQueryWithChangesCount(String query) {
		
		int i = 0;
		
		synchronized (LOCK) {
			// Get the actual query out of the way...
			Cursor c = execQuery(query);
			if (c != null) {
				c.moveToFirst();
				c.close();
			}
			
			// Get number of affected rows in last statement (query)
			c = execQuery("SELECT changes() AS 'changes'");
			
			if (c != null && c.moveToFirst()) {
				i = c.getInt(0);
			}
			
			close(c);
		}

		return i;
	}
	
	private Cursor execQuery(String query) {
		
		// If we are resuming the activity
		if (mDatabase == null || !mDatabase.isOpen())
			mDatabase = getWritableDatabase();
		
		Cursor c;
		synchronized (LOCK) {
			c = mDatabase.rawQueryWithFactory(null, query, null, null);
		}
		
		return c;
	}

	private void close(Cursor c) {
		if (c != null) {
			c.close();
		}
		
		if (!Eta.getInstance().isResumed()) {
			mDatabase.close();
		}
	}
	
	private static String escape(Object o) {
		
		if (o instanceof Integer) {
			return Integer.toString((Integer)o);
		}
		
		StringBuilder sb = new StringBuilder();
		DatabaseUtils.appendValueToSql(sb, o);
		return sb.toString();
	}
	
}