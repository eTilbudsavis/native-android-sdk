package com.tjek.sdk.publicationviewer.paged;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class PageImageView extends AppCompatImageView {
    public PageImageView(Context context) {
        super(context);
    }

    public PageImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }
}
