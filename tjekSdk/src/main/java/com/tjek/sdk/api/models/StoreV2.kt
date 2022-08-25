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
                val from = validFrom?.toValidityDate(ValidityDateStrVersion.V2)?.toLocalDate()

                // NOTE: the date range have meaning only in their date component (our api allow only date-time),
                // this means that the "until" date has to be moved to the previous day to have a correct range in case the time is set to midnight.
                // Example: one day range (30/03) is { valid_from: '2022-03-30T00:00:00Z', valid_until: '2022-03-31T00:00:00Z' }
                // Example: one day range (30/03) is { valid_from: '2022-03-30T00:00:00Z', valid_until: '2022-03-30T23:00:00Z' }
                val dateTimeUntil = validUntil?.toValidityDate(ValidityDateStrVersion.V2)?.toLocalDateTime()
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