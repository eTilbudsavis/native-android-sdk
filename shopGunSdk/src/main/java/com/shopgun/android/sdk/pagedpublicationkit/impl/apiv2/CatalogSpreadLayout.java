package com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.shopgun.android.sdk.R;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationFragment;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationOverlay;

import java.util.ArrayList;
import java.util.List;

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
    public void hideHotspots(PagedPublicationFragment.PublicationTapInfo info) {
        for (HotspotUIController huc : mHotspots) {
            huc.hide();
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
        final Animation mAnimateIn;
        final List<View> mViews;
        boolean mHideCalled = false;

        HotspotUIController(PagedPublicationFragment.PublicationTapInfo info) {
            mInfo = info;
            mAnimateIn = AnimationUtils.loadAnimation(getContext(), R.anim.sgn_pagedpubkit_hotspot_in);
            mAnimateIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    for (View v : mViews) {
                        v.clearAnimation();
                    }
                    if (mHideCalled) {
                        hide();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            mViews = new ArrayList<>();
            for (PagedPublicationHotspot h : info.getHotspots()) {
                CatalogHotspotView view = new CatalogHotspotView(getContext(), h, mPages);
                view.setAnimation(mAnimateIn);
                addView(view);
                mViews.add(view);
            }
        }

        void display() {
            mAnimateIn.startNow();
        }

        public void hide() {
            mHideCalled = true;
            if (!(mAnimateIn.hasStarted() && !mAnimateIn.hasEnded())) {

                Animation out = AnimationUtils.loadAnimation(getContext(), R.anim.sgn_pagedpubkit_hotspot_out);
                for (View v : mViews) {
                    v.clearAnimation();
                    v.setAnimation(out);
                }

                out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        for (View v : mViews) {
                            v.clearAnimation();
                        }
                        // removing view after clearing the animation causes some problem in Android 8
                        post(new Runnable() {
                            @Override
                            public void run() {
                                removeAllViews();
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                out.startNow();
            }
        }

    }

}
