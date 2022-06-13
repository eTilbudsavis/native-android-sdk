package com.tjek.sdk.publicationviewer.paged

import android.content.Context

object UnitUtils {

    @JvmStatic
    fun dpToPx(dp: Int, ctx: Context): Int {
        val metrics = ctx.resources.displayMetrics
        return (dp * metrics.density).toInt()
    }
}