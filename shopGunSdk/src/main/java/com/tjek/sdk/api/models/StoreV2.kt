package com.tjek.sdk.api.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.toDayOfWeek
import com.tjek.sdk.api.toTimeOfDay
import com.tjek.sdk.api.toValidityDate
import kotlinx.parcelize.Parcelize
import java.time.LocalTime
import java.util.ArrayList

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
    val openingHours: List<OpeningHours>?,
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
                contact = s.contact,
                openingHours = s.openingHours?.let { readOpeningHours(it) }
            )
        }

        // transform the raw hours into something more refined
        private fun readOpeningHours(oh: List<OpeningHoursDecodable>): List<OpeningHours> {
            val openingHours = ArrayList<OpeningHours>(oh.size)
            oh.forEach { with(it) {
                val from = validFrom?.toValidityDate()?.toLocalDate()

                // NOTE: the date range have meaning only in their date component (our api allow only date-time),
                // this means that the "until" date has to be moved to the previous day to have a correct range in case the time is set to midnight.
                // Example: one day range (30/03) is { valid_from: '2022-03-30T00:00:00Z', valid_until: '2022-03-31T00:00:00Z' }
                // Example: one day range (30/03) is { valid_from: '2022-03-30T00:00:00Z', valid_until: '2022-03-30T23:00:00Z' }
                val dateTimeUntil = validUntil?.toValidityDate()?.toLocalDateTime()
                val till =
                    if (dateTimeUntil?.toLocalTime() == LocalTime.MIDNIGHT) dateTimeUntil?.toLocalDate()?.minusDays(1)
                    else dateTimeUntil?.toLocalDate()

                when {
                    opens != null && closes != null && dayOfWeekStr != null ->
                        openingHours.add(
                            OpeningHours.OpenDay(
                                dayOfWeekStr.toDayOfWeek(),
                                OpenHour(opens.toTimeOfDay(), closes.toTimeOfDay())
                            )
                        )
                    opens != null && closes != null && from != null && till != null ->
                        openingHours.add(
                            OpeningHours.DateRangeOpen(
                                from..till,
                                OpenHour(opens.toTimeOfDay(), closes.toTimeOfDay())
                            )
                        )
                    from != null && till != null ->
                        openingHours.add(OpeningHours.DateRangeClosed(from..till))
                    dayOfWeekStr != null ->
                        openingHours.add(OpeningHours.ClosedDay(dayOfWeekStr.toDayOfWeek()))
                }
            } }

            return openingHours
        }
    }
}


//------------- Classes used for decoding api responses -------------//

@Keep
@JsonClass(generateAdapter = true)
data class StoreV2Decodable(
    val id: Id,
    val street: String?,
    val city: String?,
    @Json(name = "zip_code")
    val zipCode: String?,
    val country: CountryV2Decodable,
    val latitude: Double,
    val longitude: Double,
    @Json(name = "dealer_id")
    val businessId: Id,
    val branding: BrandingV2,
    val contact: String?,
    @Json(name = "opening_hours")
    val openingHours: List<OpeningHoursDecodable>?
)