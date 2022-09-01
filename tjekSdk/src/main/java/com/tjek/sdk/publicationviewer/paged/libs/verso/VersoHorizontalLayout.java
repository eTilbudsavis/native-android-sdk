package com.tjek.sdk.publicationviewer.paged.libs.verso;
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
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class VersoHorizontalLayout extends LinearLayout {
    
    public static final String TAG = VersoHorizontalLayout.class.getSimpleName();

    public VersoHorizontalLayout(Context context) {
        super(context);
    }

    public VersoHorizontalLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VersoHorizontalLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
    }

    private int getVisibleChildCount() {
        int c = getChildCount();
        for (int i = 0; i < c; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                c--;
            }
        }
        return c;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        int containerWidth = 0;
        int containerHeight = 0;

        final int childCount = getChildCount();
        if (childCount > 0) {
            final int childWidth = width / getVisibleChildCount();
            final int childHeight = height;

            final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST);
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST);

            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    containerWidth += child.getMeasuredWidth();
                    containerHeight = Math.max(containerHeight, child.getMeasuredHeight());
                }
            }
        }
        setMeasuredDimension(containerWidth, containerHeight);
    }

}
