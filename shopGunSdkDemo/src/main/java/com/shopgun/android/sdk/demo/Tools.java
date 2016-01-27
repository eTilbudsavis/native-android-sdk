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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.network.mock.api.MockApiNetwork;
import com.shopgun.android.sdk.utils.ColorUtils;

public class Tools {

    private static int mTextColorLight = Color.argb(0xF0, 0xFF, 0xFF, 0xFF);
    private static int mTextColorDark = ColorUtils.setAlphaComponent(Color.BLACK, 0xe0);

    private Tools() {

    }

    public static void shopGunCreate(Context c) {

        /*
         * There is two options to get a ShopGun instance:
         * 1) Just call ShopGun.getInstance(Context)
         * 2) Take control, by building ShopGun with the ShopGun.Builder
         *
         * You can instantiate ShopGun from your Application.onCreate().
         *
         * ApiKey and ApiSecret are not included in the demo/SDK, but you can
         * get your own at https://etilbudsavis.dk/developers/ :-)
         */
        if (!ShopGun.hasInstance()) {

            // The builder will automatically attach the ShopGun singleton.
            new ShopGun.Builder(c)
                    .setDevelop(BuildConfig.DEBUG)
                    .setNetwork(new MockApiNetwork(c))
                    .build();

        }

    }

    public static int getTextColor(int backgroundColor) {
        return getTextColor(backgroundColor, mTextColorLight, mTextColorDark);
    }

    public static int getTextColor(int backgroundColor, int textLight, int textDark) {
        return ColorUtils.calculateLuminance(backgroundColor) < 0.5 ? textLight : textDark;
    }

    public static void showDialog(Activity a, String title, String message) {
        AlertDialog.Builder b = new AlertDialog.Builder(a);
        b.setTitle(title);
        b.setMessage(message);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        b.show();
    }

}
