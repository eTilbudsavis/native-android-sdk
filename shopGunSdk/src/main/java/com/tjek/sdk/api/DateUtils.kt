package com.tjek.sdk.api

import com.tjek.sdk.TjekLogCat
import java.lang.Exception
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

typealias ValidityDateStr = String
typealias ValidityDate = ZonedDateTime

/** The date format as returned from the server  */
const val API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ"
val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(API_DATE_FORMAT, Locale.US)

fun ValidityDateStr.parse(): ValidityDate? {
    return try {
        ZonedDateTime.parse(this, DATE_FORMATTER)
    }catch (e: Exception) {
        TjekLogCat.e(e.message ?: "date parsing fail for $this")
        null
    }
}

fun distantPast(): ValidityDate {
    return Instant.ofEpochMilli(Long.MIN_VALUE).atZone(ZoneOffset.UTC)
}

fun distantFuture(): ValidityDate {
    return Instant.ofEpochMilli(Long.MAX_VALUE).atZone(ZoneOffset.UTC)
}


// Note  run_till (v2) is including. v4/valid_until is excluding. So 2022-12-31T23:00:00.000Z in v4 means when this time occurs, it's expired.

class ValidityDateIterator(val startDate: ValidityDate,
                           val endDateInclusive: ValidityDate,
                           val stepDays: Long): Iterator<ValidityDate> {

    private var currentDate = startDate

    override fun hasNext() = currentDate.plusDays(stepDays) <= endDateInclusive

    override fun next(): ValidityDate {
        val next = currentDate
        currentDate = currentDate.plusDays(stepDays)
        return next
    }
}

class ValidityDateRange(override val start: ValidityDate,
                        override val endInclusive: ValidityDate,
                        val stepDays: Long = 1) : Iterable<ValidityDate>, ClosedRange<ValidityDate> {

    override fun iterator(): Iterator<ValidityDate> =
        ValidityDateIterator(start, endInclusive, stepDays)

    infix fun step(days: Long) = ValidityDateRange(start, endInclusive, days)

}

operator fun ValidityDate.rangeTo(other: ValidityDate) = ValidityDateRange(this, other)