package com.tjek.sdk.publicationviewer.paged

import android.content.Context
import android.os.Parcelable
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.publicationviewer.paged.views.OutroView
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

// Available customisable settings
@Parcelize
data class PagedPublicationConfiguration(

    // Open the publication at this page
    val initialPageNumber: Int = 0,

    // Display the hotspot highlights. Set to false if you don't want to show them
    val displayHotspotsOnTouch: Boolean = true,

    // Use the brand colors (e.g background). Set to false if you don't want to use them
    val useBrandColor: Boolean = true,

    // Show a pulsating number as loading state when a page image is loaded
    val showPageNumberWhileLoading: Boolean = true,

    // Outro view
    val outroViewGenerator: OutroViewGenerator? = null
): Parcelable {

    @IgnoredOnParcel
    val hasOutro = outroViewGenerator != null
}

@Parcelize
open class OutroViewGenerator(
    var publication: PublicationV2? = null // set by PagedPublicationFragment
): Parcelable {

    open fun getOutroView(context: Context, page: Int): OutroView? {
        return null
    }
}