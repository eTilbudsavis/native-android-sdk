package com.tjek.sdk.publicationviewer.incito

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shopgun.android.sdk.BuildConfig
import com.shopgun.android.sdk.R
import com.tjek.sdk.DeviceOrientation
import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.ErrorType
import com.tjek.sdk.api.remote.models.v4.FeatureLabel
import com.tjek.sdk.api.remote.models.v4.IncitoData
import com.tjek.sdk.api.remote.models.v4.IncitoDeviceCategory
import com.tjek.sdk.api.remote.models.v4.IncitoOrientation
import com.tjek.sdk.getDeviceOrientation
import com.tjek.sdk.getFormattedLocale
import com.tjek.sdk.publicationviewer.LoaderAndErrorScreenCallback
import com.tjek.sdk.publicationviewer.getDefaultErrorScreen
import com.tjek.sdk.publicationviewer.getDefaultLoadingScreen
import com.tjek.sdk.publicationviewer.paged.PublicationLoadingState
import org.json.JSONArray
import kotlin.math.round
import kotlin.math.roundToInt

class IncitoPublicationFragment :
    Fragment(),
    View.OnTouchListener,
    View.OnLongClickListener
{

    companion object {
        private const val MAX_WIDTH_MINIMUM_VALUE = 100
        private const val LOCAL_HTML_RENDERER = "file:///android_asset/incito/webview/index-1.0.0.html"
        private const val REMOTE_HTML_RENDERER = "https://incito-webview.shopgun.com/index-1.0.0.html"
        var LOAD_LOCAL_RENDERER = false // for debug purposes

        const val HAS_SENT_OPEN_EVENT = "has_sent_open_event"

        private const val arg_config = "arg_config"
        private const val arg_publication = "arg_publication"
        private const val arg_publication_id = "arg_publication_id"

        fun newInstance(
            publicationId: Id,
            configuration: IncitoPublicationConfiguration
        ): IncitoPublicationFragment {
            return createInstance(Bundle().apply { putString(arg_publication_id, publicationId) }, configuration)
        }

        fun newInstance(
            publication: PublicationV2,
            configuration: IncitoPublicationConfiguration
        ): IncitoPublicationFragment {
            return createInstance(Bundle().apply { putParcelable(arg_publication, publication) }, configuration)
        }

        private fun createInstance(args: Bundle, configuration: IncitoPublicationConfiguration): IncitoPublicationFragment {
            return IncitoPublicationFragment().apply {
                arguments = args.also {
                    it.putParcelable(arg_config, configuration)
                }
            }
        }
    }

    private val viewModel: IncitoPublicationViewModel by viewModels()
    private lateinit var config: IncitoPublicationConfiguration
    private var eventListener: IncitoEventListener? = null
    private var customScreenCallback: LoaderAndErrorScreenCallback? = null

    private var yOffset = 0
    private var openAtViewWithId: String? = null
    private var recordedFeatureLabel: List<String>? = null
    private var fragmentView: View? = null
    private var incitoWebView: WebView? = null
    private var hasSentOpenEvent = false
    private var errorFrame: FrameLayout? = null
    private var loaderFrame: FrameLayout? = null


    // for the long click, store the last coordinates from onTouch and then call
    // the javascript function that returns the viewId
    private val lastTouchDownXY = FloatArray(2)
    private var density = 0f

    // used to check if everything is in place to show the offer search dialog
    // (all offers parsed and incito rendered)
    private var allRendered = false

    private var isRendererLoaded = false
    private var initCalled = false

    private val pixelRatio: Float
        get() {
            return resources.displayMetrics.density
        }

    private val screenWidth: Int
        get() {
            val configuration = context?.resources?.configuration
            return configuration?.screenWidthDp?.takeIf { it > MAX_WIDTH_MINIMUM_VALUE } ?: MAX_WIDTH_MINIMUM_VALUE
        }

    private val deviceCategory: IncitoDeviceCategory
        get() {
            // based on https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes#smallest-width
            return when (resources.configuration.smallestScreenWidthDp) {
                in 0 until 600 -> IncitoDeviceCategory.mobile
                in 600 until 960 -> IncitoDeviceCategory.tablet
                else -> IncitoDeviceCategory.desktop
            }
        }

    private val orientation: IncitoOrientation
        get() {
            return when (resources.configuration.getDeviceOrientation()) {
                DeviceOrientation.Portrait -> IncitoOrientation.vertical
                DeviceOrientation.Landscape -> IncitoOrientation.horizontal
            }
        }

    private val featureLabels: List<FeatureLabel>?
        get() {
            val total = recordedFeatureLabel?.size ?: 0
            if (total > 0) {
                val fl = mutableListOf<FeatureLabel>()
                val counts = mutableMapOf<String, Int>()
                recordedFeatureLabel?.forEach{
                    val value = if (counts.containsKey(it)) counts[it] else 0
                    counts[it] = value!! + 1
                }
                counts.map { entry ->
                    val avg = entry.value.toFloat() / total
                    val roundedAvg = round(avg * 100) / 100
                    fl.add(FeatureLabel(entry.key, roundedAvg.takeIf { it > 0 } ?: 0F))
                }
                return fl
            }
            return null
        }

    fun setIncitoEventListener(listener: IncitoEventListener) {
        eventListener = listener
    }

    fun setCustomScreenCallback(callback: LoaderAndErrorScreenCallback) {
        customScreenCallback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            // TODO savedState
            hasSentOpenEvent = savedInstanceState.getBoolean(HAS_SENT_OPEN_EVENT)
        } else {
            arguments?.let {
                config = it.getParcelable(arg_config)!!
                yOffset = config.initialVerticalOffset
                openAtViewWithId = config.openAtViewWithId
                recordedFeatureLabel = config.recordedFeatureLabel
                val publication: PublicationV2? = it.getParcelable(arg_publication)
                if (publication != null) {
                    viewModel.loadPublication(
                        publication = publication,
                        deviceCategory = deviceCategory,
                        orientation = orientation,
                        pixelRatio = pixelRatio,
                        maxWidth = screenWidth,
                        featureLabels = featureLabels,
                        locale = getFormattedLocale(requireContext())
                    )
                } else {
                    viewModel.loadPublication(
                        id = it.getString(arg_publication_id, ""),
                        deviceCategory = deviceCategory,
                        orientation = orientation,
                        pixelRatio = pixelRatio,
                        maxWidth = screenWidth,
                        featureLabels = featureLabels,
                        locale = getFormattedLocale(requireContext())
                    )
                }
            }
        }

        //Screen density
        density = resources.displayMetrics.density

        viewModel.incitoData.observe(this) { callJavascriptInit(it, isRendererLoaded) }
        viewModel.loadingState.observe(this) {
            when(it) {
                is PublicationLoadingState.Failed -> {
                    TjekLogCat.e(it.error.toString())
                    showError(it.error)
                }
                PublicationLoadingState.Loading -> showLoader()
                PublicationLoadingState.Successful -> {} // nothing to do here. The observer on the incito data will handle the init
            }
        }
    }

    private fun showLoader() {
        val view =
            customScreenCallback?.showLoaderScreen(viewModel.publication.value?.branding) ?:
            getDefaultLoadingScreen(layoutInflater, viewModel.publication.value?.branding)
        loaderFrame?.removeAllViews()
        loaderFrame?.addView(view)
        setVisible(webview = false, loader = true, error = false)
    }

    private fun showError(error: ErrorType) {
        val view =
            customScreenCallback?.showErrorScreen(viewModel.publication.value?.branding, error) ?:
            getDefaultErrorScreen(layoutInflater, viewModel.publication.value?.branding, error)
        errorFrame?.removeAllViews()
        errorFrame?.addView(view)
        setVisible(webview = false, loader = false, error = true)
    }

    private fun setVisible(webview: Boolean, loader: Boolean, error: Boolean) {
        incitoWebView?.visibility = if (webview) View.VISIBLE else View.GONE
        loaderFrame?.visibility = if (loader) View.VISIBLE else View.GONE
        errorFrame?.visibility = if (error) View.VISIBLE else View.GONE
    }

    private fun callJavascriptInit(incitoData: IncitoData?, rendererLoaded: Boolean) {
        if (!initCalled) {
            if(rendererLoaded && incitoData != null) {
                initCalled = true
                incitoWebView?.evaluateJavascript("javascript:init(${viewModel.incitoData.value})", null)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.tjek_sdk_incito_publication, container, false)
        }
        incitoWebView = fragmentView?.findViewById(R.id.incito_webview)
        errorFrame = fragmentView?.findViewById(R.id.incito_error)
        loaderFrame = fragmentView?.findViewById(R.id.incito_loader)
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        if (!hasSentOpenEvent) {
            //todo tracking
//            catalog?.let {
//                trackIncitoPublicationOpened(it.id ?: it.incitoId)
//                hasSentOpenEvent = true
//            }
        }

        initWebView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(HAS_SENT_OPEN_EVENT, hasSentOpenEvent)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        incitoWebView?.removeAllViews()
        incitoWebView?.destroy()
        incitoWebView = null
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface", "ClickableViewAccessibility")
    private fun initWebView() {
        // Enable Javascript
        val webSettings = incitoWebView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.displayZoomControls = false
        webSettings?.builtInZoomControls = false
        webSettings?.textZoom = 100
        incitoWebView?.addJavascriptInterface(this, "androidJSInterface")

        // Catch javascript console messages
        incitoWebView?.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                TjekLogCat.d("Webview console message\n${cm.message()} -- From line ${cm.lineNumber()} of ${cm.sourceId()}")
                return true
            }
        }

        // Long click behaviour
        incitoWebView?.setOnTouchListener(this)
        incitoWebView?.isLongClickable = true
        incitoWebView?.setOnLongClickListener(this)

        incitoWebView?.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                } else {
                    false
                }
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                TjekLogCat.d("onReceivedError received $failingUrl -> $errorCode: $description")
                checkFailingUrl(failingUrl)
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                TjekLogCat.d("onReceivedError received ${request.url} ->  ${error.description}")
                checkFailingUrl(request.url.toString())
            }

            private fun checkFailingUrl(url: String) {
                // if the error is regarding the remote renderer, load the local one
                if (url == REMOTE_HTML_RENDERER) {
                    TjekLogCat.d("load local renderer......")
                    incitoWebView?.loadUrl(LOCAL_HTML_RENDERER)
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                TjekLogCat.v("onPageFinished= $url")

                // if we finished loading the renderer, check if we already have the incito data
                if (url == REMOTE_HTML_RENDERER || url == LOCAL_HTML_RENDERER) {
                    isRendererLoaded = true
                    callJavascriptInit(viewModel.incitoData.value, isRendererLoaded)
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                // the renderer was killed or crashed, so let's try to reload the webview

                // destroy the current webview
                (fragmentView as FrameLayout).removeView(incitoWebView)
                incitoWebView?.destroy()

                // re-create it and add it
                incitoWebView = context?.let { WebView(it) }
                (fragmentView as FrameLayout).addView(incitoWebView, 0)

                showError(ErrorType.Unknown(message = "Error while loading webview"))
                return true // continue to execute the app
            }
        }

        // load the renderer only if it the first time we load the view
        if (!isRendererLoaded) {
            TjekLogCat.v("load remote renderer......")
            incitoWebView?.loadUrl(if (LOAD_LOCAL_RENDERER) LOCAL_HTML_RENDERER else REMOTE_HTML_RENDERER)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(webview: View, event: MotionEvent): Boolean {
        // save the X,Y coordinates
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            lastTouchDownXY[0] = event.x / density
            lastTouchDownXY[1] = event.y / density
        }

        // let the touch event pass on to whoever needs it
        return false
    }

    override fun onLongClick(webview: View): Boolean {
        // retrieve the stored coordinates
        val x = lastTouchDownXY[0]
        val y = lastTouchDownXY[1]
        incitoWebView?.evaluateJavascript("javascript:getElementIdsAtPoint($x,$y)") { viewIds: String? ->
            if (viewIds != null) {
                val jsonArray: JSONArray
                try {
                    jsonArray = JSONArray(viewIds)
                    for (i in 0 until jsonArray.length()) {
                        val incitoOffer = viewModel.getOfferFromMap(jsonArray.getString(i))
                        if (incitoOffer != null) {
                            eventListener?.onOfferLongClick(incitoOffer, viewModel.publication.value)
                            break
                        }
                    }
                } catch (e: Exception) {
                    TjekLogCat.printStackTrace(e)
                }
            }
        }
        return true
    }

    private fun dismissLoader() {
        setVisible(webview = true, loader = false, error = false)
    }


    /** Javascript Interface **********************************************************************/

    @JavascriptInterface
    fun viewClicked(viewIds: Array<String>?) {
        if (viewIds == null) {
            return
        }
        for (id in viewIds) {
            val incitoOffer = viewModel.getOfferFromMap(id)
            if (incitoOffer != null) {
                eventListener?.onOfferClick(incitoOffer, viewModel.publication.value)
                break
            }
        }
    }

    @JavascriptInterface
    fun progress(progress: Float, scrollOffset: Float) {
        yOffset = (scrollOffset * density).roundToInt()
        eventListener?.onProgressChanged(progress, yOffset)
    }

    @JavascriptInterface
    fun initDone() {
        TjekLogCat.v("init done: incito fully rendered")
        allRendered = true
        activity?.runOnUiThread {
            when {
                openAtViewWithId != null -> {
                    // call the javascript function to scroll to the given view
                    incitoWebView?.evaluateJavascript("javascript:goToView('$openAtViewWithId')", null)
                    // content is shown at top then it's moved to the right view.
                    // In order to have a smooth experience, delay the dismissLoader a little to show the right content immediately
                    Handler(Looper.getMainLooper()).postDelayed({ dismissLoader() }, 30)
                }
                else -> {
                    incitoWebView?.scrollTo(0, yOffset)
                    dismissLoader()
                }
            }
        }
    }

    /**********************************************************************************************/
}