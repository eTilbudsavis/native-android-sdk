package com.tjek.sdk.publicationviewer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.shopgun.android.sdk.R
import com.tjek.sdk.api.models.BrandingV2
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.getColorInt
import com.tjek.sdk.getPrimaryText
import com.tjek.sdk.getSecondaryText

// Interface to provide custom error and loader screens that will be added in a dedicated FrameLayout.
// The callbacks need to provide a view. They receive an optional branding (if available) to allow for color usage.
// If no callback are provided, the default screens will be used.
interface LoaderAndErrorScreenCallback {

    fun showLoaderScreen(brandingV2: BrandingV2?): View
    fun showErrorScreen(brandingV2: BrandingV2?, error: ResponseType.Error): View
}



// The publications fragments will call these function if no custom screen are provided.
// The branding is null if not available or if the publication is configured to not use it.

fun getDefaultErrorScreen(layoutInflater: LayoutInflater, branding: BrandingV2?, error: ResponseType.Error): View {
    val view = layoutInflater.inflate(R.layout.tjek_sdk_publication_error, null, false)
    val heading = view.findViewById<TextView>(R.id.heading)
    val message = view.findViewById<TextView>(R.id.error_message)
    // set message
    message?.text = error.toString()
    // set colors
    val bgColor = branding?.colorHex.getColorInt().takeUnless { it == 0 } ?: Color.BLACK
    view.setBackgroundColor(bgColor)
    heading?.setTextColor(bgColor.getPrimaryText())
    message?.setTextColor(bgColor.getPrimaryText())
    return view
}

fun getDefaultLoadingScreen(layoutInflater: LayoutInflater, branding: BrandingV2?): View {
    val view = layoutInflater.inflate(R.layout.tjek_sdk_publication_loader, null, false)
    val bgColor = branding?.colorHex.getColorInt().takeUnless { it == 0 } ?: Color.BLACK
    view.setBackgroundColor(bgColor)
    view.findViewById<ProgressBar>(R.id.circularProgressBar)?.also {
        it.isIndeterminate = true
        it.indeterminateDrawable?.setTint(bgColor.getSecondaryText())
    }
    return view
}