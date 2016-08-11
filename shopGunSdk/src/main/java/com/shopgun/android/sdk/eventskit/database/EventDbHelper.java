package com.shopgun.android.sdk.eventskit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EventDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "sgn_event.db";
    private static final int DB_VERSION = 1;

    public EventDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        EventSQLiteHelper.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        EventSQLiteHelper.upgrade(db, oldVersion, newVersion);
    }

}
