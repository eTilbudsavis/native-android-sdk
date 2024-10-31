package com.tjek.sdk.publicationviewer.paged
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import com.tjek.sdk.R
import com.tjek.sdk.DeviceOrientation
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.models.BrandingV2
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.eventstracker.TjekEventsTracker
import com.tjek.sdk.eventstracker.pagedPublicationOpened
import com.tjek.sdk.eventstracker.pagedPublicationPageOpened
import com.tjek.sdk.getColorInt
import com.tjek.sdk.getDeviceOrientation
import com.tjek.sdk.publicationviewer.LoaderAndErrorScreenCallback
import com.tjek.sdk.publicationviewer.PublicationLoadingState
import com.tjek.sdk.publicationviewer.getDefaultErrorScreen
import com.tjek.sdk.publicationviewer.getDefaultLoadingScreen
import com.tjek.sdk.publicationviewer.paged.layouts.PublicationSpreadLayout
import com.tjek.sdk.publicationviewer.paged.libs.verso.*
import com.tjek.sdk.publicationviewer.paged.libs.verso.viewpager.CenteredViewPager

class PagedPublicationFragment :
    VersoFragment(),
    VersoPageViewListener.EventListener,
    VersoPageViewListener.OnLoadCompleteListener,
    CenteredViewPager.OnPageChangeListener,
    VersoPageChangeListener {

    private val viewModel: PagedPublicationViewModel by viewModels()
    private lateinit var config: PagedPublicationConfiguration
    private var hasSentOpenEvent: Boolean = false

    private lateinit var frame: FrameLayout
    private var frameVerso: FrameLayout? = null
    private var frameLoader: FrameLayout? = null
    private var frameError: FrameLayout? = null
    private lateinit var viewPager: VersoViewPager

    private var loadCompleteListener: OnLoadComplete? = null
    private var hotspotTapListener: OnHotspotTapListener? = null
    private var pageNumberChangeListener: OnPageNumberChangeListener? = null
    private var customScreenCallback: LoaderAndErrorScreenCallback? = null

    companion object {
        private const val arg_config = "arg_config"
        private const val arg_publication = "arg_publication"
        private const val arg_publication_id = "arg_publication_id"
        private const val saved_state = "saved_state"

        fun newInstance(
            publicationId: Id,
            configuration: PagedPublicationConfiguration
        ): PagedPublicationFragment {
            return createInstance(Bundle().apply { putString(arg_publication_id, publicationId) }, configuration)
        }

        fun newInstance(
            publication: PublicationV2,
            configuration: PagedPublicationConfiguration
        ): PagedPublicationFragment {
            return createInstance(Bundle().apply { putParcelable(arg_publication, publication) }, configuration)
        }

        private fun createInstance(args: Bundle, configuration: PagedPublicationConfiguration): PagedPublicationFragment {
            return PagedPublicationFragment().apply {
                arguments = args.also {
                    it.putParcelable(arg_config, configuration)
                }
            }
        }
    }

    fun setOnLoadCompleteListener(listener: OnLoadComplete) {
        loadCompleteListener = listener
    }

    fun setOnHotspotTapListener(listener: OnHotspotTapListener) {
        hotspotTapListener = listener
    }

    fun setOnPageChangeListener(listener: OnPageNumberChangeListener) {
        pageNumberChangeListener = listener
    }

    fun setCustomScreenCallback(callback: LoaderAndErrorScreenCallback) {
        customScreenCallback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            savedInstanceState.getParcelable<PagedPublicationSavedState>(saved_state)?.let { state ->
                config = state.config
                hasSentOpenEvent = state.hasSentOpenEvent

                arguments?.let {
                    config = it.getParcelable(arg_config)!!
                    setPage(config.initialPageNumber)
                    val publication: PublicationV2? = it.getParcelable(arg_publication)
                    if (publication != null) {
                        viewModel.loadPublication(publication)
                    } else {
                        viewModel.loadPublication(it.getString(arg_publication_id, ""))
                    }
                }

            }
        } else {
            arguments?.let {
                config = it.getParcelable(arg_config)!!
                setPage(config.initialPageNumber)
                val publication: PublicationV2? = it.getParcelable(arg_publication)
                if (publication != null) {
                    viewModel.loadPublication(publication)
                } else {
                    viewModel.loadPublication(it.getString(arg_publication_id, ""))
                }
            }
        }
        viewModel.loadingState.observe(this) { state ->
            when (state) {
                is PublicationLoadingState.Failed -> showError(state.error)
                PublicationLoadingState.Loading -> showLoader()
                PublicationLoadingState.Successful -> {
                    setVersoSpreadConfiguration()
                    showVersoView()
                    notifyVersoConfigurationChanged()
                }
            }
        }

        // Forward loading of the data to host app if interested
        viewModel.publication.observe(this) {
            applyBranding(it.branding)
            loadCompleteListener?.onPublicationLoaded(it)
        }
        viewModel.pages.observe(this) { loadCompleteListener?.onPagesLoaded(it) }
        viewModel.hotspots.observe(this) { loadCompleteListener?.onHotspotLoaded(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewPager = super.onCreateView(inflater, container, savedInstanceState) as VersoViewPager
        frame = inflater.inflate(R.layout.tjek_sdk_paged_publication, container, false) as FrameLayout
        frameVerso = frame.findViewById(R.id.verso) as FrameLayout?
        frameError = frame.findViewById(R.id.paged_error) as FrameLayout?
        frameLoader = frame.findViewById(R.id.paged_loader) as FrameLayout?
        setVisible(verso = false, loader = false, error = false)

        setOnEventListener(this)
        setOnLoadCompleteListener(this)
        viewPager.addOnPageChangeListener(this)
        addOnPageChangeListener(this)

        return  frame
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!hasSentOpenEvent) {
            // look for the publication id in the arguments (the whole publication or just the id)
            arguments?.let { bundle ->
                val publication: PublicationV2? = bundle.getParcelable(arg_publication)
                val pubId = publication?.id ?: bundle.getString(arg_publication_id, null)
                pubId?.let {
                    TjekEventsTracker.track(pagedPublicationOpened(it))
                    hasSentOpenEvent = true
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(saved_state, PagedPublicationSavedState(
            config = config,
            hasSentOpenEvent = hasSentOpenEvent
        ))
        super.onSaveInstanceState(outState)
    }

    private fun setVersoSpreadConfiguration() {
        var pageCount = viewModel.publication.value?.pageCount ?: 0
        var spreadCount = when (resources.configuration.getDeviceOrientation()) {
            DeviceOrientation.Landscape -> (pageCount / 2) + 1
            else -> pageCount
        }
        if (config.hasOutro) {
            pageCount++
            spreadCount++
        }
        versoSpreadConfiguration = SpreadConfiguration(
            pageCount,
            spreadCount,
            spreadMargin = 0,
            outroViewGenerator = config.outroViewGenerator?.also { it.publication = viewModel.publication.value },
            pages = viewModel.pages.value,
            publicationBrandingColor = viewModel.publication.value?.branding?.colorHex.getColorInt(),
            deviceConfiguration = resources.configuration,
            showPageNumberWhileLoading = config.showPageNumberWhileLoading
        )
    }

    private fun applyBranding(branding: BrandingV2) {
        if (!config.useBrandColor) return
        val bgColor = branding.colorHex.getColorInt()
        if (::frame.isInitialized) {
            frame.setBackgroundColor(bgColor)
        }
    }

    private fun showVersoView() {
        frameVerso?.let {
            if (it.visibility != View.VISIBLE) {
                it.removeAllViews()
                it.addView(viewPager)
                setVisible(verso = true, loader = false, error = false)
            }
        }

    }

    private fun showLoader() {
        val view =
            customScreenCallback?.showLoaderScreen(viewModel.publication.value?.branding) ?:
            getDefaultLoadingScreen(layoutInflater = layoutInflater,
                branding = if (config.useBrandColor) viewModel.publication.value?.branding else null)
        frameLoader?.removeAllViews()
        frameLoader?.addView(view)
        setVisible(verso = false, loader = true, error = false)
    }

    private fun showError(error: ResponseType.Error) {
        val view =
            customScreenCallback?.showErrorScreen(viewModel.publication.value?.branding, error) ?:
            getDefaultErrorScreen(layoutInflater = layoutInflater,
                branding = if (config.useBrandColor) viewModel.publication.value?.branding else null,
                error = error)
        frameError?.removeAllViews()
        frameError?.addView(view)
        setVisible(verso = false, loader = false, error = true)
    }

    private fun setVisible(verso: Boolean, loader: Boolean, error: Boolean) {
        frameVerso?.visibility = if (verso) View.VISIBLE else View.GONE
        frameLoader?.visibility = if (loader) View.VISIBLE else View.GONE
        frameError?.visibility = if (error) View.VISIBLE else View.GONE
    }

    override fun onVersoPageViewEvent(event: VersoPageViewEvent): Boolean {
        return when (event) {
            is VersoPageViewEvent.Tap -> showHotspotAndNotifyListener(event.info, longPress = false)
            is VersoPageViewEvent.LongTap -> showHotspotAndNotifyListener(event.info, longPress = true)
            is VersoPageViewEvent.Touch -> {
                if (event.action == MotionEvent.ACTION_UP) {
                    event.info.fragment.spreadOverlay?.let { view ->
                        if (view is PublicationSpreadLayout) {
                            // the longTap event keep showing the hotspot until the finger goes up, so let's remove all views if any
                            view.removeHotspots()
                        }
                    }
                }
                true
            }
            else -> false
        }
    }

    // coming from the underlying Verso fragment
    override fun onPageLoadComplete(success: Boolean, versoPageView: VersoPageView?) {
        if (!success) return
        loadCompleteListener?.let { l ->
            versoPageView?.let { l.onPageLoad(it.page) }
        }
    }

    private fun showHotspotAndNotifyListener(info: VersoTapInfo, longPress: Boolean): Boolean {
        val hs = viewModel.findHotspot(info)

        // Notify listener
        if (hs.isNotEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (longPress) {
                        hotspotTapListener?.onHotspotLongTap(hs)
                    } else {
                        hotspotTapListener?.onHotspotTap(hs)
                    }

                }, 100)
        }

        // Show hotspot
        if (!config.displayHotspotsOnTouch) return false
        if (hs.isNotEmpty()) {
            info.fragment.spreadOverlay?.let { view ->
                if (view is PublicationSpreadLayout) {
                    view.showHotspots(hs, longPress)
                }
            }
        }
        return true
    }

    // Use this callback to show page numbers because it's more reactive than other callbacks,
    // so the page numbers will be updated faster in the UI (if any)
    override fun onPageSelected(position: Int) {
        pageNumberChangeListener?.let {
            val lastSpread = if (config.hasOutro) versoSpreadConfiguration.spreadCount - 1 else versoSpreadConfiguration.spreadCount
            if (position < lastSpread) {
                val currentPages = versoSpreadConfiguration.getPagesFromSpreadPosition(position)
                val tmpPages: IntArray = currentPages.copyOf(currentPages.size)
                for (i in tmpPages.indices) {
                    tmpPages[i] += 1
                }
                // make sure the first value is not 0
                tmpPages[0] = tmpPages[0].coerceAtLeast(1)
                val totalPages = if (config.hasOutro) versoSpreadConfiguration.pageCount - 1 else versoSpreadConfiguration.pageCount
                it.onPageNumberChange(currentPages = tmpPages, totalPages = totalPages)
            }
        }
    }

    // This callback is not triggered if the swipe is fast, so it's better for tracking the page open event
    // so we won't record the pages swiped away immediately but only the one
    override fun onPagesChanged(currentPosition: Int, currentPages: IntArray?, previousPosition: Int, previousPages: IntArray?) {
        currentPages?.forEach { p ->
            val page = p + 1 // it starts from 0
            val lastPage = if (config.hasOutro) versoSpreadConfiguration.pageCount - 1 else versoSpreadConfiguration.pageCount
            if (page <= lastPage) {
                viewModel.publication.value?.id?.let {
                    TjekEventsTracker.track(pagedPublicationPageOpened(
                        publicationId = it,
                        pageNumber = page
                    ))
                }
            }
        }
    }



    // Unused callbacks from Verso fragment and CenteredViewPager
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPagesScrolled(currentPosition: Int, currentPages: IntArray?, previousPosition: Int, previousPages: IntArray?) {}
    override fun onVisiblePageIndexesChanged(pages: IntArray?, added: IntArray?, removed: IntArray?) {}
}