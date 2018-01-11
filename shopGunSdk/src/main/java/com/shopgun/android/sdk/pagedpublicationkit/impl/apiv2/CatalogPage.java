package com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationPage;
import com.shopgun.android.utils.MemoryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CatalogPage implements PagedPublicationPage, Parcelable {

    private String mThumbUrl;
    private String mViewUrl;
    private String mZoomUrl;
    private int mPageIndex;
    private Bitmap.Config mBitmapConfig;
    private float mAspectRatio;

    /**
     * Calculating worst case scenario is landscape mode
     * (each view has two bitmaps), with high quality images.
     * Memory not included:
     * - The bitmap being replaced in the view
     * - The drawing cache in the PageTransformerTarget
     * - Bitmaps that have not yet been recycled
     * Overkill:
     * - Technically only two zoom images will be loaded (depending on lifecycle e.t.c.)
     * - Screen-size/View-size will possibly force down scale of bitmap.
     * ------------------------------------------------------------
     * |                 Decoding memory consumption              |
     * ------------------------------------------------------------
     * |                    | RGB_565 (2byte) | ARGB_8888 (4byte) |
     * ------------------------------------------------------------
     * | thumb (177x212)px  |            75kb |             150kb |
     * | view (800x1000)px  |          1600kb |            3200kb |
     * | zoom (1500x2000)px |          6000kb |           12000kb |
     * ------------------------------------------------------------
     */
    public static List<CatalogPage> from(Context ctx, List<Images> images, float aspectRatio) {
        int maxMem = MemoryUtils.getMaxHeap(ctx);
        List<CatalogPage> pages = new ArrayList<>(images.size());
        for (int j = 0; j < images.size(); j++) {
            Images i = images.get(j);
            CatalogPage page;
            if (maxMem >= 96) {
                // worst case: (2*12000)*3 = 72mb
                page = new CatalogPage(j, i.getThumb(), i.getView(), i.getZoom(), aspectRatio, Bitmap.Config.ARGB_8888);
            } else if (maxMem >= 48) {
                // worst case: (2*6000)*3 = 36mb
                page = new CatalogPage(j, i.getThumb(), i.getView(), i.getZoom(), aspectRatio, Bitmap.Config.RGB_565);
            } else {
                // worst case: (2*1600)*3 = 10mb
                // essentially we just hope for the best
                page = new CatalogPage(j, i.getThumb(), i.getView(), i.getView(), aspectRatio, Bitmap.Config.RGB_565);
            }
            pages.add(page);
        }
        return pages;
    }

    public CatalogPage(int pageIndex, String thumbUrl, String viewUrl, String zoomUrl, float aspectRatio, Bitmap.Config bitmapConfig) {
        mPageIndex = pageIndex;
        mThumbUrl = thumbUrl;
        mViewUrl = viewUrl;
        mZoomUrl = zoomUrl;
        mBitmapConfig = bitmapConfig;
        mAspectRatio = aspectRatio;
    }

    @NonNull
    @Override
    public String getUrl(Size size) {
        switch (size) {
            case THUMB:
                return mThumbUrl;
            case VIEW:
                return mViewUrl;
            case ZOOM:
                return mZoomUrl;
            default:
                return mViewUrl;
        }
    }

    @Override
    public int getPageIndex() {
        return mPageIndex;
    }

    @NonNull
    @Override
    public Bitmap.Config getBitmapConfig(Size size) {
        return null;
    }

    @Override
    public boolean allowResize(Size size) {
        return size == Size.VIEW;
    }

    @Override
    public float getAspectRatio() {
        return mAspectRatio;
    }

    @Override
    public String toString() {
        String format = "CatalogPage[ page:%s, viewUrl:%s, bitmapConfig:%s, aspectRatio:%.2f";
        return String.format(Locale.US, format, mPageIndex, mViewUrl, mBitmapConfig, mAspectRatio);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mViewUrl);
        dest.writeString(this.mZoomUrl);
        dest.writeInt(this.mPageIndex);
        dest.writeInt(this.mBitmapConfig == null ? -1 : this.mBitmapConfig.ordinal());
        dest.writeFloat(this.mAspectRatio);
    }

    protected CatalogPage(Parcel in) {
        this.mViewUrl = in.readString();
        this.mZoomUrl = in.readString();
        this.mPageIndex = in.readInt();
        int tmpMBitmapConfig = in.readInt();
        this.mBitmapConfig = tmpMBitmapConfig == -1 ? null : Bitmap.Config.values()[tmpMBitmapConfig];
        this.mAspectRatio = in.readFloat();
    }

    public static final Creator<CatalogPage> CREATOR = new Creator<CatalogPage>() {
        @Override
        public CatalogPage createFromParcel(Parcel source) {
            return new CatalogPage(source);
        }

        @Override
        public CatalogPage[] newArray(int size) {
            return new CatalogPage[size];
        }
    };
}
