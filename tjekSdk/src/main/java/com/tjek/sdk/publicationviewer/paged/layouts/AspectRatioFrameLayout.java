package com.tjek.sdk.publicationviewer.paged.layouts;
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.shopgun.android.utils.NumberUtils;

public class AspectRatioFrameLayout extends FrameLayout {

    public static final String TAG = AspectRatioFrameLayout.class.getSimpleName();

    float mAspectRatio;

    public AspectRatioFrameLayout(Context context) {
        super(context);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
    }

    public float getAspectRatio() {
        return mAspectRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (NumberUtils.isEqual(mAspectRatio, 0f)) {
            // Aspect ratio haven't been set, let super handle it
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        float containerWidth = MeasureSpec.getSize(widthMeasureSpec);
        float containerHeight = MeasureSpec.getSize(heightMeasureSpec);
        float containerAspectRatio = containerWidth/containerHeight;

        if (mAspectRatio < containerAspectRatio) {
            containerWidth = containerHeight * mAspectRatio;
        } else if (mAspectRatio > containerAspectRatio) {
            containerHeight = containerWidth / mAspectRatio;
        }

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int)containerWidth, MeasureSpec.AT_MOST);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int)containerHeight, MeasureSpec.AT_MOST);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);

        setMeasuredDimension((int) containerWidth, (int) containerHeight);
    }

}
