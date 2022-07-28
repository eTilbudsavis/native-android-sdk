package com.tjek.sdk.publicationviewer.paged

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import com.shopgun.android.sdk.R
import com.tjek.sdk.DeviceOrientation
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.ErrorType
import com.tjek.sdk.api.remote.models.v2.BrandingV2
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
    CenteredViewPager.OnPageChangeListener
{

    private val viewModel: PagedPublicationViewModel by viewModels()
    private lateinit var ppConfig: PagedPublicationConfiguration
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
                ppConfig = state.config
                hasSentOpenEvent = state.hasSentOpenEvent
            }
        } else {
            arguments?.let {
                ppConfig = it.getParcelable(arg_config)!!
                setPage(ppConfig.initialPageNumber)
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
                is PublicationLoadingState.Failed -> {
                    loadCompleteListener?.onError(state.error)
                    showError(state.error)
                }
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

        return  frame
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(saved_state, PagedPublicationSavedState(
            config = ppConfig,
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

        if (ppConfig.hasIntro) {
            pageCount++
            spreadCount++
        }
        if (ppConfig.hasOutro) {
            pageCount++
            spreadCount++
        }
        versoSpreadConfiguration = SpreadConfiguration(
            pageCount,
            spreadCount,
            spreadMargin = 0,
            introConfiguration = ppConfig.introConfiguration?.also { it.publication = viewModel.publication.value },
            outroConfiguration = ppConfig.outroConfiguration?.also { it.publication = viewModel.publication.value },
            pages = viewModel.pages.value,
            publicationBrandingColor = viewModel.publication.value?.branding?.colorHex.getColorInt(),
            deviceConfiguration = resources.configuration,
            showPageNumberWhileLoading = ppConfig.showPageNumberWhileLoading
        )
    }

    private fun applyBranding(branding: BrandingV2) {
        if (!ppConfig.useBrandColor) return
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
                // todo onPageChangeListener
                setVisible(verso = true, loader = false, error = false)
            }
        }

    }

    private fun showLoader() {
        val view =
            customScreenCallback?.showLoaderScreen(viewModel.publication.value?.branding) ?:
            getDefaultLoadingScreen(layoutInflater, viewModel.publication.value?.branding)
        frameLoader?.removeAllViews()
        frameLoader?.addView(view)
        setVisible(verso = false, loader = true, error = false)
    }

    private fun showError(error: ErrorType) {
        val view =
            customScreenCallback?.showErrorScreen(viewModel.publication.value?.branding, error) ?:
            getDefaultErrorScreen(layoutInflater, viewModel.publication.value?.branding, error)
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
        val hs = viewModel.findHotspot(info, ppConfig.hasIntro)

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
        if (!ppConfig.displayHotspotsOnTouch) return false
        if (hs.isNotEmpty()) {
            info.fragment.spreadOverlay?.let { view ->
                if (view is PublicationSpreadLayout) {
                    view.showHotspots(hs, longPress)
                }
            }
        }
        return true
    }

    override fun onPageSelected(position: Int) {
        pageNumberChangeListener?.let {
            if (position < versoSpreadConfiguration.spreadCount - 1) {
                val currentPages = versoSpreadConfiguration.getPagesFromSpreadPosition(position)
                val tmpPages: IntArray = currentPages.copyOf(currentPages.size)
                if (!ppConfig.hasIntro) {
                    for (i in tmpPages.indices) {
                        tmpPages[i] += 1
                    }
                }
                it.onPageNumberChange(currentPages = tmpPages, totalPages = versoSpreadConfiguration.pageCount - 1)
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}
}