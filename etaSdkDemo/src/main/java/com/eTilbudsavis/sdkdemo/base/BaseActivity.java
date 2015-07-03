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

package com.eTilbudsavis.sdkdemo.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.sdkdemo.Tools;

public class BaseActivity extends FragmentActivity {

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Tools.etaCreate(this);
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

    protected void showProgress(String title, String message) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, title, message, true, true);
        }
    }

    protected void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

}
