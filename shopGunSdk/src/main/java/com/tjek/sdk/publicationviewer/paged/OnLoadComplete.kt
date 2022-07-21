package com.tjek.sdk.publicationviewer.paged

import com.tjek.sdk.api.models.PublicationHotspotV2
import com.tjek.sdk.api.models.PublicationPageV2
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.ErrorType

// Listener for data loading performed by the PagedPublicationFragment (in the ViewModel)
interface OnLoadComplete {
    fun onPublicationLoaded(publication: PublicationV2)
    fun onPagesLoaded(pages: List<PublicationPageV2>)
    fun onHotspotLoaded(hotspots: List<PublicationHotspotV2>)
    fun onError(error: ErrorType)
}