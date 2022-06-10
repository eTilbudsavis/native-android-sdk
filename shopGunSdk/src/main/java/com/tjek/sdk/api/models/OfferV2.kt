package com.tjek.sdk.api.models

import android.os.Parcelable
import com.tjek.sdk.api.*
import com.tjek.sdk.api.remote.models.v2.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class OfferV2(
    val id: Id,
    val heading: String,
    val description: String?,
    val images: ImageUrlsV2,
    val webshopURL: String?,
    val runDateRange: ValidityPeriod,
    val publishDate: PublishDate?,
    val price: PriceV2?,
    val quantity: QuantityV2?,
    val branding: BrandingV2?,
    val publicationId: Id?,
    val publicationPageIndex: Int?,
    val incitoViewId: String?,
    val businessId: Id,
    // The id of the nearest store. Only available if a location was provided when fetching the offer.
    val storeId: Id?
): Parcelable {

    companion object {
        fun fromDecodable(o: OfferV2Decodable): OfferV2 {
            // sanity check on the dates
            val fromDate = o.runFromDateStr?.toValidityDate() ?: distantPast()
            val tillDate = o.runTillDateStr?.toValidityDate() ?: distantFuture()

            return OfferV2(
                id = o.id,
                heading = o.heading,
                description = o.description,
                images = o.imageUrls ?: ImageUrlsV2("", "", ""),
                webshopURL = o.links?.webshop,
                runDateRange = minOf(fromDate, tillDate)..maxOf(
                    fromDate,
                    tillDate
                ),
                publishDate = o.publishDateStr?.toValidityDate(),
                price = o.price,
                quantity = o.quantity,
                branding = o.branding,
                publicationId = o.catalogId,
                // incito publications have pageNum == 0, so in that case set to nil.
                // otherwise, convert pageNum to index.
                publicationPageIndex = o.catalogPage?.let { if (it > 0) it - 1 else null },
                incitoViewId = o.catalogViewId,
                businessId = o.businessId,
                storeId = o.storeId
            )
        }
    }
}