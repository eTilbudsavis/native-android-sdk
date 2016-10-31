package com.shopgun.android.sdk.pagedpublicationkit.impl;

import android.content.Context;
import android.graphics.Bitmap;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.NetworkDebugger;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationLoader;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationPage;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.impl.CatalogLoaderRequest;
import com.shopgun.android.sdk.requests.impl.CatalogRequest;
import com.shopgun.android.utils.MemoryUtils;

import java.util.ArrayList;
import java.util.List;

public class CatalogLoader implements PagedPublicationLoader {

    OnLoadComplete mCallback;
    Context mContext;
    Catalog mCatalog;
    String mCatalogId;
    Request mCatalogRequest;
    CatalogPublication mCatalogPublication;
    boolean mCatalogLoaded = false;
    boolean mHotspotsLoaded = false;
    boolean mPagesLoaded = false;

    public CatalogLoader(Context context, String catalogId) {
        mContext = context;
        mCatalogId = catalogId;
    }

    public CatalogLoader(Context context, Catalog catalog) {
        mContext = context;
        mCatalog = catalog;
    }

    @Override
    public void load(OnLoadComplete callback) {
        if (isLoading()) {
            cancel();
        }
        mCallback = callback;

        LoaderRequest.Listener<Catalog> mListener = new LoaderRequest.Listener<Catalog>() {
            @Override
            public void onRequestComplete(Catalog response, List<ShopGunError> errors) {
                onRequestIntermediate(response, errors);
            }

            @Override
            public void onRequestIntermediate(Catalog response, List<ShopGunError> errors) {
                if (!mCatalogLoaded) {
                    mCatalogLoaded = true;
                    mCatalogPublication = new CatalogPublication(response);
                    mCallback.onPublicationLoaded(mCatalogPublication);
                }
                if (!mPagesLoaded && response.getPages() != null) {
                    mPagesLoaded = true;
                    List<PagedPublicationPage> pages = convertImagesToPages(mContext, response.getPages(), mCatalogPublication.getAspectRatio());
                    mCallback.onPagesLoaded(pages);
                }
                if (!mHotspotsLoaded && response.getHotspots() != null) {
                    mHotspotsLoaded = true;
                    // FIXME: 27/10/16
                    mCallback.onHotspotsLoaded(null);
                }
            }

        };

        if (mCatalog != null) {
            CatalogLoaderRequest r = new CatalogLoaderRequest(mCatalog, mListener);
            r.loadPages(true);
            r.loadHotspots(true);
            mCatalogRequest = r;
            mListener.onRequestIntermediate(mCatalog, new ArrayList<ShopGunError>());
        } else {
            CatalogRequest r = new CatalogRequest(mCatalogId, mListener);
            r.loadPages(true);
            r.loadHotspots(true);
            mCatalogRequest = r;
        }
        ShopGun.getInstance().add(mCatalogRequest);
    }

    @Override
    public boolean isLoading() {
        return mCatalogRequest != null && (!mCatalogRequest.isFinished() || !mCatalogRequest.isCanceled());
    }

    @Override
    public void cancel() {
        mCatalogRequest.cancel();
        mCatalogRequest = null;
    }

    /**
     * Calculating worst case scenario is landscape mode
     * (each view has two bitmaps), with high quality images.
     * Memory not included:
     * - The bitmap being replaced in the view
     * - The drawing cache in the PageTransformerTarget
     * - Bitmaps that have not yet been recycled
     * Overkill:
     * - Technically only two zoom images will be loaded (depending on lifecycle e.t.c.)
     * - Screen-size/View-size will possibly force down scale of bitmap.
     * ------------------------------------------------------------
     * |                 Decoding memory consumption              |
     * ------------------------------------------------------------
     * |                    | RGB_565 (2byte) | ARGB_8888 (4byte) |
     * ------------------------------------------------------------
     * | thumb (177x212)px  |            75kb |             150kb |
     * | view (800x1000)px  |          1600kb |            3200kb |
     * | zoom (1500x2000)px |          6000kb |           12000kb |
     * ------------------------------------------------------------
     */
    private List<PagedPublicationPage> convertImagesToPages(Context ctx, List<Images> images, float aspectRatio) {
        int maxMem = MemoryUtils.getMaxHeap(ctx);
        ArrayList<PagedPublicationPage> pages = new ArrayList<>(images.size());
        for (int j = 0; j < images.size(); j++) {
            Images i = images.get(j);
            CatalogPage page;
            if (maxMem >= 96) {
                // worst case: (2*12000)*3 = 72mb
                page = new CatalogPage(j, i.getView(), i.getZoom(), aspectRatio, Bitmap.Config.ARGB_8888);
            } else if (maxMem >= 48) {
                // worst case: (2*6000)*3 = 36mb
                page = new CatalogPage(j, i.getView(), i.getZoom(), aspectRatio, Bitmap.Config.RGB_565);
            } else {
                // worst case: (2*1600)*3 = 10mb
                // essentially we just hope for the best
                page = new CatalogPage(j, i.getView(), i.getView(), aspectRatio, Bitmap.Config.RGB_565);
            }
            pages.add(page);
        }
        return pages;
    }

}
