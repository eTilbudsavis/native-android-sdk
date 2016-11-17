package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.content.Context;
import android.widget.FrameLayout;

import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationOverlay;

import java.util.List;

public class CatalogSpreadLayout extends FrameLayout implements PagedPublicationOverlay {

    public static final String TAG = CatalogSpreadLayout.class.getSimpleName();
    
    List<PagedPublicationHotspot> mHotspots;
    int[] mPages;

    public CatalogSpreadLayout(Context context, int[] pages) {
        super(context);
        mPages = pages;
    }

    @Override
    public void showHotspots(List<PagedPublicationHotspot> hotspots) {
        mHotspots = hotspots;
        removeAllViews();
        for (PagedPublicationHotspot h : mHotspots) {
            CatalogHotspotView view = new CatalogHotspotView(getContext(), h, mPages);
            addView(view);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
