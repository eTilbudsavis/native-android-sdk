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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.eTilbudsavis.etasdk.Eta;

public class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /* 
         * Eta.create(Context ctx) must be invoked once, to instantiate the SDK
         * prior to calling Eta.getInstance().
         * 
         * Calling Eta.create(Context ctx) can also be called from Application.onCreate().
         * 
         * ApiKey and ApiSecret are not included in the demo/SDK, but you can
         * get your own at https://etilbudsavis.dk/developers/ :-)
         */
        if (!Eta.isCreated()) {

            // Create your instance of Eta
            Eta.create(this);
			
			/* You can optionally set a develop flag. 
			 * I'm using BuildConfig, but you can choose what ever scheme you want.
			 */
            Eta.getInstance().setDevelop(BuildConfig.DEBUG);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        Eta.getInstance().onStart();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Eta.getInstance().onStop();
    }
}
