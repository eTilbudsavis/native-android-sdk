package com.tjek.sdk.publicationviewer.paged

import android.content.Context
import android.os.Parcelable
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.publicationviewer.paged.views.IntroOutroView
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class PagedPublicationConfiguration(
    val displayHotspotsOnTouch: Boolean = true,
    val useBrandColor: Boolean = true,
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