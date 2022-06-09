package com.tjek.sdk.api.remote.models.v2

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.*
import com.tjek.sdk.api.models.Coordinate
import com.tjek.sdk.api.models.OpeningHoursDateRange
import com.tjek.sdk.api.models.rangeTo
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.*

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
                        openingHours.add(OpeningHours.OpenDay(dayOfWeekStr.toDayOfWeek(), OpenHour(opens.toTimeOfDay(), closes.toTimeOfDay())))
                    opens != null && closes != null && from != null && till != null ->
                        openingHours.add(OpeningHours.DateRangeOpen(from..till, OpenHour(opens.toTimeOfDay(), closes.toTimeOfDay())))
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

sealed class OpeningHours : Parcelable {

    @Parcelize
    // normal opening hours
    data class OpenDay(val dayOfWeek: DayOfWeek, val dailyHours: OpenHour): OpeningHours()

    @Parcelize
    // holiday hours (e.g. reduced opening hours)
    data class DateRangeOpen(val dateRange: OpeningHoursDateRange, val dailyHours: OpenHour): OpeningHours()

    @Parcelize
    // holiday closed date
    data class DateRangeClosed(val dateRange: OpeningHoursDateRange): OpeningHours()

    @Parcelize
    // closing day
    data class ClosedDay(val dayOfWeek: DayOfWeek): OpeningHours()
}

data class OpenHour(
    val opens: TimeOfDay,
    val closes: TimeOfDay
) : Parcelable {
    constructor(parcel: Parcel) : this(
        LocalTime.ofNanoOfDay(parcel.readLong()),
        LocalTime.ofNanoOfDay(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(opens.toNanoOfDay())
        parcel.writeLong(closes.toNanoOfDay())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OpenHour> {
        override fun createFromParcel(parcel: Parcel): OpenHour {
            return OpenHour(parcel)
        }

        override fun newArray(size: Int): Array<OpenHour?> {
            return arrayOfNulls(size)
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
    val openingHours: List<OpeningHoursDecodable>?
)

@Keep
@JsonClass(generateAdapter = true)
data class OpeningHoursDecodable (
    @Json(name = "day_of_week")
    val dayOfWeekStr: DayOfWeekStr?,
    @Json(name = "valid_from")
    val validFrom: ValidityDateStr?,
    @Json(name = "valid_until")
    val validUntil: ValidityDateStr?,
    val opens: TimeOfDayStr?,
    val closes: TimeOfDayStr?
)