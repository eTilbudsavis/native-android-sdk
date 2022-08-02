package com.tjek.sdk.api.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.*
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class OfferV4(
    val id: Id,
    val name: String,
    val description: String?,
    val images: List<ImageV4>,
    val webshopLink: String?,
    val price: Double,
    val currencyCode: String,
    val savings: Double?,
    val pieceCount: PieceCount,
    val unitSymbol: QuantityUnit,
    val unitSize: UnitSize,
    val validityPeriod: ValidityPeriod,
    val visibleFrom: VisibleFromDate,
    val business: BusinessV4
//    val publicationId: Id?,
//    val publicationPageIndex: Int?,
//    val incitoViewId: String?
): Parcelable {

    @IgnoredOnParcel
    val pieceCountRange: ClosedRange<Double> = pieceCount.from..pieceCount.to

    @IgnoredOnParcel
    val unitSizeRange: ClosedRange<Double> = unitSize.from..unitSize.to

    companion object {
        fun fromDecodable(o: OfferV4Decodable): OfferV4 {
            // sanity check on the dates
            val fromDate = o.validity.from?.toValidityDate(ValidityDateStrVersion.V4) ?: distantPast()
            // because v4 API `_until` dates mean "until, but not including", we subtract 1 sec so that decoded v4 dates match v2 dates.
            val tillDate = o.validity.to?.toValidityDate(ValidityDateStrVersion.V4)?.minusSeconds(1) ?: distantFuture()

            return OfferV4(
                id = o.id,
                name = o.name,
                description = o.description,
                images = o.images,
                webshopLink = o.webshop,
                price = o.price,
                currencyCode = o.currency,
                savings = o.savings,
                pieceCount = o.pieceCount,
                unitSymbol = o.unitSymbol,
                unitSize = o.unitSize,
                validityPeriod = minOf(fromDate, tillDate)..maxOf(fromDate, tillDate),
                visibleFrom = o.visibleFrom.toValidityDate(ValidityDateStrVersion.V4) ?: fromDate,
                business = BusinessV4.fromDecodable(o.business)
            )
        }
    }
}


//------------- Classes used for decoding api responses -------------//

@Keep
@JsonClass(generateAdapter = true)
data class OfferV4DecodableContainer(
    val offer: OfferV4Decodable
)

@Keep
@JsonClass(generateAdapter = true)
data class OfferV4Decodable(
    val id: Id,
    val name: String,
    val description: String?,
    val images: List<ImageV4>,
    @Json(name = "webshop_link")
    val webshop: String?,
    val price: Double,
    @Json(name = "currency_code")
    val currency: String,
    val savings: Double?,
    @Json(name = "piece_count")
    val pieceCount: PieceCount,
    @Json(name = "unit_symbol")
    val unitSymbol: QuantityUnit,
    @Json(name = "unit_size")
    val unitSize: UnitSize,
    val validity: Validity,
    @Json(name = "visible_from")
    val visibleFrom: ValidityDateStr,
    val business: BusinessV4Decodable
)

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class PieceCount(
    val from: Double,
    val to: Double
): Parcelable

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class UnitSize(
    val from: Double,
    val to: Double
): Parcelable

@Keep
@JsonClass(generateAdapter = true)
data class Validity(
    val from: ValidityDateStr?,
    val to: ValidityDateStr?)