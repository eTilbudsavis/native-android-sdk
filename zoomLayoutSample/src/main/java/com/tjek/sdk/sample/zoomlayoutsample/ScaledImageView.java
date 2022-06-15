package com.tjek.sdk.sample.zoomlayoutsample;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class ScaledImageView extends AppCompatImageView {

    float mImageAspectRatio;

    public ScaledImageView(Context context) {
        super(context);
    }

    public ScaledImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaledImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageAspectRatio(float imageAspectRatio) {
        mImageAspectRatio = imageAspectRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        float containerWidth = MeasureSpec.getSize(widthMeasureSpec);
        float containerHeight = MeasureSpec.getSize(heightMeasureSpec);
        float containerAspectRatio = containerWidth/containerHeight;

        if (mImageAspectRatio < containerAspectRatio) {
            containerWidth = containerHeight * mImageAspectRatio;
        } else if (mImageAspectRatio > containerAspectRatio) {
            containerHeight = containerWidth / mImageAspectRatio;
        }
        
        setMeasuredDimension((int) containerWidth, (int) containerHeight);
    }

}
