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

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for handling the {@link ShoppinglistItem} SQLite table.
 */
public class ItemSQLiteHelper extends DatabaseHelper {

    public static final String TAG = Constants.getTag(ItemSQLiteHelper.class);

    public static final String TABLE = "shoppinglistitems";

    public static final String CREATE_TABLE =
            "create table if not exists " + TABLE + "(" +
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
    public static final String INSERT_STATEMENT = "INSERT OR REPLACE INTO " + TABLE + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public ItemSQLiteHelper(Context context) {
        super(context);
    }

    public static void bind(SQLiteStatement s, ShoppinglistItem sli, String userId) {
        ContentValues cv = objectToContentValues(sli, userId);
        DbUtils.bindOrNull(s, 1, cv.getAsString(ID));
        DbUtils.bindOrNull(s, 2, cv.getAsString(ERN));
        DbUtils.bindOrNull(s, 3, cv.getAsString(MODIFIED));
        DbUtils.bindOrNull(s, 4, cv.getAsString(DESCRIPTION));
        s.bindLong(5, cv.getAsInteger(COUNT));
        s.bindLong(6, cv.getAsInteger(TICK));
        DbUtils.bindOrNull(s, 7, cv.getAsString(OFFER_ID));
        DbUtils.bindOrNull(s, 8, cv.getAsString(CREATOR));
        DbUtils.bindOrNull(s, 9, cv.getAsString(SHOPPINGLIST_ID));
        DbUtils.bindOrNull(s, 10, cv.getAsString(STATE));
        DbUtils.bindOrNull(s, 11, cv.getAsString(PREVIOUS_ID));
        DbUtils.bindOrNull(s, 12, cv.getAsString(META));
        DbUtils.bindOrNull(s, 13, cv.getAsString(USER));
    }

    public static SQLiteStatement getInsertStatement(SQLiteDatabase db) {
        return db.compileStatement(INSERT_STATEMENT);
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

    public static List<ShoppinglistItem> cursorToList(Cursor c) {
        ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
        for (ContentValues cv : DbUtils.cursorToContentValues(c)) {
            ShoppinglistItem sli = ItemSQLiteHelper.contentValuesToObject(cv);
            if (sli != null) {
                list.add(sli);
            }
        }
        return list;
    }

    public static ShoppinglistItem contentValuesToObject(ContentValues cv) {
        ShoppinglistItem sli = new ShoppinglistItem();
        sli.setId(cv.getAsString(ID));
        sli.setErn(cv.getAsString(ERN));
        sli.setModified(SgnUtils.stringToDate(cv.getAsString(MODIFIED)));
        sli.setDescription(cv.getAsString(DESCRIPTION));
        sli.setCount(cv.getAsInteger(COUNT));
        sli.setTick(DbUtils.intToBool(cv.getAsInteger(TICK)));
        sli.setOfferId(cv.getAsString(OFFER_ID));
        sli.setCreator(cv.getAsString(CREATOR));
        sli.setShoppinglistId(cv.getAsString(SHOPPINGLIST_ID));
        sli.setState(cv.getAsInteger(STATE));
        sli.setPreviousId(cv.getAsString(PREVIOUS_ID));
        try {
            String meta = cv.getAsString(META);
            sli.setMeta(meta == null ? null : new JSONObject(meta));
        } catch (JSONException e) {
            SgnLog.e(TAG, null, e);
        }
        sli.setUserId(cv.getAsInteger(USER));
        return sli;
    }

    public static ContentValues objectToContentValues(ShoppinglistItem sli, String userId) {
        ContentValues cv = new ContentValues();
        cv.put(ID, sli.getId());
        cv.put(ERN, sli.getErn());
        cv.put(MODIFIED, SgnUtils.dateToString(sli.getModified()));
        cv.put(DESCRIPTION, sli.getDescription());
        cv.put(COUNT, sli.getCount());
        cv.put(TICK, DbUtils.unescape(sli.isTicked()));
        cv.put(OFFER_ID, sli.getOfferId());
        cv.put(CREATOR, sli.getCreator());
        cv.put(SHOPPINGLIST_ID, sli.getShoppinglistId());
        cv.put(STATE, sli.getState());
        cv.put(PREVIOUS_ID, sli.getPreviousId());
        cv.put(META, sli.getMeta().toString());
        cv.put(USER, userId);
        return cv;
    }

    public static ContentValues stateToContentValues(Date modified, int syncState) {
        ContentValues cv = new ContentValues();
        cv.put(MODIFIED, SgnUtils.dateToString(modified));
        cv.put(STATE, syncState);
        return cv;
    }

}
