package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.shopgun.android.sdk.R;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspot;

public class CatalogHotspotView extends View {

    public static final String TAG = CatalogHotspotView.class.getSimpleName();

    private static final int MSG_WHAT_ANIMATE_OUT = 1;
    private static final int MSG_WHAT_REMOVE_VIEW = 2;

    PagedPublicationHotspot mHotspot;
    int[] mPages;
    RectF mBounds;
    Handler mHandler;
    Animation mAnimation;

    public CatalogHotspotView(Context context, PagedPublicationHotspot hotspot, int[] pages) {
        super(context);
        mHotspot = hotspot;
        mPages = pages;
        mHandler = new Handler(Looper.getMainLooper(), new CB());
        mBounds = mHotspot.getBoundsForPages(mPages);
        setBackgroundResource(R.drawable.sgn_pagedpubkit_hotspot_bg);
    }

    class CB implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_WHAT_ANIMATE_OUT:
                    if (mAnimation != null && mAnimation.hasStarted()) {
                        mAnimation.cancel();
                    }
                    mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.sgn_pagedpubkit_hotspot_out);
                    mAnimation.setAnimationListener(new HotspotAnimationListener());
                    startAnimation(mAnimation);
                    return true;

                case MSG_WHAT_REMOVE_VIEW:
                    // delay the removal of the view, to avoid crashes when parent wants to redraw.
                    // this happens when multiple hotspot views are removed at the same time
                    ((ViewGroup) getParent()).removeView(CatalogHotspotView.this);
                    return true;

            }
            return false;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Message msg = mHandler.obtainMessage(MSG_WHAT_ANIMATE_OUT);
        mHandler.sendMessageDelayed(msg, 2500);
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.sgn_pagedpubkit_hotspot_in);
        startAnimation(mAnimation);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimation != null) {
            mAnimation.cancel();
        }
        mHandler.removeMessages(MSG_WHAT_ANIMATE_OUT);
        mHandler.removeMessages(MSG_WHAT_REMOVE_VIEW);
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

    class HotspotAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            setVisibility(View.GONE);
            Message msg = mHandler.obtainMessage(MSG_WHAT_REMOVE_VIEW);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

    }
}
