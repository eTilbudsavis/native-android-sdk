package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.content.res.Configuration;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.materialcolorcreator.MaterialColorImpl;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.HotspotMap;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublication;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationConfiguration;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspotCollection;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationPage;
import com.shopgun.android.sdk.pagedpublicationkit.PublicationException;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.impl.CatalogLoaderRequest;
import com.shopgun.android.sdk.requests.impl.CatalogRequest;
import com.shopgun.android.utils.enums.Orientation;
import com.shopgun.android.verso.VersoSpreadProperty;

import java.util.ArrayList;
import java.util.List;

public class CatalogConfiguration implements PagedPublicationConfiguration {

    public static final String TAG = CatalogConfiguration.class.getSimpleName();

    private Catalog mCatalog;
    private String mCatalogId;
    private int mLoadingTextColor;

    private CatalogPublication mPublication;
    private List<CatalogPage> mPages;
    private CatalogHotspotCollection mHotspots;
    private Orientation mOrientation = Orientation.PORTRAIT;

    private OnLoadComplete mCallback;
    private Request mCatalogRequest;

    public CatalogConfiguration(String catalogId) {
        mCatalogId = catalogId;
    }

    public CatalogConfiguration(Catalog catalog) {
        mCatalog = catalog;
        ensureData();
    }

    @NonNull
    @Override
    public View getPageView(ViewGroup container, int page) {
        if (hasIntro() && page == 0) {
            return getIntro(container, page);
        }
        if (hasOutro() && page == getPageCount() - 1 ) {
            return getOutro(container, page);
        }
        // we'll need to offset all the pages to get the right images here
        int tmpPage = hasIntro() ? page - 1 : page;
        CatalogPage catalogPage = mPages.get(tmpPage);
        return new CatalogPageView(container.getContext(), catalogPage, mLoadingTextColor);
    }

