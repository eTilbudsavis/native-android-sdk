package com.tjek.sdk.publicationviewer.paged.zoomlayout

class ZoomOnDoubleTapListener(threeStep: Boolean) : ZoomLayoutInterface {

    private var mThreeStep = threeStep

    override fun onZoomLayoutEvent(event: ZoomLayoutEvent): Boolean {
        return when (event) {
            is ZoomLayoutEvent.DoubleTap -> onDoubleTap(event.view, event.info)
            else -> false
        }
    }

    private fun onDoubleTap(view: ZoomLayout, info: TapInfo): Boolean {
        try {
            if (mThreeStep) {
                threeStep(view, info.absoluteX, info.absoluteY)
            } else {
                twoStep(view, info.absoluteX, info.absoluteY)
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            // Can sometimes happen when absoluteX and absoluteY are called
        }
        return true
    }

    private fun twoStep(view: ZoomLayout, x: Float, y: Float) {
        if (view.scale > view.minScale) {
            view.setScale(view.minScale, true)
        } else {
            view.setScale(view.maxScale, x, y, true)
        }
    }

    private fun threeStep(view: ZoomLayout, x: Float, y: Float) {
        val scale = view.scale
        val medium = view.minScale + (view.maxScale - view.minScale) * 0.3f
        if (scale < medium) {
            view.setScale(medium, x, y, true)
        } else if (scale >= medium && scale < view.maxScale) {
            view.setScale(view.maxScale, x, y, true)
        } else {
            view.setScale(view.minScale, true)
        }
    }
}