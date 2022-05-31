package com.tjek.sdk.api.remote.models.v2

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.ValidityDateStr

@Keep
@JsonClass(generateAdapter = true)
data class PublicationV2(
    val id: Id?,
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
    val dealerId: Id?,
    @Json(name = "store_id")
    val storeId: Id?,
    @Json(name = "all_stores")
    val allStores: Boolean?,
    val types: List<PublicationTypesV2>?,
    val branding: BrandingV2?,
    val dimensions: DimensionsV2?,
    @Json(name = "images")
    val frontPageImageUrls: ImageUrlsV2?
)

@Suppress("EnumEntryName")
enum class PublicationTypesV2 { paged, incito }

@Keep
@JsonClass(generateAdapter = true)
data class DimensionsV2(
    val width: Float?,
    val height: Float?
)
