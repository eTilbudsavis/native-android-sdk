package com.shopgun.android.sdk.eventskit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.database.DbUtils;
import com.shopgun.android.sdk.eventskit.Event;
import com.shopgun.android.sdk.log.SgnLog;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventDb {
    
    public static final String TAG = EventDb.class.getSimpleName();
    
    private static EventDb mInstance;

    private static final String[] COUNT_PROJECTION = new String[]{"count(*)"};

    private SQLiteOpenHelper mHelper;

    private EventDb(SQLiteOpenHelper helper) {
        mHelper = helper;
    }

    public static EventDb getInstance() {
        if (mInstance == null) {
            synchronized (EventDb.class) {
                if (mInstance == null) {
                    Context context = ShopGun.getInstance().getContext();
                    EventDbHelper helper = new EventDbHelper(context);
                    mInstance = new EventDb(helper);
                }
            }
        }
        return mInstance;
    }

    public long getEventCount() {
        return getCount(null, null);
    }

    private long getCount(String selection, String[] selectionArgs) {
        Cursor c = null;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        try {
            c = db.query(EventSQLiteHelper.TABLE, COUNT_PROJECTION, selection, selectionArgs, null, null, null);
            if (c.moveToFirst()) {
                return c.getLong(0);
            }
            return 0;
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
            return 0;
        } finally {
            DbUtils.closeCursor(c);
            db.releaseReference();
        }
    }

    public void updateRetryCount(Collection<String> eventIds, int maxRetryCount) {
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.acquireReference();
        try {

            String selection = "";
            String[] selectionArgs = new String[] {};

            Cursor c = db.query(EventSQLiteHelper.TABLE, null, selection, selectionArgs, null, null, null, null);
            List<ContentValues> contentValues = DbUtils.cursorToContentValues(c);
            List<Long> ids = new ArrayList<>();
            for (ContentValues cv : contentValues) {
                cv.put(EventSQLiteHelper.RETRY_COUNT, cv.getAsInteger(EventSQLiteHelper.RETRY_COUNT)+1);
                long rowId = db.replace(EventSQLiteHelper.TABLE, null, cv);
                ids.add(rowId);
            }
            String whereClause = EventSQLiteHelper.RETRY_COUNT + ">?";
            String[] whereArgs = new String[]{ String.valueOf(maxRetryCount) };
            db.delete(EventSQLiteHelper.TABLE, whereClause, whereArgs);

        } catch (IllegalStateException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        } finally {
            db.releaseReference();
        }

    }

    public int update(List<Event> items) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.acquireReference();
        int count = 0;
        try {
            db.beginTransaction();
            for (Event item : items) {
                ContentValues cv = EventSQLiteHelper.updateEventContentValues(item);
                String whereClause = EventSQLiteHelper.UUID + "=?";
                String[] whereArgs = new String[]{ item.getId() };
                db.update(EventSQLiteHelper.TABLE, cv, whereClause, whereArgs);
                count++;
            }
            db.setTransactionSuccessful();
            return count;
        } catch (IllegalStateException e) {
            SgnLog.e(TAG, e.getMessage(), e);
            return count;
        } finally {
            db.endTransaction();
            db.releaseReference();
        }
    }

    public List<Event> getEvents(int limit) {
        return selectEvent(null, null, String.valueOf(limit));
    }

    public List<Event> getEvents() {
        return selectEvent(null, null, null);
    }

    public long insert(Event event) {
        return insertEvent(EventSQLiteHelper.eventToCV(event));
    }

    private long insertEvent(ContentValues contentValues) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.acquireReference();
        try {
            return db.insertOrThrow(EventSQLiteHelper.TABLE, null, contentValues);
        } catch (IllegalStateException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        } finally {
            db.releaseReference();
        }
        return 0;
    }

    public int delete(Collection<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(EventSQLiteHelper.UUID).append(" in (");
        boolean first = true;
        for (int i = 0; i < eventIds.size(); i++) {
            sb.append(first ? "?" : ",?");
            first = false;
        }
        sb.append(")");
        String whereClause = sb.toString();
        String[] whereArgs = eventIds.toArray(new String[eventIds.size()]);
        return deleteEvent(whereClause, whereArgs);
    }

    private int deleteEvent(String whereClause, String[] whereArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.acquireReference();
        try {
            return db.delete(EventSQLiteHelper.TABLE, whereClause, whereArgs);
        } catch (IllegalStateException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        } finally {
            db.releaseReference();
        }
        return 0;
    }

    private List<Event> selectEvent(String selection, String[] selectionArgs, String limit) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            db.acquireReference();
            Cursor c = db.query(EventSQLiteHelper.TABLE, null, selection, selectionArgs, null, null, null, limit);
            return EventSQLiteHelper.cursorToList(c);
        } catch (IllegalStateException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        } finally {
            db.releaseReference();
        }
        return new ArrayList<>();
    }

    public void clear() {
        EventSQLiteHelper.clear(mHelper.getWritableDatabase());
    }

    public JSONArray dump() {
        return DbUtils.dumpTableToJSONArray(mHelper.getReadableDatabase(), EventSQLiteHelper.TABLE);
    }

}
