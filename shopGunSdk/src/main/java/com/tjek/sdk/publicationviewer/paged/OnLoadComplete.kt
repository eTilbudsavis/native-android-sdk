package com.tjek.sdk.publicationviewer.paged

import com.tjek.sdk.api.models.PublicationHotspotV2
import com.tjek.sdk.api.models.PublicationPageV2
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.ErrorType

interface OnLoadComplete {

    // Publication data loaded from Tjek api
    fun onPublicationLoaded(publication: PublicationV2)

    // Pages data loaded from Tjek api
    fun onPagesLoaded(pages: List<PublicationPageV2>)

    // The image of a specific page has been loaded and added to the viewpager
    fun onPageLoad(page: Int)

    // Hotspot data loaded from Tjek api
    fun onHotspotLoaded(hotspots: List<PublicationHotspotV2>)

    // An error happened during some call
    fun onError(error: ErrorType)
}