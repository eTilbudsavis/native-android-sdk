/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.interfaces.SyncState;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @deprecated No longer maintained
 *
 * Class for handling the {@link Shoppinglist} SQLite table.
 */
@Deprecated
public class ListSQLiteHelper extends SgnOpenHelper {

    public static final String TAG = Constants.getTag(ListSQLiteHelper.class);

    public static final String TABLE = "shoppinglists";

    public static final String CREATE_TABLE =
            "create table if not exists " + TABLE + "(" +
                    ID + " text primary key, " +
                    ERN + " text, " +
                    MODIFIED + " text not null, " +
                    NAME + " text not null, " +
                    ACCESS + " text not null, " +
                    STATE + " integer not null, " +
                    PREVIOUS_ID + " text, " +
                    TYPE + " text, " +
                    META + " text, " +
                    USER + " text not null, " +
                    SHARE_TOKEN + " text " +
                    ");";
    public static final String INSERT_STATEMENT = "INSERT OR REPLACE INTO " + TABLE + " VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    public ListSQLiteHelper(Context context) {
        super(context);
    }

    public static void create(SQLiteDatabase db) {
        db.acquireReference();
        db.execSQL(CREATE_TABLE);
        db.releaseReference();
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.acquireReference();
        if (oldVersion == 5 && newVersion == 6) {
           upgradeFrom5To6(db);
        }
        if (oldVersion == 6 && newVersion == 7) {
            upgradeFrom6To7(db);
        }

        if (oldVersion == 5 && newVersion == 7) {
            upgradeFrom5To6(db);
            upgradeFrom6To7(db);
        }

        if (oldVersion <= 7 && newVersion == 8) {
            if (oldVersion == 5) {
                upgradeFrom5To6(db);
                upgradeFrom6To7(db);
            }
            if (oldVersion == 6) {
                upgradeFrom6To7(db);
            }
            fixDatabase(db);
        }

        db.releaseReference();
    }

    // this should only be a problem if you have updated frm 5 to 7
    private static void fixDatabase(SQLiteDatabase db) {
        if(!existsColumnInTable(db,TABLE,SHARE_TOKEN)) {
            upgradeFrom5To6(db);
            upgradeFrom6To7(db);
        }
    }

    private static void upgradeFrom6To7(SQLiteDatabase db) {
        // add share_token
        db.execSQL("alter table " + TABLE + " add column " + SHARE_TOKEN + " text;");
    }

    private static void upgradeFrom5To6(SQLiteDatabase db) {
        // migrate USER from int to text
        db.execSQL("create table if not exists tmp_table (" +
                ID + " text primary key, " +
                ERN + " text, " +
                MODIFIED + " text not null, " +
                NAME + " text not null, " +
                ACCESS + " text not null, " +
                STATE + " integer not null, " +
                PREVIOUS_ID + " text, " +
                TYPE + " text, " +
                META + " text, " +
                USER + " text not null " +
                ");");
        db.execSQL("insert into tmp_table select " +
                String.format(Locale.US, "%s, %s, %s, %s, %s, %s, %s, %s, %s,",
                        ID, ERN, MODIFIED, NAME, ACCESS, STATE, PREVIOUS_ID, TYPE, META) +
                " cast (" + USER + " as text) from " + TABLE + ";");
        db.execSQL("drop table " + TABLE + ";");
        db.execSQL("alter table tmp_table rename to " + TABLE + ";");
    }

    public static SQLiteStatement getInsertStatement(SQLiteDatabase db) {
        return db.compileStatement(INSERT_STATEMENT);
    }

    public static void bind(SQLiteStatement s, Shoppinglist sl, String userId) {
        DbUtils.bindOrNull(s, 1, sl.getId());
        DbUtils.bindOrNull(s, 2, sl.getErn());
        DbUtils.bindOrNull(s, 3, SgnUtils.dateToString(sl.getModified()));
        DbUtils.bindOrNull(s, 4, sl.getName());
        DbUtils.bindOrNull(s, 5, sl.getAccess());
        s.bindLong(6, sl.getState());
        DbUtils.bindOrNull(s, 7, sl.getPreviousId());
        DbUtils.bindOrNull(s, 8, sl.getType());
        String meta = sl.getMeta() == null ? null : sl.getMeta().toString();
        DbUtils.bindOrNull(s, 9, meta);
        DbUtils.bindOrNull(s, 10, userId);
        DbUtils.bindOrNull(s, 11, sl.getShareToken());
    }

    public static List<Shoppinglist> cursorToList(Cursor c) {
        ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
        for (ContentValues cv : DbUtils.cursorToContentValues(c)) {
            Shoppinglist sl = ListSQLiteHelper.contentValuesToObject(cv);
            if (sl != null) {
                list.add(sl);
            }
        }
        return list;
    }

    public static Shoppinglist contentValuesToObject(ContentValues cv) {

        Shoppinglist sl = Shoppinglist.fromName(cv.getAsString(NAME));
        sl.setId(cv.getAsString(ID));
        sl.setErn(cv.getAsString(ERN));
        sl.setModified(SgnUtils.stringToDate(cv.getAsString(MODIFIED)));
        sl.setAccess(cv.getAsString(ACCESS));
        Integer state = cv.getAsInteger(STATE);
        sl.setState(state == null ? SyncState.TO_SYNC : state);
        sl.setPreviousId(cv.getAsString(PREVIOUS_ID));
        sl.setType(cv.getAsString(TYPE));
        try {
            String meta = cv.getAsString(META);
            sl.setMeta(meta == null ? null : new JSONObject(meta));
        } catch (JSONException e) {
            SgnLog.e(TAG, null, e);
        }
        sl.setUserId(cv.getAsString(USER));
        sl.setShareToken(cv.getAsString(SHARE_TOKEN));
        return sl;
    }

    public static ContentValues objectToContentValues(Shoppinglist sl, String userId) {
        ContentValues cv = new ContentValues();
        cv.put(ID, sl.getId());
        cv.put(ERN, sl.getErn());
        cv.put(MODIFIED, SgnUtils.dateToString(sl.getModified()));
        cv.put(NAME, sl.getName());
        cv.put(ACCESS, sl.getAccess());
        cv.put(STATE, sl.getState());
        cv.put(PREVIOUS_ID, sl.getPreviousId());
        cv.put(TYPE, sl.getType());
        String meta = sl.getMeta() == null ? null : sl.getMeta().toString();
        cv.put(META, meta);
        cv.put(USER, userId);
        cv.put(SHARE_TOKEN, sl.getShareToken());
        return cv;
    }

    private static boolean existsColumnInTable(SQLiteDatabase inDatabase, String inTable, String columnToCheck) {
        Cursor mCursor = null;
        try {
            // Query 1 row
            mCursor = inDatabase.rawQuery("SELECT * FROM " + inTable + " LIMIT 0", null);

            // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            if (mCursor.getColumnIndex(columnToCheck) != -1)
                return true;
            else
                return false;

        } catch (Exception Exp) {
            // Something went wrong. Missing the database? The table?
//            Log.d("... - existsColumnInTable", "When checking whether a column exists in the table, an error occurred: " + Exp.getMessage());
            return false;
        } finally {
            if (mCursor != null) mCursor.close();
        }
    }

}
