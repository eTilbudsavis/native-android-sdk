package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.sdk.R;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;

public class CatalogHotspotView extends View {

    public static final String TAG = CatalogHotspotView.class.getSimpleName();

    PagedPublicationHotspot mHotspot;
    int[] mPages;
    RectF mBounds;

    public CatalogHotspotView(Context context, PagedPublicationHotspot hotspot, int[] pages) {
        super(context);
        mHotspot = hotspot;
        mPages = pages;
        mBounds = mHotspot.getBoundsForPages(mPages);
        setBackgroundResource(R.drawable.sgn_pagedpubkit_hotspot_bg);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Rect rect = getScaledRect(mBounds, width, height);
        ((ViewGroup.MarginLayoutParams) getLayoutParams()).leftMargin = rect.left;
        ((ViewGroup.MarginLayoutParams) getLayoutParams()).topMargin = rect.top;
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private static Rect getScaledRect(RectF rect, int width, int height) {
        Rect r = new Rect();
        r.left = Math.round(rect.left * (float) width);
        r.top = Math.round(rect.top * (float) height);
        r.right = Math.round(rect.right * (float) width);
        r.bottom = Math.round(rect.bottom * (float) height);
        return r;
    }

}
