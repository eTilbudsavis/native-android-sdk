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

import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.api.JsonKeys;
import com.shopgun.android.sdk.api.MetaKeys;
import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.api.SortBy;

/**
 * The {@link Api} class contains miscellaneous helper variables, and classes, which comes very handy when interacting with the API.
 *
 * <p>For a complete set of keys and their respective parameters, as well as detailed documentation,
 * in a given context, we will refer you to the <a href="http://docs.api.etilbudsavis.dk/">API documentation</a>
 * </p>
 */
public final class Api {

    private Api() {
        // empty
    }

    /**
     * @deprecated Use {@link com.shopgun.android.sdk.api.Endpoints}
     */
    public static class Endpoint extends Endpoints {

    }

    /**
     * @deprecated Use {@link com.shopgun.android.sdk.api.JsonKeys}
     */
    public class JsonKey extends JsonKeys {

    }

    /**
     * @deprecated Use {@link com.shopgun.android.sdk.api.MetaKeys}
     */
    public class MetaKey extends MetaKeys {

    }

    /**
     * @deprecated Use {@link SortBy}
     */
    public class Sort extends SortBy {

    }

    /**
     * @deprecated Use {@link com.shopgun.android.sdk.api.Parameters}
     */
    public class Param extends Parameters {

    }


}
