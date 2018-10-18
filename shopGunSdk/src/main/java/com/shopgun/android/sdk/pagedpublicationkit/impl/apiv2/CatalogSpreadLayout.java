package com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationFragment;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationOverlay;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
public class CatalogSpreadLayout extends FrameLayout implements PagedPublicationOverlay {

    public static final String TAG = CatalogSpreadLayout.class.getSimpleName();

    List<HotspotUIController> mHotspots = new ArrayList<>();
    int[] mPages;

    public CatalogSpreadLayout(Context context, int[] pages) {
        super(context);
        mPages = pages;
    }

    @Override
    public void showHotspots(PagedPublicationFragment.PublicationTapInfo info) {
        if (info != null && info.hasHotspots()) {
            HotspotUIController huc = new HotspotUIController(info);
            mHotspots.add(huc);
            huc.display();
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

    class HotspotUIController {

        final PagedPublicationFragment.PublicationTapInfo mInfo;
        final List<View> mViews;

        HotspotUIController(PagedPublicationFragment.PublicationTapInfo info) {
            mInfo = info;
            mViews = new ArrayList<>();
            for (PagedPublicationHotspot h : info.getHotspots()) {
                CatalogHotspotView view = new CatalogHotspotView(getContext(), h, mPages);
                addView(view);
                mViews.add(view);
            }
        }

        void display() {
            for(View v : mViews) {
                v.getAnimation().startNow();
            }
        }

    }

}
