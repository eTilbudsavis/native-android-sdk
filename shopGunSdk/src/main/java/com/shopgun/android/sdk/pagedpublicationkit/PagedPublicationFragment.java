package com.shopgun.android.sdk.pagedpublicationkit;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shopgun.android.sdk.R;
import com.shopgun.android.verso.VersoFragment;
import com.shopgun.android.verso.VersoViewPager;

import java.util.List;

public class PagedPublicationFragment extends VersoFragment {

    public static final String TAG = PagedPublicationFragment.class.getSimpleName();

    public static final String STATE_CONFIGURATION = "state_paged_publication_configuration";

    FrameLayout mFrame;
    FrameLayout mFrameVerso;
    FrameLayout mFrameLoader;
    FrameLayout mFrameError;
    VersoViewPager mVersoViewPager;
    PagedPublicationConfiguration mConfig;
    PagedPublicationConfiguration.OnLoadComplete mOnLoadComplete = new PagedPublicationConfiguration.OnLoadComplete() {
        @Override
        public void onPublicationLoaded(PagedPublication publication) {
            notifyVersoConfigurationChanged();
        }

        @Override
        public void onPagesLoaded(List<? extends PagedPublicationPage> pages) {
            notifyVersoConfigurationChanged();
        }

        @Override
        public void onHotspotsLoaded(PagedPublicationHotspots hotspots) {
            notifyVersoConfigurationChanged();
        }

        @Override
        public void onError(List<PublicationException> ex) {
            if (!mConfig.hasData()) {
                showErrorView(ex.get(0));
            }
        }
    };

    @Override
    public void notifyVersoConfigurationChanged() {
        if (mConfig.hasData()) {
            showVersoView();
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
        mVersoViewPager = (VersoViewPager) super.onCreateView(inflater, container, savedInstanceState);
        mFrame = (FrameLayout) inflater.inflate(R.layout.shopgun_sdk_pagedpublication, container, false);
        mFrameVerso = (FrameLayout) mFrame.findViewById(R.id.verso);
        mFrameError = (FrameLayout) mFrame.findViewById(R.id.error);
        mFrameLoader = (FrameLayout) mFrame.findViewById(R.id.loader);
        setVisible(false, false, false);
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
            showLoaderView();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mConfig.load(mOnLoadComplete);
                }
            }, 2000);
        }
    }

    private void ensurePublicationBranding() {
        PagedPublication pub = mConfig == null ? null : mConfig.getPublication();
        if (pub != null && mFrame != null) {
            int bgColor = pub.getBackgroundColor();
            mFrame.setBackgroundColor(bgColor);
        }
    }

    private void showVersoView() {
        if (mFrame != null &&
                mFrameVerso.getVisibility() != View.VISIBLE) {
            ensurePublicationBranding();
            mFrameVerso.removeAllViews();
            mFrameVerso.addView(mVersoViewPager);
            setVisible(true, false, false);
        }
    }

    private void showLoaderView() {
        if (mFrame != null &&
                mFrameLoader.getVisibility() != View.VISIBLE) {
            ensurePublicationBranding();
            mFrameLoader.removeAllViews();
//            View loaderView = getErrorView(mFrame, new PublicationException("Testing error message while loading..."));
            View loaderView = getLoaderView(mFrameLoader);
            mFrameLoader.addView(loaderView);
            setVisible(false, true, false);
        }
    }

    private void showErrorView(PublicationException ex) {
        if (mFrame != null &&
                mFrameError.getVisibility() != View.VISIBLE) {
            ensurePublicationBranding();
            mFrameError.removeAllViews();
            View errorView = getErrorView(mFrame, ex);
            mFrameError.addView(errorView);
            setVisible(false, false, true);
        }
    }

    private void setVisible(boolean verso, boolean loader, boolean error) {
        if (mFrame != null) {
            // TODO: 10/11/16 Add animation between the views
            mFrameVerso.setVisibility(verso ? View.VISIBLE : View.GONE);
            mFrameLoader.setVisibility(loader ? View.VISIBLE : View.GONE);
            mFrameError.setVisibility(error ? View.VISIBLE : View.GONE);
        }
    }

    View getErrorView(ViewGroup container, PublicationException ex) {
        LayoutInflater i = LayoutInflater.from(container.getContext());
        View v = i.inflate(R.layout.shopgun_sdk_pagedpublication_error, container, false);
        TextView msg = (TextView) v.findViewById(R.id.message);
        msg.setText(ex.getMessage());
        return v;
    }

    View getLoaderView(ViewGroup container) {
        LayoutInflater i = LayoutInflater.from(container.getContext());
        return i.inflate(R.layout.shopgun_sdk_pagedpublication_loader, container, false);
    }

}
