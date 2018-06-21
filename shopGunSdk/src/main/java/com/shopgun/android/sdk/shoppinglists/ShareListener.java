package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.database.SgnDatabase;
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.network.ShopGunError;

import org.json.JSONObject;

/**
 * @deprecated No longer maintained
 */
@Deprecated
class ShareListener extends JSONObjectListener<Share> {

    public ShareListener(SgnDatabase database, User user, Share local) {
        super(database, user, local);
    }

    @Override
    public void onComplete(JSONObject response, ShopGunError error) {
        if (response != null) {
            onSuccess(Share.fromJSON(response));
        } else {
            onError(error);
        }
    }

    public void onSuccess(Share response) {

    }

    public void onError(ShopGunError error) {

    }

}