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
 * <p>This class contains a sub-set of strings that ShopGun uses for sorting lists queries.
 * All sort types are in ascending order, but can be reverted to descending order by prepending
 * the string "-" to any of them.</p>
 */
public class SortBy {

    /**
     * SortBy a list by popularity in ascending order.
     */
    public static final String POPULARITY = "popularity";

    /**
     * SortBy a list by distance in ascending order.
     */
    public static final String DISTANCE = "distance";

    /**
     * SortBy a list by name in ascending order.
     */
    public static final String NAME = "name";

    /**
     * SortBy a list by published in ascending order.
     */
    public static final String PUBLICATION_DATE = "publication_date";

    /**
     * SortBy a list by expired in ascending order.
     */
    public static final String EXPIRATION_DATE = "expiration_date";

    /**
     * SortBy a list by created in ascending order.
     */
    public static final String CREATED = "created";

    /**
     * SortBy a list by page (in catalog) in ascending order.
     */
    public static final String PAGE = "page";

    /**
     * SortBy a list by it's internal score in ascending order.
     */
    public static final String SCORE = "score";

    /**
     * SortBy a list by price in ascending order.
     */
    public static final String PRICE = "price";

    public static final String DEALER = "dealer";

    public static final String SAVINGS = "savings";

    public static final String QUANTITY = "quantity";

    public static final String COUNT = "count";

    public static final String VALID_DATE = "valid_date";

}
