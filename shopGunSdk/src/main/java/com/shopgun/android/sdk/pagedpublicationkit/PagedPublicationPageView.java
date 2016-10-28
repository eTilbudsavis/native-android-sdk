package com.shopgun.android.sdk.pagedpublicationkit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shopgun.android.utils.log.L;
import com.shopgun.android.verso.VersoPageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.util.Locale;

public class PagedPublicationPageView extends ImageView implements VersoPageView {

    public static final String TAG = PagedPublicationPageView.class.getSimpleName();

    PagedPublicationPage mPagedPublicationPage;
    PagedPublicationPage.Size mQuality = PagedPublicationPage.Size.VIEW;
    Target mTarget;

    public PagedPublicationPageView(Context context, PagedPublicationPage page) {
        super(context);
        mPagedPublicationPage = page;
        init();
    }

    private void init() {
        int wh = ViewGroup.LayoutParams.MATCH_PARENT;
        setLayoutParams(new ViewGroup.LayoutParams(wh, wh));
        load(PagedPublicationPage.Size.VIEW);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        L.d(TAG, String.format(Locale.US, "onMeasure[ w:%s, h:%s ]", getMeasuredWidth(), getMeasuredHeight()));
//    }

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
        return mQuality == PagedPublicationPage.Size.ZOOM;
    }

    @Override
    public void onInvisible() {
        if (isZoomed()) {
            load(PagedPublicationPage.Size.ZOOM);
        }
    }

    private void load(PagedPublicationPage.Size q) {
//        L.d(TAG, String.format(Locale.US, "[%s] load ", q.name()));
        Picasso p = Picasso.with(getContext());
        RequestCreator rc = p.load(mPagedPublicationPage.getUrl(q));
        rc.config(mPagedPublicationPage.getBitmapConfig(q));
        if (mPagedPublicationPage.allowResize(q)) {
//            rc.fit();
//            rc.centerInside();
        }
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                L.d(TAG, String.format(Locale.US, "[%s] onBitmapLoaded", mPagedPublicationPage.getPageIndex()));
                setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
//                L.d(TAG, String.format(Locale.US, "[%s] onBitmapFailed", mPagedPublicationPage.getPageIndex()));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
//                L.d(TAG, String.format(Locale.US, "[%s] onPrepareLoad", mPagedPublicationPage.getPageIndex()));
            }
        };
        rc.into(mTarget);
    }

}
