package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import com.shopgun.android.verso.VersoSpreadProperty;

public class CatalogSpreadProperty implements VersoSpreadProperty {

    private final int[] mPages;
    private final float mWidth;
    private final float mMinZoomScale;
    private final float mMaxZoomScale;

    public CatalogSpreadProperty(int[] pages, float width, float minZoomScale, float maxZoomScale) {
        mPages = pages;
        mWidth = width;
        mMinZoomScale = minZoomScale;
        mMaxZoomScale = maxZoomScale;
    }

    @Override
    public int[] getPages() {
        return mPages;
    }

    @Override
    public float getWidth() {
        return mWidth;
    }

    @Override
    public float getMaxZoomScale() {
        return mMaxZoomScale;
    }

    @Override
    public float getMinZoomScale() {
        return mMinZoomScale;
    }
}
