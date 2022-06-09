package com.tjek.sdk.api.remote.models.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.models.Coordinate
import kotlinx.parcelize.Parcelize
import org.json.JSONArray

@Parcelize
data class StoreV2(
    val id: Id,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val country: String,
    val coordinate: Coordinate,
    val businessId: Id,
    val branding: BrandingV2,
//    val openingHours: [OpeningHours_v2] todo
    val contact: String?
): Parcelable {

    companion object {
        fun fromDecodable(s: StoreV2Decodable): StoreV2 {
            return StoreV2(
                id = s.id,
                street = s.street,
                city = s.city,
                zipCode = s.zipCode,
                country = s.country.id,
                coordinate = Coordinate(s.latitude, s.longitude),
                businessId = s.businessId,
                branding = s.branding,
                contact = s.contact
            )
        }
    }
}

@Keep
@JsonClass(generateAdapter = true)
data class StoreV2Decodable(
    val id: Id,
    val street: String?,
    val city: String?,
    @Json(name = "zip_code")
    val zipCode: String?,
    val country: CountryV2,
    val latitude: Double,
    val longitude: Double,
    @Json(name = "dealer_id")
    val businessId: Id,
    val branding: BrandingV2,
    val contact: String?,
    @Json(name = "opening_hours")
    val openingHours: JSONArray?
)