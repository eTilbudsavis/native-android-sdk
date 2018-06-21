package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.database.SgnDatabase;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;

import org.json.JSONObject;

/**
 * @deprecated No longer maintained
 */
@Deprecated
abstract class JSONObjectListener<T> implements Response.Listener<JSONObject> {

    SgnDatabase mDatabase;
    User mUser;
    T mLocalCopy;

    public JSONObjectListener(SgnDatabase database, User user, T local) {
        mDatabase = database;
        mUser = user;
        mLocalCopy = local;
    }

    public abstract void onSuccess(T response);

    public abstract void onError(ShopGunError error);

}