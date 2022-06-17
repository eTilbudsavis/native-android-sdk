package com.tjek.sdk.sample.zoomlayoutsample.utils

import android.graphics.RectF
import android.widget.TextView
import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomLayout
import com.tjek.sdk.publicationviewer.paged.zoomlayout.Event
import com.tjek.sdk.publicationviewer.paged.zoomlayout.EventListener
import java.util.*

class SimpleEventLogger(
    var tag: String,
    var textView: TextView? = null
) : EventListener {

    companion object {
        const val FORMAT = "%s - s:%.2f, x:%.0f, y:%.0f\n" +
                "DrawRect %s"
        const val RECT_FORMAT = "[ %.0f, %.0f, %.0f, %.0f ]"
    }

    init {
        log("init", 1.0f, 0f, 0f, "none")
    }

    override fun onEvent(event: Event): Boolean {
        when(event) {
            is Event.DoubleTap -> return false
            is Event.LongTap -> return false
            is Event.Pan -> {
                log("Pan Event", event.view)
                return true
            }
            is Event.PanBegin -> {
                log("PanBegin Event", event.view)
                return true
            }
            is Event.PanEnd -> {
                log("PanEnd Event", event.view)
                return true
            }
            is Event.Tap -> return false
            is Event.Touch -> return false
            is Event.Zoom -> {
                log("Zoom Event", event.view)
                return true
            }
            is Event.ZoomBegin -> {
                log("ZoomBegin Event", event.view)
                return true
            }
            is Event.ZoomEnd -> {
                log("ZoomEnd Event", event.view)
                return true
            }
        }
    }

    fun setLogger(zoomLayout: ZoomLayout) {
        zoomLayout.DEBUG = true
        zoomLayout.addEventListener(this)
    }

    private fun log(msg: String, view: ZoomLayout) {
        log(msg, view.getScale(), view.getPosX(), view.getPosY(), r(view.getDrawRect()))
    }

    private fun log(msg: String, scale: Float, x: Float, y: Float, drawRect: String) {
        val text = String.format(Locale.US, FORMAT, msg, scale, x, y, drawRect)
        textView?.let { it.text = text }
    }

    private fun r(r: RectF): String {
        return String.format(Locale.US, RECT_FORMAT, r.left, r.top, r.right, r.bottom)
    }

}