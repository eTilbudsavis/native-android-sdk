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

package com.shopgun.android.sdk.pageflip.stats;

import java.util.Random;

public class ClockFactory {

    static final Random mRandom = new Random();

    public static Clock getClock() {

        int i = mRandom.nextInt(2);

        switch (i) {
            case 0: return new NanoTimeClock();
            case 1: return new TimeSinceBootClock();
            case 2: return new WallClock();
            default:
                throw new IllegalStateException("What, can't you do math?");
        }

    }

}
