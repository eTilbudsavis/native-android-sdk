package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.database.SgnDatabase;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.network.ShopGunError;

import org.json.JSONObject;

/**
 * @deprecated No longer maintained
 */
@Deprecated
class ItemListener extends JSONObjectListener<ShoppinglistItem> {

    public ItemListener(SgnDatabase database, User user, ShoppinglistItem local) {
        super(database, user, local);
    }

    @Override
    public void onComplete(JSONObject response, ShopGunError error) {
        if (response != null) {
            // in case of DELETE request, the success response is empty. Avoid parsing in that case
            onSuccess(response.length() > 0 ? ShoppinglistItem.fromJSON(response) : null);
        } else {
            onError(error);
        }
    }

    public void onSuccess(ShoppinglistItem response) {

    }

    public void onError(ShopGunError error) {

    }

}