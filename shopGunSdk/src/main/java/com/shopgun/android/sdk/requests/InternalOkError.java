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

package com.shopgun.android.sdk.requests;

import android.os.Parcelable;

import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.Constants;

public class InternalOkError extends ShopGunError {

    public static final String TAG = Constants.getTag(InternalOkError.class);

    private static final int INTERNAL_ERROR_OK_SIGNAL = Integer.MAX_VALUE;

    protected InternalOkError() {
        super(INTERNAL_ERROR_OK_SIGNAL,
                "Internal OK",
                "An internal signal to run the remaining request queue");
    }

    public static final Parcelable.Creator<ShopGunError> CREATOR = ShopGunError.CREATOR;

}
