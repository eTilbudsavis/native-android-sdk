package com.tjek.sdk.api.models

import com.tjek.sdk.api.*

data class Publication(
    val id: Id = "",
    val width: Float = 1f,
    val height: Float = 1f,
    val offerCount: Int = 0,
    val pageCount: Int = 0,
    val label: String = "",
    val isAvailableInAllStores: Boolean = true,
    val businessId: Id = "",
    val storeId: Id = "",
    val branding: Branding = Branding(),
    val frontPageImageUrls: ImageUrls = ImageUrls(),
    val runDateRange: ClosedRange<ValidityDate> = ValidityDateRange(distantPast(), distantFuture()),
    val types: List<PublicationTypes> = listOf(PublicationTypes.Paged),
) {
    // True if this publication can only be viewed as an incito (if viewed in a PagedPublication view it would appear as a single-page pdf)
    val isOnlyIncitoPublication: Boolean = types.size == 1 && types.contains(PublicationTypes.Incito)

    // True if this publication can be viewed as an incito
    val hasIncitoPublication: Boolean = types.contains(PublicationTypes.Incito)

    // True if this publication can be viewed as an paged publication
    val hasPagedPublication: Boolean =  types.contains(PublicationTypes.Paged)

    val aspectRatio: Float = if (width > 0 && height > 0) width / height else 1f
}

enum class PublicationTypes { Paged, Incito }

