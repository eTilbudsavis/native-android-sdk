package com.tjek.sdk.publicationviewer.paged

interface OnPageNumberChangeListener {
    fun onPageNumberChange(currentPages: IntArray, totalPages: Int)
}