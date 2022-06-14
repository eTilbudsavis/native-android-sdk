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
    private VersoPageViewInterface.OnZoomListener mOnZoomListener;
    private VersoPageViewInterface.OnPanListener mOnPanListener;
    private VersoPageViewInterface.OnTouchListener mOnTouchListener;
    private VersoPageViewInterface.OnTapListener mOnTapListener;
    private VersoPageViewInterface.OnDoubleTapListener mOnDoubleTapListener;
    private VersoPageViewInterface.OnLongTapListener mOnLongTapListener;
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
        fragment.setOnZoomListener(mOnZoomListener);
        fragment.setOnPanListener(mOnPanListener);
        fragment.setOnTouchListener(mOnTouchListener);
        fragment.setOnTapListener(mOnTapListener);
        fragment.setOnDoubleTapListener(mOnDoubleTapListener);
        fragment.setOnLongTapListener(mOnLongTapListener);
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

    public void setOnTouchListener(VersoPageViewInterface.OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    public void setOnTapListener(VersoPageViewInterface.OnTapListener listener) {
        mOnTapListener = listener;
    }

    public void setOnDoubleTapListener(VersoPageViewInterface.OnDoubleTapListener listener) {
        mOnDoubleTapListener = listener;
    }

    public void setOnLongTapListener(VersoPageViewInterface.OnLongTapListener listener) {
        mOnLongTapListener = listener;
    }

    public void setOnZoomListener(VersoPageViewInterface.OnZoomListener listener) {
        mOnZoomListener = listener;
    }

    public void setOnPanListener(VersoPageViewInterface.OnPanListener listener) {
        mOnPanListener = listener;
    }

    public void setOnLoadCompleteListener(VersoPageViewInterface.OnLoadCompleteListener listener) {
        mOnLoadCompleteListener = listener;
    }

}
