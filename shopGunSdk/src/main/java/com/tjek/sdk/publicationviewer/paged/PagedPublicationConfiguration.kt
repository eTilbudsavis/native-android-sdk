package com.tjek.sdk.publicationviewer.paged

import android.content.Context
import android.os.Parcelable
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoSpreadProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class PagedPublicationConfiguration(
    val displayHotspotsOnTouch: Boolean = true,
    val useBrandColor: Boolean = true,
    val introConfiguration: IntroConfiguration = IntroConfiguration(hasIntro = false),
    val outroConfiguration: OutroConfiguration = OutroConfiguration(hasOutro = false)
): Parcelable

@Parcelize
open class IntroConfiguration(
    val hasIntro: Boolean
): Parcelable {

    open fun getIntroView(context: Context, page: Int): IntroOutroView? {
        return null
    }
}

@Parcelize
open class OutroConfiguration(
    val hasOutro: Boolean
): Parcelable {

    open fun getOutroView(context: Context, page: Int): IntroOutroView? {
        return null
    }
}