package com.tjek.sdk.publicationviewer.paged.verso

sealed interface VersoInterface {

    interface OnPageChangeListener {
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
}

class SimpleOnPageChangeListener : VersoInterface.OnPageChangeListener {
    override fun onPagesScrolled(
        currentPosition: Int,
        currentPages: IntArray?,
        previousPosition: Int,
        previousPages: IntArray?
    ) {
    }

    override fun onPagesChanged(
        currentPosition: Int,
        currentPages: IntArray?,
        previousPosition: Int,
        previousPages: IntArray?
    ) {
    }

    override fun onVisiblePageIndexesChanged(
        pages: IntArray?,
        added: IntArray?,
        removed: IntArray?
    ) {
    }
}