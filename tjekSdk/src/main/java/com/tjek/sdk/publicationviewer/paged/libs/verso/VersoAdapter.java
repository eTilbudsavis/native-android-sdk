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
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.ArrayList;
import java.util.List;

public class VersoAdapter extends FragmentStatelessPagerAdapter {

    public static final String TAG = VersoAdapter.class.getSimpleName();

    private final VersoSpreadConfiguration mConfiguration;
    private VersoPageViewListener.EventListener mEventListener;
    private VersoPageViewListener.OnLoadCompleteListener mOnLoadCompleteListener;

    public VersoAdapter(FragmentManager fragmentManager, VersoSpreadConfiguration configuration) {
        super(fragmentManager);
        mConfiguration = configuration;
    }

    @Override
    public Fragment createItem(int position) {
        VersoPageViewFragment f = VersoPageViewFragment.newInstance(position);
        f.setVersoSpreadConfiguration(mConfiguration);
        return f;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        VersoPageViewFragment fragment = (VersoPageViewFragment) super.instantiateItem(container, position);
        fragment.setVersoPageViewEventListener(mEventListener);
        fragment.setOnLoadCompleteListener(mOnLoadCompleteListener);
        return fragment;
    }

    @Override
    public int getCount() {
        return mConfiguration.getSpreadCount();
    }

    @Override
    public float getPageWidth(int position) {
        return mConfiguration.getSpreadProperty(position).getWidth();
    }

    public VersoPageViewFragment getVersoFragment(int position) {
        return (VersoPageViewFragment) getItem(position);
    }

    public List<VersoPageViewFragment> getVersoFragments() {
        ArrayList<VersoPageViewFragment> list = new ArrayList<>();
        for (Fragment f : getFragments()) {
            if (f != null) {
                list.add((VersoPageViewFragment)f);
            }
        }
        return list;
    }

    @Override
    public Fragment[] getFragments() {
        return super.getFragments();
    }

    public void setEventListener(VersoPageViewListener.EventListener listener) {
        mEventListener = listener;
    }

    public void setOnLoadCompleteListener(VersoPageViewListener.OnLoadCompleteListener listener) {
        mOnLoadCompleteListener = listener;
    }

}
