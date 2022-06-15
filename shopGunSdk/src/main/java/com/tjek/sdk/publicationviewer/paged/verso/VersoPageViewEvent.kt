package com.tjek.sdk.publicationviewer.paged.verso

sealed class VersoPageViewEvent {
    data class Touch(val action: Int, val info: VersoTapInfo) : VersoPageViewEvent()
    data class Tap(val info: VersoTapInfo) : VersoPageViewEvent()
    data class DoubleTap(val info: VersoTapInfo) : VersoPageViewEvent()
    data class LongTap(val info: VersoTapInfo) : VersoPageViewEvent()
    data class ZoomBegin(val info: VersoZoomPanInfo) : VersoPageViewEvent()
    data class Zoom(val info: VersoZoomPanInfo) : VersoPageViewEvent()
    data class ZoomEnd(val info: VersoZoomPanInfo) : VersoPageViewEvent()
    data class PanBegin(val info: VersoZoomPanInfo) : VersoPageViewEvent()
    data class Pan(val info: VersoZoomPanInfo) : VersoPageViewEvent()
    data class PanEnd(val info: VersoZoomPanInfo) : VersoPageViewEvent()
}
