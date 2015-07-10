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

package com.shopgun.android.sdk.utils;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.EtaLog;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.network.ShopGunError;

import org.json.JSONObject;

import java.util.List;

/**
 * A class to help print debug messages.
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class PrettyPrint {

    public static final String TAG = Constants.getTag(PrettyPrint.class);

    /**
     * Prints the essential parameters (for debugging) a list of ShoppinglistItems
     * @param name A name, an action perhaps.
     * @param items The list to print
     */
    public static void printItems(String name, List<ShoppinglistItem> items) {

        StringBuilder sb = new StringBuilder();
        for (ShoppinglistItem sli : items) {
            sb.append(shoppinglistItemToString(sli)).append("\n");
        }
        EtaLog.d(TAG, name + "\n" + sb.toString());
    }

    /**
     * Method for printing some essentials of a ShoppinglistItem.
     * <p>Example: "Deleted - item[cola      ] prev[00000000] id[36473829] modified[2014-03-11T14:39:25+0100]"</p>
     * @param name A name for the item (an action perhaps)
     * @param sli A ShoppinglistItem to print
     */
    public static void printItem(String name, ShoppinglistItem sli) {
        EtaLog.d(TAG, String.format("%s - %s", name, shoppinglistItemToString(sli)));
    }

    /**
     * Get the essential parameters from a shoppinglistItem.
     * <p>Example: "item[cola      ] prev[00000000] id[36473829] modified[2014-03-11T14:39:25+0100]"</p>
     * @param sli
     * @return
     */
    public static String shoppinglistItemToString(ShoppinglistItem sli) {
        String id = sli.getId().substring(0, 8);
        String prev = sli.getPreviousId() == null ? "null" : sli.getPreviousId().substring(0, 8);
        String title = sli.getDescription();
        if (title.length() > 8) {
            title = title.substring(0, 8);
        }
        String resp = "item[%-8s] prev[%s] id[%s] modified[%s]";
        resp = String.format(resp, title, prev, id, Utils.dateToString(sli.getModified()));
        return resp;
    }

    /**
     *
     * @param type
     * @param isServer
     * @param added
     * @param deleted
     * @param edited
     */
    public static void printListenerCallback(String type, boolean isServer, List<?> added, List<?> deleted, List<?> edited) {
        String text = "type[%s], isServer[%s], added[%s], deleted[%s], edited[%s]";
        EtaLog.d(TAG, String.format(text, type, isServer, added.size(), deleted.size(), edited.size()));
    }


    public static void printResponse(String tag, String name, JSONObject response, ShopGunError error) {

        if (response != null) {
            EtaLog.d(tag, name + " - " + response.toString());
        } else {
            EtaLog.d(tag, name + " - " + error.toJSON().toString());
        }

    }

}
