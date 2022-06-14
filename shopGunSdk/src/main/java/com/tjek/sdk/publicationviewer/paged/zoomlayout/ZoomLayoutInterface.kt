package com.tjek.sdk.publicationviewer.paged.zoomlayout

sealed interface ZoomLayoutInterface {

    interface OnZoomListener {
        fun onZoomBegin(view: ZoomLayout?, scale: Float)
        fun onZoom(view: ZoomLayout?, scale: Float)
        fun onZoomEnd(view: ZoomLayout?, scale: Float)
    }

    interface OnPanListener {
        fun onPanBegin(view: ZoomLayout?)
        fun onPan(view: ZoomLayout?)
        fun onPanEnd(view: ZoomLayout?)
    }

    interface OnZoomTouchListener {
        fun onTouch(view: ZoomLayout?, action: Int, info: TapInfo?): Boolean
    }

    interface OnTapListener {
        fun onTap(view: ZoomLayout?, info: TapInfo?): Boolean
    }

    interface OnDoubleTapListener {
        fun onDoubleTap(view: ZoomLayout?, info: TapInfo?): Boolean
    }

    interface OnLongTapListener {
        fun onLongTap(view: ZoomLayout?, info: TapInfo?)
    }
}