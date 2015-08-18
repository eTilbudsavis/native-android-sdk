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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.R;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.imageloader.ImageRequest;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;
import com.shopgun.android.sdk.pageflip.widget.LoadingTextView;
import com.shopgun.android.sdk.pageflip.widget.ZoomPhotoView;
import com.shopgun.android.sdk.photoview.PhotoView;

import java.util.List;

public class CatalogPageFragment extends Fragment implements
        PhotoView.OnPhotoTapListener,
        PhotoView.OnPhotoDoubleClickListener,
        PhotoView.OnPhotoLongClickListener,
        ZoomPhotoView.OnZoomChangeListener {

    public static final String TAG = Constants.getTag(CatalogPageFragment.class);

    protected static final int FADE_IN_DURATION = 150;
    protected static final float MAX_SCALE = 3.0f;

    protected static final String ARG_PAGE = Constants.getArg(CatalogPageFragment.class, "page");
    protected static final String ARG_POSITION = Constants.getArg(CatalogPageFragment.class, "position");

    private static final Object HOTSPOT_LOCK = new Object();

    private int[] mPages;
    private ZoomPhotoView mPhotoView;
    private LoadingTextView mLoader;
    private boolean mHasZoomImage = false;
    private boolean mDebugRects = false;
    private PageStat mStats;

    private boolean mPageVisible = false;
    private int mPosition = -1;

    private CatalogPageCallback mCallback;

    private PageLoader.PageLoaderListener mPageLoaderListener = new PageLoader.PageLoaderListener() {
        @Override
        public void onComplete() {
            toggleContentVisibility(false);
        }

        @Override
        public void onError() {
        }
    };

    public static CatalogPageFragment newInstance(int position, int[] pages) {
        Bundle b = new Bundle();
        b.putIntArray(ARG_PAGE, pages);
        b.putInt(ARG_POSITION, position);
        CatalogPageFragment f = new CatalogPageFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mPages = getArguments().getIntArray(ARG_PAGE);
            mPosition = getArguments().getInt(ARG_POSITION, 0);
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
        int content = isLoading ? View.INVISIBLE : View.VISIBLE;
        int loader = isLoading ? View.VISIBLE : View.INVISIBLE;
        mPhotoView.setVisibility(content);
        mLoader.setVisibility(loader);
        if (isLoading) {
            runLoader();
        }
    }

    private void updateBranding() {
        if (isAdded() && mCallback != null && mCallback.getCatalog() != null) {
            int brandingColor = mCallback.getCatalog().getBranding().getColor();
            int complimentColor = PageflipUtils.getTextColor(brandingColor, getActivity());
            mLoader.setTextColor(complimentColor);
        }
    }

    private void runLoader() {
        updateBranding();
        mLoader.setLoadingText(PageflipUtils.join("-", mPages));
        mLoader.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private PageStat getStat() {
        if (mStats == null) {
            mStats = new PageStat(mCallback.getCatalog().getId(), mCallback.getViewSession(), mPages);
        }
        return mStats;
    }

    protected void addRequest(ImageRequest ir) {
        ir.setMemoryCache(false);
        ShopGun.getInstance().getImageloader().displayImage(ir);
    }

    protected ZoomPhotoView getPhotoView() {
        return mPhotoView;
    }

    public void setCatalogPageCallback(CatalogPageCallback callback) {
        mCallback = callback;
    }

    public int[] getPages() {
        return mPages;
    }

    PageLoader mViewLoader;

    /**
     * When called, start loading view images into the {@link ZoomPhotoView}.
     */
    public void loadView() {
        if (mPhotoView == null) {
            return;
        }
        if (mPhotoView.isBitmapValid()) {
            return;
        }
        if (mCallback == null) {
            return;
        }
        if (!mCallback.isPositionSet()) {
            return;
        }
        if (mViewLoader == null) {
            List<String> urls = mCallback.getCatalog().getPagesUrls(Images.VIEW);
            mViewLoader = new PageLoader(getActivity(), urls, mPages);
            mViewLoader.setPageLoaderListener(mPageLoaderListener);
            mViewLoader.into(mPhotoView);
        }
    }

    /**
     * When called, start loading zoom images into the {@link ZoomPhotoView}.
     */
    public void loadZoom() {
        List<String> urls = mCallback.getCatalog().getPagesUrls(Images.ZOOM);
        PageLoader l = new PageLoader(getActivity(), urls, mPages);
        l.into(mPhotoView);
    }

    @Override
    public void onResume() {
//		SgnLog.d(TAG, String.format("pos: %s, onResume", mPosition));
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

    /**
     * Tell if the fragment is current visible in the {@link PageflipViewPager}.
     *
     * @return true if visible, else false.
     */
    public boolean isPageVisible() {
        return mPageVisible;
    }

    /**
     * called once the {@link CatalogPageFragment} becomes visible in the {@link PageflipViewPager}
     */
    public void onVisible() {
        updateBranding();
        loadView();
        if (!mPageVisible) {
            if (mPhotoView != null && mPhotoView.getBitmap() != null) {
                getStat().startView();
            }
            // TODO do performance stuff, low memory devices can start loading here instead of onResume
        }
        mPageVisible = true;
    }

    /**
     * called once the {@link CatalogPageFragment} becomes invisible in the {@link PageflipViewPager}
     */
    public void onInvisible() {
//		SgnLog.d(TAG, String.format("pos: %s, onInvisible, isAdded: %s", mPosition, isAdded()));
        if (mCallback != null) {
            getStat().collectView();
        }
        mPageVisible = false;
    }

    @Override
    public void onPause() {
//		SgnLog.d(TAG, String.format("pos: %s, onPause", mPosition));
        mLoader.stop();
        mPhotoView.recycle();
        onInvisible();
        super.onPause();
    }

    /* Implemented interfaces */

    @Override
    public void onPhotoTap(View view, float x, float y) {
        PageflipClickCoordinate c = new PageflipClickCoordinate(mCallback.getCatalog(), mPages, x, y);
        mCallback.onSingleClick(mPhotoView, c.page, c.x, c.y, c.list);
    }

    @Override
    public void onPhotoDoubleTap(View view, float x, float y) {
        PageflipClickCoordinate c = new PageflipClickCoordinate(mCallback.getCatalog(), mPages, x, y);
        mCallback.onDoubleClick(mPhotoView, c.page, c.x, c.y, c.list);
    }

    @Override
    public void onPhotoLongTap(View view, float x, float y) {
        PageflipClickCoordinate c = new PageflipClickCoordinate(mCallback.getCatalog(), mPages, x, y);
        mCallback.onLongClick(mPhotoView, c.page, c.x, c.y, c.list);
    }

    public void onZoomChange(boolean isZoomed) {
        if (isZoomed && !mHasZoomImage) {
            mHasZoomImage = true;
            loadZoom();
        }

        if (isZoomed) {
            getStat().startZoom();
        } else {
            getStat().collectZoom();
        }

        mCallback.onZoom(mPhotoView, mPages, isZoomed);
    }

    private void log(String msg) {
        SgnLog.d(TAG, "pages[" + PageflipUtils.join(",", mPages) + "] " + msg);
    }

}
