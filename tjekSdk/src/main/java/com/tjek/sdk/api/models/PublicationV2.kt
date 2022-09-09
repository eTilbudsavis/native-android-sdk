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
    val width: Double,
    val height: Double,
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

    // The ratio of width to height for the page-images. So if an image is (w:100, h:200), the aspectRatio is 0.5 (width/height).
    @IgnoredOnParcel
    val aspectRatio: Double = width / height

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
            val fromDate = p.runFromDateStr?.toValidityDate(ValidityDateStrVersion.V2) ?: distantPast()
            val tillDate = p.runTillDateStr?.toValidityDate(ValidityDateStrVersion.V2) ?: distantFuture()

            return PublicationV2(
                id = p.id,
                label = p.label,
                pageCount = p.pageCount ?: 0,
                offerCount = p.offerCount ?: 0,
                runDateRange = minOf(fromDate, tillDate)..maxOf(
                    fromDate,
                    tillDate
                ),
                width = p.dimensions?.width ?: 1.0,
                height = p.dimensions?.height ?: 1.0,
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

@Keep
@Suppress("EnumEntryName")
enum class PublicationType { paged, incito }


//------------- Classes used for decoding api responses -------------//

@Keep
@JsonClass(generateAdapter = true)
data class PublicationV2Decodable(
    val id: Id,
    val label: String?,
    @Json(name = "page_count")
    val pageCount: Int?,
    @Json(name = "offer_count")
    val offerCount: Int?,
    @Json(name = "run_from")
    val runFromDateStr: ValidityDateStr?,
    @Json(name = "run_till")
    val runTillDateStr: ValidityDateStr?,
    @Json(name = "dealer_id")
    val businessId: Id,
    @Json(name = "store_id")
    val storeId: Id?,
    @Json(name = "all_stores")
    val allStores: Boolean?,
    val types: List<PublicationType>?,
    val branding: BrandingV2,
    val dimensions: DimensionsV2?,
    @Json(name = "images")
    val frontPageImageUrls: ImageUrlsV2?
)

@Keep
@JsonClass(generateAdapter = true)
data class DimensionsV2(
    val width: Double?,
    val height: Double?
)