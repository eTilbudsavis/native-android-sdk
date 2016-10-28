package com.shopgun.android.sdk.pagedpublicationkit;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopgun.android.utils.enums.Orientation;
import com.shopgun.android.verso.VersoPageView;
import com.shopgun.android.verso.VersoSpreadConfiguration;
import com.shopgun.android.verso.VersoSpreadProperty;

import java.util.List;

public class PagedPublicationConfiguration implements VersoSpreadConfiguration {

    private PagedPublication mPublication;
    private List<PagedPublicationPage> mPublicationPages;
    private PagedPublicationHotspots mPublicationHotspots;
    private Configuration mConfiguration;
    private Orientation mOrientation;

    public PagedPublicationConfiguration(Context ctx, PagedPublication publication, List<PagedPublicationPage> pages) {
        this(Orientation.fromContext(ctx), publication, pages);
    }

    public PagedPublicationConfiguration(Orientation orientation, PagedPublication publication, List<PagedPublicationPage> pages) {
        mOrientation = orientation;
        mPublication = publication;
        mPublicationPages = pages;
    }

    @NonNull
    @Override
    public View getPageView(ViewGroup container, int page) {
        return new PagedPublicationPageView(container.getContext(), mPublicationPages.get(page));
//        return new MT(container.getContext(), "Page: " + page);
    }

    class MT extends TextView implements VersoPageView {

        public MT(Context context, String text) {
            super(context);
            setText(text);
        }

        @Override
        public boolean onZoom(float scale) {
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

        @Override
        public void onInvisible() {

        }
    }

    @Override
    public View getSpreadOverlay(ViewGroup container, int[] pages) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mConfiguration = newConfig;
    }

    @Override
    public int getPageCount() {
        return mPublicationPages == null ? 0 : mPublicationPages.size()-1 ;
    }

    @Override
    public int getSpreadCount() {
        return getPageCount() == 0 ? 0 : (mOrientation.isLandscape() ? (getPageCount()/2)+1 : getPageCount());
    }

    @Override
    public int getSpreadMargin() {
        return 0;
    }

    @Override
    public VersoSpreadProperty getSpreadProperty(int spreadPosition) {
        return new PagedPublicationSpreadProperty(positionToPages(spreadPosition), 1f, 1f, 3f);
    }

    private int[] positionToPages(int position) {

        // default is offset by one
        int page = position;
        if (mOrientation.isLandscape() && position > 0) {
            page = (position * 2) - 1;
        }

        if (mOrientation.isPortrait() || page == 0 || page >= getPageCount()-1) {
            // first, last, and everything in portrait is single-page
            return new int[]{page};
        } else {
            // Anything else is double page
            return new int[]{page, (page + 1)};
        }

    }

    @Override
    public int getSpreadPositionFromPage(int page) {
        if (mOrientation.isLandscape() && page > 0) {
            page += page % 2;
            return page/2;
        }
        return page;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private PagedPublicationConfiguration(Parcel in) {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Creator<PagedPublicationConfiguration> CREATOR = new Creator<PagedPublicationConfiguration>() {
        @Override
        public PagedPublicationConfiguration createFromParcel(Parcel source) {
            return new PagedPublicationConfiguration(source);
        }

        @Override
        public PagedPublicationConfiguration[] newArray(int size) {
            return new PagedPublicationConfiguration[size];
        }
    };

}
