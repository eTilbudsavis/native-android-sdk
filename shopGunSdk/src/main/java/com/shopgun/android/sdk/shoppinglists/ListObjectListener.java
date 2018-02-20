package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.database.SgnDatabase;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.network.ShopGunError;

import org.json.JSONObject;

/**
 * @Deprecated No longer maintained
 */
class ListObjectListener extends JSONObjectListener<Shoppinglist> {

    public ListObjectListener(SgnDatabase database, User user, Shoppinglist local) {
        super(database, user, local);
    }

    @Override
    public void onComplete(JSONObject response, ShopGunError error) {
        if (response != null) {
            onSuccess(Shoppinglist.fromJSON(response));
        } else {
            onError(error);
        }
    }

    @Override
    public void onSuccess(Shoppinglist response) {

    }

    @Override
    public void onError(ShopGunError error) {

    }

}