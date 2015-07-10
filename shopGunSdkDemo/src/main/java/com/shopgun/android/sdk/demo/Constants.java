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

package com.shopgun.android.sdk.demo;

import java.util.HashMap;
import java.util.Map;


public class Constants {

    public static final LatLng ETA_HQ = new LatLng(55.6310771f, 12.5771624f);
    public static final LatLng AALBORG = new LatLng(57.0433192f, 9.9198792f);
    public static final LatLng AARHUS = new LatLng(56.1499752f, 10.2009121f);
    public static final LatLng ODENSE = new LatLng(55.3949f, 10.3915109f);

    private Constants() {
    }

    public static class LatLng {
        float lat;
        float lng;

        public LatLng(float lat, float lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

}
