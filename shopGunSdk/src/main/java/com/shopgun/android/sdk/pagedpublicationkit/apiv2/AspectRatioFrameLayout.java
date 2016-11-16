package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.shopgun.android.utils.NumberUtils;

public class AspectRatioFrameLayout extends FrameLayout {

    public static final String TAG = AspectRatioFrameLayout.class.getSimpleName();

    float mAspectRatio;

    public AspectRatioFrameLayout(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // TODO: 10/11/16 we can add xml aspectRation here later
//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, R.styleable.FrameLayout, defStyleAttr, defStyleRes);
//
//        if (a.getBoolean(R.styleable.FrameLayout_measureAllChildren, false)) {
//            setMeasureAllChildren(true);
//        }
//
//        a.recycle();

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

        float containerWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        float containerHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        float containerAspectRatio = containerWidth/containerHeight;

        if (mAspectRatio < containerAspectRatio) {
            containerWidth = containerHeight * mAspectRatio;
        } else if (mAspectRatio > containerAspectRatio) {
            containerHeight = containerWidth / mAspectRatio;
        }

        int childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)containerWidth, View.MeasureSpec.AT_MOST);
        int childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)containerHeight, View.MeasureSpec.AT_MOST);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);

        setMeasuredDimension((int) containerWidth, (int) containerHeight);
    }

}
