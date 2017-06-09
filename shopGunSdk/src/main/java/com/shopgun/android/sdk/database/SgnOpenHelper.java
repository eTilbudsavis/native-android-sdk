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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.Constants;

/**
 * The SgnOpenHelper is a class for doing easy/clean database instantiation, and migration.
 * SgnOpenHelper calls static methods to sub-classes (each subclass essentially being a table)
 * to do creation and upgrades. This way any sub-class can be instantiated and queried for data
 * without having the trouble of database creation and upgrades.
 */
public class SgnOpenHelper extends SQLiteOpenHelper {

    public static final String TAG = Constants.getTag(SgnOpenHelper.class);

    public static final String ID = "id";
    public static final String MODIFIED = "modified";
    public static final String ERN = "ern";
    public static final String NAME = "name";
    public static final String ACCESS = "access";
    public static final String STATE = "state";
    public static final String DESCRIPTION = "description";
    public static final String COUNT = "count";
    public static final String TICK = "tick";
    public static final String OFFER_ID = "offer_id";
    public static final String CREATOR = "creator";
    public static final String SHOPPINGLIST_ID = "shopping_list_id";
    public static final String PREVIOUS_ID = "previous_id";
    public static final String TYPE = "type";
    public static final String META = "meta";
    public static final String SHARES = "shares";
    public static final String USER = "user";
    public static final String EMAIL = "email";
    public static final String ACCEPTED = "accepted";
    public static final String ACCEPT_URL = "accept_url";

    private static final String DB_NAME = "shoppinglist.db";
    private static final int DB_VERSION = 5;

    protected SgnOpenHelper(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // call all classes that extends this class, to let them know there has been an update
        ListSQLiteHelper.create(db);
        ItemSQLiteHelper.create(db);
        ShareSQLiteHelper.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // call all classes that extends this class, to let them know there has been an update
        String format = "Upgrading database from version %s to %s. Calling static methods in subclasses";
        SgnLog.i(TAG, String.format(format, oldVersion, newVersion));
        ListSQLiteHelper.upgrade(db, oldVersion, newVersion);
        ItemSQLiteHelper.upgrade(db, oldVersion, newVersion);
        ShareSQLiteHelper.upgrade(db, oldVersion, newVersion);
    }

}
