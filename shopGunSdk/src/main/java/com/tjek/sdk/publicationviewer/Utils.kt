package com.tjek.sdk.publicationviewer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.shopgun.android.sdk.R
import com.tjek.sdk.api.remote.ErrorType
import com.tjek.sdk.api.remote.models.v2.BrandingV2
import com.tjek.sdk.getColorInt
import com.tjek.sdk.getPrimaryText
import com.tjek.sdk.getSecondaryText

fun getDefaultErrorScreen(layoutInflater: LayoutInflater, branding: BrandingV2?, error: ErrorType): View {
    val view = layoutInflater.inflate(R.layout.tjek_sdk_publication_error, null, false)
    val heading = view.findViewById<TextView>(R.id.heading)
    val message = view.findViewById<TextView>(R.id.error_message)
    // set message
    message?.text = error.toFormattedString()
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