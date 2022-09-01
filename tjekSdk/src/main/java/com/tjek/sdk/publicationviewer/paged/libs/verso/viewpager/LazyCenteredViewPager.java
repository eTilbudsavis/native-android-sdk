package com.tjek.sdk.publicationviewer.paged.libs.verso.viewpager;
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
import androidx.viewpager.widget.PagerAdapter;

/**
 * When the PagerAdapter is set on the ViewPager, the first items are always populated.
 * We'll try to prevent this behaviour.
 */
public class LazyCenteredViewPager extends CenteredViewPager {

    public static final String TAG = LazyCenteredViewPager.class.getSimpleName();

    boolean mCurrentItemSet = false;
    boolean mSetAdapterFlag = false;

    public LazyCenteredViewPager(Context context) {
        super(context);
    }

    public LazyCenteredViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        mSetAdapterFlag = true;
        super.setAdapter(adapter);
        mSetAdapterFlag = false;
    }

    @Override
    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
        if(!mSetAdapterFlag) {
            mCurrentItemSet = true;
        }
        super.setCurrentItemInternal(item, smoothScroll, always, velocity);
    }

    @Override
    void populate() {
        if(mCurrentItemSet) {
            super.populate();
        }
    }

}
