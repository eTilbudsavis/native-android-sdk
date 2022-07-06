package com.tjek.sdk.publicationviewer.paged

import android.content.Context
import android.os.Parcelable
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.publicationviewer.paged.views.IntroOutroView
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

// Available customisable settings
@Parcelize
data class PagedPublicationConfiguration(
    // Display the hotspot highlights. Set to false if you don't want to show them
    val displayHotspotsOnTouch: Boolean = true,

    // Use the brand colors (e.g background). Set to false if you don't want to use them (default color: dark gray)
    val useBrandColor: Boolean = true,

    // Configurations for intro and outro view
    val introConfiguration: IntroConfiguration? = null,
    val outroConfiguration: OutroConfiguration? = null
): Parcelable {
    @IgnoredOnParcel
    val hasIntro = introConfiguration != null

    @IgnoredOnParcel
    val hasOutro = outroConfiguration != null
}

@Parcelize
open class IntroConfiguration(
    var publication: PublicationV2? = null
): Parcelable {

    open fun getIntroView(context: Context, page: Int): IntroOutroView? {
        return null
    }
}

@Parcelize
open class OutroConfiguration(
    var publication: PublicationV2? = null
): Parcelable {

    open fun getOutroView(context: Context, page: Int): IntroOutroView? {
        return null
    }
}