package com.tjek.sdk.publicationviewer.paged

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import com.shopgun.android.sdk.R
import com.tjek.sdk.DeviceOrientation
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.getColorFromHexStr
import com.tjek.sdk.getDeviceOrientation
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoFragment
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoPageViewEvent
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoPageViewListener
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoViewPager
import com.tjek.sdk.publicationviewer.paged.layouts.PublicationSpreadLayout

class PagedPublicationFragment : VersoFragment(), VersoPageViewListener.EventListener {

    private val viewModel: PagedPublicationViewModel by viewModels()
    private lateinit var ppConfig: PagedPublicationConfiguration
    private var hasSentOpenEvent: Boolean = false

    private lateinit var frame: FrameLayout
    private var frameVerso: FrameLayout? = null
    private var frameLoader: FrameLayout? = null
    private var frameError: FrameLayout? = null
    private lateinit var viewPager: VersoViewPager

    companion object {
        private const val arg_config = "arg_config"
        private const val arg_page = "arg_page"
        private const val arg_publication = "arg_publication"
        private const val arg_publication_id = "arg_publication_id"
        private const val saved_state = "saved_state"

        fun newInstance(
            publicationId: Id,
            configuration: PagedPublicationConfiguration,
            page: Int = 0
        ): PagedPublicationFragment {
            return createInstance(Bundle().apply { putString(arg_publication_id, publicationId) }, configuration, page)
        }

        fun newInstance(
            publication: PublicationV2,
            configuration: PagedPublicationConfiguration,
            page: Int = 0
        ): PagedPublicationFragment {
            return createInstance(Bundle().apply { putParcelable(arg_publication, publication) }, configuration, page)
        }

        private fun createInstance(args: Bundle, configuration: PagedPublicationConfiguration, page: Int = 0): PagedPublicationFragment {
            return PagedPublicationFragment().apply {
                arguments = args.also {
                    it.putParcelable(arg_config, configuration)
                    it.putInt(arg_page, page)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            savedInstanceState.getParcelable<PublicationSavedState>(saved_state)?.let { state ->
                ppConfig = state.config
                hasSentOpenEvent = state.hasSentOpenEvent
            }
        } else {
            arguments?.let {
                ppConfig = it.getParcelable(arg_config)!!
                setPage(it.getInt(arg_page, 0))
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
                is PublicationLoadingState.Failed -> showErrorView()
                PublicationLoadingState.Loading -> showLoaderView()
                PublicationLoadingState.Successful -> {
                    setVersoSpreadConfiguration()
                    showVersoView()
                    notifyVersoConfigurationChanged()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewPager = super.onCreateView(inflater, container, savedInstanceState) as VersoViewPager
        frame = inflater.inflate(R.layout.tjek_sdk_pagedpublication, container, false) as FrameLayout
        frameVerso = frame.findViewById(R.id.verso) as FrameLayout?
        frameError = frame.findViewById(R.id.error) as FrameLayout?
        frameLoader = frame.findViewById(R.id.loader) as FrameLayout?
        setVisible(verso = false, loader = false, error = false)

        setOnEventListener(this)

        return  frame
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(saved_state, PublicationSavedState(
            config = ppConfig,
            hasSentOpenEvent = hasSentOpenEvent
        ))
        super.onSaveInstanceState(outState)
    }

    private fun setVersoSpreadConfiguration() {
        var pageCount = viewModel.publication?.pageCount ?: 0
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
            introConfiguration = ppConfig.introConfiguration?.also { it.publication = viewModel.publication },
            outroConfiguration = ppConfig.outroConfiguration?.also { it.publication = viewModel.publication },
            pages = viewModel.pages,
            publicationBrandingColor = viewModel.publication?.branding?.colorHex.getColorFromHexStr(),
            deviceConfiguration = resources.configuration
        )
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

    private fun showLoaderView() {
        frameLoader?.let {
            if (it.visibility != View.VISIBLE) {
                setVisible(verso = false, loader = true, error = false)
            }
        }
    }

    private fun showErrorView() {
        frameError?.let {
            if (it.visibility != View.VISIBLE) {
                it.removeAllViews()
                it.addView(getErrorView(frame))
                setVisible(verso = false, loader = false, error = true)
            }
        }
    }

    fun getErrorView(container: ViewGroup): View? {
        val i = LayoutInflater.from(container.context)
        val v = i.inflate(R.layout.tjek_sdk_pagedpublication_error, container, false)
        return v
    }

    private fun setVisible(verso: Boolean, loader: Boolean, error: Boolean) {
        frameVerso?.visibility = if (verso) View.VISIBLE else View.GONE
        frameLoader?.visibility = if (loader) View.VISIBLE else View.GONE
        frameError?.visibility = if (error) View.VISIBLE else View.GONE
    }

    override fun onVersoPageViewEvent(event: VersoPageViewEvent): Boolean {
        return when (event) {
            is VersoPageViewEvent.Tap -> {
                if (!ppConfig.displayHotspotsOnTouch) return false
                val hs = viewModel.findHotspot(event.info, ppConfig.hasIntro)
                if (hs.isNotEmpty()) {
                    event.info.fragment.spreadOverlay?.let { view ->
                        if (view is PublicationSpreadLayout) {
                            view.showHotspots(hs)
                        }
                    }
                }
                true
            }
            else -> false
        }
    }
}