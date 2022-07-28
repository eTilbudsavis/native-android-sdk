package com.shopgun.android.sdk.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.shopgun.android.sdk.demo.base.BaseActivity
import com.tjek.sdk.api.models.PublicationHotspotV2
import com.tjek.sdk.api.models.PublicationPageV2
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.ErrorType
import com.tjek.sdk.publicationviewer.paged.*
import com.tjek.sdk.publicationviewer.paged.PagedPublicationFragment.Companion.newInstance

class PagedPublicationActivity : BaseActivity() {

    private var mPagedPublicationFragment: PagedPublicationFragment? = null
    private lateinit var pageTV: TextView

    private var publicationConfiguration = PagedPublicationConfiguration(
        outroConfiguration = OutroConfig()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paged_publication_layout)

        pageTV = findViewById(R.id.pageTV)

        mPagedPublicationFragment =
            supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as PagedPublicationFragment?

        if (mPagedPublicationFragment == null) {
            val publication: PublicationV2 = intent.extras!!.getParcelable(KEY_PUB)!!
            mPagedPublicationFragment = newInstance(
                publication,
                publicationConfiguration
            )
            supportFragmentManager
                .beginTransaction()
                .add(R.id.pagedPublication, mPagedPublicationFragment!!, FRAGMENT_TAG)
                .commit()

            // Note: the initial count needs to be set up here, you won't receive any event (that is triggered when the page changes).
            // It's up to you pass in the correct initial page number
            pageTV.text = "${publicationConfiguration.initialPageNumber + 1} / ${publication.pageCount}"
        }
        addPagedPublicationListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mPagedPublicationFragment?.clearAdapter()
        super.onSaveInstanceState(outState)
    }

    private fun addPagedPublicationListeners() {
        mPagedPublicationFragment?.setOnPageChangeListener(object : OnPageNumberChangeListener {
            override fun onPageNumberChange(currentPages: IntArray, totalPages: Int) {
                pageTV.text = "${currentPages.joinToString(" - ")} / $totalPages"
            }
        })
        mPagedPublicationFragment?.setOnHotspotTapListener(object : OnHotspotTapListener {
            override fun onHotspotTap(hotspots: List<PublicationHotspotV2>) {
                val sb = StringBuilder()
                for (h in hotspots) {
                    if (sb.isNotEmpty()) {
                        sb.append("\n")
                    }
                    h.offer?.let {
                        sb.append(it.heading)
                    }
                }
                Toast.makeText(this@PagedPublicationActivity, sb.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onHotspotLongTap(hotspots: List<PublicationHotspotV2>) {}
        })

        mPagedPublicationFragment?.setOnLoadCompleteListener(object : OnLoadComplete {
            override fun onPublicationLoaded(publication: PublicationV2) {
                Log.d(TAG, "publication loaded ${publication.id}")
            }

            override fun onPagesLoaded(pages: List<PublicationPageV2>) {
                Log.d(TAG, "pages loaded ${pages.size}")
            }

            override fun onPageLoad(page: Int) {
                Log.d(TAG, "page loaded $page")
            }

            override fun onHotspotLoaded(hotspots: List<PublicationHotspotV2>) {
                Log.d(TAG, "hotspot loaded ${hotspots.size}")
            }

            override fun onError(error: ErrorType) {}
        })
    }

    companion object {
        val TAG = PagedPublicationActivity::class.java.simpleName
        const val FRAGMENT_TAG = "publication-fragment"
        private const val KEY_PUB = "PUBLICATION"
        fun start(context: Context, publication: PublicationV2?) {
            val i = Intent(context, PagedPublicationActivity::class.java)
            i.putExtra(KEY_PUB, publication)
            context.startActivity(i)
        }
    }
}