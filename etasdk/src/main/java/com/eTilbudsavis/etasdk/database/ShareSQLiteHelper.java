package com.eTilbudsavis.etasdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.model.Share;

import java.util.ArrayList;
import java.util.List;

public class ShareSQLiteHelper extends DatabaseHelper {

    public static final String TAG = Constants.getTag(ShareSQLiteHelper.class);

    public static final String TABLE = "shares";

    public static final String CREATE_TABLE =
            "create table if not exists " + TABLE + "(" +
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

    public ShareSQLiteHelper(Context context) {
        super(context);
    }

    public static void create(SQLiteDatabase db) {
        db.acquireReference();
        db.execSQL(CREATE_TABLE);
        db.releaseReference();
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.acquireReference();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.releaseReference();
    }

    public static final String INSERT_STATEMENT = "INSERT OR REPLACE INTO " + TABLE + " VALUES (?,?,?,?,?,?,?,?,?)";

    public static SQLiteStatement getInsertStatement(SQLiteDatabase db) {
        return db.compileStatement(INSERT_STATEMENT);
    }

    public static void bind(SQLiteStatement s, Share share, String userId) {
        DbUtils.bindOrNull(s, 2, share.getShoppinglistId());
        DbUtils.bindOrNull(s, 3, userId);
        DbUtils.bindOrNull(s, 4, share.getEmail());
        DbUtils.bindOrNull(s, 5, share.getName());
        s.bindLong(6, DbUtils.boolToInt(share.getAccepted()));
        DbUtils.bindOrNull(s, 7, share.getAccess());
        DbUtils.bindOrNull(s, 8, share.getAcceptUrl());
        s.bindLong(9, share.getState());
    }

    public static List<Share> cursorToList(Cursor c, String shoppinglistId) {
        ArrayList<Share> list = new ArrayList<Share>();
        for (ContentValues cv : DbUtils.cursorToContentValues(c)) {
            Share s = ShareSQLiteHelper.contentValuesToObject(cv, shoppinglistId);
            if (s!=null) {
                list.add(s);
            }
        }
        return list;
    }

    public static Share contentValuesToObject(ContentValues cv, String shoppinglistId) {
        String email = cv.getAsString(EMAIL);
        String acceptUrl = cv.getAsString(ACCEPT_URL);
        String access = cv.getAsString(ACCESS);
        Share s = new Share(email, access, acceptUrl);
        s.setShoppinglistId(shoppinglistId);
        s.setName(cv.getAsString(NAME));
        s.setAccepted(0 < cv.getAsInteger(ACCEPTED));
        s.setState(cv.getAsInteger(STATE));
        return s;
    }

    public static ContentValues objectToContentValues(Share s, String userId) {
        ContentValues cv = new ContentValues();
        cv.put(SHOPPINGLIST_ID, s.getShoppinglistId());
        cv.put(USER, userId);
        cv.put(EMAIL, s.getEmail());
        cv.put(NAME, s.getName());
        cv.put(ACCEPTED, s.getAccepted());
        cv.put(ACCESS, s.getAccess());
        cv.put(ACCEPT_URL, s.getAcceptUrl());
        cv.put(STATE, s.getState());
        return cv;
    }

}
