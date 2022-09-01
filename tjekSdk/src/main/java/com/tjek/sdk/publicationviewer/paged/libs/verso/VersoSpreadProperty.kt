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
data class VersoSpreadProperty (
    val pages: IntArray?,
    val width: Float,
    val maxZoomScale: Float,
    val minZoomScale: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VersoSpreadProperty) return false

        if (pages != null) {
            if (other.pages == null) return false
            if (!pages.contentEquals(other.pages)) return false
        } else if (other.pages != null) return false
        if (width != other.width) return false
        if (maxZoomScale != other.maxZoomScale) return false
        if (minZoomScale != other.minZoomScale) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pages?.contentHashCode() ?: 0
        result = 31 * result + width.hashCode()
        result = 31 * result + maxZoomScale.hashCode()
        result = 31 * result + minZoomScale.hashCode()
        return result
    }
}