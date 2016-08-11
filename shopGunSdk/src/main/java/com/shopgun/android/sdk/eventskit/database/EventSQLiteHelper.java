package com.shopgun.android.sdk.eventskit.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.shopgun.android.sdk.database.DbUtils;
import com.shopgun.android.sdk.eventskit.Event;
import com.shopgun.android.sdk.log.SgnLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class EventSQLiteHelper {

    public static final String TAG = EventSQLiteHelper.class.getSimpleName();

    public static final String TABLE = "events";

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String RETRY_COUNT = "retry_count";
    public static final String EVENT = "event";

    public static final String CREATE_TABLE =
            "create table if not exists " + TABLE + "(" +
                    ID + " integer primary key autoincrement, " +
                    UUID + " text not null unique, " +
                    RETRY_COUNT + " integer not null, " +
                    EVENT + " text);";

    public static void create(SQLiteDatabase db) {
        db.acquireReference();
        db.execSQL(CREATE_TABLE);
        db.releaseReference();
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.acquireReference();
        switch (oldVersion) {
            case 1:
                // First version
            case 2:
                // Do migration steps
//                db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        }
        db.releaseReference();
    }

    public static void clear(SQLiteDatabase db) {
        try {
            db.acquireReference();
            db.delete(TABLE, "1", null);
        } catch (IllegalStateException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        } finally {
            db.releaseReference();
        }
    }

    public static ContentValues eventToCV(Event event) {
        ContentValues cv = new ContentValues();
        cv.put(UUID, event.getId());
        cv.put(RETRY_COUNT, 0);
        cv.put(EVENT, event.toJson().toString());
        return cv;
    }

    public static ContentValues updateEventContentValues(Event event) {
        ContentValues cv = new ContentValues();
        cv.put(UUID, event.getId());
        cv.put(EVENT, event.toJson().toString());
        return cv;
    }

    public static List<Event> cursorToList(Cursor c) {
        return DbUtils.cursorToList(c, new EventConverter());
    }

    private static class EventConverter implements DbUtils.ContentValuesConverter<Event> {
        @Override
        public Event convert(ContentValues cv) {
            return contentValuesToObject(cv);
        }
    }

    public static Event contentValuesToObject(ContentValues cv) {
        try {
            return Event.fromJson(new JSONObject(cv.getAsString(EVENT)));
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return null;
    }

}
