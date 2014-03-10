package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collections;
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
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class DbHelper extends SQLiteOpenHelper {

	public static final String TAG = "DbHelper";
	
	private static final String DB_NAME = "shoppinglist.db";
	private static final int DB_VERSION = 5;
	
	public static final String LIST_TABLE = "shoppinglists";
	public static final String ITEM_TABLE = "shoppinglistitems";
	public static final String SHARE_TABLE = "shares";
	
	public static final String ID = "id";
	public static final String MODIFIED = "modified";
	public static final String ERN = "ern";
	public static final String NAME = "name";
	public static final String ACCESS = "access";
	public static final String STATE = "state";
	public static final String DESCRIPTION = "description";
	public static final String COUNT = "count";
	public static final String TICK = "tick";
	public static final String OFFER_ID = "offer_id";
	public static final String CREATOR = "creator";
	public static final String SHOPPINGLIST_ID = "shopping_list_id";
	public static final String PREVIOUS_ID = "previous_id";
	public static final String TYPE = "type";
	public static final String META = "meta";
	public static final String SHARES = "shares";
	public static final String USER = "user";
	public static final String EMAIL = "email";
	public static final String ACCEPTED = "accepted";
	public static final String ACCEPT_URL = "accept_url";
	
	
	private Object LOCK = new Object();
	
	private static final String CREATE_LIST_TABLE = 
			"create table if not exists " + LIST_TABLE + "(" + 
				ID + " text primary key, " + 
				ERN + " text, " + 
				MODIFIED + " text not null, " + 
				NAME + " text not null, " + 
				ACCESS + " text not null, " + 
				STATE + " integer not null, " + 
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


	private static final String CREATE_SHARE_TABLE = 
			"create table if not exists " + SHARE_TABLE + "(" + 
				ID + " integer not null primary key, " + 
				SHOPPINGLIST_ID + " text not null, " + 
				USER + " integer not null, " + 
				EMAIL + " text, " + 
				NAME + " text, " + 
				ACCEPTED + " text, " + 
				ACCESS + " text, " + 
				ACCEPT_URL + " text, " + 
				STATE + " integer " + 
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
			database.execSQL(CREATE_SHARE_TABLE);
		}
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		EtaLog.d(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		
		synchronized (LOCK) {
			db.execSQL("DROP TABLE IF EXISTS " + LIST_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + ITEM_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SHARE_TABLE);
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
		execQueryWithChangesCount("DELETE FROM " + SHARE_TABLE);
	}

	public synchronized void clear(int userId) {
		String lists = String.format("DELETE FROM %s WHERE %s=%s", LIST_TABLE, USER, userId);
		execQueryWithChangesCount(lists);
		
		String items = String.format("DELETE FROM %s WHERE %s=%s", ITEM_TABLE, USER, userId);
		execQueryWithChangesCount(items);
		
		String shares = String.format("DELETE FROM %s WHERE %s=%s", SHARE_TABLE, USER, userId);
		execQueryWithChangesCount(shares);
		
	}

	public static Shoppinglist cursorToSl(Cursor c) {
		Shoppinglist sl = Shoppinglist.fromName(c.getString(c.getColumnIndex(NAME)));
		sl.setId(c.getString(c.getColumnIndex(ID)));
		sl.setErn(c.getString(c.getColumnIndex(ERN)));
		sl.setModified(c.getString(c.getColumnIndex(MODIFIED)));
		sl.setAccess(c.getString(c.getColumnIndex(ACCESS)));
		sl.setState(c.getInt(c.getColumnIndex(STATE)));
		sl.setPreviousId(c.getString(c.getColumnIndex(PREVIOUS_ID)));
		sl.setType(c.getString(c.getColumnIndex(TYPE)));
		String meta = c.getString(c.getColumnIndex(META));
		try {
			sl.setMeta(meta == null ? null : new JSONObject(meta));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		sl.setUserId(c.getInt(c.getColumnIndex(USER)));
		return sl;
	}

	/**
	 * Creates a string of values, to insert into a table.<br>
	 * e.g.: (column1, column2) VALUES (value1, value2)
	 * @param sli to convert
	 * @return a string of the format: (column1, column2) VALUES (value1, value2)
	 */
	public static String listToValues(Shoppinglist sl, int userId) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("(")
		.append(ID).append(",")
		.append(ERN).append(",")
		.append(MODIFIED).append(",")
		.append(NAME).append(",")
		.append(ACCESS).append(",")
		.append(STATE).append(",")
		.append(PREVIOUS_ID).append(",")
		.append(TYPE).append(",")
		.append(META).append(",")
		.append(USER)
		.append(") VALUES (")
		.append(escape(sl.getId())).append(",")
		.append(escape(sl.getErn())).append(",")
		.append(escape(Utils.parseDate(sl.getModified()))).append(",")
		.append(escape(sl.getName())).append(",")
		.append(escape(sl.getAccess())).append(",")
		.append(escape(sl.getState())).append(",")
		.append(escape(sl.getPreviousId())).append(",")
		.append(escape(sl.getType())).append(",")
		.append(escape(sl.getMeta() == null ? null : sl.getMeta().toString())).append(",")
		.append(escape(userId)).append(")");
		String str = sb.toString();
		return str;
	}
	
	/**
	 * Method does not close the Cursor.
	 * @param c with data
	 * @return A shoppinglistitem
	 */
	public static ShoppinglistItem cursorToSli(Cursor c) {
		ShoppinglistItem sli = new ShoppinglistItem();
		sli.setId(c.getString(c.getColumnIndex(ID)));
		sli.setErn(c.getString(c.getColumnIndex(ERN)));
		sli.setModified(Utils.parseDate(c.getString(c.getColumnIndex(MODIFIED))));
		sli.setDescription(c.getString(c.getColumnIndex(DESCRIPTION)));
		sli.setCount(c.getInt(c.getColumnIndex(COUNT)));
		sli.setTick(0 < c.getInt(c.getColumnIndex(TICK)));
		sli.setOfferId(c.getString(c.getColumnIndex(OFFER_ID)));
		sli.setCreator(c.getString(c.getColumnIndex(CREATOR)));
		sli.setShoppinglistId(c.getString(c.getColumnIndex(SHOPPINGLIST_ID)));
		sli.setState(c.getInt(c.getColumnIndex(STATE)));
		sli.setPreviousId(c.getString(c.getColumnIndex(PREVIOUS_ID)));
		String meta = c.getString(c.getColumnIndex(META));
		try {
			sli.setMeta(meta == null ? null : new JSONObject(meta));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		sli.setUserId(c.getInt(c.getColumnIndex(USER)));
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
		.append(escape(Utils.parseDate(sli.getModified()))).append(",")
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
	

	public static Share cursorToShare(Cursor c, Shoppinglist sl) {
		String email = c.getString(c.getColumnIndex(EMAIL));
		String acceptUrl = c.getString(c.getColumnIndex(ACCEPT_URL));
		String access = c.getString(c.getColumnIndex(ACCESS));
		Share s = new Share(email, access, acceptUrl);
		s.setShoppinglistId(sl.getId());
		s.setName(c.getString(c.getColumnIndex(NAME)));
		s.setAccepted(0 < c.getInt(c.getColumnIndex(ACCEPTED)));
		s.setState(c.getInt(c.getColumnIndex(STATE)));
		return s;
	}
	
	/**
	 * Creates a string of values, to insert into a table.<br>
	 * e.g.: (column1, column2) VALUES (value1, value2)
	 * @param sli to convert
	 * @return a string of the format: (column1, column2) VALUES (value1, value2)
	 */
	public static String shareToValues(Share share, int userId) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("(")
		.append(SHOPPINGLIST_ID).append(",")
		.append(USER).append(",")
		.append(EMAIL).append(",")
		.append(NAME).append(",")
		.append(ACCEPTED).append(",")
		.append(ACCESS).append(",")
		.append(ACCEPT_URL).append(",")
		.append(STATE)
		.append(") VALUES (")
		.append(escape(share.getShoppinglistId())).append(",")
		.append(escape(userId)).append(",")
		.append(escape(share.getEmail())).append(",")
		.append(escape(share.getName())).append(",")
		.append(escape(share.getAccepted())).append(",")
		.append(escape(share.getAccess())).append(",")
		.append(escape(share.getAcceptUrl())).append(",")
		.append(escape(share.getState())).append(")");
		String str = sb.toString();
		return str;
	}
	
	
	/**
	 * Insert new shopping list into DB
	 * @param sl to insert
	 * @return number of affected rows
	 */
	public int insertList(Shoppinglist sl, User user) {
		String q = String.format("INSERT OR REPLACE INTO %s %s", LIST_TABLE, listToValues(sl, user.getUserId()));
		int count = execQueryWithChangesCount(q);
		if (0<count) {
			cleanShares(sl, user);
		}
		return count;
	}

	/**
	 * Get a shoppinglist by it's id
	 * @param id to get from db
	 * @return A shoppinglist or null if no march is found
	 */
	public Shoppinglist getList(String id, User user) {
		id = escape(id);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s AND %s!=%s", LIST_TABLE, ID, id, USER, user.getId(), STATE, Shoppinglist.State.DELETE);
		Cursor c = execQuery(q);
		Shoppinglist sl = null;
		sl = c.moveToFirst() ? cursorToSl(c) : null;
		close(c);
		if (sl != null) {
			sl.putShares(getShares(sl, user, false));
			sl = sl.getShares().containsKey(user.getEmail()) ? sl : null;
		}
		return sl;
	}
	
	/**
	 * Get all Shoppinglists that match the given name.
	 * @param name to match up against
	 * @return A list of shoppinglists
	 */
	public List<Shoppinglist> getListFromName(String name, User user) {
		name = escape(name);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s AND %s!=%s", LIST_TABLE, NAME, name, USER, user.getId(), STATE, Shoppinglist.State.DELETE);
		Cursor c = execQuery(q);
		List<Shoppinglist> tmp = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				tmp.add(cursorToSl(c));
			} while (c.moveToNext());
		}
		close(c);
		List<Shoppinglist> lists = new ArrayList<Shoppinglist>(c.getCount());
		for (Shoppinglist sl : tmp) {
			sl.putShares(getShares(sl, user, false));
			// Only add list, if list has user as share
			if (sl.getShares().containsKey(user.getEmail()))
				lists.add(sl);
		}
		return lists;
	}

	/**
	 * Get all shoppinglists, deleted lists are not included
	 * @return A list of shoppinglists
	 */
	public List<Shoppinglist> getLists(User user) {
		return getLists(user, false);
	}

	/**
	 * 
	 * @param userId
	 * @param includeDeleted
	 * @return A list of shoppinglists
	 */
	public List<Shoppinglist> getLists(User user, boolean includeDeleted) {
		String q = null;
		if (includeDeleted) {
			q = String.format("SELECT * FROM %s WHERE %s=%s", LIST_TABLE, USER, user.getId());
		} else {
			q = String.format("SELECT * FROM %s WHERE %s!=%s AND %s=%s", LIST_TABLE, STATE, Shoppinglist.State.DELETE, USER, user.getId());
		}
		Cursor c = execQuery(q);
		List<Shoppinglist> tmp = new ArrayList<Shoppinglist>(c.getCount());
		if (c.moveToFirst() ) {
			do { 
				tmp.add(cursorToSl(c));
			} while (c.moveToNext());
		}
		close(c);
		List<Shoppinglist> lists = new ArrayList<Shoppinglist>(c.getCount());
		for (Shoppinglist sl : tmp) {
			sl.putShares(getShares(sl, user, includeDeleted));
			// Only add list, if list has user as share
			if (sl.getShares().containsKey(user.getEmail()))
				lists.add(sl);
		}
		Collections.sort(lists);
		return lists;
	}

	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return number of affected rows
	 */
	public int deleteList(Shoppinglist sl, User user) {
		return deleteList(sl.getId(), user);
	}
	
	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return number of affected rows
	 */
	public int deleteList(String shoppinglistId, User user) {
		String id = escape(shoppinglistId);
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", LIST_TABLE, ID, id, USER, user.getUserId());
		return execQueryWithChangesCount(q);
	}

	/**
	 * Replaces a shoppinglist, that have been updated in some way
	 * @param sl that have been edited
	 * @return number of affected rows
	 */
	public int editList(Shoppinglist sl, User user) {
		String q = String.format("REPLACE INTO %s %s", LIST_TABLE, listToValues(sl, user.getUserId()));
		int count = execQueryWithChangesCount(q);
		return count;
	}

	/**
	 * Adds item to db, IF it does not yet exist, else nothing
	 * @param sli to add to db
	 * @return number of affected rows
	 */
	public int insertItem(ShoppinglistItem sli, User user) {
		String q = String.format("INSERT OR REPLACE INTO %s %s", ITEM_TABLE, itemToValues(sli, user.getUserId()));
		return execQueryWithChangesCount(q);
	}
	
	/**
	 * Adds a list of items to db, IF they do not yet exist, else nothing
	 * @param items to insert
	 * @return number of affected rows
	 */
	public int insertItems(ArrayList<ShoppinglistItem> items, User user) {
		int count = 0;
		for (ShoppinglistItem sli : items) {
			count += insertItem(sli, user);
		}
		return count;
	}

	/**
	 * Get a shoppinglistitem from the db
	 * @param itemId to get from db
	 * @return A shoppinglistitem or null if no match is found
	 */
	public ShoppinglistItem getItem(String itemId, User user) {
		itemId = escape(itemId);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND  %s=%s", ITEM_TABLE, ID, itemId, USER , user.getId());
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
	public List<ShoppinglistItem> getItemFromDescription(String description, User user) {
		description = escape(description);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, DESCRIPTION, description, USER, user.getId());
		Cursor c = execQuery(q);
		List<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
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
	public List<ShoppinglistItem> getItems(Shoppinglist sl, User user) {
		return getItems(sl.getId(), user, false);
	}

	/**
	 * Get all Shoppinglistitems from a shoppinglist.
	 * @param sl from which to get items
	 * @return A list of shoppinglistitems
	 */
	public List<ShoppinglistItem> getItems(Shoppinglist sl, User user, boolean includeDeleted) {
		return getItems(sl.getId(), user, includeDeleted);
	}
	
	/**
	 * Get all Shoppinglistitems from a shoppinglist.
	 * @param sl from which to get items
	 * @return A list of shoppinglistitems
	 */
	public List<ShoppinglistItem> getItems(String slId, User user, boolean includeDeleted) {
		String id = escape(slId);
		String q = null;
		if (includeDeleted) {
			q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, id, USER, user.getId());
		} else {
			q = String.format("SELECT * FROM %s WHERE %s=%s AND %s!=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, id, STATE, ShoppinglistItem.State.DELETE, USER, user.getId());
		}
		Cursor c = execQuery(q);
		List<ShoppinglistItem> items = new ArrayList<ShoppinglistItem>();
		if (c.moveToFirst() ) {
			do { 
				items.add(cursorToSli(c));
			} while (c.moveToNext());
		}
		close(c);
		return items;
	}

	public ShoppinglistItem getFirstItem(String shoppinglistId, User user) {
		return getItemPrevious(shoppinglistId, ShoppinglistItem.FIRST_ITEM, user);
	}
	
	public ShoppinglistItem getItemPrevious(String shoppinglistId, String previousId, User user) {
		String id = escape(shoppinglistId);
		String prev = escape(previousId);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, id, PREVIOUS_ID, prev, USER, user.getId());
		Cursor c = execQuery(q);
		ShoppinglistItem sli = c.moveToFirst() ? cursorToSli(c) : null;
		close(c);
		return sli;
	}

	public Shoppinglist getFirstList(User user) {
		return getListPrevious(Shoppinglist.FIRST_ITEM, user);
	}
	
	public Shoppinglist getListPrevious(String previousId, User user) {
		String prev = escape(previousId);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", LIST_TABLE, PREVIOUS_ID, prev, USER, user.getId());
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
	public int deleteItem(ShoppinglistItem sli, User user) {
		String id = escape(sli.getId());
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, ID, id, USER, user.getId());
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
	public int deleteItems(String shoppinglistId, Boolean state, User user) {
		shoppinglistId = escape(shoppinglistId);
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, shoppinglistId, USER, user.getId());
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
	public int editItem(ShoppinglistItem sli, User user) {
		String q = String.format("REPLACE INTO %s %s", ITEM_TABLE,  itemToValues(sli, user.getUserId()));
		return execQueryWithChangesCount(q);
	}
	
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

	/**
	 * 
	 * @param sl
	 * @param userId
	 * @param includeDeleted
	 * @return
	 */
	public List<Share> getShares(Shoppinglist sl, User user, boolean includeDeleted) {
		String slId = escape(sl.getId());
		String q = null;
		if (includeDeleted) {
				q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", SHARE_TABLE, SHOPPINGLIST_ID, slId, USER, user.getUserId());
			} else {
				q = String.format("SELECT * FROM %s WHERE %s=%s AND %s!=%s AND %s=%s", SHARE_TABLE, SHOPPINGLIST_ID, slId, STATE, Share.State.DELETE, USER, user.getUserId());
			}
		Cursor c = execQuery(q);
		List<Share> shares = new ArrayList<Share>(c.getCount());
		if (c.moveToFirst() ) {
			do {
				shares.add(cursorToShare(c, sl));
			} while (c.moveToNext());
		}
		close(c);
		return shares;
	}
	
	public int cleanShares(Shoppinglist sl, User user) {
		deleteShares(sl, user);
		int count = 0;
		for (Share s: sl.getShares().values())
			count += editShare(s, user);
		return count;
	}
	
	public int insertShare(Share s, User user) {
		String q = String.format("INSERT OR REPLACE INTO %s %s", SHARE_TABLE, shareToValues(s, user.getUserId()));
		return execQueryWithChangesCount(q);
	}

	public int editShare(Share s, User user) {
		deleteShare(s, user);
		return insertShare(s, user);
	}
	
	public int deleteShare(Share s, User user) {
		String email = escape(s.getEmail());
		String slId = escape(s.getShoppinglistId());
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s AND %s=%s", SHARE_TABLE, SHOPPINGLIST_ID, slId, EMAIL, email, USER, user.getUserId());
		return execQueryWithChangesCount(q);
	}

	public int deleteShares(Shoppinglist sl, User user) {
		return deleteShares(sl.getId(), user);
	}
	
	public int deleteShares(String shoppinglistId, User user) {
		String slId = escape(shoppinglistId);
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", SHARE_TABLE, SHOPPINGLIST_ID, slId, USER, user.getId());
		return execQueryWithChangesCount(q);
	}
	
	private Cursor execQuery(String query) {
		
		// If we are resuming the activity
		if (mDatabase == null || !mDatabase.isOpen())
			mDatabase = getWritableDatabase();
		
		synchronized (LOCK) {
			
//			try {
				return mDatabase.rawQueryWithFactory(null, query, null, null);
//			} catch (Exception e) {
//				EtaLog.d(TAG, e);
//				return new MatrixCursor(new String[] {"empty"});
//			}
		}
		
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