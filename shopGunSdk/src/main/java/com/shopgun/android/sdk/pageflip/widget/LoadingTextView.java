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

package com.shopgun.android.sdk.pageflip.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.R;

public class LoadingTextView extends TextView {

    public static final String TAG = Constants.getTag(LoadingTextView.class);

    public static final int DELAY = 350;
    public static final int NUM_DOTS = 5;
    private Handler mHandler;
    private int mDots = 1;
    private boolean mCountUp = true;
    private String mLoadingText;
    Runnable mTextRunner = new Runnable() {

        public void run() {

            mHandler.postDelayed(mTextRunner, DELAY);

            StringBuilder sb = new StringBuilder();
            sb.append(mLoadingText == null ? "" : mLoadingText).append("\n");
            for (int i = 0; i < mDots; i++) {
                sb.append(".");
            }
            setText(sb.toString());
            if (mCountUp) {
                mDots++;
                mCountUp = mDots != NUM_DOTS;
            } else {
                mDots--;
                mCountUp = (mDots == 1);
            }
        }
    };

    public LoadingTextView(Context context) {
        super(context);
        init();
    }

    public LoadingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        mHandler = new Handler();
    }

    public void setLoadingText(String header) {
        mLoadingText = header;
    }

    public void error() {
        error(getResources().getString(R.string.shopgun_sdk_pageflip_load, mLoadingText));
    }

    public void error(String text) {
        stop();
        super.setText(text);
    }

    public void stop() {
        mHandler.removeCallbacks(mTextRunner);
        mDots = 1;
        mCountUp = true;
    }

    public void start() {
        stop();
        mTextRunner.run();
    }

}