package com.tjek.sdk.publicationviewer.paged
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