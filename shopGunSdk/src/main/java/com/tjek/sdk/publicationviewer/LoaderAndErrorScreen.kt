package com.tjek.sdk.publicationviewer

import android.view.View
import com.tjek.sdk.api.remote.ErrorType
import com.tjek.sdk.api.remote.models.v2.BrandingV2

// Interface to provide custom error and loader screens that will be added in a dedicated FrameLayout.
// The callbacks need to provide a view. They receive an optional branding (if available) to allow for color usage.
// If no callback are provided, the default screens will be used.
interface LoaderAndErrorScreenCallback {

    fun showLoaderScreen(brandingV2: BrandingV2?): View
    fun showErrorScreen(brandingV2: BrandingV2?, error: ErrorType): View
}