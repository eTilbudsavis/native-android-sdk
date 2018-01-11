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

package com.shopgun.android.sdk.bus;

import com.shopgun.android.sdk.BuildConfig;
import com.shopgun.android.sdk.utils.Constants;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusBuilder;

public class SgnBus {

    public static final String TAG = Constants.getTag(SgnBus.class);

    private static EventBus BUS;

    private static boolean DEBUG = BuildConfig.DEBUG;

    private SgnBus() {
        // empty
    }

    public static EventBus getInstance() {
        if (BUS == null) {
            EventBusBuilder b = EventBus.builder();
            b.throwSubscriberException(DEBUG);
            b.logSubscriberExceptions(DEBUG);
            b.logNoSubscriberMessages(DEBUG);
            b.sendSubscriberExceptionEvent(DEBUG);
            b.sendNoSubscriberEvent(DEBUG);
            BUS = b.build();
        }
        return BUS;
    }

}
