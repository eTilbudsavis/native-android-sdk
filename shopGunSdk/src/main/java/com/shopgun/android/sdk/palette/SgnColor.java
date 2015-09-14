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

package com.shopgun.android.sdk.palette;

import android.graphics.Color;
import android.os.Parcel;

import com.shopgun.android.sdk.utils.ColorUtils;

public class SgnColor extends Color implements  MaterialColor {

    public static final String TAG = SgnColor.class.getSimpleName();

    private final int mColor;

    public SgnColor() {
        this(Color.BLACK);
    }

    public SgnColor(int color) {
        this.mColor = color;
    }

    public SgnColor(SgnColor color) {
        this.mColor = color.mColor;
    }

    @Override
    public MaterialColor getColor(Shade s) {
        return SgnPalette.getModifiedColor(mColor, s);
    }

    @Override
    public int getValue() {
        return mColor;
    }

    @Override
    public int getPrimaryText() {
        return SgnPalette.getPrimaryTextColor(mColor);
    }

    @Override
    public int getSecondaryText() {
        return SgnPalette.getSecondaryTextColor(mColor);
    }

    @Override
    public int getDisabledText() {
        return SgnPalette.getDisabledTextColor(mColor);
    }

    /**
     * Returns the luminance of the color.
     *
     * Formula defined here: http://www.w3.org/TR/2008/REC-WCAG20-20081211/#relativeluminancedef
     */
    @Override
    public double getLuminance() {
        return ColorUtils.calculateLuminance(mColor);
    }

    /**
     * luminance value above 0.95
     *
     * @return
     */
    @Override
    public boolean isVeryBright() {
        return SgnPalette.isVeryBright(mColor);
    }

    /** luminance value above 0.87 */
    @Override
    public boolean isBright() {
        return SgnPalette.isBright(mColor);
    }

    /** luminance value above 0.64 */
    @Override
    public boolean isLight() {
        return SgnPalette.isLight(mColor);
    }

    /** luminance value below 0.025 */
    @Override
    public boolean isVeryDark() {
        return SgnPalette.isVeryDark(mColor);
    }

    @Override
    public String toString() {
        return String.format("#%08X", mColor);
    }

    public boolean isDarker(int other) {
        return getLuminance() < ColorUtils.calculateLuminance(other);
    }

    public boolean isLighter(int other) {
        return getLuminance() > ColorUtils.calculateLuminance(other);
    }

    public String toHSVString() {
        return com.shopgun.android.sdk.utils.ColorUtils.toHsvString(mColor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SgnColor sgnColor = (SgnColor) o;

        return mColor == sgnColor.mColor;

    }

    @Override
    public int hashCode() {
        return mColor;
    }

    public boolean equals(SgnColor o, int tolerance) {

        if (this == o) return true;

        if (o == null) return false;

        if (mColor == o.mColor) {
            return true;
        }

        if (tolerance == 0) {
            return false;
        }

        int r1 = Color.red(mColor);
        int g1 = Color.green(mColor);
        int b1 = Color.blue(mColor);
        int a1 = Color.alpha(mColor);

        int r2 = Color.red(o.mColor);
        int g2 = Color.green(o.mColor);
        int b2 = Color.blue(o.mColor);
        int a2 = Color.alpha(o.mColor);

        return Math.abs(r1-r2) <= tolerance &&
                Math.abs(g1-g2) <= tolerance &&
                Math.abs(b1-b2) <= tolerance &&
                Math.abs(a1-a2) <= tolerance;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mColor);
    }

    protected SgnColor(Parcel in) {
        this.mColor = in.readInt();
    }

    public static final Creator<SgnColor> CREATOR = new Creator<SgnColor>() {
        public SgnColor createFromParcel(Parcel source) {
            return new SgnColor(source);
        }

        public SgnColor[] newArray(int size) {
            return new SgnColor[size];
        }
    };
}
