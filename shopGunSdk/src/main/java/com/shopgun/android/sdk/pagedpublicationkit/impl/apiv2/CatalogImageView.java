package com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

public class CatalogImageView extends AppCompatImageView {
    public CatalogImageView(Context context) {
        super(context);
    }

    public CatalogImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CatalogImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }
}
