package com.tjek.sdk.publicationviewer.paged.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.shopgun.android.utils.NumberUtils;

public class AspectRatioFrameLayout extends FrameLayout {

    public static final String TAG = AspectRatioFrameLayout.class.getSimpleName();

    float mAspectRatio;

    public AspectRatioFrameLayout(Context context) {
        super(context);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
    }

    public float getAspectRatio() {
        return mAspectRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (NumberUtils.isEqual(mAspectRatio, 0f)) {
            // Aspect ratio haven't been set, let super handle it
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        float containerWidth = MeasureSpec.getSize(widthMeasureSpec);
        float containerHeight = MeasureSpec.getSize(heightMeasureSpec);
        float containerAspectRatio = containerWidth/containerHeight;

        if (mAspectRatio < containerAspectRatio) {
            containerWidth = containerHeight * mAspectRatio;
        } else if (mAspectRatio > containerAspectRatio) {
            containerHeight = containerWidth / mAspectRatio;
        }

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int)containerWidth, MeasureSpec.AT_MOST);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int)containerHeight, MeasureSpec.AT_MOST);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);

        setMeasuredDimension((int) containerWidth, (int) containerHeight);
    }

}
