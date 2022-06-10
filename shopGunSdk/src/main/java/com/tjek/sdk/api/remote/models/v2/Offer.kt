package com.tjek.sdk.api.remote.models.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.ValidityDateStr
import kotlinx.parcelize.Parcelize

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