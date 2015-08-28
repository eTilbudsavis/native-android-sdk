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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.interfaces.SyncState;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for handling the {@link Shoppinglist} SQLite table.
 */
public class ListSQLiteHelper extends DatabaseHelper {

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
                    USER + " integer not null " +
                    ");";
    public static final String INSERT_STATEMENT = "INSERT OR REPLACE INTO " + TABLE + " VALUES (?,?,?,?,?,?,?,?,?,?)";

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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.releaseReference();
    }

    public static SQLiteStatement getInsertStatement(SQLiteDatabase db) {
        return db.compileStatement(INSERT_STATEMENT);
    }

    public static void bind(SQLiteStatement s, Shoppinglist sl, String userId) {
        DbUtils.bindOrNull(s, 1, sl.getId());
        DbUtils.bindOrNull(s, 2, sl.getErn());
        DbUtils.bindOrNull(s, 3, Utils.dateToString(sl.getModified()));
        DbUtils.bindOrNull(s, 4, sl.getName());
        DbUtils.bindOrNull(s, 5, sl.getAccess());
        s.bindLong(6, sl.getState());
        DbUtils.bindOrNull(s, 7, sl.getPreviousId());
        DbUtils.bindOrNull(s, 8, sl.getType());
        String meta = sl.getMeta() == null ? null : sl.getMeta().toString();
        DbUtils.bindOrNull(s, 9, meta);
        DbUtils.bindOrNull(s, 10, userId);
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
        sl.setModified(Utils.stringToDate(cv.getAsString(MODIFIED)));
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
        sl.setUserId(cv.getAsInteger(USER));
        return sl;
    }

    public static ContentValues objectToContentValues(Shoppinglist sl, String userId) {
        ContentValues cv = new ContentValues();
        cv.put(ID, sl.getId());
        cv.put(ERN, sl.getErn());
        cv.put(MODIFIED, Utils.dateToString(sl.getModified()));
        cv.put(NAME, sl.getName());
        cv.put(ACCESS, sl.getAccess());
        cv.put(STATE, sl.getState());
        cv.put(PREVIOUS_ID, sl.getPreviousId());
        cv.put(TYPE, sl.getType());
        String meta = sl.getMeta() == null ? null : sl.getMeta().toString();
        cv.put(META, meta);
        cv.put(USER, userId);
        return cv;
    }

}
