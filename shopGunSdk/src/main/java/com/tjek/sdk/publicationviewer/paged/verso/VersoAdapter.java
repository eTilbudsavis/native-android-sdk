package com.tjek.sdk.publicationviewer.paged.verso;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.ArrayList;
import java.util.List;

public class VersoAdapter extends FragmentStatelessPagerAdapter {

    public static final String TAG = VersoAdapter.class.getSimpleName();

    private final VersoSpreadConfiguration mConfiguration;
    private VersoPageViewInterface.EventListener mEventListener;
    private VersoPageViewInterface.OnLoadCompleteListener mOnLoadCompleteListener;

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

    public void setEventListener(VersoPageViewInterface.EventListener listener) {
        mEventListener = listener;
    }

    public void setOnLoadCompleteListener(VersoPageViewInterface.OnLoadCompleteListener listener) {
        mOnLoadCompleteListener = listener;
    }

}
