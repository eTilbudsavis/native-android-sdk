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

package com.shopgun.android.sdk;

import android.content.Context;
import android.support.v4.app.Fragment;

public class SgnFragment extends Fragment {

    private ShopGun mShopgun;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mShopgun = ShopGun.getInstance(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        // We'll just make sure we're running for the duration of this fragment
        mShopgun.onStart();
    }

    @Override
    public void onStop() {
        mShopgun.onStop();
        super.onStop();
    }

}
