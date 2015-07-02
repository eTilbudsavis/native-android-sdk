/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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

package com.eTilbudsavis.sdkdemo;

import com.eTilbudsavis.etasdk.Eta;

public class ApiCheck {

    public static final String DEF_KEY = "add_your_key";

    private static final String ERR = "You must replace the API key, and API secret found in strings.xml " +
            "with your own keys found here: https://etilbudsavis.dk/developers/apps/";

    private ApiCheck() {

    }

    public static void checkKeys(Eta e) {
        if (DEF_KEY.equals(e.getApiKey()) || DEF_KEY.equals(e.getApiSecret())) {
            throw new IllegalArgumentException(ERR);
        }
    }

}
