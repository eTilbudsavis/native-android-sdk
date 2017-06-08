package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.database.DatabaseWrapper;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;

import org.json.JSONObject;

abstract class JSONObjectListener<T> implements Response.Listener<JSONObject> {

    DatabaseWrapper mDatabase;
    User mUser;
    T mLocalCopy;

    public JSONObjectListener(DatabaseWrapper database, User user, T local) {
        mDatabase = database;
        mUser = user;
        mLocalCopy = local;
    }

    public abstract void onSuccess(T response);

    public abstract void onError(ShopGunError error);

}