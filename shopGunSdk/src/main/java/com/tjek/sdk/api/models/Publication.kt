package com.tjek.sdk.api.models

import android.os.Parcelable
import com.tjek.sdk.api.*
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
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
    val runDateRange: ValidityPeriod = distantPast()..distantFuture(),
    val types: List<PublicationTypes> = listOf(PublicationTypes.Paged),
) : Parcelable {
    // True if this publication can only be viewed as an incito (if viewed in a PagedPublication view it would appear as a single-page pdf)
    @IgnoredOnParcel
    val isOnlyIncitoPublication: Boolean = types.size == 1 && types.contains(PublicationTypes.Incito)

    // True if this publication can be viewed as an incito
    @IgnoredOnParcel
    val hasIncitoPublication: Boolean = types.contains(PublicationTypes.Incito)

    // True if this publication can be viewed as an paged publication
    @IgnoredOnParcel
    val hasPagedPublication: Boolean =  types.contains(PublicationTypes.Paged)

    @IgnoredOnParcel
    val aspectRatio: Float = if (width > 0 && height > 0) width / height else 1f
}

enum class PublicationTypes { Paged, Incito }

