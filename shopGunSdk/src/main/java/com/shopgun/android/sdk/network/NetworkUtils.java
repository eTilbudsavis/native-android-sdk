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

import com.shopgun.android.sdk.SgnLocation;
import com.shopgun.android.sdk.api.Parameters;

import java.util.Map;

public class NetworkUtils {

    private NetworkUtils() {
        // private
    }

    public static void appendLocationParams(Map<String, String> map, SgnLocation l) {

        if (!l.isSet()) {
            return;
        }

        if (!map.containsKey(Parameters.LATITUDE)) {
            map.put(Parameters.LATITUDE, String.valueOf(l.getLatitude()));
        }
        if (!map.containsKey(Parameters.LONGITUDE)) {
            map.put(Parameters.LONGITUDE, String.valueOf(l.getLongitude()));
        }
        if (!map.containsKey(Parameters.SENSOR)) {
            map.put(Parameters.SENSOR, String.valueOf(l.isSensor()));
        }
        if (!map.containsKey(Parameters.RADIUS)) {
            map.put(Parameters.RADIUS, String.valueOf(l.getRadius()));
        }

        // Determine whether to include bounds.
        if (l.isBoundsSet()) {
            if (!map.containsKey(Parameters.BOUND_EAST)) {
                map.put(Parameters.BOUND_EAST, String.valueOf(l.getBoundEast()));
            }
            if (!map.containsKey(Parameters.BOUND_NORTH)) {
                map.put(Parameters.BOUND_NORTH, String.valueOf(l.getBoundNorth()));
            }
            if (!map.containsKey(Parameters.BOUND_SOUTH)) {
                map.put(Parameters.BOUND_SOUTH, String.valueOf(l.getBoundSouth()));
            }
            if (!map.containsKey(Parameters.BOUND_WEST)) {
                map.put(Parameters.BOUND_WEST, String.valueOf(l.getBoundWest()));
            }
        }

    }

}
