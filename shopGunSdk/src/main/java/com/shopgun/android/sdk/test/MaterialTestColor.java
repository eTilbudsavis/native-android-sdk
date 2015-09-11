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

package com.shopgun.android.sdk.test;

import android.graphics.Color;
import android.os.Parcel;

import com.shopgun.android.sdk.palette.MaterialColor;
import com.shopgun.android.sdk.palette.SgnColor;
import com.shopgun.android.sdk.palette.Shade;

import java.util.HashMap;
import java.util.Map;

public class MaterialTestColor implements MaterialColor {

    float threshold = 0.01f;
    int value = Color.BLACK;
    double luminance = 0.0d;
    int primaryText = 0xFFFFFFFF;
    int secondaryText = 0xB2FFFFFF;
    int disabledText = 0x4CFFFFFF;
    Map<Shade, Float[]> shades = new HashMap<Shade, Float[]>();


    @Override
    public MaterialColor getColor(Shade s) {
        return null;
    }

    @Override
    public int getValue() {
        return 0;
    }

    @Override
    public int getPrimaryText() {
        return 0;
    }

    @Override
    public int getSecondaryText() {
        return 0;
    }

    @Override
    public int getDisabledText() {
        return 0;
    }

    @Override
    public double getLuminance() {
        return 0;
    }

    @Override
    public boolean isVeryBright() {
        return false;
    }

    @Override
    public boolean isBright() {
        return false;
    }

    @Override
    public boolean isLight() {
        return false;
    }

    @Override
    public boolean isVeryDark() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
