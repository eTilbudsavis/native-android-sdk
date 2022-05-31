package com.tjek.sdk.api.remote.models.v2

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.ValidityDateStr

@Keep
@JsonClass(generateAdapter = true)
data class OfferV2(
    val id: Id?,
    val heading: String?,
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
    val dealerId: Id?,
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
data class PriceV2(
    val price: Double?,
    @Json(name = "pre_price")
    val prePrice: Double?,
    val currency: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class QuantityV2(
    val unit: UnitV2?,
    val size: SizeV2?,
    val pieces: PiecesV2?
)

@Keep
@JsonClass(generateAdapter = true)
data class PiecesV2(
    val from: Int?,
    val to: Int?
)

@Keep
@JsonClass(generateAdapter = true)
data class SizeV2(
    val from: Double?,
    val to: Double?
)

@Keep
@JsonClass(generateAdapter = true)
data class UnitV2(
    val symbol: String?,
    val si: SiV2?
)

@Keep
@JsonClass(generateAdapter = true)
data class SiV2(
    val symbol: String?,
    val factor: Double?
)