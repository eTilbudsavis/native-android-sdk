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

package com.shopgun.android.sdk.network;


/**
 * TODO: Write documentation on how this interface should be implemented
 *
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public interface Network {

    /**
     * Method performing the Request
     * @param request to perform
     * @return a NetworkResponse, fulfilling the Request
     * @throws ShopGunError in the case of an unexpected error
     */
    public NetworkResponse performRequest(Request<?> request) throws ShopGunError;

}
