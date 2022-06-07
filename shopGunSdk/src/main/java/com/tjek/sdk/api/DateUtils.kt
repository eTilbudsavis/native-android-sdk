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