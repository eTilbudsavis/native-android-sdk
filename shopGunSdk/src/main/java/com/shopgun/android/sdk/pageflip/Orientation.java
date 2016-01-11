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

package com.shopgun.android.sdk.pageflip;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;

public enum Orientation implements Parcelable {

    LANDSCAPE {
        @Override
        public String toString() {
            return "landscape";
        }
    } , PORTRAIT {
        @Override
        public String toString() {
            return "portrait";
        }
    };

    private static Orientation[] allValues = values();

    public static Orientation fromOrdinal(int n) {
        return allValues[n > allValues.length ? 0 : n];
    }

    public static Orientation fromContext(Context c) {
        return fromConfiguration(c.getResources().getConfiguration());
    }

    public static Orientation fromConfiguration(Configuration c) {
        if (c == null) {
            return PORTRAIT;
        }
        switch (c.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return LANDSCAPE;
            case Configuration.ORIENTATION_PORTRAIT:
                return PORTRAIT;
            default:
                return PORTRAIT;
        }
    }

    public boolean isLandscape() {
        return Orientation.this == LANDSCAPE;
    }

    public boolean isPortrait() {
        return Orientation.this == PORTRAIT;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public static final Creator<Orientation> CREATOR = new Creator<Orientation>() {
        @Override
        public Orientation createFromParcel(final Parcel source) {
            return fromOrdinal(source.readInt());
        }

        @Override
        public Orientation[] newArray(final int size) {
            return new Orientation[size];
        }
    };

}