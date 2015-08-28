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
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.model.interfaces.SyncState;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.utils.Api;

import org.json.JSONObject;

import java.util.ArrayList;

public class ListSyncHelper extends SyncManagerHelper<Shoppinglist> {

    public ListSyncHelper(DatabaseWrapper database) {
        super(database);
    }

    @Override
    public boolean syncLocalChanges(ArrayList<Shoppinglist> lists, User user) {

        int count = lists.size();

        for (Shoppinglist sl : lists) {

            switch (sl.getState()) {

                case SyncState.TO_SYNC:
                    put(sl, user);
                    break;

                case SyncState.DELETE:
                    delete(sl, user);
                    break;

                case SyncState.ERROR:
                    revert(sl, user);
                    break;

                default:
                    count--;
                    break;
            }

        }

        return count != 0;

    }

    @Override
    public boolean put(final Shoppinglist object, final User user) {
        object.setState(SyncState.SYNCING);
        getDB().editList(object, user);

        Response.Listener<JSONObject> listListener = new Response.Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (response != null) {

					/*
					 * If local isn't equal to server version, take server version.
					 * Don't push changes yet, we want to check item state too.
					 */

                    Shoppinglist serverSl = Shoppinglist.fromJSON(response);
                    Shoppinglist localSl = getDB().getList(serverSl.getId(), user);
                    if (localSl != null && !serverSl.getModified().equals(localSl.getModified())) {
                        serverSl.setState(SyncState.SYNCED);
                        // If server haven't delivered an prev_id, then use old id
                        serverSl.setPreviousId(serverSl.getPreviousId() == null ? object.getPreviousId() : serverSl.getPreviousId());
                        getDB().editList(serverSl, user);
//                        mBuilder.edit(serverSl);
                    }
                    popRequest();
//                    syncLocalItemChanges(object, user);

                } else {

                    popRequest();
                    if (error.getCode() == ShopGunError.Code.NETWORK_ERROR) {
						/* Ignore missing network, wait for next iteration */
                    } else {
                        revert(object, user);
                    }

                }


            }
        };

        String url = Api.Endpoint.list(user.getUserId(), object.getId());
        JsonObjectRequest listReq = new JsonObjectRequest(Request.Method.PUT, url, object.toJSON(), listListener);
        addRequest(listReq);
        return true;
    }

    @Override
    public boolean delete(final Shoppinglist object, final User user) {
        return false;
    }

    @Override
    public boolean insert(final Shoppinglist object, final User user) {
        return false;
    }

    @Override
    public boolean revert(final Shoppinglist object, final User user) {
        return false;
    }


}
