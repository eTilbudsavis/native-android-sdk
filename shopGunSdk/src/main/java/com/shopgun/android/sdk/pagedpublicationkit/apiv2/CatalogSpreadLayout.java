package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.shopgun.android.sdk.R;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationOverlay;

import java.util.List;

public class CatalogSpreadLayout extends FrameLayout implements PagedPublicationOverlay {

    public static final String TAG = CatalogSpreadLayout.class.getSimpleName();

    int[] mPages;

    public CatalogSpreadLayout(Context context, int[] pages) {
        super(context);
        mPages = pages;
    }

    @Override
    public void showHotspots(List<PagedPublicationHotspot> hotspots) {
        cancelAnimation();
        removeAllViews();
        if (hotspots == null || hotspots.isEmpty()) {
            return;
        }
        Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.sgn_pagedpubkit_hotspot_in);
        for (PagedPublicationHotspot h : hotspots) {
            CatalogHotspotView view = new CatalogHotspotView(getContext(), h, mPages);
            view.setAnimation(a);
            addView(view);
        }
        a.startNow();
    }

    private void cancelAnimation() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v.getAnimation() != null) {
                v.getAnimation().cancel();
            }
        }
    }

    @Override
    public void hideHotspots() {
        cancelAnimation();
        removeAllViews();
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
