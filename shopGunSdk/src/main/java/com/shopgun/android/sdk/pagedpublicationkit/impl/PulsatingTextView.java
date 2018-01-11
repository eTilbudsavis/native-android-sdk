package com.shopgun.android.sdk.pagedpublicationkit.impl;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.shopgun.android.utils.ColorUtils;

public class PulsatingTextView extends TextView {

    public static final String TAG = PulsatingTextView.class.getSimpleName();

    ValueAnimator mValueAnimator;
    int mColorFrom;
    int mColorTo;
    boolean mCancelled = false;

    public PulsatingTextView(Context context) {
        super(context);
    }

    public PulsatingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PulsatingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPulseColors(int colorFrom, int colorTo) {
        mColorFrom = colorFrom;
        mColorTo = colorTo;
        setTextColor(mColorFrom);
    }

    public void setPulseColors(@ColorInt int color,
                               @IntRange(from = 0x0, to = 0xFF) int fromAlpha,
                               @IntRange(from = 0x0, to = 0xFF) int toAlpha) {
        if ((0 <= fromAlpha || fromAlpha <= 255)
                && (0 <= toAlpha || toAlpha <= 255)) {
            setPulseColors(ColorUtils.setAlphaComponent(color, fromAlpha),
                    ColorUtils.setAlphaComponent(color, toAlpha));
        }
    }

    public void startPulse() {
        cancelPulse();
        mCancelled = false;
        mValueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mColorFrom, mColorTo);
        mValueAnimator.setDuration(1000);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        CancelAnimationListener l = new CancelAnimationListener();
        mValueAnimator.addUpdateListener(l);
        mValueAnimator.addListener(l);
        mValueAnimator.start();
    }

    public void cancelPulse(boolean onNextIteration) {
        mCancelled = true;
        if (!onNextIteration) {
            cancelPulse();
        }
    }

    private void cancelPulse() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            setTextColor(mColorFrom);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startPulse();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelPulse(false);
    }

    private class CancelAnimationListener implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int color = (int) animation.getAnimatedValue();
            setTextColor(color);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            if (mCancelled) {
                cancelPulse();
            }
        }

        @Override
        public void onAnimationStart(Animator animation) { }

        @Override
        public void onAnimationEnd(Animator animation) { }

        @Override
        public void onAnimationCancel(Animator animation) { }

    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        switch (visibility) {
            case View.INVISIBLE:
            case View.GONE: cancelPulse(); break;
            case View.VISIBLE: startPulse(); break;
        }
    }
}
