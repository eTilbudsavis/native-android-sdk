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

import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;

import com.eTilbudsavis.etasdk.Eta;

public class Tools {

    private static int mTextColorLight = Color.argb(0xF0, 0xFF, 0xFF, 0xFF);
    private static int mTextColorDark = ColorUtils.setAlphaComponent(Color.BLACK, 0xe0);

    private Tools() {

    }

    public static void etaCreate(Context c) {

        /*
         * Eta.etaCreate(Context ctx) must be invoked once, to instantiate the SDK
         * prior to calling Eta.getInstance().
         *
         * Calling Eta.etaCreate(Context ctx) can also be called from Application.onCreate().
         *
         * ApiKey and ApiSecret are not included in the demo/SDK, but you can
         * get your own at https://etilbudsavis.dk/developers/ :-)
         */
        if (!Eta.isCreated()) {

            // Create your instance of Eta
            Eta.create(c);

			/* You can optionally set a develop flag.
			 * I'm using BuildConfig, but you can choose what ever scheme you want.
			 */
            Eta.getInstance().setDevelop(BuildConfig.DEBUG);
        }

    }

    public static int getTextColor(int backgroundColor) {
        return getTextColor(backgroundColor, mTextColorLight, mTextColorDark);
    }

    public static int getTextColor(int backgroundColor, int textLight, int textDark) {
        return ColorUtils.calculateLuminance(backgroundColor) < 0.5 ? textLight : textDark;
    }

}
