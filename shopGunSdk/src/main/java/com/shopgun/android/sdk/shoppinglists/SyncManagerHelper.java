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

package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.database.DatabaseWrapper;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.network.Request;

import java.util.ArrayList;

public abstract class SyncManagerHelper<T> {

    private DatabaseWrapper mDatabase;

    public SyncManagerHelper(DatabaseWrapper database) {
        this.mDatabase = database;
    }

    public DatabaseWrapper getDB() {
        return mDatabase;
    }

    public abstract boolean syncLocalChanges(ArrayList<T> object, User user);
    public abstract boolean put(T object, User user);
    public abstract boolean delete(T object, User user);
    public abstract boolean insert(T object, User user);
    public abstract boolean revert(T object, User user);

    protected void popRequestAndPostShoppinglistEvent() {

    }

    protected void popRequest() {

    }

    protected void addRequest(Request<?> r) {

    }

}
