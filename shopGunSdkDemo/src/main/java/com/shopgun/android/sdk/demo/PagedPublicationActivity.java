package com.shopgun.android.sdk.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopgun.android.sdk.demo.base.BaseActivity;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationConfiguration;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationFragment;
import com.shopgun.android.sdk.pagedpublicationkit.apiv2.CatalogConfiguration;
import com.shopgun.android.utils.TextUtils;
import com.shopgun.android.utils.log.L;
import com.shopgun.android.verso.VersoFragment;
import com.shopgun.android.verso.VersoPageView;

import java.util.Locale;

public class PagedPublicationActivity extends BaseActivity {

    public static final String TAG = PagedPublicationActivity.class.getSimpleName();

    public static final String FRAGMENT_TAG = "publication-fragment";

    private static final String KEY_CATALOG = "CATALOG";

    private PagedPublicationFragment mPagedPublicationFragment;
    private CatalogConfiguration mConfig;

    public static void start(Context context, Catalog catalog) {
        Intent i = new Intent(context, PagedPublicationActivity.class);
        i.putExtra(KEY_CATALOG, catalog);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagedpublicationkit);
        Catalog catalog = getIntent().getExtras().getParcelable(KEY_CATALOG);
        ensurePagedPublicationFragment(catalog);
        addPagedPublicationListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mConfig == null) {
            PagedPublicationConfiguration config =
                    mPagedPublicationFragment.getPublicationConfiguration();
            if (config instanceof CatalogConfiguration) {
                mConfig = (CatalogConfiguration) config;
            }
        }
    }

    private void ensurePagedPublicationFragment(Catalog catalog) {
        mPagedPublicationFragment = (PagedPublicationFragment)
                getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (mPagedPublicationFragment == null) {
            mConfig = new CatConfig(catalog);
            mPagedPublicationFragment = PagedPublicationFragment.newInstance(mConfig);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.pagedPublication, mPagedPublicationFragment, FRAGMENT_TAG)
                    .commit();
        }

    }

    private void addPagedPublicationListeners() {

        mPagedPublicationFragment.setOnPageChangeListener(new VersoFragment.OnPageChangeListener() {
            @Override
            public void onPagesScrolled(int currentPosition, int[] currentPages, int previousPosition, int[] previousPages) {
                L.d(TAG, String.format(Locale.US, "onPagesChanged[ currentPosition:%s, currentPages:%s, previousPosition:%s, previousPages:%s ]"
                        , currentPosition, TextUtils.join(",", currentPages), previousPosition, TextUtils.join(",", previousPages)));

                String spread = String.format("spread[ %s / %s ]", currentPosition+1, mConfig.getSpreadCount());
                if (mConfig.hasIntro() && currentPosition == 0) {
                    setTitle("Intro " + spread);
                } else if (mConfig.hasOutro() && currentPosition == mConfig.getSpreadCount()-1) {
                    setTitle("Outro " + spread);
                } else {
                    if (!mConfig.hasIntro()) {
                        for (int i = 0; i < currentPages.length; i++) {
                            currentPages[i] = currentPages[i] + 1;
                        }
                    }
                    String pages = String.format("page[ %s / %s ]",
                            TextUtils.join("-", currentPages),
                            mConfig.getPublicationPageCount());
                    setTitle(String.format("%s %s %s",
                            mConfig.getCatalog().getBranding().getName(), spread, pages));

                }
            }

            @Override
            public void onPagesChanged(int currentPosition, int[] currentPages, int previousPosition, int[] previousPages) {
            }

            @Override
            public void onVisiblePageIndexesChanged(int[] pages, int[] added, int[] removed) {

            }
        });

    }

    static class CatConfig extends CatalogConfiguration {

        public CatConfig(Catalog catalog) {
            super(catalog);
        }

        @Override
        public boolean hasIntro() {
            return true;
        }

        @Override
        public View getIntro(ViewGroup container, int page) {
            TextView tv = getTextView(container);
            tv.setText("IntroView for:\n" + getCatalog().getBranding().getName());
            return tv;
        }

        @Override
        public boolean hasOutro() {
            return true;
        }

        @Override
        public View getOutro(ViewGroup container, int page) {
            TextView tv = getTextView(container);
            tv.setText("Outro for:\n" + getCatalog().getBranding().getName());
            return tv;
        }

        private TextView getTextView(ViewGroup container) {
            TextView tv = new MyTextView(container.getContext());
            tv.setBackgroundColor(Color.YELLOW);
            tv.setTextColor(Color.BLACK);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(30f);
            return tv;
        }

    }

    static class MyTextView extends TextView implements VersoPageView {

        public MyTextView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
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

}
