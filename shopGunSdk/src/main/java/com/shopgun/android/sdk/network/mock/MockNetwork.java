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

package com.shopgun.android.sdk.network.mock;

import com.shopgun.android.sdk.network.Network;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.ShopGunError;

import java.util.Random;

public abstract class MockNetwork implements Network {

    long mDelayMin = 0;
    long mDelayMax = 0;

    @Override
    public NetworkResponse performRequest(Request<?> request) throws ShopGunError {

        if (mDelayMin > 0) {
            try {
                int offset = (int)(mDelayMax-mDelayMin);
                int sleep = new Random().nextInt(offset+1) + (int)(mDelayMin);
                Thread.sleep(sleep);
            } catch (Exception e) {
                // ignore
            }

        }

        return new MockUnsupportedNetworkResponse(request);
    }

    public long getDelayMin() {
        return mDelayMin;
    }

    public void setDelayMin(long delayMin) {
        if (delayMin >= 0) {
            this.mDelayMin = delayMin;
        }
        if (mDelayMin > mDelayMax) {
            mDelayMax = mDelayMin;
        }
    }

    public long getDelayMax() {
        return mDelayMax;
    }

    public void setDelayMax(long delayMax) {
        if (delayMax >= 0) {
            this.mDelayMax = delayMax;
        }
        if (mDelayMax < mDelayMin) {
            mDelayMin = mDelayMax;
        }
    }

}
