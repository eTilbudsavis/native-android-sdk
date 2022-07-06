package com.tjek.sdk.publicationviewer.paged.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.shopgun.android.sdk.R;
import com.tjek.sdk.api.models.PublicationHotspotV2;
import com.tjek.sdk.publicationviewer.paged.layouts.PublicationSpreadLayout;

@SuppressLint("ViewConstructor")
public class HotspotView extends View {

    public static final String TAG = HotspotView.class.getSimpleName();

    PublicationHotspotV2 mHotspot;
    int[] mPages;
    RectF mBounds;

    public HotspotView(Context context, PublicationHotspotV2 hotspot, int[] pages) {
        super(context);
        mHotspot = hotspot;
        mPages = pages;
        mBounds = mHotspot.getBoundsForPages(mPages);
        setBackgroundResource(R.drawable.tjek_pagedpub_hotspot_bg);
        // set the 'in' animation
        setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.tjek_pagedpub_hotspot_in));
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

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();
        // set the view to gone to avoid flickering
        setVisibility(View.GONE);
    }

}
