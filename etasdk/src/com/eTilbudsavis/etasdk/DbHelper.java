package com.eTilbudsavis.etasdk;

import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.Tools.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	public static final String TAG = "DatabaseHelper";
	
	private static final String DB_NAME = "shoppinglist.db";
	private static final int DB_VERSION = 1;
	
	public static final String SL = "shoppinglists";
	public static final String SLI = "_shoppinglistitems";
	
	public static final String ID = "id";
	public static final String MODIFIED = "modified";
	public static final String ERN = "ern";
	public static final String NAME = "name";
	public static final String ACCESS = "access";
	public static final String SYNCED = "synced";
	public static final String OFFLINE = "offline";
	public static final String CURRENT = "current";
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
		MODIFIED + " integer not null, " + 
		ERN + " text, " + 
		NAME + " text not null, " + 
		ACCESS + " text not null, " + 
		SYNCED + " integer not null " + 
		OFFLINE + " integer not null " + 
		CURRENT + " integer not null " + 
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
		SHOPPINGLIST_ID + " text not null );";

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
		Shoppinglist sl = new Shoppinglist();
		sl.setId(c.getString(1));
		sl.setModified(c.getString(2));
		sl.setErn(c.getString(3));
		sl.setName(c.getString(4));
		sl.setAccess(c.getString(5));
		sl.setSynced(0 < c.getInt(6));
		return sl;
	}
	
	public static ContentValues slToCV(Shoppinglist sl) {
		ContentValues c = new ContentValues();
		c.put(DbHelper.ID, sl.getId());
		c.put(DbHelper.MODIFIED, sl.getModifiedString());
		c.put(DbHelper.ERN, sl.getErn());
		c.put(DbHelper.NAME, sl.getName());
		c.put(DbHelper.ACCESS, sl.getAccess());
		c.put(DbHelper.SYNCED, sl.isSynced());
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
		return c;
	}
	
	public void openDB() {
		mDatabase = getWritableDatabase();
	}
	
	public Cursor getList(String id) {
		return mDatabase.rawQuery("SELECT * FROM " + DbHelper.SL + " WHERE " + DbHelper.ID + "=" + id, null);
	}
	
}
