package com.tjek.sdk.publicationviewer.paged

import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import com.tjek.sdk.DeviceOrientation
import com.tjek.sdk.api.models.PublicationPageV2
import com.tjek.sdk.getDeviceOrientation
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoSpreadConfiguration
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoSpreadProperty

internal class SpreadConfiguration(
    override val pageCount: Int,
    override val spreadCount: Int,
    override val spreadMargin: Int,
    private val pages: List<PublicationPageV2>?,
    private val publicationBrandingColor: Int,
    private val hasIntro: Boolean = false,
    private val hasOutro: Boolean = false,
    private val introView: View? = null,
    private val outroView: View? = null
) : VersoSpreadConfiguration {

    private var orientation = DeviceOrientation.Portrait

    override fun getPageView(container: ViewGroup, page: Int): View? {
        return when {
            hasIntro && page == 0 -> introView
            hasOutro && page == pageCount - 1 -> outroView
            else -> {
                // Offset the page by one if there is an intro to get
                // the publicationPage, rather than the verso page
                val publicationPage = if (hasIntro) page - 1 else page
                getPublicationPageView(container, publicationPage)
            }
        }
    }

    private fun getPublicationPageView(container: ViewGroup, publicationPage: Int): View {
        val page = publicationPage.coerceIn(0, (pages?.size?.minus(1))?.coerceAtLeast(0))
        return PageView(container.context, pages?.get(page), publicationBrandingColor)
    }

    override fun getSpreadOverlay(
        container: ViewGroup,
        pages: IntArray
    ): View? {
        val position = getSpreadPositionFromPage(pages[0])
        return when {
            hasIntro && position == 0 -> null
            hasOutro && position == spreadCount - 1 -> null
            else -> PublicationSpreadLayout(container.context, fixPages(pages))
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        orientation = newConfig.getDeviceOrientation()
    }

    override fun getSpreadProperty(spreadPosition: Int): VersoSpreadProperty {
        val p = getPagesFromSpreadPosition(spreadPosition)
        return when {
            hasIntro && spreadPosition == 0 -> VersoSpreadProperty(
                pages = p, width = 0.6f, maxZoomScale = 1f, minZoomScale = 1f
            )
            hasOutro && spreadPosition == spreadCount - 1 -> VersoSpreadProperty(
                pages = p, width = 0.8f, maxZoomScale = 1f, minZoomScale = 1f
            )
            else -> VersoSpreadProperty(
                pages = p, width = 1f, minZoomScale = 1f, maxZoomScale = 3f
            )
        }
    }

    override fun getSpreadPositionFromPage(page: Int): Int {
        return when {
            orientation == DeviceOrientation.Portrait -> page
            page == 0 || (hasIntro && page == 1) -> page
            hasOutro && page == pageCount - 1 -> spreadCount - 1
            hasIntro -> ((page - (page % 2)) / 2) + 1
            else -> ((page - 1) / 2) + 1
        }
    }

    override fun getPagesFromSpreadPosition(spreadPosition: Int): IntArray {
        if (orientation == DeviceOrientation.Portrait)
            return intArrayOf(spreadPosition)

        if (spreadPosition == 0 || (hasIntro && spreadPosition == 1)) {
            // either intro or first page of publication
            return intArrayOf(spreadPosition)
        }

        var page: Int = if (hasIntro) (spreadPosition - 1) * 2 else (spreadPosition * 2) - 1
        if (hasOutro && spreadPosition == spreadCount - 1 && !missingLastPage()) {
            page--
        }

        var lastDoublePage: Int = spreadCount - 1
        lastDoublePage -= if (hasOutro) 2 else 1
        if (missingLastPage()) {
            lastDoublePage++
        }
        val isSinglePage = spreadPosition > lastDoublePage
        return if (isSinglePage) intArrayOf(page) else intArrayOf(page, page + 1)
    }

    override fun hasData(): Boolean {
        return pages != null
    }

    private fun fixPages(pages: IntArray): IntArray {
        val tmp = pages.copyOf(pages.size)
        if (hasIntro) {
            for (i in tmp.indices) {
                tmp[i] -= 1
            }
        }
        return tmp
    }

    private fun missingLastPage(): Boolean {
        return pageCount % 2 != 0
    }
}