    @Override
    public View getSpreadOverlay(ViewGroup container, int[] pages) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setOrientation(Orientation.fromConfiguration(newConfig));
    }

    public void setOrientation(Orientation orientation) {
        mOrientation = orientation;
    }

    @Override
    public int getPageCount() {
        int count = getPublicationPageCount();
        if (count > 0) {
            if (hasIntro()) count++;
            if (hasOutro()) count++;
        }
        return count;
    }

    public int getPublicationPageCount() {
        return mPages == null ? 0 : mPages.size();
    }

    public Catalog getCatalog() {
        return mCatalog;
    }

    @Override
    public int getSpreadCount() {
        int pageCount = getPublicationPageCount();
        if (pageCount > 0) {
            int count = mOrientation.isLandscape() && pageCount > 0 ? (pageCount/2)+1 : pageCount;
            if (hasIntro()) count++;
            if (hasOutro()) count++;
            return count;
        }
        return 0;
    }

    @Override
    public int getSpreadMargin() {
        return 0;
    }

    @Override
    public VersoSpreadProperty getSpreadProperty(int spreadPosition) {
        int[] pages = positionToPages(spreadPosition);
        if ((hasIntro() && spreadPosition == 0) || (hasOutro() && spreadPosition == getSpreadCount()-1 ) ) {
            return new CatalogSpreadProperty(pages, 0.6f, 1f, 1f);
        }
        return new CatalogSpreadProperty(pages, 1f, 1f, 4f);
    }

    private int[] positionToPages(int position) {

        int page = position;

        if (mOrientation.isPortrait()) {
            return new int[]{ page };
        }

        if (position == 0 || (hasIntro() && position == 1)) {
            // either intro or first page of catalog
            return new int[]{ page };
        }

        int spreadCount = getSpreadCount();

        boolean isOutro = hasOutro() && position == spreadCount - 1;
        boolean lastCatalogPage = position == spreadCount - (hasOutro() ? 2 : 1);

        if (hasIntro()) {
            if (isOutro) {
                page = ((position - 1) * 2) - 1;
            } else {
                page = (position - 1) * 2;
            }
        } else {
            if (hasOutro()) {
                page = (position - 1) * 2;
            } else {
                page = (position * 2) - 1;
            }
        }

        if (isOutro || lastCatalogPage) {
            return new int[]{ page };
        }

        // Anything else is double page
        return new int[]{ page, page+1 };

    }

    @Override
    public int getSpreadPositionFromPage(int page) {
        if (mOrientation.isPortrait()) {
            return page;
        }
        if (page == 0 || (hasIntro() && page == 1)) {
            return page;
        }

        if (hasOutro() && page == getPageCount()-1) {
            return getSpreadCount()-1;
        } else if (hasIntro()) {
            return ((page - (page % 2))/2)+1;
        } else {
            return ((page-1)/2)+1;
        }
    }

    @Override
    public boolean hasData() {
        return mPublication != null && mPages != null;
    }

    @Override
    public PagedPublication getPublication() {
        return mPublication;
    }

    @Override
    public boolean hasPublication() {
        return mPublication != null;
    }

    @Override
    public List<? extends PagedPublicationPage> getPages() {
        return mPages;
    }

    @Override
    public boolean hasPages() {
        return mPages != null;
    }

    @Override
    public PagedPublicationHotspotCollection getHotspotCollection() {
        return mHotspots;
    }

    @Override
    public boolean hasHotspotCollection() {
        return mHotspots != null;
    }

    @Override
    public boolean hasIntro() {
        return false;
    }

    @Override
    public View getIntro(ViewGroup container, int page) {
        return null;
    }

    @Override
    public boolean hasOutro() {
        return false;
    }

    @Override
    public View getOutro(ViewGroup container, int page) {
        return null;
    }

    @Override
    public void load(OnLoadComplete callback) {

        if (isLoading()) {
            cancel();
        }

        LoaderRequest.Listener<Catalog> listener = new LoaderRequest.Listener<Catalog>() {
            @Override
            public void onRequestComplete(Catalog response, List<ShopGunError> errors) {
                populate(response, errors);
            }

            @Override
            public void onRequestIntermediate(Catalog response, List<ShopGunError> errors) {
                populate(response, errors);
            }

            private void populate(Catalog response, List<ShopGunError> errors) {

                boolean publication = mPublication == null;
                if (publication) {
                    mCatalog = response;
                    ensureData();
                    mCallback.onPublicationLoaded(mPublication);
                }

                boolean pages = mPages == null && response.getPages() != null;
                if (pages) {
                    mPages = CatalogPage.from(ShopGun.getInstance().getContext(), response.getPages(), mPublication.getAspectRatio());
                    response.setPages(null);
                    mCallback.onPagesLoaded(mPages);
                }

                boolean hotspots = mHotspots == null && response.getHotspots() != null;
                if (hotspots) {
                    HotspotMap hotspotMap = response.getHotspots();
                    response.setHotspots(null);
                    mHotspots = new CatalogHotspotCollection(hotspotMap);
                    mCallback.onHotspotsLoaded(mHotspots);
                }

                if (!(publication || pages || hotspots)) {
                    List<PublicationException> tmp = new ArrayList<>(errors.size());
                    for (ShopGunError e : errors) {
                        tmp.add(new PublicationException(e));
                    }
                    mCallback.onError(tmp);
                }
            }

        };

        mCallback = callback;
        if (mCatalog != null) {
            CatalogLoaderRequest r = new CatalogLoaderRequest(mCatalog, listener);
            r.loadPages(mPages == null);
            r.loadHotspots(mHotspots == null);
            mCatalogRequest = r;
            mCallback.onPublicationLoaded(mPublication);
        } else {
            CatalogRequest r = new CatalogRequest(mCatalogId, listener);
            r.loadPages(mPages == null);
            r.loadHotspots(mHotspots == null);
            mCatalogRequest = r;
        }
//        mCatalogRequest.setDebugger(new NetworkDebugger());
        ShopGun.getInstance().add(mCatalogRequest);
    }

    @Override
    public boolean isLoading() {
        return mCatalogRequest != null && (!mCatalogRequest.isFinished() || !mCatalogRequest.isCanceled());
    }

    @Override
    public void cancel() {
        // Cancel and release expensive resources
        if (mCatalogRequest != null) {
            mCatalogRequest.cancel();
        }
        mCallback = null;
        mCatalogRequest = null;
    }

    @Override
    public String getSource() {
        return "legacy";
    }

    private void ensureData() {
        if (mCatalog != null) {
            mCatalogId = mCatalog.getId();
            int color = mCatalog.getBranding().getColor();
            mLoadingTextColor = new MaterialColorImpl(color).getPrimaryText();
            mPublication = new CatalogPublication(mCatalog);
            if (mCatalog.getPages() != null) {
                mPages = CatalogPage.from(ShopGun.getInstance().getContext(), mCatalog.getPages(), mPublication.getAspectRatio());
            }
            if (mCatalog.getHotspots() != null) {
                mHotspots = new CatalogHotspotCollection(mCatalog.getHotspots());
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCatalog, flags);
        dest.writeString(this.mCatalogId);
        dest.writeInt(this.mOrientation == null ? -1 : this.mOrientation.ordinal());
    }

    protected CatalogConfiguration(Parcel in) {
        this.mCatalog = in.readParcelable(Catalog.class.getClassLoader());
        this.mCatalogId = in.readString();
        int tmpMOrientation = in.readInt();
        this.mOrientation = tmpMOrientation == -1 ? null : Orientation.values()[tmpMOrientation];
    }

    public static final Creator<CatalogConfiguration> CREATOR = new Creator<CatalogConfiguration>() {
        @Override
        public CatalogConfiguration createFromParcel(Parcel source) {
            return new CatalogConfiguration(source);
        }

        @Override
        public CatalogConfiguration[] newArray(int size) {
            return new CatalogConfiguration[size];
        }
    };

}
