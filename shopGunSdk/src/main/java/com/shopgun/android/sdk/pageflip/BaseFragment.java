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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.sdk.log.SgnLog;

public class BaseFragment extends Fragment {

    private final boolean mPrintMethods = true;

    /* Lifecycle related events */

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        printCallingMethod(mPrintMethods || false);
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        printCallingMethod(mPrintMethods || false);
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        printCallingMethod(mPrintMethods || false);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        printCallingMethod(mPrintMethods || false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        printCallingMethod(mPrintMethods || false);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        printCallingMethod(mPrintMethods || false);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        printCallingMethod(mPrintMethods || false);
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        printCallingMethod(mPrintMethods || false);
        super.onStart();
    }


    @Override
    public void onResume() {
        printCallingMethod(mPrintMethods || false);
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        printCallingMethod(mPrintMethods || false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        printCallingMethod(mPrintMethods || false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPause() {
        printCallingMethod(mPrintMethods || false);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        printCallingMethod(mPrintMethods || false);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        printCallingMethod(mPrintMethods || false);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        printCallingMethod(mPrintMethods || false);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        printCallingMethod(mPrintMethods || false);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        printCallingMethod(mPrintMethods || false);
        super.onDetach();
    }

    /* other events */

    private void printCallingMethod(boolean print) {
        if (print) {
            StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
            String text = ste.getMethodName() + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")";
            SgnLog.d(getClass().getSimpleName(), text);
        }
    }

}
