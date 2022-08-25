package com.tjek.sdk.publicationviewer.paged.libs.zoomlayout

import android.content.Context
import android.graphics.*
import com.tjek.sdk.publicationviewer.paged.utils.UnitUtils.dpToPx
import kotlin.math.roundToInt

object ZoomUtils {

    // Flags for debugging purposes
    private var debugPaintBlue: Paint? = null
    private var debugPaintWhite: Paint? = null
    private var debugPaintYellow: Paint? = null
    private var debugPaintRed: Paint? = null
    private var debugRadius = 0

    @JvmStatic
    fun debugDraw(
        canvas: Canvas,
        context: Context,
        tx: Float,
        ty: Float,
        fx: Float,
        fy: Float,
        invScale: Float
    ) {
        ensureDebugOptions(context)
        val r = (debugRadius.toFloat() * invScale).toInt()
        debugDrawCirc(canvas, tx, ty, r, debugPaintBlue!!)
        debugDrawCirc(canvas, 0f, 0f, r, debugPaintRed!!)
        debugDrawCirc(canvas, fx, fy, r, debugPaintYellow!!)
    }

    private fun ensureDebugOptions(context: Context) {
        if (debugPaintBlue == null) {
            debugPaintWhite = Paint().apply { color = Color.WHITE }
            debugPaintBlue = Paint().apply { color = Color.BLUE }
            debugPaintYellow = Paint().apply { color = Color.YELLOW }
            debugPaintRed = Paint().apply { color = Color.RED }
            debugRadius = dpToPx(4, context)
        }
    }

    private fun debugDrawCirc(canvas: Canvas, cx: Float, cy: Float, r: Int, p: Paint) {
        debugPaintWhite?.let { canvas.drawCircle(cx, cy, r.toFloat(), it) }
        canvas.drawCircle(cx, cy, (r / 2).toFloat(), p)
    }

    @JvmStatic
    fun setArray(array: FloatArray, rect: Rect) {
        array[0] = rect.left.toFloat()
        array[1] = rect.top.toFloat()
        array[2] = rect.right.toFloat()
        array[3] = rect.bottom.toFloat()
    }

    @JvmStatic
    fun setArray(array: FloatArray, rect: RectF) {
        array[0] = rect.left
        array[1] = rect.top
        array[2] = rect.right
        array[3] = rect.bottom
    }

    /**
     * Round and set the values on the rectangle
     * @param rect the rectangle to set
     * @param array the array to read the values from
     */
    @JvmStatic
    fun setRect(rect: Rect, array: FloatArray) {
        setRect(rect, array[0], array[1], array[2], array[3])
    }

    /**
     * Round and set the values on the rectangle
     * @param rect the rectangle to set
     * @param array the array to read the values from
     */
    @JvmStatic
    fun setRect(rect: RectF, array: FloatArray) {
        setRect(rect, array[0], array[1], array[2], array[3])
    }

    /**
     * Round and set the values on the rectangle
     * @param rect the rectangle to set
     * @param l left
     * @param t top
     * @param r right
     * @param b bottom
     */
    @JvmStatic
    fun setRect(rect: RectF, l: Float, t: Float, r: Float, b: Float) {
        rect[l.roundToInt().toFloat(), t.roundToInt().toFloat(), r.roundToInt().toFloat()] =
            b.roundToInt().toFloat()
    }

    /**
     * Round and set the values on the rectangle
     * @param rect the rectangle to set
     * @param l left
     * @param t top
     * @param r right
     * @param b bottom
     */
    @JvmStatic
    fun setRect(rect: Rect, l: Float, t: Float, r: Float, b: Float) {
        rect[l.roundToInt(), t.roundToInt(), r.roundToInt()] = b.roundToInt()
    }
}