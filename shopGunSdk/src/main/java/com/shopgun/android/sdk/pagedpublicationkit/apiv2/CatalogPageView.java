package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationPage;
import com.shopgun.android.utils.ColorUtils;
import com.shopgun.android.utils.UnitUtils;
import com.shopgun.android.verso.VersoPageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

public class CatalogPageView extends RatioFrameLayout implements VersoPageView {

    public static final String TAG = CatalogPageView.class.getSimpleName();

    PagedPublicationPage mPagedPublicationPage;
    PagedPublicationPage.Size mSize;
    ImageView mImageView;
    PulsatingTextView mTextView;
    PageTarget mPageTarget;

    public CatalogPageView(Context context, PagedPublicationPage page, int textColor) {
        super(context);
        mPagedPublicationPage = page;
        setAspectRatio(mPagedPublicationPage.getAspectRatio());

        // Add the ImageView
        mImageView = new CatalogImageView(context);
        addView(mImageView);

        // Add the pulsing page number
        mTextView = new PulsatingTextView(context);
        FrameLayout.LayoutParams lp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        mTextView.setLayoutParams(lp);
        mTextView.setPulseColors(textColor, ColorUtils.setAlphaComponent(textColor, 64));
        mTextView.setText(String.valueOf(mPagedPublicationPage.getPageIndex()));
        mTextView.setTextSize(UnitUtils.spToPx(26, getContext()));
        addView(mTextView);

        // load image
        load(PagedPublicationPage.Size.VIEW);
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
            mTextView.setVisibility(View.GONE);
            mImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

}
