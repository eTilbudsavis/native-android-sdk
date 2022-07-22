package com.shopgun.android.sdk.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;

import com.shopgun.android.sdk.demo.base.BaseActivity;
import com.tjek.sdk.api.models.PublicationHotspotV2;
import com.tjek.sdk.api.models.PublicationV2;
import com.tjek.sdk.publicationviewer.paged.IntroConfiguration;
import com.tjek.sdk.publicationviewer.paged.OnHotspotTapListener;
import com.tjek.sdk.publicationviewer.paged.OutroConfiguration;
import com.tjek.sdk.publicationviewer.paged.PagedPublicationConfiguration;
import com.tjek.sdk.publicationviewer.paged.PagedPublicationFragment;

import java.util.List;
import java.util.Locale;

public class PagedPublicationActivity extends BaseActivity {

    public static final String TAG = PagedPublicationActivity.class.getSimpleName();

    public static final String FRAGMENT_TAG = "publication-fragment";

    private static final String KEY_PUB = "PUBLICATION";

    private PagedPublicationFragment mPagedPublicationFragment;

    public static void start(Context context, PublicationV2 publication) {
        Intent i = new Intent(context, PagedPublicationActivity.class);
        i.putExtra(KEY_PUB, publication);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagedpublicationkit);

        mPagedPublicationFragment = (PagedPublicationFragment)
                getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (mPagedPublicationFragment == null) {
            PublicationV2 publication = getIntent().getExtras().getParcelable(KEY_PUB);
            mPagedPublicationFragment = PagedPublicationFragment.Companion.newInstance(
                    publication,
                    new PagedPublicationConfiguration(
                            true,
                            true,
                            null,
                            new OutroConfig()
                    ),
                    0);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.pagedPublication, mPagedPublicationFragment, FRAGMENT_TAG)
                    .commit();
        }

        addPagedPublicationListeners();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mPagedPublicationFragment.clearAdapter();
        super.onSaveInstanceState(outState);
    }

    private void addPagedPublicationListeners() {

//        mPagedPublicationFragment.addOnPageChangeListener(new VersoFragment.OnPageChangeListener() {
//            @Override
//            public void onPagesScrolled(int currentPosition, int[] currentPages, int previousPosition, int[] previousPages) {
//                L.d(TAG, String.format(Locale.US, "onPagesChanged[ currentPosition:%s, currentPages:%s, previousPosition:%s, previousPages:%s ]"
//                        , currentPosition, TextUtils.join(",", currentPages), previousPosition, TextUtils.join(",", previousPages)));
//                String name = getConfig().getCatalog().getBranding().getName();
//                String spread = String.format(" spread[ %s / %s ]", currentPosition+1, getConfig().getSpreadCount());
//
//                if (!getConfig().hasIntro()) {
//                    for (int i = 0; i < currentPages.length; i++) {
//                        currentPages[i] = currentPages[i] + 1;
//                    }
//                }
//                boolean isIntroOutro = (getConfig().hasIntro() && currentPosition == 0) ||
//                        (getConfig().hasOutro() && currentPosition == getConfig().getSpreadCount()-1);
//                String pages = isIntroOutro ? "" : String.format(" page[ %s / %s ]", TextUtils.join("-", currentPages), getConfig().getPublicationPageCount());
//                setTitle(name + spread + pages);
//            }
//
//            @Override
//            public void onPagesChanged(int currentPosition, int[] currentPages, int previousPosition, int[] previousPages) {
//            }
//
//            @Override
//            public void onVisiblePageIndexesChanged(int[] pages, int[] added, int[] removed) {
//
//            }
//        });
//
        mPagedPublicationFragment.setOnHotspotTapListener(new OnHotspotTapListener() {
            @Override
            public void onHotspotTap(@NonNull List<PublicationHotspotV2> hotspots) {
                StringBuilder sb = new StringBuilder();
                for (PublicationHotspotV2 h : hotspots) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    if (h.getOffer() != null) {
                        sb.append(h.getOffer().getHeading());
                    }
                }
                Toast.makeText(PagedPublicationActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onHotspotLongTap(@NonNull List<PublicationHotspotV2> hotspots) {

            }
        });

    }

}
