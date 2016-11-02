package com.shopgun.android.sdk.pagedpublicationkit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shopgun.android.verso.VersoFragment;
import com.shopgun.android.verso.VersoViewPager;

import java.util.List;

public class PagedPublicationFragment extends VersoFragment implements PagedPublicationLoader.OnLoadComplete {

    public static final String TAG = PagedPublicationFragment.class.getSimpleName();

    private static final String VERSO_FRAGMENT_TAG = "verso_fragment";

    FrameLayout mFrame;
    VersoViewPager mVersoView;
    PagedPublication mPagedPublication;
    List<PagedPublicationPage> mPublicationPages;
    PagedPublicationHotspots mPublicationHotspots;
    PagedPublicationLoader mPublicationLoader;

    public static PagedPublicationFragment newInstance() {
        return new PagedPublicationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mVersoView = (VersoViewPager) super.onCreateView(inflater, container, savedInstanceState);
        mFrame = new FrameLayout(container.getContext());
        int wh = ViewGroup.LayoutParams.MATCH_PARENT;
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(wh, wh);
        mFrame.setLayoutParams(lp);
        mFrame.addView(mVersoView);
        return mFrame;
    }

    public PagedPublicationLoader getPublicationLoader() {
        return mPublicationLoader;
    }

    public void setPublicationLoader(PagedPublicationLoader publicationLoader) {
        mPublicationLoader = publicationLoader;
        if (mFrame != null) {
            loadPagedPublication();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPagedPublication();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPublicationLoader != null) {
            mPublicationLoader.cancel();
        }
    }

    private void loadPagedPublication() {
        if (mPublicationLoader != null && !mPublicationLoader.isLoading()) {
            mPublicationLoader.load(this);
            // TODO: 27/10/16 Show loader view
        }
    }

    View getErrorView() {
        TextView tv = new TextView(mFrame.getContext());
        tv.setText("Error!");
        return tv;
    }

    View getLoaderView() {
        TextView tv = new TextView(mFrame.getContext());
        tv.setText("Loading...");
        return tv;
    }

    @Override
    public void onPublicationLoaded(PagedPublication publication) {
        if (publication != null) {
            mPagedPublication = publication;
            updateVerso();
        } else {
            // TODO: 27/10/16 Show error view
        }
    }

    @Override
    public void onPagesLoaded(List<PagedPublicationPage> pages) {
        if (pages != null) {
            mPublicationPages = pages;
            updateVerso();
        } else {
            // TODO: 27/10/16 Show error view
        }
    }

    @Override
    public void onHotspotsLoaded(PagedPublicationHotspots hotspots) {
        if (hotspots != null) {
            mPublicationHotspots = hotspots;
            updateVerso();
        }
    }

    private void updateVerso() {
        if (mPagedPublication != null && mPublicationPages != null) {
            PagedPublicationConfiguration config = new PagedPublicationConfiguration(getContext(), mPagedPublication, mPublicationPages);
            setVersoSpreadConfiguration(config);
        }
    }

}
