package com.shopgun.android.sdk.pagedpublicationkit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shopgun.android.verso.VersoPageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

public class PagedPublicationPageView extends ImageView implements VersoPageView {

    public static final String TAG = PagedPublicationPageView.class.getSimpleName();

    PagedPublicationPage mPagedPublicationPage;
    PagedPublicationPage.Size mSize;
    PageTarget mPageTarget;

    public PagedPublicationPageView(Context context, PagedPublicationPage page) {
        super(context);
        mPagedPublicationPage = page;
        load(PagedPublicationPage.Size.VIEW);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float containerWidth = MeasureSpec.getSize(widthMeasureSpec);
        float containerHeight = MeasureSpec.getSize(heightMeasureSpec);
        float containerAspectRatio = containerWidth/containerHeight;
        float pageAspectRatio = mPagedPublicationPage.getAspectRatio();
//        L.d(TAG, String.format(Locale.US, "MeasureSpec[ w:%.0f, h:%.0f, containerAspectRatio:%.2f ], pageAspectRatio:%.2f",
//                containerWidth, containerHeight, containerAspectRatio, pageAspectRatio));
        if (pageAspectRatio < containerAspectRatio) {
            containerWidth = containerHeight * pageAspectRatio;
        } else if (pageAspectRatio > containerAspectRatio) {
            containerHeight = containerWidth / pageAspectRatio;
        }
        setMeasuredDimension((int) containerWidth, (int) containerHeight);
//        L.d(TAG, String.format(Locale.US, "[%s] Measured[ w:%s, h:%s ]", mPagedPublicationPage.getPageIndex(), getMeasuredWidth(), getMeasuredHeight()));
    }

    @Override
    public boolean onZoom(float scale) {
        if (scale > 1.1f && !isZoomed()) {
            load(PagedPublicationPage.Size.ZOOM);
        } else if (scale < 1.1f && isZoomed()) {
            load(PagedPublicationPage.Size.VIEW);
        }
        return false;
    }

    @Override
    public void setOnCompletionListener() {

    }

    @Override
    public OnLoadCompletionListener getOnLoadCompleteListener() {
        return null;
    }

    @Override
    public void onVisible() {

    }

    private boolean isZoomed() {
        return mSize == PagedPublicationPage.Size.ZOOM;
    }

    @Override
    public void onInvisible() {
        if (isZoomed()) {
            load(PagedPublicationPage.Size.ZOOM);
        }
    }

    private void load(PagedPublicationPage.Size size) {
        if (mSize == size) {
            return;
        }
        mSize = size;
        Picasso p = Picasso.with(getContext());
        RequestCreator rc = p.load(mPagedPublicationPage.getUrl(size));
        rc.config(mPagedPublicationPage.getBitmapConfig(size));
        mPageTarget = new PageTarget();
        rc.into(mPageTarget);
    }

    class PageTarget implements Target {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

}
