package com.shopgun.android.sdk.pagedpublicationkit.impl;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationPage;

public class CatalogPage implements PagedPublicationPage {

    String mViewUrl;
    String mZoomUrl;
    int mPageIndex;
    Bitmap.Config mBitmapConfig;
    float mAspectRatio;

    public CatalogPage(int pageIndex, String viewUrl, String zoomUrl, float aspectRatio, Bitmap.Config bitmapConfig) {
        mPageIndex = pageIndex;
        mViewUrl = viewUrl;
        mZoomUrl = zoomUrl;
        mBitmapConfig = bitmapConfig;
        mAspectRatio = aspectRatio;
    }

    @NonNull
    @Override
    public String getUrl(Size size) {
        switch (size) {
            case VIEW:
                return mViewUrl;
            case ZOOM:
                return mZoomUrl;
            default:
                return mViewUrl;
        }
    }

    @Override
    public int getPageIndex() {
        return mPageIndex;
    }

    @NonNull
    @Override
    public Bitmap.Config getBitmapConfig(Size size) {
        return null;
    }

    @Override
    public boolean allowResize(Size size) {
        return size == Size.VIEW;
    }

    @Override
    public float getAspectRatio() {
        return mAspectRatio;
    }
}
