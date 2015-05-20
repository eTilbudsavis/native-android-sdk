package com.eTilbudsavis.etasdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Share;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.interfaces.SyncState;
import com.eTilbudsavis.etasdk.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static List<Share> cursorToList(Cursor c, Shoppinglist sl) {
        ArrayList<Share> list = new ArrayList<Share>();
        for (ContentValues cv : DbUtils.cursorToContentValues(c)) {
            Share s = ShareSQLiteHelper.contentValuesToObject(cv, sl);
            if (s!=null) {
                list.add(s);
            }
        }
        return list;
    }

    public static Share contentValuesToObject(ContentValues cv, Shoppinglist sl) {
        String email = cv.getAsString(EMAIL);
        String acceptUrl = cv.getAsString(ACCEPT_URL);
        String access = cv.getAsString(ACCESS);
        Share s = new Share(email, access, acceptUrl);
        s.setShoppinglistId(sl.getId());
        s.setName(cv.getAsString(NAME));
        s.setAccepted(0 < cv.getAsInteger(ACCEPTED));
        s.setState(cv.getAsInteger(STATE));
        return s;
    }

    public static ContentValues objectToContentValues(Share s, int userId) {
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
