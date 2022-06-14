package com.tjek.sdk.publicationviewer.paged.verso

sealed interface VersoPageViewInterface {

    interface OnTouchListener {
        fun onTouch(action: Int, info: VersoTapInfo?): Boolean
    }

    interface OnTapListener {
        fun onTap(info: VersoTapInfo?): Boolean
    }

    interface OnDoubleTapListener {
        fun onDoubleTap(info: VersoTapInfo?): Boolean
    }

    interface OnLongTapListener {
        fun onLongTap(info: VersoTapInfo?)
    }

    interface OnZoomListener {
        fun onZoomBegin(info: VersoZoomPanInfo?)
        fun onZoom(info: VersoZoomPanInfo?)
        fun onZoomEnd(info: VersoZoomPanInfo?)
    }

    interface OnPanListener {
        fun onPanBegin(info: VersoZoomPanInfo?)
        fun onPan(info: VersoZoomPanInfo?)
        fun onPanEnd(info: VersoZoomPanInfo?)
    }

    interface OnLoadCompleteListener {
        fun onPageLoadComplete(success: Boolean, versoPageView: VersoPageView?)
    }
}