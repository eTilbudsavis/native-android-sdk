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

package com.shopgun.android.sdk.api;

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Dealer;
import com.shopgun.android.sdk.model.Offer;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.utils.SgnUtils;

/**
 * This class contains a sub-set of paths needed to request data from the ShopGun API,
 * and has methods to generate paths, that dependent on information from objects (id's e.t.c).
 */
public class Endpoints {

    public static final String CATALOG_LIST = "/v2/catalogs";
    public static final String CATALOG_ID = "/v2/catalogs/";
    public static final String CATALOG_SEARCH = "/v2/catalogs/search";
    public static final String CATALOG_TYPEAHEAD = "/v2/catalogs/typeahead";
    public static final String CATALOG_SUGGEST = "/v2/catalogs/suggest";

    public static final String DEALER_LIST = "/v2/dealers";
    public static final String DEALER_ID = "/v2/dealers/";
    public static final String DEALER_SEARCH = "/v2/dealers/search";
    public static final String DEALER_SUGGEST = "/v2/dealers/suggest";

    public static final String OFFER_LIST = "/v2/offers";
    public static final String OFFER_ID = "/v2/offers/";
    public static final String OFFER_SEARCH = "/v2/offers/search";
    public static final String OFFER_TYPEAHEAD = "/v2/offers/typeahead";

    public static final String STORE_LIST = "/v2/stores";
    public static final String STORE_ID = "/v2/stores/";
    public static final String STORE_SEARCH = "/v2/stores/search";
    public static final String STORE_QUICK_SEARCH = "/v2/stores/quicksearch";

    public static final String FAVORITES_DEALERS_ID = "/v2/favorites/dealers/";
    public static final String FAVORITES_DEALERS_LIST = "/v2/favorites/dealers";

    public static final String SESSIONS = "/v2/sessions";

    public static final String USER = "/v2/users";

    public static final String USER_RESET = "/v2/users/reset";

    public static final String CATEGORIES = "/v2/categories";

    public static final String COUNTRIES = "/v2/countries";

    public static final String PUSH_PAYLOAD_ID = "/v2/push/payloads/";

    public static final String PUSH_DEVICE_ID = "/v2/push/devices";

    public static final String APP_LOG_ENDPOINT = "/v2/admin/utils/apps/log";

    public static final String SHOPPINGLIST_OFFERS = "/v2/shoppinglists/offers";

    /**
     * @param offerId An {@link Offer#getId()}
     * @return /v2/offers/{offer_id}
     */
    public static String offerId(String offerId) {
        return String.format("/v2/offers/%s", offerId);
    }

    /**
     * @param storeId A {@link Store#getId()}
     * @return /v2/stores/{store_id}
     */
    public static String storeId(String storeId) {
        return String.format("/v2/stores/%s", storeId);
    }

    /**
     * @param dealerId A {@link Dealer#getId()}
     * @return /v2/dealers/{dealer_id}
     */
    public static String dealerId(String dealerId) {
        return String.format("/v2/dealers/%s", dealerId);
    }

    /**
     * @param catalogId A {@link Catalog#getId()}
     * @return /v2/catalogs/{catalog_id}
     */
    public static String catalogId(String catalogId) {
        return String.format("/v2/catalogs/%s", catalogId);
    }

    /**
     * @param catalogId A {@link Catalog#getId()}
     * @return /v2/catalogs/{catalog_id}/pages
     */
    public static String catalogPages(String catalogId) {
        return String.format("/v2/catalogs/%s/pages", catalogId);
    }

    /**
     * @param catalogId A {@link Catalog#getId()}
     * @return /v2/catalogs/{catalog_id}/hotspots
     */
    public static String catalogHotspots(String catalogId) {
        return String.format("/v2/catalogs/%s/hotspots", catalogId);
    }

    /**
     * @param catalogId A {@link Catalog#getId()}
     * @return /v2/catalogs/{catalog_id}/collect
     */
    public static String catalogCollect(String catalogId) {
        return String.format("/v2/catalogs/%s/collect", catalogId);
    }

    /**
     * @param offerId An {@link Offer#getId()}
     * @return /v2/offers/{offer_id}/collect
     */
    public static String offerCollect(String offerId) {
        return String.format("/v2/offers/%s/collect", offerId);
    }

    /**
     * @param storeId A {@link Store#getId()}
     * @return /v2/stores/{store_id}/collect
     */
    public static String storeCollect(String storeId) {
        return String.format("/v2/stores/%s/collect", storeId);
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @return /v2/users/{user_id}/facebook
     */
    public static String facebook(int userId) {
        return String.format("/v2/users/%s/facebook", userId);
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @return /v2/users/{user_id}/shoppinglists
     */
    public static String lists(int userId) {
        return String.format("/v2/users/%s/shoppinglists", userId);
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @param listId A {@link Shoppinglist#getId()}
     * @return /v2/users/{user_id}/shoppinglists/{list_uuid}
     */
    public static String list(int userId, String listId) {
        return String.format("/v2/users/%s/shoppinglists/%s", userId, listId);
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @param listId A {@link Shoppinglist#getId()}
     * @return /v2/users/{user_id}/shoppinglists/{list_uuid}/modified
     */
    public static String listModified(int userId, String listId) {
        return String.format("/v2/users/%s/shoppinglists/%s/modified", userId, listId);
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @param listId A {@link Shoppinglist#getId()}
     * @return /v2/users/{user_id}/shoppinglists/{list_uuid}/empty
     */
    public static String listEmpty(int userId, String listId) {
        return String.format("/v2/users/%s/shoppinglists/%s/empty", userId, listId);
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @param listId A {@link Shoppinglist#getId()}
     * @return /v2/users/{user_id}/shoppinglists/{list_uuid}/shares
     */
    public static String listShares(int userId, String listId) {
        return String.format("/v2/users/%s/shoppinglists/%s/shares", userId, listId);
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @param listId A {@link Shoppinglist#getId()}
     * @param email An emailaddress
     * @return /v2/users/{user_id}/shoppinglists/{list_uuid}/shares/{email}
     */
    public static String listShareEmail(int userId, String listId, String email) {
        return String.format("/v2/users/%s/shoppinglists/%s/shares/%s", userId, listId, SgnUtils.encode(email, "UTF-8"));
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @param listId A {@link Shoppinglist#getId()}
     * @return /v2/users/{user_id}/shoppinglists/{list_uuid}/items
     */
    public static String listitems(int userId, String listId) {
        return String.format("/v2/users/%s/shoppinglists/%s/items", userId, listId);
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @param listId A {@link Shoppinglist#getId()}
     * @param itemId A {@link ShoppinglistItem#getId()}
     * @return /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid}
     */
    public static String listitem(int userId, String listId, String itemId) {
        return String.format("/v2/users/%s/shoppinglists/%s/items/%s", userId, listId, itemId);
    }

    /**
     * @param userId A {@link User#getUserId()}
     * @param listId A {@link Shoppinglist#getId()}
     * @param itemId A {@link ShoppinglistItem#getId()}
     * @return /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid}/modified
     */
    public static String listitemModifiedById(int userId, String listId, String itemId) {
        return String.format("/v2/users/%s/shoppinglists/%s/items/%s/modified", userId, listId, itemId);
    }

    /**
     * @param payloadId A payload id
     * @return /v2/push/payloads/{payload_id}
     */
    public static String pushPayload(String payloadId) {
        return String.format("/v2/push/payloads/%s", payloadId);
    }

}
