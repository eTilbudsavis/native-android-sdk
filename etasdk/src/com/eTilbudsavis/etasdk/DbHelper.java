package com.eTilbudsavis.etasdk;

import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;

import Utils.Utilities;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	public static final String TAG = "DatabaseHelper";
	
	private static final String DB_NAME = "shoppinglist.db";
	private static final int DB_VERSION = 1;
	
	public static final String TABLE_SL = "shoppinglists";
	public static final String TABLE_SLI = "_shoppinglistitems";
	
	
	public static final String SL_L_ID = "l_id";
	public static final String SL_ID = "id";
	public static final String SL_MODIFIED = "modified";
	public static final String SL_ERN = "ern";
	public static final String SL_NAME = "name";
	public static final String SL_ACCESS = "access";
	public static final String SL_SYNCED = "synced";
	
	public static final String SLI_L_ID = "l_id";
	public static final String SLI_ID = "id";
	public static final String SLI_ERN = "ern";
	public static final String SLI_MODIFIED = "modified";
	public static final String SLI_DESCRIPTION = "description";
	public static final String SLI_COUNT = "count";
	public static final String SLI_TICK = "tick";
	public static final String SLI_OFFER_ID = "offer_id";
	public static final String SLI_CREATOR = "creator";
	public static final String SLI_SHOPPINGLIST_ID = "shopping_list_id";
	
	// Shoppinglist table
	private static final String DB_CREATE_SL = 
		"create table if not exists " + TABLE_SL + "(" + 
		SL_L_ID + " integer primary key autoincrement, " +
		SL_ID + " text, " + 
		SL_MODIFIED + " integer not null, " + 
		SL_ERN + " text, " + 
		SL_NAME + " text not null, " + 
		SL_ACCESS + " text not null, " + 
		SL_SYNCED + " integer not null " + 
		");";

	// Shoppinglist item table
	private static final String DB_CREATE_SLI = 
		"create table if not exists " + TABLE_SLI + "(" + 
		SLI_L_ID + " integer primary key autoincrement, " +
		SLI_ID + " text, " + 
		SLI_ERN + " text, " + 
		SLI_MODIFIED + " text not null, " + 
		SLI_DESCRIPTION + " text, " + 
		SLI_COUNT + " integer not null, " + 
		SLI_TICK + " integer not null, " + 
		SLI_OFFER_ID + " text, " + 
		SLI_CREATOR + " text, " + 
		SLI_SHOPPINGLIST_ID + " text );";

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
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SL);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLI);
		onCreate(db);
	}

	public static Shoppinglist curToSl(Cursor c) {
		Shoppinglist sl = new Shoppinglist();
		sl.setId(c.getString(1));
		sl.setModified(c.getString(2));
		sl.setErn(c.getString(3));
		sl.setName(c.getString(4));
		sl.setAccess(c.getString(5));
		sl.setSynced(c.getInt(6) == 1);
		return sl;
	}
	
	public static ContentValues slToCV(Shoppinglist sl) {
		ContentValues c = new ContentValues();
		c.put(DbHelper.SL_ID, sl.getId());
		c.put(DbHelper.SL_MODIFIED, sl.getModifiedString());
		c.put(DbHelper.SL_ERN, sl.getErn());
		c.put(DbHelper.SL_NAME, sl.getName());
		c.put(DbHelper.SL_ACCESS, sl.getAccess());
		c.put(DbHelper.SL_SYNCED, sl.getSynced());
		return c;
	}
	
}
