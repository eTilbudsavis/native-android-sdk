package com.tjek.sdk.publicationviewer
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.tjek.sdk.R
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