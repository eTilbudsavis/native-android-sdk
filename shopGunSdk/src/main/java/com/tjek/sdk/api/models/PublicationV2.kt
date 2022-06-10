package com.tjek.sdk.api.models

import android.os.Parcelable
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.distantFuture
import com.tjek.sdk.api.distantPast
import com.tjek.sdk.api.remote.models.v2.BrandingV2
import com.tjek.sdk.api.remote.models.v2.ImageUrlsV2
import com.tjek.sdk.api.remote.models.v2.PublicationV2Decodable
import com.tjek.sdk.api.toValidityDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class PublicationV2(
    // The unique identifier of this Publication.
    val id: Id,
    // The name of the publication. eg. "Christmas Special".
    val label: String?,
    // How many pages this publication has.
    val pageCount: Int,
    // How many `Offer`s are in this publication.
    val offerCount: Int,
    // The range of dates that this publication is valid from and until.
    val runDateRange: ValidityPeriod,
    // The ratio of width to height for the page-images. So if an image is (w:100, h:200), the aspectRatio is 0.5 (width/height).
    val aspectRatio: Double,
    // The branding information for the publication's dealer.
    val branding: BrandingV2,
    // A set of URLs for the different sized images for the cover of the publication.
    val frontPageImages: ImageUrlsV2,
    // Whether this publication is available in all stores, or just in a select few stores.
    val isAvailableInAllStores: Boolean,
    // The unique identifier of the business that published this publication.
    val businessId: Id,
    // The unique identifier of the nearest store. This will only contain a value if the `Publication` was fetched with a request that includes store information (eg. one that takes a precise location as a parameter).
    val storeId: Id?,

    // Defines what types of publication this represents.
    // If it contains `paged`, the `id` can be used to view this in a PagedPublicationViewer
    // If it contains `incito`, the `id` can be used to view this with the IncitoViewer
    // If it ONLY contains `incito`, this cannot be viewed in a PagedPublicationViewer (see `isOnlyIncito`)
    val types: List<PublicationType> = listOf(PublicationType.paged)
) : Parcelable {
    // True if this publication can only be viewed as an incito (if viewed in a PagedPublication view it would appear as a single-page pdf)
    @IgnoredOnParcel
    val isOnlyIncitoPublication: Boolean = types.size == 1 && types.contains(PublicationType.incito)

    // True if this publication can be viewed as an incito
    @IgnoredOnParcel
    val hasIncitoPublication: Boolean = types.contains(PublicationType.incito)

    // True if this publication can be viewed as an paged publication
    @IgnoredOnParcel
    val hasPagedPublication: Boolean =  types.contains(PublicationType.paged)

    companion object {
        fun fromDecodable(p: PublicationV2Decodable): PublicationV2 {
            // sanity check on the dates
            val fromDate = p.runFromDateStr?.toValidityDate() ?: distantPast()
            val tillDate = p.runTillDateStr?.toValidityDate() ?: distantFuture()

            val width = p.dimensions?.width ?: 1.0
            val height = p.dimensions?.height ?: 1.0

            return PublicationV2(
                id = p.id,
                label = p.label,
                pageCount = p.pageCount ?: 0,
                offerCount = p.offerCount ?: 0,
                runDateRange = com.tjek.sdk.api.minOf(fromDate, tillDate)..com.tjek.sdk.api.maxOf(
                    fromDate,
                    tillDate
                ),
                aspectRatio = width / height,
                branding = p.branding,
                frontPageImages = p.frontPageImageUrls ?: ImageUrlsV2("", "", ""),
                isAvailableInAllStores = p.allStores ?: true,
                businessId = p.businessId,
                storeId = p.storeId,
                types = p.types ?: listOf(PublicationType.paged)
            )
        }
    }
}

@Suppress("EnumEntryName")
enum class PublicationType { paged, incito }