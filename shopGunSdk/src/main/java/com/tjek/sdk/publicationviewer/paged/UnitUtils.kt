package com.tjek.sdk.publicationviewer.paged

import android.content.Context
import android.util.TypedValue

object UnitUtils {

    @JvmStatic
    fun dpToPx(dp: Int, ctx: Context): Int {
        val metrics = ctx.resources.displayMetrics
        return (dp * metrics.density).toInt()
    }

    @JvmStatic
    fun spToPx(sp: Int, ctx: Context): Int {
        val metrics = ctx.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), metrics).toInt()
    }
}