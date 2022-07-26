package com.tjek.sdk.publicationviewer.incito

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Available customisable settings
@Parcelize
data class IncitoPublicationConfiguration(

    // Use the brand colors (e.g background). Set to false if you don't want to use them
    val useBrandColor: Boolean = true

): Parcelable
