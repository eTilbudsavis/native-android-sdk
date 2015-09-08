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

/**
 * This class contains sub-set of strings, that ShopGun API v2 uses as parameters,
 * for queries to the API.
 */
public class Parameters {

    /**
     * String identifying the order by parameter for all list calls to the API
     */
    public static final String ORDER_BY = "order_by";

    /**
     * API v2 parameter name for sensor.
     */
    public static final String SENSOR = "r_sensor";

    /**
     * API v2 parameter name for latitude.
     */
    public static final String LATITUDE = "r_lat";

    /**
     * API v2 parameter name for longitude.
     */
    public static final String LONGITUDE = "r_lng";

    /**
     * API v2 parameter name for radius.
     */
    public static final String RADIUS = "r_radius";

    /**
     * API v2 parameter name for bounds east.
     */
    public static final String BOUND_EAST = "b_east";

    /**
     * API v2 parameter name for bounds north.
     */
    public static final String BOUND_NORTH = "b_north";

    /**
     * API v2 parameter name for bounds south.
     */
    public static final String BOUND_SOUTH = "b_south";

    /**
     * API v2 parameter name for bounds west.
     */
    public static final String BOUND_WEST = "b_west";

    /**
     * API v2 parameter name for API Key
     */
    public static final String API_KEY = "api_key";

    /**
     * String identifying the offset parameter for all list calls to the API
     */
    public static final String OFFSET = "offset";

    /**
     * String identifying the limit parameter for all list calls to the API
     */
    public static final String LIMIT = "limit";

    /**
     * String identifying the run from parameter for all list calls to the API
     */
    public static final String RUN_FROM = "run_from";

    /**
     * String identifying the run till parameter for all list calls to the API
     */
    public static final String RUN_TILL = "run_till";

    /**
     * String identifying the color parameter for all list calls to the API
     */
    public static final String COLOR = "color";

    /**
     * Parameter for pdf file location
     */
    public static final String PDF = "pdf";

    /**
     * Parameter for a resource name, e.g. dealer name
     */
    public static final String NAME = "name";

    /**
     * Parameter for a dealer resource
     */
    public static final String DEALER = "dealer";

    /**
     * Parameter for the friendly name of a website
     */
    public static final String URL_NAME = "url_name";

    /** Parameter for pageflip color */
//		public static final String PAGEFLIP_COLOR = "pageflip_color";

    /**
     * Parameter for the absolute address of a website
     */
    public static final String WEBSITE = "website";

    /**
     * Parameter for a resource logo
     */
    public static final String LOGO = "logo";

    /**
     * Parameter for search
     */
    public static final String QUERY = "query";

    /** Parameter for pageflip logo location */
//		public static final String PAGEFLIP_LOGO = "pageflip_Logo";

    /**
     * Parameter for catalog id's
     */
    public static final String CATALOG_IDS = "catalog_ids";

    /**
     * Parameter for catalog id's
     */
    public static final String CATALOG_ID = "catalog_id";

    /**
     * Parameter for store id's
     */
    public static final String STORE_IDS = "store_ids";

    /**
     * Parameter for area id's
     */
    public static final String AREA_IDS = "area_ids";

    /**
     * Parameter for store id's
     */
    public static final String OFFER_IDS = "offer_ids";

    /**
     * Parameter for getting a list of specific dealer id's
     */
    public static final String DEALER_IDS = "dealer_ids";

    /**
     * Parameter for a resource e-mail
     */
    public static final String EMAIL = "email";

    /**
     * Parameter for a resource password
     */
    public static final String PASSWORD = "password";

    /**
     * Parameter for a resource birth year
     */
    public static final String BIRTH_YEAR = "birth_year";

    /**
     * Parameter for a resource gender
     */
    public static final String GENDER = "gender";

    /**
     * Parameter for a resource success redirect
     */
    public static final String SUCCESS_REDIRECT = "success_redirect";

    /**
     * Parameter for a resource error redirect
     */
    public static final String ERROR_REDIRECT = "error_redirect";

    /**
     * Parameter for a resource old password
     */
    public static final String OLD_PASSWORD = "old_password";

    /**
     * Parameter for a facebook token
     */
    public static final String FACEBOOK_TOKEN = "facebook_token";

    /**
     * Parameter for a delete filter
     */
    public static final String FILTER_DELETE = "filter";

    public static final String ID = "id";

    public static final String MODIFIED = "modified";

    public static final String ERN = "ern";

    public static final String ACCESS = "access";

    public static final String ACCEPT_URL = "accept_url";

    public static final String DESCRIPTION = "description";

    public static final String COUNT = "count";

    public static final String TICK = "tick";

    public static final String OFFER_ID = "offer_id";

    public static final String CREATOR = "creator";

    public static final String SHOPPING_LIST_ID = "shopping_list_id";

    /**
     * Parameter for a resource token time to live
     */
    public static final String TOKEN_TTL = "token_ttl";

    /**
     * Parameter for a v1 session migration
     */
    public static final String V1_AUTH_ID = "v1_auth_id";

    /**
     * Parameter for a v1 session migration
     */
    public static final String V1_AUTH_TIME = "v1_auth_time";

    /**
     * Parameter for a v1 session migration
     */
    public static final String V1_AUTH_HASH = "v1_auth_hash";

    /**
     * Parameter for locale
     */
    public static final String LOCALE = "locale";

    /**
     * Parameter for sending the app version for better app statistics in insight
     */
    public static final String API_AV = "api_av";

    /**
     * Parameter for sending the current locale of the device
     */
    public static final String API_LOCALE = "r_locale";

    /**
     * Parameter not yet integrated in the API
     */
    public static final String ACCURACY = "accuracy";

    /**
     * Parameter not yet integrated in the API
     */
    public static final String BEARING = "bearing";

    /**
     * Parameter not yet integrated in the API
     */
    public static final String ALTITUDE = "altitude";

    /**
     * Parameter not yet integrated in the API
     */
    public static final String PROVIDER = "provider";

    /**
     * Parameter not yet integrated in the API
     */
    public static final String SPEED = "speed";

    /**
     * Parameter not yet integrated in the API
     */
    public static final String TIME = "time";

    /**
     * Parameter not yet integrated in the API
     */
    public static final String ADDRESS = "address";

}
