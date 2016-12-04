package com.shopgun.android.sdk.pagedpublicationkit.impl;

import com.shopgun.android.verso.VersoSpreadProperty;

public class SpreadPropertyImpl implements VersoSpreadProperty {

    public static VersoSpreadProperty getCatalogSpread(int[] pages) {
        return new SpreadPropertyImpl(pages, 1f, 1f, 3f);
    }

    public static VersoSpreadProperty getOutroSpread(int[] pages) {
        return new SpreadPropertyImpl(pages, 0.6f, 1f, 1f);
    }

    public static VersoSpreadProperty getIntroSpread(int[] pages) {
        return new SpreadPropertyImpl(pages, 0.6f, 1f, 1f);
    }

    private final int[] mPages;
    private final float mWidth;
    private final float mMinZoomScale;
    private final float mMaxZoomScale;

    private SpreadPropertyImpl(int[] pages, float width, float minZoomScale, float maxZoomScale) {
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
