package com.shopgun.android.sdk.pagedpublicationkit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shopgun.android.utils.log.L;
import com.shopgun.android.utils.log.LogUtil;
import com.shopgun.android.verso.VersoFragment;
import com.shopgun.android.verso.VersoViewPager;

import java.util.List;

public class PagedPublicationFragment extends VersoFragment {

    public static final String TAG = PagedPublicationFragment.class.getSimpleName();

    public static final String STATE_CONFIGURATION = "state_paged_publication_configuration";

    FrameLayout mFrame;
    PagedPublicationConfiguration mConfig;
    PagedPublicationConfiguration.OnLoadComplete mOnLoadComplete = new PagedPublicationConfiguration.OnLoadComplete() {
        @Override
        public void onPublicationLoaded(PagedPublication publication) {
            L.d(TAG, "onPublicationLoaded");
            if (publication != null) {
                notifyVersoConfigurationChanged();
            } else {
                // TODO: 27/10/16 Show error view
            }

        }

        @Override
        public void onPagesLoaded(List<? extends PagedPublicationPage> pages) {
            L.d(TAG, "onPagesLoaded");
            if (pages != null) {
                notifyVersoConfigurationChanged();
            } else {
                // TODO: 27/10/16 Show error view
            }
        }

        @Override
        public void onHotspotsLoaded(PagedPublicationHotspots hotspots) {
            L.d(TAG, "onHotspotsLoaded");
            if (hotspots != null) {
                notifyVersoConfigurationChanged();
            }
        }

    };

    @Override
    public void notifyVersoConfigurationChanged() {
        if (mConfig.hasData()) {
            super.notifyVersoConfigurationChanged();
        }
    }

    public static PagedPublicationFragment newInstance() {
        return new PagedPublicationFragment();
    }

    public static PagedPublicationFragment newInstance(PagedPublicationConfiguration config) {
        Bundle args = new Bundle();
        args.putParcelable(STATE_CONFIGURATION, config);
        PagedPublicationFragment f = newInstance();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        VersoViewPager versoViewPager = (VersoViewPager) super.onCreateView(inflater, container, savedInstanceState);
        mFrame = new FrameLayout(container.getContext());
        int wh = ViewGroup.LayoutParams.MATCH_PARENT;
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(wh, wh);
        mFrame.setLayoutParams(lp);
        mFrame.addView(versoViewPager);
        return mFrame;
    }

    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mConfig = savedInstanceState.getParcelable(STATE_CONFIGURATION);
            setPublicationConfiguration(mConfig);
        } else if (getArguments() != null) {
            mConfig = getArguments().getParcelable(STATE_CONFIGURATION);
            setPublicationConfiguration(mConfig);
        }
        super.onRestoreState(savedInstanceState);
    }

    public PagedPublicationConfiguration getPublicationConfiguration() {
        return mConfig;
    }

    public void setPublicationConfiguration(PagedPublicationConfiguration configuration) {
        mConfig = configuration;
        if (getContext() != null) {
            mConfig.onConfigurationChanged(getResources().getConfiguration());
        }
        setVersoSpreadConfiguration(mConfig);
        loadPagedPublication();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPagedPublication();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mConfig != null) {
            mConfig.cancel();
        }
        outState.putParcelable(STATE_CONFIGURATION, mConfig);
    }

    private void loadPagedPublication() {
        if (mConfig != null) {
            if (mConfig.isLoading()) {
                // loader is already working on it,
                return;
            }
            if (mConfig.getPublication() != null &&
                            mConfig.hasPages() &&
                            mConfig.hasHotspots()) {
                // Already have the needed data
                return;
            }
            mConfig.load(mOnLoadComplete);
            // TODO: 27/10/16 Show loader view
        }
    }

    View getErrorView() {
        TextView tv = new TextView(mFrame.getContext());
        tv.setText("Error!");
        return tv;
    }

    View getLoaderView() {
        TextView tv = new TextView(mFrame.getContext());
        tv.setText("Loading...");
        return tv;
    }

}
