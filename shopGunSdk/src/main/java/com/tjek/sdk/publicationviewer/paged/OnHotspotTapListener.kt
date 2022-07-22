package com.tjek.sdk.publicationviewer.paged

import com.tjek.sdk.api.models.PublicationHotspotV2

interface OnHotspotTapListener {
    fun onHotspotTap(hotspots: List<PublicationHotspotV2>)
    fun onHotspotLongTap(hotspots: List<PublicationHotspotV2>)
}