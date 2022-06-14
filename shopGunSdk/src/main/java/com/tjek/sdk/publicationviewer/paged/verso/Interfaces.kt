package com.tjek.sdk.publicationviewer.paged.verso

import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup

interface VersoPageView {
    fun onZoom(scale: Float): Boolean
    val page: Int

    fun setOnLoadCompleteListener(listener: VersoPageViewInterface.OnLoadCompleteListener)
    fun onVisible()
    fun onInvisible()
}

interface VersoSpreadConfiguration {
    /**
     * Get the [View] representing the current page.
     * @param page a page
     * @return a View
     */
    fun getPageView(container: ViewGroup, page: Int): View
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

