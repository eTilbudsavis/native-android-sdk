package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.database.SgnDatabase;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;

import org.json.JSONArray;

/**
 * @deprecated No longer maintained
 */
@Deprecated
abstract class JSONArrayListener<T> implements Response.Listener<JSONArray> {

    SgnDatabase mDatabase;
    User mUser;
    T mData;

    public JSONArrayListener(SgnDatabase database, User user, T local) {
        mDatabase = database;
        mUser = user;
        mData = local;
    }

    public abstract void onSuccess(T response);

    public abstract void onError(ShopGunError error);

}