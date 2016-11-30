package com.shopgun.android.sdk.pagedpublicationkit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.CenteredViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shopgun.android.sdk.R;
import com.shopgun.android.verso.VersoFragment;
import com.shopgun.android.verso.VersoPageViewFragment;
import com.shopgun.android.verso.VersoTapInfo;
import com.shopgun.android.verso.VersoViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PagedPublicationFragment extends VersoFragment {

    public static final String TAG = PagedPublicationFragment.class.getSimpleName();

    public static final String STATE_CONFIGURATION = "paged_publication_configuration";
    public static final String STATE_PAGE = "page";

    private static final int HOTSPOT_DELAY_SHOW = 100;
    private static final int HOTSPOT_DELAY_HIDE = 1500;
    private static final int HOTSPOT_WHAT_SHOW = 1;
    private static final int HOTSPOT_WHAT_HIDE = 0;

    PagedPublicationConfiguration mConfig;

    FrameLayout mFrame;
    FrameLayout mFrameVerso;
    FrameLayout mFrameLoader;
    FrameLayout mFrameError;
    VersoViewPager mVersoViewPager;

    Handler mHandler;
    boolean mShowOnHotspotTouch = true;
    OnTapListenerWrapper mHotspotTapWrapper;
    OnLongTapListenerWrapper mHotspotLongTapWrapper;
    OnTouchListenerWrapper mOnTouchListenerWrapper;

    public static PagedPublicationFragment newInstance() {
        return new PagedPublicationFragment();
    }

    public static PagedPublicationFragment newInstance(PagedPublicationConfiguration config) {
        return newInstance(config, 0);
    }

    public static PagedPublicationFragment newInstance(PagedPublicationConfiguration config, int page) {
        Bundle args = new Bundle();
        args.putParcelable(STATE_CONFIGURATION, config);
        args.putInt(STATE_PAGE, page);
        PagedPublicationFragment f = newInstance();
        f.setArguments(args);
        return f;
    }

    public PagedPublicationFragment() {
        mHandler = new Handler(Looper.getMainLooper(), new HotSpotHandlerCallback());
        super.setOnLongTapListener(mHotspotLongTapWrapper = new OnLongTapListenerWrapper());
        super.setOnTapListener(mHotspotTapWrapper = new OnTapListenerWrapper());
        super.setOnTouchListener(mOnTouchListenerWrapper = new OnTouchListenerWrapper());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CONFIGURATION)) {
            mConfig = savedInstanceState.getParcelable(STATE_CONFIGURATION);
        } else if (getArguments() != null) {
            mConfig = getArguments().getParcelable(STATE_CONFIGURATION);
            if (getArguments().containsKey(STATE_PAGE)) {
                int page = getArguments().getInt(STATE_PAGE);
                setPage(page);
            }
        }
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

        if (getVersoSpreadConfiguration() == null) {
            setPublicationConfiguration(mConfig);
        }

        mVersoViewPager.addOnPageChangeListener(new CenteredViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == CenteredViewPager.SCROLL_STATE_DRAGGING) {
                    mHandler.removeMessages(HOTSPOT_WHAT_HIDE);
                    mHandler.removeMessages(HOTSPOT_WHAT_SHOW);
                    hideHotspots(getCurrentFragment());
                }
            }
        });

        return mFrame;
    }

    public PagedPublicationConfiguration getPublicationConfiguration() {
        return mConfig;
    }

    public void setPublicationConfiguration(PagedPublicationConfiguration configuration) {
        if (configuration != null) {
            mConfig = configuration;
            setVersoSpreadConfiguration(mConfig);
            loadPagedPublication();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPagedPublication();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mConfig != null) {
            mConfig.cancel();
        }
        outState.putParcelable(STATE_CONFIGURATION, mConfig);
        super.onSaveInstanceState(outState);
    }

    private void loadPagedPublication() {
        if (mConfig != null) {
            if (mConfig.isLoading()) {
                // loader is already working on it,
                return;
            }
            if (mConfig.getPublication() != null &&
                            mConfig.hasPages() &&
                            mConfig.hasHotspotCollection()) {
                // Already have the needed data
                showVersoView();
                notifyVersoConfigurationChanged();
                return;
            }
            showLoaderView();
            mConfig.load(new PagedPublicationOnLoadComplete());
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
        if (mFrameVerso != null &&
                mFrameVerso.getVisibility() != View.VISIBLE) {
            ensurePublicationBranding();
            mFrameVerso.removeAllViews();
            mFrameVerso.addView(mVersoViewPager);
            setVisible(true, false, false);
        }
    }

    private void showLoaderView() {
        if (mFrameLoader != null &&
                mFrameLoader.getVisibility() != View.VISIBLE) {
            ensurePublicationBranding();
            mFrameLoader.removeAllViews();
            View loaderView = getLoaderView(mFrameLoader);
            mFrameLoader.addView(loaderView);
            setVisible(false, true, false);
        }
    }

    private void showErrorView(PublicationException ex) {
        if (mFrameError != null &&
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

    public View getErrorView(ViewGroup container, PublicationException ex) {
        LayoutInflater i = LayoutInflater.from(container.getContext());
        View v = i.inflate(R.layout.shopgun_sdk_pagedpublication_error, container, false);
        TextView msg = (TextView) v.findViewById(R.id.message);
        msg.setText(ex.getMessage());
        return v;
    }

    public View getLoaderView(ViewGroup container) {
        LayoutInflater i = LayoutInflater.from(container.getContext());
        return i.inflate(R.layout.shopgun_sdk_pagedpublication_loader, container, false);
    }

    private class PagedPublicationOnLoadComplete implements PagedPublicationConfiguration.OnLoadComplete {
        @Override
        public void onPublicationLoaded(PagedPublication publication) {
            notifyVersoConfigurationChanged();
        }

        @Override
        public void onPagesLoaded(List<? extends PagedPublicationPage> pages) {
            notifyVersoConfigurationChanged();
        }

        @Override
        public void onHotspotsLoaded(PagedPublicationHotspotCollection hotspots) {
            notifyVersoConfigurationChanged();
        }

        @Override
        public void onError(List<PublicationException> ex) {
            if (!mConfig.hasData()) {
                showErrorView(ex != null && !ex.isEmpty() ?
                        ex.get(0) : new PublicationException("Unknown error occurred"));
            }
        }
    }

    @Override
    public void notifyVersoConfigurationChanged() {
        if (mConfig.hasData()) {
            showVersoView();
            super.notifyVersoConfigurationChanged();
        }
    }

    public boolean showOnHotspotTouch() {
        return mShowOnHotspotTouch;
    }

    public void showOnHotspotTouch(boolean showHotspotTouches) {
        mShowOnHotspotTouch = showHotspotTouches;
    }

    private void showHotspots(VersoPageViewFragment f, List<PagedPublicationHotspot> hotspots) {
        if (f != null) {
            View v = f.getSpreadOverlay();
            if (v instanceof PagedPublicationOverlay) {
                PagedPublicationOverlay overlay = (PagedPublicationOverlay) v;
                overlay.showHotspots(hotspots);
            }
        }
    }

    private void hideHotspots(VersoPageViewFragment f) {
        if (f != null) {
            View v = f.getSpreadOverlay();
            if (v instanceof PagedPublicationOverlay) {
                PagedPublicationOverlay overlay = (PagedPublicationOverlay) v;
                overlay.hideHotspots();
            }
        }
    }

    private VersoPageViewFragment getCurrentFragment() {
        if (mVersoViewPager.getVersoAdapter() != null) {
            for (Fragment fragment : mVersoViewPager.getVersoAdapter().getFragments()) {
                VersoPageViewFragment f = (VersoPageViewFragment) fragment;
                if (f != null && f.getSpreadPosition() == getPosition()) {
                    return f;
                }
            }
        }
        return null;
    }

    private List<PagedPublicationHotspot> findHotspots(VersoTapInfo info) {
        PagedPublicationHotspotCollection collection = mConfig.getHotspotCollection();
        if (collection != null && info.isContentClicked()) {
            int[] pages = Arrays.copyOf(info.getPages(), info.getPages().length);
            int introOffset = mConfig.hasIntro() ? -1 : 0;
            for (int i = 0; i < pages.length; i++) {
                pages[i] = pages[i] + introOffset;
            }
            int pageTapped = info.getPageTapped() + introOffset;
            return collection.getPagedPublicationHotspots(pages, pageTapped, info.getPercentX(), info.getPercentY());
        }
        return new ArrayList<>();
    }

    private class HotSpotHandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            VersoTapInfo info = (VersoTapInfo) msg.obj;
            switch (msg.what) {
                case HOTSPOT_WHAT_HIDE:
                    hideHotspots(info.getFragment());
                    break;
                case HOTSPOT_WHAT_SHOW:
                    if (mShowOnHotspotTouch) {
                        showHotspots(info.getFragment(), findHotspots(info));
                    }
                    break;
            }
            return false;
        }
    }

    @Override
    public void setOnTapListener(VersoPageViewFragment.OnTapListener tapListener) {
        mHotspotTapWrapper.mTapListener = tapListener;
    }

    public void setOnHotspotTapListener(OnHotspotTapListener tapListener) {
        mHotspotTapWrapper.mHotspotTapListener = tapListener;
    }

    @Override
    public void setOnLongTapListener(VersoPageViewFragment.OnLongTapListener longTapListener) {
        mHotspotLongTapWrapper.mLongTapListener = longTapListener;
    }

    public void setOnHotspotLongTapListener(OnHotspotLongTapListener longTapListener) {
        mHotspotLongTapWrapper.mHotspotLongTapListener = longTapListener;
    }

    @Override
    public void setOnTouchListener(VersoPageViewFragment.OnTouchListener touchListener) {
        mOnTouchListenerWrapper.mOnTouchListener = touchListener;
    }

    public interface OnHotspotTapListener {
        void onHotspotsTap(List<PagedPublicationHotspot> hotspots);
    }

    public interface OnHotspotLongTapListener {
        void onHotspotsLongTap(List<PagedPublicationHotspot> hotspots);
    }

    private class OnTouchListenerWrapper implements VersoPageViewFragment.OnTouchListener {

        VersoPageViewFragment.OnTouchListener mOnTouchListener;

        @Override
        public boolean onTouch(int action, VersoTapInfo info) {
            if (action == MotionEvent.ACTION_DOWN) {
                Message msg = mHandler.obtainMessage(HOTSPOT_WHAT_SHOW, info);
                mHandler.sendMessageDelayed(msg, HOTSPOT_DELAY_SHOW);
            } else if (action == MotionEvent.ACTION_UP) {
                Message msg = mHandler.obtainMessage(HOTSPOT_WHAT_HIDE, info);
                mHandler.sendMessageDelayed(msg, HOTSPOT_DELAY_HIDE);
            }
            if (mOnTouchListener != null) {
                mOnTouchListener.onTouch(action, info);
            }
            return false;
        }
    }

    private class OnTapListenerWrapper implements VersoPageViewFragment.OnTapListener {

        OnHotspotTapListener mHotspotTapListener;
        VersoPageViewFragment.OnTapListener mTapListener;

        @Override
        public boolean onTap(VersoTapInfo info) {
            if (mTapListener != null) {
                mTapListener.onTap(info);
            }
            if (mHotspotTapListener != null) {
                List<PagedPublicationHotspot> hotspots = findHotspots(info);
                if (!hotspots.isEmpty()) {
                    mHotspotTapListener.onHotspotsTap(hotspots);
                }
            }
            return true;
        }
    }

    private class OnLongTapListenerWrapper implements VersoPageViewFragment.OnLongTapListener {

        OnHotspotLongTapListener mHotspotLongTapListener;
        VersoPageViewFragment.OnLongTapListener mLongTapListener;

        @Override
        public void onLongTap(VersoTapInfo info) {
            if (mLongTapListener != null) {
                mLongTapListener.onLongTap(info);
            }
            if (mHotspotLongTapListener != null) {
                List<PagedPublicationHotspot> hotspots = findHotspots(info);
                if (!hotspots.isEmpty()) {
                    mHotspotLongTapListener.onHotspotsLongTap(hotspots);
                }
            }
        }

    }

}
