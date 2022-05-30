package com.tjek.sdk.api

import com.tjek.sdk.TjekLogCat
import java.time.*
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
private var parser: DateTimeFormatter = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .appendOptional(offset1)
    .appendOptional(offset2)
    .parseCaseInsensitive()
    .toFormatter(Locale.ENGLISH)

fun ValidityDateStr.parse(): ValidityDate? {
    return try {
        OffsetDateTime.parse(this, parser)
    }catch (e: Exception) {
        TjekLogCat.e(e.message ?: "date parsing fail for $this")
        null
    }
}

fun distantPast(): ValidityDate {
    return OffsetDateTime.MIN
}

fun distantFuture(): ValidityDate {
    return OffsetDateTime.MAX
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