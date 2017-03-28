package com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationPage;
import com.shopgun.android.sdk.pagedpublicationkit.impl.AspectRatioFrameLayout;
import com.shopgun.android.sdk.pagedpublicationkit.impl.PulsatingTextView;
import com.shopgun.android.utils.ColorUtils;
import com.shopgun.android.utils.UnitUtils;
import com.shopgun.android.verso.VersoPageView;
import com.shopgun.android.verso.VersoPageViewFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class CatalogPageView extends AspectRatioFrameLayout implements VersoPageView {

    public static final String TAG = CatalogPageView.class.getSimpleName();

    private PagedPublicationPage mPagedPublicationPage;
    private PagedPublicationPage.Size mSize;
    private ImageView mImageView;
    private PulsatingTextView mTextView;
    private PageTarget mPageTarget = new PageTarget();
    private boolean mVisible;
    private VersoPageViewFragment.OnLoadCompleteListener mLoadCompletionListener;

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
        mTextView.setText(String.valueOf(mPagedPublicationPage.getPageIndex()+1));
        mTextView.setTextSize(UnitUtils.spToPx(26, getContext()));
        addView(mTextView);

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
    public void setOnLoadCompleteListener(VersoPageViewFragment.OnLoadCompleteListener listener) {
        mLoadCompletionListener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        load(PagedPublicationPage.Size.VIEW);
    }

    @Override
    public void onVisible() {
        mVisible = true;
    }

    @Override
    public void onInvisible() {
        mVisible = false;
    }

    @Override
    public int getPage() {
        return mPagedPublicationPage.getPageIndex();
    }

    private boolean isZoomed() {
        return mSize == PagedPublicationPage.Size.ZOOM;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Picasso.with(getContext()).cancelRequest(mPageTarget);
    }

    private void load(PagedPublicationPage.Size size) {
        if (mSize == size) {
            return;
        }
        mSize = size;
        Picasso p = Picasso.with(getContext());
        p.cancelRequest(mPageTarget);
        p.load(mPagedPublicationPage.getUrl(size))
                .config(mPagedPublicationPage.getBitmapConfig(size))
                .into(mPageTarget);
    }

    class PageTarget implements Target {

        public boolean mCallback = true;

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mTextView.setVisibility(View.GONE);
            mImageView.setImageBitmap(bitmap);
            if (mLoadCompletionListener != null && mCallback) {
                mCallback = false;
                mLoadCompletionListener.onPageLoadComplete(true, CatalogPageView.this);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if (mLoadCompletionListener != null) {
                mLoadCompletionListener.onPageLoadComplete(false, CatalogPageView.this);
            }
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

}
