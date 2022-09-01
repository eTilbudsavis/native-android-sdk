package com.tjek.sdk.api
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
import com.tjek.sdk.TjekLogCat
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*

// V2 date format is in the form yyyy-MM-dd'T'HH:mm:ssZZZZ.
// I want to be able to parse both types of offset that this format can give:
// 2000-05-06T22:00:00+0000
// 2000-05-06T22:00:00+00:00


private val offset1: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendOffset("+HHMM", "0000")
    .toFormatter(Locale.ENGLISH)
private val offset2: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendOffset("+HH:MM", "+00:00")
    .toFormatter(Locale.ENGLISH)
private var parserV2: DateTimeFormatter = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .appendOptional(offset1)
    .appendOptional(offset2)
    .parseCaseInsensitive()
    .toFormatter(Locale.ENGLISH)

// V4 date format is in the form yyyy-MM-dd'T'HH:mm:ss.SSSZ.
private var parserV4: DateTimeFormatter = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    .parseCaseInsensitive()
    .toFormatter(Locale.ENGLISH)

enum class ValidityDateStrVersion {
    V2, V4
}

fun ValidityDateStr.toValidityDate(version: ValidityDateStrVersion): ValidityDate? {
    return try {
        when (version) {
            ValidityDateStrVersion.V2 -> OffsetDateTime.parse(this, parserV2)
            ValidityDateStrVersion.V4 -> OffsetDateTime.parse(this, parserV4)
        }
    }catch (e: Exception) {
        TjekLogCat.e("toValidityDate parsing fail for $this")
        TjekLogCat.printStackTrace(e)
        null
    }
}

fun minOf(d1: ValidityDate, d2: ValidityDate): ValidityDate {
    return if (d1 < d2) d1 else d2
}

fun maxOf(d1: ValidityDate, d2: ValidityDate): ValidityDate {
    return if (d1 > d2) d1 else d2
}

fun distantPast(): ValidityDate {
    return OffsetDateTime.MIN
}

fun distantFuture(): ValidityDate {
    return OffsetDateTime.MAX
}

fun TimeOfDayStr.toTimeOfDay(): TimeOfDay {
    var hours = 0
    var minutes = 0
    var seconds = 0
    try {
        // seconds could fail in case of weird patterns like 08:00:00-0100. In that case, just ignore it
        val components = split(':').map { it.toIntOrNull() ?: 0 }
        hours = if (components.isNotEmpty()) components[0] else 0
        minutes = if (components.size > 1) components[1] else 0
        seconds = if (components.size > 2) components[2] else 0
    } catch (e: Exception) {
        TjekLogCat.printStackTrace(e)
    }
    return try {
        TimeOfDay.of(hours, minutes, seconds)
    } catch (e: java.lang.Exception) {
        // if the hours were bogus, like 24:00:12
        TjekLogCat.printStackTrace(e)
        TimeOfDay.MIDNIGHT
    }
}

fun DayOfWeekStr.toDayOfWeek(): DayOfWeek {
    return DayOfWeek.valueOf(uppercase(Locale.ENGLISH))
}

fun LocalDateTime.getV4FormattedStr(): String {
    val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
    return this.atOffset(ZoneOffset.UTC).format(dtf)
}