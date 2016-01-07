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

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.R;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Branding;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Hotspot;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;
import com.shopgun.android.sdk.pageflip.widget.LoadingTextView;
import com.shopgun.android.sdk.requests.CatalogFillerRequest;
import com.shopgun.android.sdk.requests.FillerRequest;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PageflipFragment extends Fragment implements FillerRequest.Listener<Catalog> {

    public static final String TAG = Constants.getTag(PageflipFragment.class);

    public static final String ARG_CATALOG = Constants.getArg(PageflipFragment.class, "catalog");
    public static final String ARG_CATALOG_ID = Constants.getArg(PageflipFragment.class, "catalog-id");
    public static final String ARG_READER_CONFIG = Constants.getArg(PageflipFragment.class, "reader-config");
    public static final String ARG_PAGE = Constants.getArg(PageflipFragment.class, "page");
    public static final String ARG_VIEW_SESSION = Constants.getArg(PageflipFragment.class, "view-session");
    public static final String ARG_BRANDING = Constants.getArg(PageflipFragment.class, "branding");

    private static final double PAGER_SCROLL_FACTOR = 0.5d;

    // Need this
    private Catalog mCatalog;
    private String mCatalogId;
    private Branding mBranding;
    // Views
    private ViewGroup mContainer;
    private FrameLayout mFrame;
    private LoadingTextView mLoader;
    private PageflipViewPager mPager;
    private CatalogPagerAdapter mAdapter;
    // State
    private ReaderConfig mConfig;
    private int mCurrentPosition = 0;
    private boolean mPagesReady = false;
    private boolean mPageflipStarted = false;
    private String mViewSessionUuid;

    List<OnDrawPage> mDrawList = new ArrayList<OnDrawPage>();

    // The meta fragments for intro/outro in Pageflip. Need to keep track of these until the Adapter is created
    private Fragment mIntroFragment;
    private Fragment mOutroFragment;

    // internal representation for when a full lifecycle isn't performed.
    private Bundle mSavedInstanceState;

    // Callbacks and stats
    private PageflipListenerWrapper mWrapperListener = new PageflipListenerWrapper();

    private CatalogFillerRequest mCatalogFillRequest;

    private CatalogPageCallback mCatalogPageCallback = new CatalogPageCallback() {

        public void onReady(int position) {
            if (position == mCurrentPosition) {
                getPage(position).onVisible();
                mPagesReady = true;
            }
        }

        @Override
        public void onSingleClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
            mWrapperListener.onSingleClick(v, page, x, y, hotspots);
        }

        @Override
        public void onDoubleClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
            mWrapperListener.onDoubleClick(v, page, x, y, hotspots);
        }

        @Override
        public void onLongClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
            mWrapperListener.onLongClick(v, page, x, y, hotspots);
        }

        @Override
        public void onZoom(View v, int[] pages, boolean isZoomed) {
            mWrapperListener.onZoom(v, pages, isZoomed);
        }

        @Override
        public Catalog getCatalog() {
            return mCatalog;
        }

        @Override
        public String getViewSession() {
            return mViewSessionUuid;
        }

        @Override
        public Bitmap onDrawPage(int page, int[] pages, Bitmap b) {
            synchronized (this) {
                mCatalog.getHotspots().normalize(b);
            }
            for (OnDrawPage odp : mDrawList) {
                Bitmap tmp = odp.onDraw(mCatalog, page, pages, b);
                if (tmp == b && b.isRecycled()) {
                    throw new IllegalStateException("Original bitmap returned, but has been recycled.");
                } else if (tmp != b && !b.isRecycled()) {
                    throw new IllegalStateException("New bitmap returned, but source bitmap haven't been recycled.");
                }
                b = tmp;
            }
            return b;
        }

    };

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            int prev = mCurrentPosition;
            mCurrentPosition = position;
            if (mPagesReady) {
                getPage(prev).onInvisible();
                getPage(mCurrentPosition).onVisible();
            }
            mWrapperListener.onPageChange(position, getPages());
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mWrapperListener.onDragStateChanged(state);
        }

    };

    private PageflipViewPager.OnPageBoundListener mPageBoundListener = new PageflipViewPager.OnPageBoundListener() {
        @Override
        public void onLeftBound() {
            mWrapperListener.onOutOfBounds(true);
        }

        @Override
        public void onRightBound() {
            mWrapperListener.onOutOfBounds(false);
        }
    };

    /**
     * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
     * @param c A {@link Catalog} to show
     * @return A Fragment
     */
    public static PageflipFragment newInstance(Catalog c) {
        return newInstance(c, 1);
    }

    /**
     * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
     * @param c A {@link Catalog} to show
     * @param page The first page to display
     * @return A Fragment
     */
    public static PageflipFragment newInstance(Catalog c, int page) {
        return newInstance(c, page, new DoublePageReaderConfig());
    }

    /**
     * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
     * @param c A {@link Catalog} to show
     * @param page The first page to display
     * @param config A ReaderConfiguration
     * @return A Fragment
     */
    public static PageflipFragment newInstance(Catalog c, int page, ReaderConfig config) {
        return newInstance(c, c.getId(), page, config, c.getBranding());
    }

    /**
     * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
     * @param catalogId A {@link Catalog#getId() catalog.id} to show.
     * @return A Fragment
     */
    public static PageflipFragment newInstance(String catalogId) {
        return newInstance(catalogId, 1);
    }

    /**
     * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
     * @param catalogId A {@link Catalog#getId() catalog.id} to show.
     * @param page The first page to display
     * @return A Fragment
     */
    public static PageflipFragment newInstance(String catalogId, int page) {
        return newInstance(catalogId, page, null);
    }

    /**
     * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
     * @param catalogId A {@link Catalog#getId() catalog.id} to show.
     * @param page The first page to display
     * @param branding An initial {@link Branding} to show, while loading the catalog
     * @return A Fragment
     */
    public static PageflipFragment newInstance(String catalogId, int page, Branding branding) {
        return newInstance(catalogId, page, branding, new DoublePageReaderConfig());
    }

    /**
     * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
     * @param catalogId A {@link Catalog#getId() catalog.id} to show.
     * @param page The first page to display
     * @param config A {@link ReaderConfig}
     * @param branding An initial {@link Branding} to show, while loading the catalog
     * @return A Fragment
     */
    public static PageflipFragment newInstance(String catalogId, int page, Branding branding, ReaderConfig config) {
        return newInstance(null, catalogId, page, config, branding);
    }

    /**
     * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
     * @param catalog A catalog to show
     * @param catalogId A {@link Catalog#getId() catalog.id} to show.
     * @param page The first page to display
     * @param config A {@link ReaderConfig}
     * @param branding An initial {@link Branding} to show, while loading the catalog
     * @return A Fragment
     */
    private static PageflipFragment newInstance(Catalog catalog, String catalogId, int page, ReaderConfig config, Branding branding) {
        Bundle b = new Bundle();
        if (catalog != null) {
            b.putParcelable(ARG_CATALOG, catalog);
            b.putString(ARG_CATALOG_ID, catalog.getId());
        } else if (catalogId != null) {
            b.putString(ARG_CATALOG_ID, catalogId);
        }
        b.putParcelable(ARG_READER_CONFIG, config);
        b.putInt(ARG_PAGE, page);
        b.putParcelable(ARG_BRANDING, branding);
        b.putString(ARG_VIEW_SESSION, Utils.createUUID());
        PageflipFragment f = new PageflipFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (!ShopGun.isCreated()) {
            // Make sure, that ShopGun is running
            ShopGun.create(getActivity());
        }

        if (mSavedInstanceState != null) {
            onRestoreState(mSavedInstanceState);
        } else {
            onRestoreState(savedInstanceState);
        }
        mSavedInstanceState = null;

        mContainer = container;
        mFrame = (FrameLayout) inflater.inflate(R.layout.shopgun_sdk_layout_pageflip, mContainer, false);
        mLoader = (LoadingTextView) mFrame.findViewById(R.id.shopgun_sdk_layout_pageflip_loader);
        mPager = (PageflipViewPager) mFrame.findViewById(R.id.shopgun_sdk_layout_pageflip_viewpager);
        mPager.setScrollDurationFactor(PAGER_SCROLL_FACTOR);
        mPager.addOnPageChangeListener(mOnPageChangeListener);
        mPager.setOnPageBound(mPageBoundListener);

        return mFrame;
    }

    private void onRestoreState(Bundle savedInstanceState) {

        Bundle args = new Bundle();
        if (getArguments() != null) {
            args.putAll(getArguments());
        }
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
        }

        mConfig = args.getParcelable(ARG_READER_CONFIG);
        if (mConfig == null) {
            mConfig = new DoublePageReaderConfig();
        }
        mConfig.setConfiguration(getActivity().getResources().getConfiguration());

        setPage(args.getInt(ARG_PAGE, 1));

        if (args.containsKey(ARG_CATALOG)) {
            setCatalog((Catalog) args.getParcelable(ARG_CATALOG));
            mBranding = mCatalog.getBranding();
        } else if (args.containsKey(ARG_CATALOG_ID)) {
            setCatalogId(args.getString(ARG_CATALOG_ID));
            mBranding = args.getParcelable(ARG_BRANDING);
        } else {
            mLoader.error("No catalog provided");
        }

        mViewSessionUuid = args.getString(ARG_VIEW_SESSION);

        if (mViewSessionUuid == null) {
            mViewSessionUuid = Utils.createUUID();
        }

    }

    private void showContent(boolean show) {
        mLoader.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        mPager.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Get the {@link PageflipListener}.
     *
     * @return The listener, or {@code null}.
     */
    public PageflipListener getListener() {
        return mWrapperListener.getListener();
    }

    /**
     * Set a listener to call on {@link PageflipFragment} events.
     *
     * @param l The listener
     */
    public void setPageflipListener(PageflipListener l) {
        mWrapperListener.setListener(l);
    }

    public void addOnDrawPage(OnDrawPage odp) {
        mDrawList.add(odp);
    }

    public void removeOnDrawPage(OnDrawPage odp) {
        mDrawList.remove(odp);
    }

    /**
     * Get the pages currently being displayed in the {@link PageflipFragment}.
     *
     * @return An array of pages being displayed
     */
    public int[] getPages() {
        return mConfig.positionToPages(mCurrentPosition, mCatalog.getPageCount());
    }

    /**
     * Get the pages currently being displayed in the {@link PageflipFragment}.
     * But with corrected values for the Intro, and Outro fragments, where they
     * will be also be counted as being either the first or last page respectively.
     * @return An array of pages being displayed
     */
    public int[] getPagesCorrected() {
        int pos = mCurrentPosition;
        if (mConfig.hasIntro() && mCurrentPosition == 0) {
            pos = mCurrentPosition + 1;
        } else if (mConfig.hasOutro() && (mAdapter.getCount()-1) == mCurrentPosition) {
            pos = mCurrentPosition - 1;
        }
        return mConfig.positionToPages(pos, mCatalog.getPageCount());
    }

    /**
     * Set the {@link PageflipFragment} to show the given page number in the catalog.
     * Note that page number doesn't directly correlate to the position of the {@link PageflipViewPager}.
     *
     * @param page The page to turn to
     */
    public void setPage(int page) {
        if (page > 0) {
            setPosition(mConfig.pageToPosition(page));
        }
    }

    /**
     * Get the current position of the {@link PageflipViewPager}.
     *
     * @return The current position
     */
    public int getPosition() {
        return mCurrentPosition;
    }

    /**
     * Set the position of the {@link PageflipViewPager}.
     * Note that this does not correlate directly to the catalog page number.
     *
     * @param position A position
     */
    public void setPosition(int position) {
        if (mCurrentPosition >= 0) {
            mCurrentPosition = position;
            if (mPager != null) {
                mPager.setCurrentItem(mCurrentPosition);
            }
        }
    }

    /**
     * Go to the next page in the catalog
     */
    public void nextPage() {
        mPager.setCurrentItem(mCurrentPosition + 1, true);
    }

    /**
     * Go to the previous page in the catalog
     */
    public void previousPage() {
        mPager.setCurrentItem(mCurrentPosition - 1, true);
    }

    /**
     * Return the current branding, used by {@link PageflipFragment}.
     *
     * @return A {@link Branding}
     */
    public Branding getBranding() {
        return mBranding;
    }

    private void setBranding(Branding b) {

        if (b == null) {
            return;
        }

        mBranding = b;
        mLoader.setLoadingText(mBranding.getName());
        mLoader.setTextColor(b.getMaterialColor().getSecondaryText());
        mFrame.setBackgroundColor(mBranding.getColor());
        mContainer.setBackgroundColor(mBranding.getColor());

    }

    /**
     * Method for determining if the {@link PageflipFragment} is ready.
     * It checks if the {@link PageflipViewPager} has an {@link FragmentStatelessPagerAdapter} attached.
     *
     * @return true if the fragment if ready, else false.
     */
    public boolean isReady() {
        return mAdapter != null;
    }

    /**
     * Method for determining if the catalog is ready;
     *
     * @return true, if the catalog is fully loaded, including pages and hotspots
     */
    public boolean isCatalogReady() {
        return PageflipUtils.isCatalogReady(mCatalog);
    }

    /**
     * Set the id of the {@link Catalog#getId() catalog} that you want to display.
     * This is unnecessary if you have created the fragment with one of the provided
     * {@link PageflipFragment} newInstance methods.
     *
     * @param catalogId A catalog id
     */
    public void setCatalogId(String catalogId) {
        mCatalogId = catalogId;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        try {
            // A few things can go wrong here, but we can safely ignore them
            // worst case, the reader will start at page 1
            int page = getPagesCorrected()[0];
            outState.putInt(ARG_PAGE, page);
        } catch (Exception e) {
            SgnLog.w(TAG, "Pageflip isn't ready, to save all state", e);
        }
        if (mCatalog != null) {
            outState.putParcelable(ARG_CATALOG, mCatalog);
        }
        outState.putString(ARG_CATALOG_ID, mCatalogId);
        outState.putString(ARG_VIEW_SESSION, mViewSessionUuid);
        outState.putParcelable(ARG_BRANDING, mBranding);
        mSavedInstanceState = outState;
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        internalResume();
    }

    /**
     * Method for instantiating the {@link PageflipFragment}.
     * This will perform all needed actions in order to get the show started.
     */
    private void internalResume() {
        synchronized (this) {
            if (mPageflipStarted) {
                return;
            }
            mPageflipStarted = true;
        }
        showContent(false);
        setBranding(mBranding);
        mLoader.start();
        ensureCatalog();
    }

    private void ensureCatalog() {

        if (mCatalog != null) {
            setBrandingAndFillCatalog();
            return;
        }

        Response.Listener<JSONObject> l = new Response.Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {

                if (!isAdded()) {
                    // Ignore callback
                    return;
                }

                if (response != null) {
                    setCatalog(Catalog.fromJSON(response));
                    setBrandingAndFillCatalog();
                } else {
                    mLoader.error();
                }

            }
        };

        String url = Endpoints.catalogId(mCatalogId);
        JsonObjectRequest r = new JsonObjectRequest(url, l);
        r.setIgnoreCache(true);
        ShopGun.getInstance().add(r);

    }

    @Override
    public void onFillIntermediate(Catalog response, List<ShopGunError> errors) {
        // empty
    }

    @Override
    public void onFillComplete(Catalog response, List<ShopGunError> errors) {

        if (!isAdded()) {
            return;
        }

        if (!errors.isEmpty()) {

            mLoader.error();
            // doesn't matter which error we choose, so we'll just take the first one
            mWrapperListener.onError(errors.get(0));
            showContent(false);

        } else {
            applyAdapter();
            mWrapperListener.onReady();
            // force the first page change if needed
            if (mPager.getCurrentItem() != mCurrentPosition) {
                mPager.setCurrentItem(mCurrentPosition);
            }
            mWrapperListener.onPageChange(mCurrentPosition, getPages());
        }

    }

    private void applyAdapter() {
        int heap = Utils.getMaxHeap(getActivity());
        mAdapter = new CatalogPagerAdapter(getChildFragmentManager(), heap, mCatalogPageCallback, mConfig);
        mAdapter.setIntroFragment(mIntroFragment);
        mAdapter.setOutroFragment(mOutroFragment);
        mPager.setAdapter(mAdapter);
        showContent(true);
    }

    private void setBrandingAndFillCatalog() {
        if (mCatalog != null) {
            setBranding(mCatalog.getBranding());
            mCatalogFillRequest = new CatalogFillerRequest(mCatalog, this);
            mCatalogFillRequest.addHotspots(true);
            mCatalogFillRequest.addPages(true);
            ShopGun.getInstance().add(mCatalogFillRequest);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean land = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (land != mConfig.isLandscape()) {
            // To correctly destroy the state of the CatalogPagerAdapter
            // we will mimic the lifecycle of a fragment being destroyed
            // and restored.
            internalPause();
            Bundle b = new Bundle();
            onSaveInstanceState(b);
            onRestoreState(b);
            mSavedInstanceState = null;
            internalResume();
        }
    }

    @Override
    public void onPause() {
        internalPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mSavedInstanceState == null) {
            onSaveInstanceState(new Bundle());
        }
        super.onStop();
    }

    private void internalPause() {

        // The visibility usually happens via the OnPageChangeListener,
        // but when lifecycle events happens this isn't the case, so we'll fake it.
        getPage(mCurrentPosition).onInvisible();

        mLoader.stop();
        if (mCatalogFillRequest != null) {
            mCatalogFillRequest.cancel();
        }
        mPagesReady = false;
        mPageflipStarted = false;
        clearAdapter();
    }

    private void clearAdapter() {
        if (mAdapter != null) {
            mAdapter.clearState();
        }
        mPager.setAdapter(null);
        showContent(false);
    }

    public Catalog getCatalog() {
        return mCatalog;
    }

    private Fragment getFragment(int position) {
        return (Fragment) mAdapter.instantiateItem(mContainer, position);
    }

    private PageflipPage getPage(int position) {
        return (PageflipPage) mAdapter.instantiateItem(mContainer, position);
    }

    /**
     * Set the {@link Catalog} that you want to display.
     * This is unnecessary if you have created the fragment with one of the provided
     * {@link PageflipFragment} newInstance methods.
     *
     * @param c A catalog to display
     */
    public void setCatalog(Catalog c) {
        if (c != null) {
            mCatalog = c;
            mCatalogId = mCatalog.getId();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int page = PageOverviewDialog.parseOnActivityResult(requestCode, resultCode, data);
        if (page != -1) {
            setPage(page);
        }
    }

    /**
     * This will display a DialogFragment, that lets the user choose a page to go to.
     * When an item has been selected, PageflipFragment, will automatically navigate to the
     * selected page.
     */
    public void showPageOverview() {
        if (isCatalogReady()) {
            int page = getPagesCorrected()[0];
            PageOverviewDialog f = PageOverviewDialog.newInstance(PageflipFragment.this, mCatalog, page);
            f.show(getChildFragmentManager(), PageOverviewDialog.TAG);
        }
    }

    /**
     * A wrapper for the {@link PageflipListener}, that allows us to intercept all calls.
     * This also allows us to do some debugging.
     */
    protected class PageflipListenerWrapper implements PageflipListener {

        protected PageflipListener mListener;

        private boolean post() {
            return mListener != null;
        }

        public PageflipListener getListener() {
            return mListener;
        }

        public void setListener(PageflipListener l) {
            mListener = l;
        }

        public void onReady() {
            if (post()) mListener.onReady();
        }

        public void onPageChange(int position, int[] pages) {
            if (post()) mListener.onPageChange(position, pages);
        }

        public void onOutOfBounds(boolean left) {
            if (post()) mListener.onOutOfBounds(left);
        }

        public void onError(ShopGunError error) {
            error = (error == null) ? new ShopGunError(0, "Unknown Error", "No details available") : error;
            if (post()) mListener.onError(error);
        }

        public void onDragStateChanged(int state) {
            if (post()) mListener.onDragStateChanged(state);
        }

        public void onSingleClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
            if (post()) mListener.onSingleClick(v, page, x, y, hotspots);
        }

        public void onDoubleClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
            if (post()) mListener.onDoubleClick(v, page, x, y, hotspots);
        }

        public void onLongClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
            if (post()) mListener.onLongClick(v, page, x, y, hotspots);
        }

        public void onZoom(View v, int[] pages, boolean zoonIn) {
            if (post()) mListener.onZoom(v, pages, zoonIn);
        }

    }

    public void setIntroFragment(Fragment intro) {
        if (intro != null && !(intro instanceof PageflipPage)) {
            String msg = String.format("%s must implement %s", intro.getClass().getSimpleName(), PageflipPage.class.getCanonicalName());
            throw new ClassCastException(msg);
        }
        mIntroFragment = intro;
        if (mAdapter != null) {
            mAdapter.setIntroFragment(mIntroFragment);
        }
    }

    public Fragment getIntroFragment() {
        return mIntroFragment;
    }

    public void setOutroFragment(Fragment outro) {
        if (outro != null && !(outro instanceof PageflipPage)) {
            String msg = String.format("%s must implement %s", outro.getClass().getSimpleName(), PageflipPage.class.getCanonicalName());
            throw new ClassCastException(msg);
        }
        mOutroFragment = outro;
        if (mAdapter != null) {
            mAdapter.setOutroFragment(mOutroFragment);
        }
    }

    public Fragment getOutroFragment() {
        return mOutroFragment;
    }

    private void saveState() {

    }

}
