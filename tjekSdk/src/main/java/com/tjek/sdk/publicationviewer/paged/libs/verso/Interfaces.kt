package com.tjek.sdk.publicationviewer.paged.libs.verso

import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup

sealed interface VersoPageViewListener {

    interface EventListener {
        fun onVersoPageViewEvent(event: VersoPageViewEvent): Boolean
    }

    interface OnLoadCompleteListener {
        fun onPageLoadComplete(success: Boolean, versoPageView: VersoPageView?)
    }
}

interface VersoPageView {
    fun onZoom(scale: Float): Boolean
    val page: Int
    fun setOnLoadCompleteListener(listener: VersoPageViewListener.OnLoadCompleteListener)
    fun onVisible()
    fun onInvisible()
}

interface VersoSpreadConfiguration {
    /**
     * Get the [View] representing the current page.
     * @param page a page
     * @return a View
     */
    fun getPageView(container: ViewGroup, page: Int): View?
    fun getSpreadOverlay(container: ViewGroup, pages: IntArray): View?
    fun onConfigurationChanged(newConfig: Configuration)
    val pageCount: Int
    val spreadCount: Int
    val spreadMargin: Int

    fun getSpreadProperty(spreadPosition: Int): VersoSpreadProperty
    fun getSpreadPositionFromPage(page: Int): Int
    fun getPagesFromSpreadPosition(spreadPosition: Int): IntArray
    fun hasData(): Boolean
}

interface VersoPageChangeListener {

    fun onPagesScrolled(
        currentPosition: Int,
        currentPages: IntArray?,
        previousPosition: Int,
        previousPages: IntArray?
    )

    fun onPagesChanged(
        currentPosition: Int,
        currentPages: IntArray?,
        previousPosition: Int,
        previousPages: IntArray?
    )

    fun onVisiblePageIndexesChanged(pages: IntArray?, added: IntArray?, removed: IntArray?)
}

