package com.tjek.sdk.publicationviewer.paged.zoomlayout

interface EventListener {

    /**
     * Receive events from the ZoomLayout.
     * Each event contains the reference to the view and the relevant information for the event.
     * The receiver has the possibility to change the view parameters.
     *
     * Return: true if the event was consumed, false otherwise
     */
    fun onEvent(event: Event): Boolean
}

sealed class Event {

    // Zoom
    data class ZoomBegin(val view: ZoomLayout, val scale: Float) : Event()
    data class Zoom(val view: ZoomLayout, val scale: Float) : Event()
    data class ZoomEnd(val view: ZoomLayout, val scale: Float) : Event()

    // Pan
    data class PanBegin(val view: ZoomLayout) : Event()
    data class Pan(val view: ZoomLayout) : Event()
    data class PanEnd(val view: ZoomLayout) : Event()

    // Touch
    data class Touch(val view: ZoomLayout, val action: Int, val info: TapInfo) : Event()

    // Tap
    data class Tap(val view: ZoomLayout, val info: TapInfo) : Event()
    data class DoubleTap(val view: ZoomLayout, val info: TapInfo) : Event()
    data class LongTap(val view: ZoomLayout, val info: TapInfo) : Event()
}