package com.tjek.sdk.publicationviewer.paged.libs.verso
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