package com.tjek.sdk.api.models
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

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.*
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
            val fromDate = o.runFromDateStr?.toValidityDate(ValidityDateStrVersion.V2) ?: distantPast()
            val tillDate = o.runTillDateStr?.toValidityDate(ValidityDateStrVersion.V2) ?: distantFuture()

            return OfferV2(
                id = o.id,
                heading = o.heading,
                description = o.description,
                images = o.imageUrls ?: ImageUrlsV2("", "", ""),
                webshopURL = o.links?.webshop,
                runDateRange = minOf(fromDate, tillDate)..maxOf(fromDate, tillDate),
                publishDate = o.publishDateStr?.toValidityDate(ValidityDateStrVersion.V2),
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


//------------- Classes used for decoding api responses -------------//

@Keep
@JsonClass(generateAdapter = true)
data class OfferV2Decodable(
    val id: Id,
    val heading: String,
    val description: String?,
    @Json(name = "images")
    val imageUrls: ImageUrlsV2?,
    val links: LinksV2?,
    @Json(name = "run_from")
    val runFromDateStr: ValidityDateStr?,
    @Json(name = "run_till")
    val runTillDateStr: ValidityDateStr?,
    @Json(name = "publish")
    val publishDateStr: ValidityDateStr?,
    @Json(name = "pricing")
    val price: PriceV2?,
    val quantity: QuantityV2?,
    val branding: BrandingV2?,
    @Json(name = "catalog_id")
    val catalogId: Id?,
    @Json(name = "catalog_page")
    val catalogPage: Int?,
    @Json(name = "catalog_view_id")
    val catalogViewId: Id?,
    @Json(name = "dealer_id")
    val businessId: Id,
    @Json(name = "store_id")
    val storeId: Id?
)

@Keep
@JsonClass(generateAdapter = true)
data class LinksV2(
    val webshop: String?
)

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class PriceV2(
    val currency: String,
    val price: Double,
    @Json(name = "pre_price")
    val prePrice: Double?,
): Parcelable

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class QuantityV2(
    val unit: UnitV2?,
    val size: SizeV2,
    val pieces: PiecesV2
): Parcelable

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class PiecesV2(
    val from: Int?,
    val to: Int?
): Parcelable

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class SizeV2(
    val from: Double?,
    val to: Double?
): Parcelable

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class UnitV2(
    val symbol: String,
    val si: SiV2
): Parcelable

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class SiV2(
    val symbol: String,
    val factor: Double
): Parcelable