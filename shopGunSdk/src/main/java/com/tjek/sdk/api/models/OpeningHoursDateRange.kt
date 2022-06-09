package com.tjek.sdk.api.models

import android.os.Parcel
import android.os.Parcelable
import com.tjek.sdk.api.OpeningHoursDate
import com.tjek.sdk.api.ValidityDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class OpeningHoursDateRange(
    override val start: OpeningHoursDate,
    override val endInclusive: OpeningHoursDate
) : Iterable<OpeningHoursDate>, ClosedRange<OpeningHoursDate>, Parcelable {

    constructor(parcel: Parcel) : this(
        OpeningHoursDate.ofEpochDay(parcel.readLong()),
        OpeningHoursDate.ofEpochDay(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(start.toEpochDay())
        parcel.writeLong(endInclusive.toEpochDay())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ValidityPeriod> {
        override fun createFromParcel(parcel: Parcel): ValidityPeriod {
            return ValidityPeriod(parcel)
        }

        override fun newArray(size: Int): Array<ValidityPeriod?> {
            return arrayOfNulls(size)
        }
    }

    override fun iterator(): Iterator<OpeningHoursDate> {
        return OpeningHoursDateIterator(start, endInclusive, 1)
    }

}

operator fun OpeningHoursDate.rangeTo(other: OpeningHoursDate) = OpeningHoursDateRange(this, other)

class OpeningHoursDateIterator(
    startDate: OpeningHoursDate,
    private val endDateInclusive: OpeningHoursDate,
    private val stepDays: Long): Iterator<OpeningHoursDate> {

    private var currentDate = startDate

    override fun hasNext() = currentDate.plusDays(stepDays) <= endDateInclusive

    override fun next(): OpeningHoursDate {
        val next = currentDate
        currentDate = currentDate.plusDays(stepDays)
        return next
    }
}