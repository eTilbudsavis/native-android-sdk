package com.shopgun.android.sdk.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.shopgun.android.sdk.demo.base.BaseActivity;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublication;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationConfiguration;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationFragment;
import com.shopgun.android.sdk.pagedpublicationkit.impl.CatalogLoader;
import com.shopgun.android.sdk.pagedpublicationkit.impl.CatalogPublication;
import com.shopgun.android.utils.TextUtils;
import com.shopgun.android.utils.log.L;
import com.shopgun.android.verso.VersoFragment;

import java.util.Locale;

public class PagedPublicationActivity extends BaseActivity {

    public static final String TAG = PagedPublicationActivity.class.getSimpleName();

    public static final String FRAGMENT_TAG = "publication-fragment";

    private static final String KEY_CATALOG = "CATALOG";

    private Catalog mCatalog;
    private PagedPublicationFragment mPagedPublicationFragment;

    public static void start(Context context, Catalog catalog) {
        start(context, catalog.getId(), catalog);
    }

    public static void start(Context context, String catalogId, Catalog catalog) {
        if (catalogId == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        Intent i = new Intent(context, PagedPublicationActivity.class);
        i.putExtra(KEY_CATALOG, catalog);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagedpublicationkit);
        setupState(savedInstanceState);
        ensurePagedPublicationFragment();
    }

    private void setupState(@Nullable Bundle savedInstanceState) {
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            mCatalog = extras.getParcelable(KEY_CATALOG);
        }
        if (savedInstanceState != null) {
            // TODO: 24/10/16 Add any saved-state here
        }
    }

    private void ensurePagedPublicationFragment() {
        mPagedPublicationFragment = (PagedPublicationFragment)
                getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (mPagedPublicationFragment == null) {
            mPagedPublicationFragment = PagedPublicationFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.pagedPublication, mPagedPublicationFragment, FRAGMENT_TAG)
                    .addToBackStack(FRAGMENT_TAG)
                    .commit();
        }

        mPagedPublicationFragment.setPublicationLoader(new CatalogLoader(this, mCatalog));

        mPagedPublicationFragment.setOnPageChangeListener(new VersoFragment.OnPageChangeListener() {
            @Override
            public void onPagesScrolled(int currentPosition, int[] currentPages, int previousPosition, int[] previousPages) {

            }

            @Override
            public void onPagesChanged(int currentPosition, int[] currentPages, int previousPosition, int[] previousPages) {
                L.d(TAG, String.format(Locale.US, "onPagesChanged[ currentPosition:%s, currentPages:%s, previousPosition:%s, previousPages:%s ]"
                , currentPosition, TextUtils.join(",", currentPages), previousPosition, TextUtils.join(",", previousPages)));
            }

            @Override
            public void onVisiblePageIndexesChanged(int[] pages, int[] added, int[] removed) {

            }
        });

    }

}
