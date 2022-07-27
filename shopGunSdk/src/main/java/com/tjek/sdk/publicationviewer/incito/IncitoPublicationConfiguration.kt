package com.tjek.sdk.publicationviewer.incito

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Available customisable settings
@Parcelize
data class IncitoPublicationConfiguration(

    // Use the brand colors (e.g background). Set to false if you don't want to use them
    val useBrandColor: Boolean = true,

    // Initial vertical offset
    val initialVerticalOffset: Int = 0,

    // Set an id if you want to open the incito at a specific offer
    val openAtViewWithId: String? = null,

    // Feature label recorded by your app in previous incito sessions
    val recordedFeatureLabel: ArrayList<String>? = null

): Parcelable
