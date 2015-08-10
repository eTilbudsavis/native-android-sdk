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
