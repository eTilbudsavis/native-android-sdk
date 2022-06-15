package com.tjek.sdk.publicationviewer.paged.zoomlayout

interface ZoomLayoutInterface {

    /**
     * Receive events from the ZoomLayout.
     * Each event contains the reference to the view and the relevant information for the event.
     * The receiver has the possibility to change the view parameters.
     *
     * Return: true if the event was consumed, false otherwise
     */
    fun onZoomLayoutEvent(event: ZoomLayoutEvent): Boolean
}

sealed class ZoomLayoutEvent {

    // Zoom
    data class ZoomBegin(val view: ZoomLayout, val scale: Float) : ZoomLayoutEvent()
    data class Zoom(val view: ZoomLayout, val scale: Float) : ZoomLayoutEvent()
    data class ZoomEnd(val view: ZoomLayout, val scale: Float) : ZoomLayoutEvent()

    // Pan
    data class PanBegin(val view: ZoomLayout) : ZoomLayoutEvent()
    data class Pan(val view: ZoomLayout) : ZoomLayoutEvent()
    data class PanEnd(val view: ZoomLayout) : ZoomLayoutEvent()

    // Touch
    data class Touch(val view: ZoomLayout, val action: Int, val info: TapInfo) : ZoomLayoutEvent()

    // Tap
    data class Tap(val view: ZoomLayout, val info: TapInfo) : ZoomLayoutEvent()
    data class DoubleTap(val view: ZoomLayout, val info: TapInfo) : ZoomLayoutEvent()
    data class LongTap(val view: ZoomLayout, val info: TapInfo) : ZoomLayoutEvent()
}