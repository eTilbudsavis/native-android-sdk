package com.shopgun.android.sdk.pagedpublicationkit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    public static final String ARG_CONFIGURATION = "arg_config";
    public static final String ARG_PAGE = "arg_page";
    public static final String SAVED_STATE = "saved_state";

    PagedPublicationConfiguration mConfig;

    FrameLayout mFrame;
    FrameLayout mFrameVerso;
    FrameLayout mFrameLoader;
    FrameLayout mFrameError;
    VersoViewPager mVersoViewPager;

    boolean mDisplayHotspotsOnTouch = true;
    OnTapListenerWrapper mHotspotTapWrapper;
    OnLongTapListenerWrapper mHotspotLongTapWrapper;

    public static PagedPublicationFragment newInstance() {
        return new PagedPublicationFragment();
    }

    public static PagedPublicationFragment newInstance(PagedPublicationConfiguration config) {
        return newInstance(config, 0);
    }

    public static PagedPublicationFragment newInstance(PagedPublicationConfiguration config, int page) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_CONFIGURATION, config);
        args.putInt(ARG_PAGE, page);
        PagedPublicationFragment f = newInstance();
        f.setArguments(args);
        return f;
    }

    public PagedPublicationFragment() {
        super.setOnTouchListener(new OnHotspotTouchListener());
        super.setOnLongTapListener(mHotspotLongTapWrapper = new OnLongTapListenerWrapper());
        super.setOnTapListener(mHotspotTapWrapper = new OnTapListenerWrapper());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_STATE)) {
            SavedState state = savedInstanceState.getParcelable(SAVED_STATE);
            if (state != null) {
                mConfig = state.config;
                mDisplayHotspotsOnTouch = state.displayHotspotOnTouch;
            }
        } else if (getArguments() != null) {
            mConfig = getArguments().getParcelable(ARG_CONFIGURATION);
            if (getArguments().containsKey(ARG_PAGE)) {
                int page = getArguments().getInt(ARG_PAGE);
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
    public void onPause() {
        super.onPause();
        if (mConfig != null) {
            mConfig.cancel();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mConfig != null) {
            mConfig.cancel();
        }
        outState.putParcelable(SAVED_STATE, new SavedState(this));
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

    public boolean isDisplayHotspotsOnTouch() {
        return mDisplayHotspotsOnTouch;
    }

    public void setDisplayHotspotsOnTouch(boolean displayHotspotsOnTouch) {
        mDisplayHotspotsOnTouch = displayHotspotsOnTouch;
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

    public interface OnHotspotTapListener {
        void onHotspotsTap(List<PagedPublicationHotspot> hotspots);
    }

    public interface OnHotspotLongTapListener {
        void onHotspotsLongTap(List<PagedPublicationHotspot> hotspots);
    }

    private class OnTapListenerWrapper implements VersoPageViewFragment.OnTapListener {

        OnHotspotTapListener mHotspotTapListener;
        VersoPageViewFragment.OnTapListener mTapListener;

        @Override
        public boolean onTap(VersoTapInfo info) {
            if (mTapListener != null) {
                mTapListener.onTap(info);
            }
            PublicationTapInfo pti = new PublicationTapInfo(info);
            if (pti.hasHotspots()) {
                if (mHotspotTapListener != null) {
                    mHotspotTapListener.onHotspotsTap(pti.getHotspots());
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
            PublicationTapInfo pti = new PublicationTapInfo(info);
            if (pti.hasHotspots()) {
                if (mHotspotLongTapListener != null) {
                    mHotspotLongTapListener.onHotspotsLongTap(pti.getHotspots());
                }
            }
        }

    }

    public class OnHotspotTouchListener implements VersoPageViewFragment.OnTouchListener, Handler.Callback {

        private static final int HOTSPOT_DISPLAY_DELAY = 180;
        private static final int WHAT = 1;

        Handler mHandler;
        PublicationTapInfo mTapInfo;

        public OnHotspotTouchListener() {
            mHandler =  new Handler(Looper.getMainLooper(), this);
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == WHAT) {
                showHotspots();
            }
            return false;
        }

        @Override
        public boolean onTouch(int action, VersoTapInfo info) {
//            L.d(TAG, "onTouch.action: " + ToStringUtils.motionEventAction(action));
            if (!mDisplayHotspotsOnTouch) {
                return false;
            }
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mTapInfo = new PublicationTapInfo(info);
                    if (mTapInfo.hasHotspots()) {
                        mHandler.sendEmptyMessageDelayed(WHAT, HOTSPOT_DISPLAY_DELAY);
                    } else {
                        mTapInfo = null;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mHandler.hasMessages(WHAT)) {
                        showHotspots();
                    }
                case MotionEvent.ACTION_CANCEL:
                    mHandler.removeMessages(WHAT);
                    dismissHotspots();
                    break;
            }
            return false;
        }

        private void showHotspots() {
            PagedPublicationOverlay o = getSpreadOverlay();
            if (o != null) {
                o.showHotspots(mTapInfo);
            }
        }

        private void dismissHotspots() {
            PagedPublicationOverlay o = getSpreadOverlay();
            if (o != null) {
                o.hideHotspots(mTapInfo);
            }
            mTapInfo = null;
        }

        private PagedPublicationOverlay getSpreadOverlay() {
            if (mTapInfo != null && mTapInfo.getFragment() != null) {
                View v = mTapInfo.getFragment().getSpreadOverlay();
                if (v instanceof PagedPublicationOverlay) {
                    return (PagedPublicationOverlay) v;
                }
            }
            return null;
        }

    }

    public class PublicationTapInfo extends VersoTapInfo {

        private final List<PagedPublicationHotspot> mHotspots;

        public PublicationTapInfo(VersoTapInfo info) {
            super(info);
            this.mHotspots = findHotspots(info);
        }

        public PublicationTapInfo(PublicationTapInfo info) {
            super(info);
            this.mHotspots = info.mHotspots;
        }

        public List<PagedPublicationHotspot> getHotspots() {
            return mHotspots;
        }

        public boolean hasHotspots() {
            return mHotspots != null && !mHotspots.isEmpty();
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

    }

    private static class SavedState implements Parcelable {

        PagedPublicationConfiguration config;
        boolean displayHotspotOnTouch;

        private SavedState(PagedPublicationFragment f) {
            config = f.mConfig;
            displayHotspotOnTouch = f.mDisplayHotspotsOnTouch;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.config, flags);
            dest.writeByte(this.displayHotspotOnTouch ? (byte) 1 : (byte) 0);
        }

        protected SavedState(Parcel in) {
            this.config = in.readParcelable(PagedPublicationConfiguration.class.getClassLoader());
            this.displayHotspotOnTouch = in.readByte() != 0;
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
