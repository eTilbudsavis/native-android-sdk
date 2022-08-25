package com.tjek.sdk.publicationviewer.paged.views

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import com.tjek.sdk.R
import com.tjek.sdk.publicationviewer.paged.layouts.PublicationSpreadLayout

// At the moment we use only Round Rectangle, but we can add other shapes as well if we need to.
sealed class HoleType {
    data class Rectangle(val rectF: RectF = RectF()) : HoleType()
    data class RoundRectangle(val cornerRadius: Float, val rectF: RectF = RectF()) : HoleType()
}

data class HighlightedView(
    val targetView: View,
    val holeType: HoleType
)

class HotspotOverlay(
    context: Context,
    private val longPress: Boolean
) : View(context) {

    private lateinit var drawer: HoleDrawer
    private var bgColor: Int = ResourcesCompat.getColor(resources, R.color.tjek_pagedpub_hotspot_dimmed_bg, null)

    init {
        setWillNotDraw(false)
        animation = AnimationUtils.loadAnimation(context, if (longPress) R.anim.tjek_sdk_pagedpub_hotspot_in_long_press else R.anim.tjek_sdk_pagedpub_hotspot_in)
    }

    private fun setHoles(holes: List<HoleType>) {
        drawer = HoleDrawer(bgColor, holes)
        invalidate()
    }

    // Set the specifics of the shape based on the target view and the type of hole it needs
    fun drawHolesForViews(vararg viewToHighlight: HighlightedView) {
        viewToHighlight.forEach {
            when(it.holeType) {
                is HoleType.Rectangle -> {
                    val r = Rect()
                    it.targetView.getDrawingRect(r)
                    (parent as PublicationSpreadLayout).offsetDescendantRectToMyCoords(it.targetView, r)
                    it.holeType.rectF.set(r)
                }
                is HoleType.RoundRectangle -> {
                    val r = Rect()
                    it.targetView.getDrawingRect(r)
                    (parent as PublicationSpreadLayout).offsetDescendantRectToMyCoords(it.targetView, r)
                    it.holeType.rectF.set(r)
                }
            }
        }
        setHoles(viewToHighlight.map { it.holeType })
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (::drawer.isInitialized && canvas != null) {
            drawer.draw(canvas, width, height)
        }
    }

    override fun onAnimationEnd() {
        super.onAnimationEnd()
        if (!longPress) {
            visibility = GONE
            // this view is added to the parent only once, so at the end of the animation,
            // ask it to remove all views related to hotspots
            val parent = parent as PublicationSpreadLayout
            parent.post { parent.removeHotspots() }
        }
    }

}

class HoleDrawer(private val bgColor: Int, private val holes: List<HoleType>) {

    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = bgColor
    }

    private fun preparePath(screenWidth: Int, screenHeight: Int) {
        // Merge the hotspots together to correctly highlight overlapping areas
        val hotspotPath = Path()
        holes.forEach {
            when (it) {
                is HoleType.Rectangle -> hotspotPath.op(Path().apply { addRect(it.rectF, Path.Direction.CCW) }, Path.Op.UNION)
                is HoleType.RoundRectangle -> hotspotPath.op(Path().apply { addRoundRect(it.rectF, it.cornerRadius, it.cornerRadius, Path.Direction.CCW) }, Path.Op.UNION)
            }
        }
        with(path) {
            reset()
            fillType = Path.FillType.EVEN_ODD
            // rectangle for the screen
            addRect(0F, 0F, screenWidth.toFloat(), screenHeight.toFloat(), Path.Direction.CCW)
            // hotspots highlight
            addPath(hotspotPath)
        }
    }

    fun draw(canvas: Canvas, screenWidth: Int, screenHeight: Int) {
        preparePath(screenWidth, screenHeight)
        canvas.drawPath(path, paint)
    }
}