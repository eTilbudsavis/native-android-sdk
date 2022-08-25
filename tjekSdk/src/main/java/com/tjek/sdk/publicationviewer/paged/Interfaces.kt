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
import com.tjek.sdk.api.models.PublicationHotspotV2
import com.tjek.sdk.api.models.PublicationPageV2
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.ResponseType

interface OnHotspotTapListener {
    fun onHotspotTap(hotspots: List<PublicationHotspotV2>)
    fun onHotspotLongTap(hotspots: List<PublicationHotspotV2>)
}

interface OnLoadComplete {

    /**
     * Publication data loaded from Tjek api
     */
    fun onPublicationLoaded(publication: PublicationV2)

    /**
     * Pages data loaded from Tjek api
     */
    fun onPagesLoaded(pages: List<PublicationPageV2>)

    /**
     * The image of a specific page has been loaded and added to the viewpager
     */
    fun onPageLoad(page: Int)

    /**
     * Hotspot data loaded from Tjek api
     */
    fun onHotspotLoaded(hotspots: List<PublicationHotspotV2>)
    
}

/**
 * Easy access to page counter to be shown in the UI.
 * First page is 1, last is publicationV2.pageCount - 1
 * This accounts for intro and outro views.
 */
interface OnPageNumberChangeListener {

    /**
     * Triggered when a new page is selected.
     * currentPages: e.g [1] or [1, 2] (landscape)
     * totalPages: is publicationV2.pageCount - 1
     *
     * Note: not triggered for the initial page, only when it changes
     */
    fun onPageNumberChange(currentPages: IntArray, totalPages: Int)
}