package com.shopgun.android.sdk.pagedpublicationkit;

import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.CenteredViewPager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shopgun.android.materialcolorcreator.MaterialColor;
import com.shopgun.android.materialcolorcreator.MaterialColorImpl;
import com.shopgun.android.sdk.R;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.verso.VersoFragment;
import com.shopgun.android.verso.VersoPageView;
import com.shopgun.android.verso.VersoPageViewFragment;
import com.shopgun.android.verso.VersoTapInfo;
import com.shopgun.android.verso.VersoViewPager;
import com.shopgun.android.verso.VersoZoomPanInfo;

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
    OnTouchWrapper mOnTouchWrapper;
    PageChangeListener mPageChangeLisetner = new PageChangeListener();

    PagedPublicationLifecycle mLifecycle;
    String mViewSessionUuid;

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
        mOnTouchWrapper = new OnTouchWrapper();
        super.setOnTapListener(mOnTouchWrapper);
        super.setOnDoubleTapListener(mOnTouchWrapper);
        super.setOnLongTapListener(mOnTouchWrapper);
        super.setOnZoomListener(mOnTouchWrapper);
        super.addOnPageChangeListener(mPageChangeLisetner);
        super.setOnLoadCompleteListener(mOnTouchWrapper);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLifecycle = new PagedPublicationLifecycle();
        mViewSessionUuid = SgnUtils.createUUID();
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_STATE)) {
            SavedState savedState = savedInstanceState.getParcelable(SAVED_STATE);
            if (savedState != null) {
                mConfig = savedState.config;
                mDisplayHotspotsOnTouch = savedState.displayHotspotOnTouch;
                mViewSessionUuid = savedState.viewSessionUuid;
                setupConfigAndTrackers();
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

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        // Resume lifecycle now, so callbacks for pagechanges is registered correctly when state is restored
        setupConfigAndTrackers();
        super.onViewStateRestored(savedInstanceState);
    }

    private void setupConfigAndTrackers() {
        Configuration config = getResources().getConfiguration();
        mConfig.onConfigurationChanged(config);
        mLifecycle.setConfig(mConfig);
        mLifecycle.resetSpreadsPagesLoadedAndZoom();
    }

    public PagedPublicationConfiguration getPublicationConfiguration() {
        return mConfig;
    }

    public void setPublicationConfiguration(PagedPublicationConfiguration configuration) {
        if (configuration != null) {
            mConfig = configuration;
            mLifecycle.setConfig(mConfig);
            setVersoSpreadConfiguration(mConfig);
            loadPagedPublication();
        }
    }

    @Override
    protected void onInternalPause() {
        if (isCurrentSpreadScaled()) {
            resetCurrentSpreadScale(false);
        }
        mLifecycle.spreadDisappeared(getPosition(), getCurrentPages());
        mLifecycle.resetSpreadsPagesLoadedAndZoom();
        super.onInternalPause();
    }

    @Override
    protected void onInternalResume(Configuration config) {
        mLifecycle.resumed();
        super.onInternalResume(config);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPagedPublication();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLifecycle.paused();
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
            ProgressBar bar = (ProgressBar) mFrameLoader.findViewById(R.id.circulareProgressBar);
            if (bar != null && bar.getIndeterminateDrawable() != null) {
                bar.setIndeterminate(true);
                MaterialColor c = new MaterialColorImpl(bgColor);
                bar.getIndeterminateDrawable().setColorFilter(c.getSecondaryText(), PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    private void showVersoView() {
        if (mFrameVerso != null &&
                mFrameVerso.getVisibility() != View.VISIBLE) {
            ensurePublicationBranding();
            mFrameVerso.removeAllViews();
            mFrameVerso.addView(mVersoViewPager);
            mVersoViewPager.addOnPageChangeListener(new PageScrolledListener());
            setVisible(true, false, false);
        }
        mLifecycle.appeared();
    }

    private void showLoaderView() {
        if (mFrameLoader != null &&
                mFrameLoader.getVisibility() != View.VISIBLE) {
            ensurePublicationBranding();
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
            mLifecycle.opened();
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

    @Override
    public void setOnTapListener(VersoPageViewFragment.OnTapListener tapListener) {
        mOnTouchWrapper.mTapListener = tapListener;
    }

    @Override
    public void setOnDoubleTapListener(VersoPageViewFragment.OnDoubleTapListener doubleTapListener) {
        mOnTouchWrapper.mDoubleTapListener = doubleTapListener;
    }

    public void setOnHotspotTapListener(OnHotspotTapListener tapListener) {
        mOnTouchWrapper.mHotspotTapListener = tapListener;
    }

    @Override
    public void setOnLongTapListener(VersoPageViewFragment.OnLongTapListener longTapListener) {
        mOnTouchWrapper.mLongTapListener = longTapListener;
    }

    public void setOnHotspotLongTapListener(OnHotspotLongTapListener longTapListener) {
        mOnTouchWrapper.mHotspotLongTapListener = longTapListener;
    }

    @Override
    public void setOnZoomListener(VersoPageViewFragment.OnZoomListener zoomListener) {
        mOnTouchWrapper.mZoomListener = zoomListener;
    }

    public interface OnHotspotTapListener {
        void onHotspotsTap(List<PagedPublicationHotspot> hotspots);
    }

    public interface OnHotspotLongTapListener {
        void onHotspotsLongTap(List<PagedPublicationHotspot> hotspots);
    }

    private class OnTouchWrapper implements VersoPageViewFragment.OnTapListener,
            VersoPageViewFragment.OnLongTapListener,
            VersoPageViewFragment.OnDoubleTapListener,
            VersoPageViewFragment.OnZoomListener,
            VersoPageViewFragment.OnLoadCompleteListener,
            Handler.Callback {

        private static final int DELAY = 100;
        private static final int WHAT_TAP = 1;
        private static final int WHAT_LONG_TAP = 2;

        OnHotspotTapListener mHotspotTapListener;
        VersoPageViewFragment.OnTapListener mTapListener;
        VersoPageViewFragment.OnDoubleTapListener mDoubleTapListener;
        OnHotspotLongTapListener mHotspotLongTapListener;
        VersoPageViewFragment.OnLongTapListener mLongTapListener;
        VersoPageViewFragment.OnZoomListener mZoomListener;
        VersoPageViewFragment.OnLoadCompleteListener mLoadCompleteListener;

        Handler mHandler = new Handler(Looper.getMainLooper(), this);

        @Override
        public boolean onTap(VersoTapInfo info) {
            if (mTapListener != null) {
                mTapListener.onTap(info);
            }
            PublicationTapInfo pti = new PublicationTapInfo(info);
            if (pti.hasHotspots()) {
                post(WHAT_TAP, pti);
                showHotspots(pti);
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(VersoTapInfo info) {
            return mDoubleTapListener != null && mDoubleTapListener.onDoubleTap(info);
        }

        @Override
        public void onLongTap(VersoTapInfo info) {
            if (mLongTapListener != null) {
                mLongTapListener.onLongTap(info);
            }
            PublicationTapInfo pti = new PublicationTapInfo(info);
            if (pti.hasHotspots()) {
                post(WHAT_LONG_TAP, pti);
                showHotspots(pti);
            }
        }

        boolean zoomBegin = false;
        @Override
        public void onZoom(VersoZoomPanInfo info) {
            if (mZoomListener != null) {
                mZoomListener.onZoom(info);
            }
            if (zoomBegin && info.getScale() > 1.0f) {
                zoomBegin = false;
                mLifecycle.spreadZoomedIn(info);
            }
        }

        @Override
        public void onZoomBegin(VersoZoomPanInfo info) {
            mVersoViewPager.setPagingEnabled(false);
            if (mZoomListener != null) {
                mZoomListener.onZoomBegin(info);
            }
            zoomBegin = true;
        }

        @Override
        public void onZoomEnd(VersoZoomPanInfo info) {
            mVersoViewPager.setPagingEnabled(!isCurrentSpreadScaled());
            if (mZoomListener != null) {
                mZoomListener.onZoomEnd(info);
            }
            zoomBegin = false;
            mLifecycle.spreadZoomedOut(info);
        }

        @Override
        public void onPageLoadComplete(boolean success, VersoPageView versoPageView) {
            if (mLoadCompleteListener != null) {
                mLoadCompleteListener.onPageLoadComplete(success, versoPageView);
            }
            if (success) {
                mLifecycle.pageLoaded(versoPageView.getPage());
            }
        }

        private void showHotspots(PublicationTapInfo info) {
            if (!mDisplayHotspotsOnTouch) {
                return;
            }
            PagedPublicationOverlay o = getSpreadOverlay(info);
            if (o != null) {
                o.showHotspots(info);
            }
        }

        private PagedPublicationOverlay getSpreadOverlay(PublicationTapInfo info) {
            if (info != null && info.getFragment() != null) {
                View v = info.getFragment().getSpreadOverlay();
                if (v instanceof PagedPublicationOverlay) {
                    return (PagedPublicationOverlay) v;
                }
            }
            return null;
        }

        private void post(int what, PublicationTapInfo info) {
            Message msg = Message.obtain(mHandler, what, info);
            mHandler.sendMessageDelayed(msg, DELAY);
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_TAP:
                    if (mHotspotTapListener != null) {
                        PublicationTapInfo info = (PublicationTapInfo) msg.obj;
                        mHotspotTapListener.onHotspotsTap(info.getHotspots());
                    }
                    break;
                case WHAT_LONG_TAP:
                    if (mHotspotLongTapListener != null) {
                        PublicationTapInfo info = (PublicationTapInfo) msg.obj;
                        mHotspotLongTapListener.onHotspotsLongTap(info.getHotspots());
                    }
                    break;
            }
            return false;
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

    public void onPublicationDisappeared() {
        mLifecycle.saveState();
        mLifecycle.disappeared();
    }

    public void onPublicationAppeared() {
        int spread = getPosition();
        int[] pages = mConfig.getPagesFromSpreadPosition(spread);
        mLifecycle.applyState(spread, pages);
        mLifecycle.appeared();
        mLifecycle.spreadAppeared(spread, pages, true);
    }

    private class PageScrolledListener implements CenteredViewPager.OnPageChangeListener {

        int mLastState = ViewPager.SCROLL_STATE_IDLE;
        int mDragFromSpread;
        int[] mDragFromPages;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
//            L.d(TAG, "onPageScrollStateChanged: " + ToStringUtils.pageScrollState(state));
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    mDragFromSpread = getPosition();
                    mDragFromPages = mConfig.getPagesFromSpreadPosition(mDragFromSpread);
                    mLifecycle.saveState();
                    mLifecycle.spreadDisappeared(mDragFromSpread, mDragFromPages);
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    if (mDragFromSpread == getPosition() && mDragFromPages != null) {
                        // User starts dragging, but doesn't swipe to the next spread - re-appear
                        mLifecycle.applyState(mDragFromSpread, mDragFromPages);
                        mLifecycle.spreadAppeared(mDragFromSpread, mDragFromPages, true);
                    }
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    if (mLastState != ViewPager.SCROLL_STATE_DRAGGING) {
                        mPageChangeLisetner.mDisappearPreviousSpread = true;
                    }

            }
            mLastState = state;
        }

    }

    private class PageChangeListener implements VersoFragment.OnPageChangeListener {

        boolean mDisappearPreviousSpread = false;

        @Override
        public void onPagesScrolled(int currentPosition, int[] currentPages, int previousPosition, int[] previousPages) {

        }

        @Override
        public void onPagesChanged(int currentPosition, int[] currentPages, int previousPosition, int[] previousPages) {
            if (mDisappearPreviousSpread) {
                mLifecycle.spreadDisappeared(previousPosition, previousPages);
            }
            mDisappearPreviousSpread = false;
            mLifecycle.spreadAppeared(currentPosition, currentPages, true);
        }

        @Override
        public void onVisiblePageIndexesChanged(int[] pages, int[] added, int[] removed) {
        }

    }

    private static class SavedState implements Parcelable {

        PagedPublicationConfiguration config;
        boolean displayHotspotOnTouch;
        String viewSessionUuid;

        private SavedState(PagedPublicationFragment f) {
            config = f.mConfig;
            displayHotspotOnTouch = f.mDisplayHotspotsOnTouch;
            viewSessionUuid = f.mViewSessionUuid;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.config, flags);
            dest.writeByte(this.displayHotspotOnTouch ? (byte) 1 : (byte) 0);
            dest.writeString(this.viewSessionUuid);
        }

        protected SavedState(Parcel in) {
            this.config = in.readParcelable(PagedPublicationConfiguration.class.getClassLoader());
            this.displayHotspotOnTouch = in.readByte() != 0;
            this.viewSessionUuid = in.readString();
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
