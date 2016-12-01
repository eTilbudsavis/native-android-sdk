package com.shopgun.android.sdk.pagedpublicationkit.apiv2;

import android.content.res.Configuration;
import android.os.Parcel;
import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.materialcolorcreator.MaterialColorImpl;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.HotspotMap;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublication;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspotCollection;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationPage;
import com.shopgun.android.sdk.pagedpublicationkit.PublicationException;
import com.shopgun.android.sdk.pagedpublicationkit.impl.IntroOutroConfiguration;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.impl.CatalogLoaderRequest;
import com.shopgun.android.sdk.requests.impl.CatalogRequest;
import com.shopgun.android.utils.enums.Orientation;
import com.shopgun.android.verso.VersoSpreadProperty;

import java.util.ArrayList;
import java.util.List;

public class CatalogConfiguration extends IntroOutroConfiguration {

    public static final String TAG = CatalogConfiguration.class.getSimpleName();

    private Catalog mCatalog;
    private String mCatalogId;
    private int mLoadingTextColor;

    private CatalogPublication mPublication;
    private List<CatalogPage> mPages;
    private HotspotMap mHotspots;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = Orientation.fromConfiguration(newConfig);
    }

    public Orientation getOrientation() {
        return mOrientation;
    }

    public int getPublicationPageCount() {
        return mPages == null ? 0 : mPages.size();
    }

    public Catalog getCatalog() {
        return mCatalog;
    }

    @Override
    public int getSpreadMargin() {
        return 0;
    }

    @Override
    public VersoSpreadProperty getPublicationSpreadProperty(int spreadPosition, int[] pages) {
        return new CatalogSpreadProperty(pages, 1f, 1f, 3f);
    }

    @Override
    public View getPublicationPageView(ViewGroup container, int publicationPage) {
        CatalogPage catalogPage = mPages.get(publicationPage);
        return new CatalogPageView(container.getContext(), catalogPage, mLoadingTextColor);
    }

    @Override
    public View getPublicationSpreadOverlay(ViewGroup container, int[] publicationPages) {
        return new CatalogSpreadLayout(container.getContext(), publicationPages);
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
    public void load(OnLoadComplete callback) {

        if (isLoading()) {
            cancel();
        }

        mCallback = callback;
        if (mCatalog != null) {
            CatalogLoaderRequest r = new CatalogLoaderRequest(mCatalog, new CatalogListener());
            r.loadPages(mPages == null);
            r.loadHotspots(mHotspots == null);
            mCatalogRequest = r;
            mCallback.onPublicationLoaded(mPublication);
        } else {
            CatalogRequest r = new CatalogRequest(mCatalogId, new CatalogListener());
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
                mHotspots = mCatalog.getHotspots();
            }
        }
    }

    private class CatalogListener implements LoaderRequest.Listener<Catalog> {

        @Override
        public void onRequestComplete(Catalog response, List<ShopGunError> errors) {
            ensurePublicationData(response, errors);
        }

        @Override
        public void onRequestIntermediate(Catalog response, List<ShopGunError> errors) {
            ensurePublicationData(response, errors);
        }

        private void ensurePublicationData(Catalog catalog, List<ShopGunError> errors) {

            boolean publication = mPublication == null;
            if (publication) {
                mCatalog = catalog;
                ensureData();
                mCallback.onPublicationLoaded(mPublication);
            }

            boolean pages = mPages == null && catalog.getPages() != null;
            if (pages) {
                mPages = CatalogPage.from(ShopGun.getInstance().getContext(), catalog.getPages(), mPublication.getAspectRatio());
                catalog.setPages(null);
                mCallback.onPagesLoaded(mPages);
            }

            boolean hotspots = mHotspots == null && catalog.getHotspots() != null;
            if (hotspots) {
                HotspotMap hotspotMap = catalog.getHotspots();
                catalog.setHotspots(null);
                mHotspots = hotspotMap;
                mCallback.onHotspotsLoaded(mHotspots);
            }

            if (!(publication || pages)) {
                List<PublicationException> tmp = new ArrayList<>(errors.size());
                for (ShopGunError e : errors) {
                    tmp.add(new PublicationException(e));
                }
                mCallback.onError(tmp);
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
