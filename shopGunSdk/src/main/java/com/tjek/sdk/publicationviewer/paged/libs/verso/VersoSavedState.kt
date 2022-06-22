package com.tjek.sdk.publicationviewer.paged.libs.verso

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class VersoSavedState (
    var bounceDecore: Boolean = false,
    var position: Int = 0,
    var pages: IntArray,
    var visiblePages: IntArray
) : Parcelable {

    constructor(fragment: VersoFragment) : this(
        bounceDecore = fragment.isBounceDecoreEnabled,
        position = fragment.position,
        pages = fragment.currentPages.copyOf(fragment.currentPages.size),
        visiblePages = fragment.visiblePages.copyOf(fragment.visiblePages.size)
    )

    override fun toString(): String {
        val f = "SavedState[ position:%s pages: %s, visiblePages:%s , bounceDecore:%s]"
        return String.format(
            Locale.ENGLISH,
            f,
            position,
            pages.joinToString(),
            visiblePages.joinToString(),
            bounceDecore
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VersoSavedState) return false

        if (bounceDecore != other.bounceDecore) return false
        if (position != other.position) return false
        if (!pages.contentEquals(other.pages)) return false
        if (!visiblePages.contentEquals(other.visiblePages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bounceDecore.hashCode()
        result = 31 * result + position
        result = 31 * result + pages.contentHashCode()
        result = 31 * result + visiblePages.contentHashCode()
        return result
    }
}