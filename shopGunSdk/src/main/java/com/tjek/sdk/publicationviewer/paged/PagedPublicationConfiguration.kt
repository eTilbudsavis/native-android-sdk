package com.tjek.sdk.publicationviewer.paged

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PagedPublicationConfiguration(
    val displayHotspotsOnTouch: Boolean = true,
    val useBrandColor: Boolean = true,
    val hasIntro: Boolean = false,
    val hasOutro: Boolean = false
): Parcelable
