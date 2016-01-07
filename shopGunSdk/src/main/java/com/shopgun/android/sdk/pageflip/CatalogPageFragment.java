/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk.pageflip;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.R;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.pageflip.stats.Clock;
import com.shopgun.android.sdk.pageflip.stats.ClockFactory;
import com.shopgun.android.sdk.pageflip.stats.PageStatsCollector;
import com.shopgun.android.sdk.pageflip.stats.PageStatsCollectorImpl;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;
import com.shopgun.android.sdk.pageflip.widget.LoadingTextView;
import com.shopgun.android.sdk.pageflip.widget.ZoomPhotoView;
import com.shopgun.android.sdk.photoview.PhotoView;

public class CatalogPageFragment extends Fragment implements
        PhotoView.OnPhotoTapListener,
        PhotoView.OnPhotoDoubleClickListener,
        PhotoView.OnPhotoLongClickListener,
        ZoomPhotoView.OnZoomChangeListener,
        PageLoader.PageLoaderListener,
        PageflipPage {

    public static final String TAG = Constants.getTag(CatalogPageFragment.class);

    private static boolean LOG = false;

    protected static final float MAX_SCALE = 3.0f;

    protected static final String ARG_PAGE = Constants.getArg(CatalogPageFragment.class, "page");
    protected static final String ARG_POSITION = Constants.getArg(CatalogPageFragment.class, "position");
    protected static final String ARG_CONFIG = Constants.getArg(CatalogPageFragment.class, "config");

    private int[] mPages;
    private int mPosition = -1;
    private PageLoader.Config mConfig;

    private boolean mPageVisible = false;

    private ZoomPhotoView mPhotoView;
    private LoadingTextView mLoader;
    private PageStatsCollector mStats;

    private PageLoader mPageLoader;

    private CatalogPageCallback mCallback;

    public static CatalogPageFragment newInstance(int position, int[] pages, PageLoader.Config config) {
        Bundle b = new Bundle();
        b.putIntArray(ARG_PAGE, pages);
        b.putInt(ARG_POSITION, position);
        b.putParcelable(ARG_CONFIG, config);
        CatalogPageFragment f = new CatalogPageFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mPages = getArguments().getIntArray(ARG_PAGE);
            mPosition = getArguments().getInt(ARG_POSITION, 0);
            mConfig = getArguments().getParcelable(ARG_CONFIG);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopgun_sdk_layout_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPhotoView = (ZoomPhotoView) view.findViewById(R.id.shopgun_sdk_layout_page_photoview);
        mLoader = (LoadingTextView) view.findViewById(R.id.shopgun_sdk_layout_page_pagenum);
        mPhotoView.setMaximumScale(MAX_SCALE);
        mPhotoView.setOnZoomListener(this);
        toggleContentVisibility(true);
    }

    private void toggleContentVisibility(boolean isLoading) {
        if (!isAdded()) {
            return;
        }
        mPhotoView.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        mLoader.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
        if (isLoading) {
            runLoader();
        }
    }

    private void runLoader() {
        updateBranding();
        mLoader.setLoadingText(PageflipUtils.join("-", mPages));
        mLoader.start();
    }

    private void updateBranding() {
        if (isAdded() && mCallback != null && mCallback.getCatalog() != null) {
            int brandingColor = mCallback.getCatalog().getBranding().getMaterialColor().getSecondaryText();
            mLoader.setTextColor(brandingColor);
        }
    }

    private void ensurePageCollector() {
        if (mStats == null) {
            Clock c = ClockFactory.getClock();
            mStats = new PageStatsCollectorImpl(ShopGun.getInstance(), mCallback.getViewSession(), mCallback.getCatalog().getId(), mPages, c);
        }
    }

    public void setCatalogPageCallback(CatalogPageCallback callback) {
        mCallback = callback;
    }

    public int[] getPages() {
        return mPages;
    }

    private boolean ensureLoader() {
        if (mCallback != null && isAdded()) {
            if (mPageLoader == null) {
                if (mConfig == null) {
                    mConfig = new PageLoader.Config(getActivity(), mPages, mCallback.getCatalog());
                }
                mPageLoader = new PageLoader(getActivity(), mConfig);
                mPageLoader.setPageLoaderListener(this);
                log(mConfig.toString());
            }
            return true;
        }
        return false;
    }

    /**
     * When called, start loading view images into the {@link ZoomPhotoView}.
     */
    private void loadView() {
        if (mPhotoView == null) {
            return;
        }
        if (mPhotoView.isBitmapValid()) {
            return;
        }
        if (ensureLoader()) {
            mPageLoader.into(mPhotoView, PageLoader.Quality.MEDIUM);
        }
    }

    /**
     * When called, start loading zoom images into the {@link ZoomPhotoView}.
     */
    private void loadZoom() {
        if (ensureLoader()) {
            mPageLoader.into(mPhotoView, PageLoader.Quality.HIGH);
        }
    }

    @Override
    public void onResume() {
        log("onResume");
        updateBranding();

        mPhotoView.setOnPhotoDoubleClickListener(this);
        mPhotoView.setOnPhotoLongClickListener(this);
        mPhotoView.setOnPhotoTapListener(this);

        if (mCallback != null) {
            mCallback.onReady(mPosition);
        }

        loadView();
        super.onResume();
    }

    public float getPageWidth() {
        return 1.0f;
    }

    public void onVisible() {
        log("onVisible");
        ensurePageCollector();
        updateBranding();
        loadView();
        if (!mPageVisible && mPhotoView != null && mPhotoView.getBitmap() != null) {
            // first start if the page is visible, and has a bitmap
            mStats.startView();
        }
        mPageVisible = true;
    }

    public void onInvisible() {
        log("onInvisible");
        mPageVisible = false;
        mStats.collect();
        mStats = null;
    }

    @Override
    public void onPause() {
        log("onPause");
        mLoader.stop();
        mPhotoView.recycle();
        if (mPageLoader != null) {
            mPageLoader.cancel();
        }
        super.onPause();
    }

    /* Implemented interfaces */

    @Override
    public void onPhotoTap(View view, float x, float y) {
        PageflipClickCoordinate c = new PageflipClickCoordinate(mCallback.getCatalog(), mPages, x, y);
        mCallback.onSingleClick(mPhotoView, c.getPage(), c.getX(), c.getY(), c.getHotspots());
    }

    @Override
    public void onPhotoDoubleTap(View view, float x, float y) {
        PageflipClickCoordinate c = new PageflipClickCoordinate(mCallback.getCatalog(), mPages, x, y);
        mCallback.onDoubleClick(mPhotoView, c.getPage(), c.getX(), c.getY(), c.getHotspots());
    }

    @Override
    public void onPhotoLongTap(View view, float x, float y) {
        PageflipClickCoordinate c = new PageflipClickCoordinate(mCallback.getCatalog(), mPages, x, y);
        mCallback.onLongClick(mPhotoView, c.getPage(), c.getX(), c.getY(), c.getHotspots());
    }

    public void onZoomChange(boolean isZoomed) {
        loadZoom();
        if (isZoomed) {
            mStats.startZoom();
        } else {
            mStats.stopZoom();
        }

        mCallback.onZoom(mPhotoView, mPages, isZoomed);
    }

    @Override
    public void onComplete() {
        log("onComplete");
        toggleContentVisibility(false);
        if (mPageVisible) {
            mStats.startView();
        }
    }

    @Override
    public void onError() {
        mLoader.stop();
        String page = PageflipUtils.join("-", mPages);
        mLoader.error(getString(R.string.com_shopgun_android_sdk_pageflip_page_error, page));
    }

    @Override
    public Bitmap onTransform(Bitmap b, int page) {
        return mCallback.onDrawPage(page, mPages, b);
    }

    private void log(String msg) {
        if (LOG) {
            String format = "pos[%s], pages[%s] %s";
            String text = String.format(format, mPosition, PageflipUtils.join(",", mPages), msg);
            SgnLog.d(TAG, text);
        }
    }

}
