package com.tjek.sdk.publicationviewer.paged.verso

sealed interface VersoPageViewInterface {

    interface EventListener {
        fun onVersoPageViewEvent(event: VersoPageViewEvent): Boolean
    }

    interface OnLoadCompleteListener {
        fun onPageLoadComplete(success: Boolean, versoPageView: VersoPageView?)
    }
}