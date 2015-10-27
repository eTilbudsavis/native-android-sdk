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
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.shoppinglists.ListManager;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbUtils {

    public static final String TAG = Constants.getTag(DbUtils.class);

    /**
     * Creates a full image of any data stored in the specific table in a specific database.
     * @param db A {@link SQLiteDatabase}
     * @param table A table in the DB
     * @return A {@link JSONArray}
     */
    public static List<ContentValues> dumpTable(SQLiteDatabase db, String table) {
        db.acquireReference();
        try {
            Cursor c = db.query(table, null, null, null, null, null, null);
            return cursorToContentValues(c);
        } finally {
            db.releaseReference();
        }
    }

    /**
     * Creates a full image of any data stored in the specific table in a specific database.
     * @param db A {@link SQLiteDatabase}
     * @param table A table in the DB
     * @return A {@link JSONArray}
     */
    public static JSONArray dumpTableToJSONArray(SQLiteDatabase db, String table) {
        try {
            List<ContentValues> list = dumpTable(db, table);
            JSONArray jTable = new JSONArray();
            for (ContentValues cv : list) {
                try {
                    JSONObject jRow = new JSONObject();
                    for (Map.Entry<String, Object> e : cv.valueSet()) {
                        jRow.put(e.getKey(), String.valueOf(e.getValue()));
                    }
                    jTable.put(jRow);
                } catch (JSONException e) {
                    // ignore
                }
            }
            return jTable;
        } finally {
            db.releaseReference();
        }
    }

    /**
     * Read the cursor into a {@link ContentValues} object, and close the {@link Cursor} when finished
     *
     * @param c A cursor
     * @return A {@link List} of {@link ContentValues}
     */
    public static List<ContentValues> cursorToContentValues(Cursor c) {
        ArrayList<ContentValues> list = new ArrayList<ContentValues>();
        try {
            if (c.moveToFirst()) {
                do {
                    ContentValues map = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(c, map);
                    list.add(map);
                } while (c.moveToNext());
            }
        } finally {
            closeCursor(c);
        }
        return list;
    }

    /**
     * Read the cursor into a {@link ContentValues} object, and close the {@link Cursor} when finished
     *
     * @param c A cursor
     * @return A {@link List} of {@link ContentValues}
     */
    public static ContentValues cursorToContentValuesSingle(Cursor c) {
        try {
            ContentValues map = new ContentValues();
            if (c.moveToFirst()) {
                DatabaseUtils.cursorRowToContentValues(c, map);
            }
            return map;
        } catch (IllegalStateException e) {
            return new ContentValues();
        } finally {
            closeCursor(c);
        }
    }

    /**
     * Safely close a cursor
     *
     * @param c A cursor
     */
    public static void closeCursor(Cursor c) {
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }

    /**
     * This method will migrate any {@link Shoppinglist}, and their {@link ShoppinglistItem} from
     * the offline state, to the currently logged in {@link User}, and they will at a later point
     * in time be synchronized to the ShopGun API.
     *
     * @param manager A {@link ListManager}
     * @param db      A {@link DatabaseWrapper}
     * @param delete  {@code true} if you want to have the offline {@link Shoppinglist} and
     *                {@link ShoppinglistItem} deleted on a successful migration completion, else {@code false}.
     * @return the number of migrated lists
     */
    public static int migrateOfflineLists(ListManager manager, DatabaseWrapper db, boolean delete) {

        User offlineUser = new User();
        List<Shoppinglist> offlineUserLists = db.getLists(offlineUser);

        if (offlineUserLists.isEmpty()) {
            return 0;
        }

        for (Shoppinglist sl : offlineUserLists) {

            List<ShoppinglistItem> noUserItems = db.getItems(sl, offlineUser);
            if (noUserItems.isEmpty()) {
                continue;
            }

            // Create a new list, with a new ID to avoid conflicts in the database
            Shoppinglist tmpSl = Shoppinglist.fromName(sl.getName());
            tmpSl.setType(sl.getType());

            manager.addList(tmpSl);

            for (ShoppinglistItem sli : noUserItems) {
                sli.setShoppinglistId(tmpSl.getId());
                sli.setId(Utils.createUUID());
                manager.addItem(sli);
            }

            if (delete) {
                db.deleteList(sl, offlineUser);
                db.deleteItems(sl.getId(), null, offlineUser);
            }

        }

        return offlineUserLists.size();
    }

    /**
     * SDK default mapping between int and boolean.
     * <p>
     *     returns <code>i == 1</code> so 1 is {@code true}, and all else is {@code false}
     * </p>
     * @param i The integer to convert
     * @return A boolean value
     */
    public static boolean intToBool(int i) {
        return i == 1;
    }

    /**
     * SDK default mapping between int and boolean.
     * <p>
     *     {@code true} returns 1, {@code false} returns 0;
     * </p>
     *
     * @param b The boolean to convert
     * @return An integer
     */
    public static int unescape(boolean b) {
        return b ? 1 : 0;
    }

    /**
     * SDK default mapping between string and boolean.
     * <p>{@code true} returns 1, {@code null} and {@code false} returns 0;</p>
     *
     * @param b The Boolean to convert
     * @return A String
     */
    public static String unescape(Boolean b) {
        return (b != null && b) ? "1" : "0";
    }

    /**
     * Safe binding of a String to a {@link SQLiteStatement}.
     * @param s A {@link SQLiteStatement}
     * @param index The index to bind to
     * @param value The value to bind
     */
    public static void bindOrNull(SQLiteStatement s, int index, String value) {
        if (value == null) {
            s.bindNull(index);
        } else {
            s.bindString(index, value);
        }
    }

    public static List<String> cursorToStrings(Cursor c, String column) {
        try {
            List<String> list = new ArrayList<String>(c.getCount());
            if (c.moveToFirst()) {
                int index = c.getColumnIndex(column);
                do {
                    list.add(c.getString(index));
                } while (c.moveToNext());
            }
            return list;
        } catch (IllegalStateException e) {
            // ignore
            return new ArrayList<String>();
        } finally {
            closeCursor(c);
        }
    }

}
